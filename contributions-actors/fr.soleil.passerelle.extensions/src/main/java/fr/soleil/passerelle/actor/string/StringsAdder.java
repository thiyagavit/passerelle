/*	Synchrotron Soleil
 *
 *   File          :  StringsAdder.java
 *
 *   Project       :  passerelle-soleil
 *
 *   Description   :
 *
 *   Author        :  ABEILLE
 *
 *   Original      :  30 mars 2006
 *
 *   Revision:  					Author:
 *   Date: 							State:
 *
 *   Log: StringsAdder.java,v
 *
 */
/*
 * Created on 30 mars 2006
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package fr.soleil.passerelle.actor.string;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.core.PortHandler;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.MessageHelper;
import com.isencia.passerelle.util.ExecutionTracerService;

import fr.soleil.passerelle.actor.PortUtilities;
import fr.soleil.passerelle.util.ExceptionUtil;
import fr.soleil.passerelle.util.PasserelleUtil;

/**
 * This actor allows string concatenation
 * 
 * @author ABEILLE
 * 
 */
@SuppressWarnings("serial")
public class StringsAdder extends Actor {

  private final static Logger logger = LoggerFactory.getLogger(StringsAdder.class);

  private static final String X = "x";

  private boolean finishRequested = false;

  // private PortHandler leftHandler = null;
  // private PortHandler rightHandler = null;
  // private boolean rightReceived = false;
  // private boolean leftReceived = false;

  private List<PortHandler> inputsHandlers = new ArrayList<PortHandler>(10);

  // private String leftString = "";
  // private String rigthString = "";

  private List<Port> inputsPorts = new ArrayList<Port>(10);
  private List<String> inputValues = new ArrayList<String>();
  /** The left input port */
  // public Port left;
  /** The right input port */
  // public Port right;
  /** The output port */
  public Port output;

  private String separator = ",";
  public Parameter separatorParam;

  private int stringNumber = 0;
  public Parameter stringNumberParam;

  boolean tokenIsNull = false;

  public StringsAdder(final CompositeEntity container, final String name) throws NameDuplicationException, IllegalActionException {
    super(container, name);

    output = PortFactory.getInstance().createOutputPort(this, "output");

    stringNumberParam = new StringParameter(this, "nb of string to add");
    stringNumberParam.setExpression(Integer.toString(stringNumber));

    separatorParam = new StringParameter(this, "Separator");
    separatorParam.setExpression(separator);

  }

  @Override
  public void attributeChanged(final Attribute attribute) throws IllegalActionException {
    if (attribute == separatorParam) {
      // since separatorParam.getToken() remove spaces, use getExpression
      // which does not evaluate.
      if (separatorParam.getExpression().contains("$")) {
        separator = ((StringToken) separatorParam.getToken()).stringValue();
      } else {
        separator = separatorParam.getExpression();
      }
    } else if (attribute == stringNumberParam) {
      final int nrPorts = inputsPorts.size();
      stringNumber = Integer.valueOf(((StringToken) stringNumberParam.getToken()).stringValue());
      final int newPortCount = stringNumber;
      try {
        // remove no more needed ports
        if (newPortCount < nrPorts) {
          for (int i = nrPorts - 1; i >= newPortCount; i--) {
            try {
              inputsPorts.get(i).setContainer(null);
              inputsPorts.remove(i);
            } catch (final NameDuplicationException e) {
              throw new IllegalActionException(this, e, "Error for index " + i);
            }
          }

        }// add missing ports
        else if (newPortCount > nrPorts) {
          for (int i = nrPorts; i < newPortCount; i++) {
            try {
              final String intputPortName = X + (i + 1);
              Port extraInputPort = (Port) getPort(intputPortName);
              if (extraInputPort == null) {
                extraInputPort = PortFactory.getInstance().createInputPort(this, intputPortName, null);
              }
              extraInputPort.setExpectedMessageContentType(String.class);
              // extraInputPort.setTypeEquals(BaseType.OBJECT);
              inputsPorts.add(extraInputPort);
            } catch (final NameDuplicationException e) {
              throw new IllegalActionException(this, e, "Error for index " + i);
            }
          }
        }
        // the paramaters of the ports do not seemed to be well passed
        // so, recall the method on the objects of the list
        for (int j = 0; j < inputsPorts.size(); j++) {
          inputsPorts.get(j).setExpectedMessageContentType(String.class);
          inputsPorts.get(j).setMultiport(false);
        }
      } catch (final IllegalActionException e) {
        throw e;
      }
    }
  }

  @Override
  protected void doFire() throws ProcessingException {

    if (logger.isTraceEnabled()) {
      logger.trace(getName() + " doFire() - entry");
    }
    inputValues.clear();
    final List<Port> list = PortUtilities.getOrderedInputPorts(this, X, 1);
    for (int i = 0; i < list.size(); i++) {
      logger.debug("waiting for input " + i);
      final Port port = inputsPorts.get(i);
      final PortHandler portHandler = inputsHandlers.get(i);
      if (port.getWidth() > 0) {
        final Token inputToken = portHandler.getToken();
        ManagedMessage mes = null;
        try {
          mes = MessageHelper.getMessageFromToken(inputToken);
        } catch (final PasserelleException e) {
            ExceptionUtil.throwProcessingException(e.getMessage(), inputToken, e);
        }
        if (mes != null) {
          String inputValue = "";
          try {
            final Object input = mes.getBodyContent();
            if (input.getClass().isArray()) {
              final String[] table = (String[]) input;
              for (final String element : table) {
                inputValues.add(element);
              }
            } else {
              inputValue = (String) mes.getBodyContent();
              inputValues.add(inputValue);
            }
          } catch (final MessageException e) {
              ExceptionUtil.throwProcessingException("cannot get input value", mes, e);
          }

        } else {
          finishRequested = true;
          requestFinish();
        }
      } else {// if (port.getWidth() > 0)
          ExceptionUtil.throwProcessingException(ErrorCode.FATAL,"Input port " + port.getName() + " has no data.", this);
      }
    }// For
    if (!finishRequested) {
      // calculate the output string
      String result = "";
      final StringBuffer buf = new StringBuffer("");
      for (int i = 0; i < inputValues.size() - 1; i++) {
        buf.append(inputValues.get(i));
        buf.append(separator);
        // result += inputValues.get(i)+separator;
      }
      buf.append(inputValues.get(inputValues.size() - 1));
      result = buf.toString();
      // result += inputValues.get(inputValues.size()-1);

      ExecutionTracerService.trace(this, "the output string is " + result);

      sendOutputMsg(output, PasserelleUtil.createContentMessage(this, result));

    }

    if (logger.isTraceEnabled()) {
      logger.trace(getName() + " doFire() - exit");
    }

  }

  @Override
  public void doInitialize() throws InitializationException {

    if (logger.isTraceEnabled()) {
      logger.trace(getName() + " doInitialize() - entry");
    }
    inputValues.clear();
    inputsHandlers.clear();
    finishRequested = false;
    for (int i = 0; i < inputsPorts.size(); i++) {
      final Port port = inputsPorts.get(i);
      if (port.getWidth() > 0) {
        final PortHandler inputHandler = new PortHandler(port);
        inputHandler.start();
        inputsHandlers.add(inputHandler);
      }
    }

    super.doInitialize();
    if (logger.isTraceEnabled()) {
      logger.trace(getName() + " doInitialize() - exit");
    }

  }

  @Override
  protected String getExtendedInfo() {
    return this.getName();
  }

  @Override
  public Object clone(final Workspace workspace) throws CloneNotSupportedException {
    final StringsAdder copy = (StringsAdder) super.clone(workspace);
    copy.inputValues = new ArrayList<String>();
    copy.inputsPorts = new ArrayList<Port>(10);
    copy.inputsHandlers = new ArrayList<PortHandler>(10);
    return copy;
  }

}
