/**
 * 
 */
package com.isencia.passerelle.actor.error;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.actor.gui.style.TextStyle;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.TerminationException;
import com.isencia.passerelle.actor.dynaport.OutputPortSetterBuilder;
import com.isencia.passerelle.actor.v5.Actor;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.ext.ErrorHandler;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageOutputContext;

/**
 * @author erwin
 */
public class ErrorHandlerByCodeRange extends Actor implements ErrorHandler {
  private static final long serialVersionUID = 1L;
  private final static Logger LOGGER = LoggerFactory.getLogger(ErrorHandlerByCodeRange.class);

  public OutputPortSetterBuilder outputPortBuilder;

  /**
   * multi-line definition of error ranges for which this handler will take ownership. <br/>
   * Each line is of format <code>name=range</code>, where for each line :
   * <ul>
   * <li>name will be used to create a corresponding output port</li>
   * <li>range is of format <code>nnnn[-mmmm]</code>, i.e. either one 4-digit number or two of them separated by a mid-score</li>
   * </ul>
   */
  public StringParameter errorRangesParameter;

  private Set<CodeRange> errorRanges = new TreeSet<ErrorHandlerByCodeRange.CodeRange>();

  private BlockingQueue<MessageOutputContext> bufferedErrorOutputs = new LinkedBlockingQueue<MessageOutputContext>();

  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public ErrorHandlerByCodeRange(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    outputPortBuilder = new OutputPortSetterBuilder(this, "outputPortBldr");
    errorRangesParameter = new StringParameter(this, "error ranges");
    new TextStyle(errorRangesParameter, "textbox");

    _attachText("_iconDescription", "<svg>\n" + "<rect x=\"-20\" y=\"-20\" width=\"40\" height=\"40\" style=\"fill:red;stroke:red\"/>\n"
        + "<line x1=\"-19\" y1=\"-19\" x2=\"19\" y2=\"-19\" style=\"stroke-width:1.0;stroke:white\"/>\n"
        + "<line x1=\"-19\" y1=\"-19\" x2=\"-19\" y2=\"19\" style=\"stroke-width:1.0;stroke:white\"/>\n"
        + "<line x1=\"20\" y1=\"-19\" x2=\"20\" y2=\"20\" style=\"stroke-width:1.0;stroke:black\"/>\n"
        + "<line x1=\"-19\" y1=\"20\" x2=\"20\" y2=\"20\" style=\"stroke-width:1.0;stroke:black\"/>\n"
        + "<line x1=\"19\" y1=\"-18\" x2=\"19\" y2=\"19\" style=\"stroke-width:1.0;stroke:grey\"/>\n"
        + "<line x1=\"-18\" y1=\"19\" x2=\"19\" y2=\"19\" style=\"stroke-width:1.0;stroke:grey\"/>\n"
        + "<circle cx=\"0\" cy=\"0\" r=\"10\" style=\"fill:white;stroke-width:2.0\"/>\n"
        + "<line x1=\"0\" y1=\"-15\" x2=\"0\" y2=\"0\" style=\"stroke-width:2.0\"/>\n"
        + "<line x1=\"-3\" y1=\"-3\" x2=\"0\" y2=\"0\" style=\"stroke-width:2.0\"/>\n"
        + "<line x1=\"3\" y1=\"-3\" x2=\"0\" y2=\"0\" style=\"stroke-width:2.0\"/>\n" + "</svg>\n");
  }

  @Override
  public void attributeChanged(Attribute attribute) throws IllegalActionException {
    if (attribute != errorRangesParameter) {
      super.attributeChanged(attribute);
    } else
      try {
        List<String> rangeNames = new ArrayList<String>();
        String mappingDefs = errorRangesParameter.getExpression();
        BufferedReader reader = new BufferedReader(new StringReader(mappingDefs));
        String mappingDef = null;
        while ((mappingDef = reader.readLine()) != null) {
          String[] mappingParts = mappingDef.split("=");
          if (mappingParts.length == 2) {
            String rangeName = mappingParts[0];
            rangeNames.add(rangeName);
          } else {
            getLogger().warn("{} - Invalid mapping definition: {}", getFullName(), mappingDef);
          }
        }
        outputPortBuilder.setOutputPortNames(rangeNames.toArray(new String[rangeNames.size()]));
      } catch (Exception e) {
        throw new IllegalActionException(this, e, "Error processing error range mapping");
      }
  }

  @Override
  protected void doPreInitialize() throws InitializationException {
    super.doPreInitialize();
    try {
      bufferedErrorOutputs.clear();
      
      String mappingDefs = errorRangesParameter.getExpression();
      BufferedReader reader = new BufferedReader(new StringReader(mappingDefs));
      String mappingDef = null;
      while ((mappingDef = reader.readLine()) != null) {
        String[] mappingParts = mappingDef.split("=");
        if (mappingParts.length == 2) {
          String rangeName = mappingParts[0];
          String rangeDef = mappingParts[1];
          CodeRange cr = CodeRange.buildFrom(rangeName, rangeDef);
          if (cr != null) {
            errorRanges.add(cr);
          }
        } else {
          getLogger().warn("{} - Invalid mapping definition: {}", getFullName(), mappingDef);
        }
      }
    } catch (Exception e) {
      throw new InitializationException(ErrorCode.ACTOR_INITIALISATION_ERROR, "Error processing error range mapping", this, e);
    }
  }

  /**
   * Checks if the given error contains a msg and an error code.
   * If the code matches one of the configured ranges, the msg will be sent out via the corresponding port.
   */
  public synchronized boolean handleError(NamedObj errorSource, PasserelleException error) {
    boolean result = false;
    ManagedMessage msg = error.getMsgContext();
    ErrorCode errCode = error.getErrorCode();
    if ((msg != null) && (errCode != null)) {
      for (CodeRange codeRange : errorRanges) {
        if (codeRange.isInRange(errCode.getCodeAsInteger())) {
          Object outputPort = getPort(codeRange.getName());
          if (outputPort == null || !(outputPort instanceof Port)) {
            getLogger().error("Error in actor's ports",new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error finding port for range " + codeRange, this, error));
          } else {
            try {
              bufferedErrorOutputs.put(new MessageOutputContext((Port) outputPort, msg));
              super.triggerNextIteration();
              result = true;
            } catch (InterruptedException e1) {
              // should not happen, or if it does only when terminating the model execution
              getLogger().error("Receipt interrupted for ", error);
            } catch (IllegalActionException e2) {
              getLogger().error("Failed to trigger next iteration ", e2);
              getLogger().error("Error received ", error);
            }
          }
          break;
        }
      }
    }
    return result;
  }

  @Override
  protected synchronized void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    // This actor has no data input ports,
    // so it's like a Source in the days of the original Actor API.
    // The BlockingQueue (errors) is our data feed.
    try {
      MessageOutputContext errOutput = bufferedErrorOutputs.poll(1, TimeUnit.SECONDS);
      if (errOutput != null) {
        sendOutErrorInfo(response, errOutput);
        drainErrorsQueueTo(response);
      }
    } catch (InterruptedException e) {
      // should not happen,
      // or if it does only when terminating the model execution
      // and with an empty queue, so we can just finish then
      requestFinish();
    }
  }

  private void sendOutErrorInfo(ProcessResponse response, MessageOutputContext msgOutputCtxt) throws ProcessingException {
    if(response!=null) {
      response.addOutputContext(msgOutputCtxt);
    } else {
      sendOutputMsg(msgOutputCtxt.getPort(), msgOutputCtxt.getMessage());
    }
  }

  private synchronized void drainErrorsQueueTo(ProcessResponse response) throws ProcessingException {
    while (!bufferedErrorOutputs.isEmpty()) {
      MessageOutputContext errOutput = bufferedErrorOutputs.poll();
      if (errOutput != null) {
        sendOutErrorInfo(response, errOutput);
      } else {
        break;
      }
    }
  }

  @Override
  protected void doWrapUp() throws TerminationException {
    try {
      drainErrorsQueueTo(null);
    } catch (Exception e) {
      throw new TerminationException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error draining remaining error queue " + bufferedErrorOutputs, this, e);
    }
    super.doWrapUp();
  }

  @Override
  protected void triggerFirstIteration() throws IllegalActionException {
    // no unconditional triggering here, dude!
  }

  @Override
  protected void triggerNextIteration() throws IllegalActionException {
    // no unconditional triggering here, dude!
  }

  public Logger getLogger() {
    return LOGGER;
  }

  private static class CodeRange implements Comparable<CodeRange> {
    int minValue = 0;
    int maxValue = Integer.MAX_VALUE;
    String name;

    public CodeRange(String name, int minValue) {
      this.name = name;
      this.minValue = minValue;
      this.maxValue = minValue;
    }

    public CodeRange(String name, int minValue, int maxValue) {
      this.name = name;
      this.minValue = minValue;
      this.maxValue = maxValue;
    }

    public static CodeRange buildFrom(String name, String rangeDef) {
      CodeRange result = null;
      String[] rangeLimits = rangeDef.split("-");
      if (rangeLimits.length == 2) {
        result = new CodeRange(name, Integer.parseInt(rangeLimits[0]), Integer.parseInt(rangeLimits[1]));
      } else if (rangeLimits.length == 1) {
        result = new CodeRange(name, Integer.parseInt(rangeLimits[0]));
      }
      return result;
    }

    public String getName() {
      return name;
    }

    public boolean isInRange(int i) {
      return minValue <= i && maxValue >= i;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + maxValue;
      result = prime * result + minValue;
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      CodeRange other = (CodeRange) obj;
      if (maxValue != other.maxValue)
        return false;
      if (minValue != other.minValue)
        return false;
      if (name == null) {
        if (other.name != null)
          return false;
      } else if (!name.equals(other.name))
        return false;
      return true;
    }

    public int compareTo(CodeRange o) {
      if (this == o) {
        return 0;
      }
      if (o == null) {
        return 1;
      }
      return toString().compareTo(o.toString());
    }

    @Override
    public String toString() {
      return "CodeRange [minValue=" + minValue + ", maxValue=" + maxValue + ", name=" + name + "]";
    }
  }
}
