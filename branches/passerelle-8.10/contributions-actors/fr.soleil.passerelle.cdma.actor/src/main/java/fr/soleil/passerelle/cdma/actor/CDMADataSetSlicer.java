/* Copyright 2013 - Synchrotron Soleil

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package fr.soleil.passerelle.cdma.actor;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.cdma.exception.InvalidRangeException;
import org.cdma.exception.ShapeNotMatchException;
import org.cdma.interfaces.IArray;
import org.cdma.interfaces.IDataItem;
import org.cdma.interfaces.ISliceIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v5.Actor;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.core.PortMode;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.MessageInputContext;
import com.isencia.passerelle.util.ExecutionTracerService;

/**
 * @author delerw
 */
public class CDMADataSetSlicer extends Actor {
  private static final long serialVersionUID = 1L;

  private final static Logger LOGGER = LoggerFactory.getLogger(CDMADataSetSlicer.class);

  public static final String INPUT_PORT_NAME = "input";
  public static final String NEXT_PORT_NAME = "next";
  public static final String END_PORT_NAME = "end";
  public static final String OUTPUT_PORT_NAME = "output";

  // input ports
  public Port inputPort;
  public Port nextPort;
  // output ports
  public Port outputPort;
  public Port endPort;

  public Parameter sliceRankParameter;
  private BlockingQueue<SliceContext> sliceContexts = new LinkedBlockingQueue<SliceContext>();

  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public CDMADataSetSlicer(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    inputPort = PortFactory.getInstance().createInputPort(this, IDataItem.class);
    nextPort = PortFactory.getInstance().createInputPort(this, "next", PortMode.PUSH, IArray.class);
    endPort = PortFactory.getInstance().createOutputPort(this, "end");
    outputPort = PortFactory.getInstance().createOutputPort(this);

    final StringAttribute outputPortCardinal = new StringAttribute(outputPort, "_cardinal");
    outputPortCardinal.setExpression("SOUTH");

    final StringAttribute nextPortCardinal = new StringAttribute(nextPort, "_cardinal");
    nextPortCardinal.setExpression("SOUTH");

    sliceRankParameter = new Parameter(this, "Slice rank", new IntToken(2));
  }

  public Logger getLogger() {
    return LOGGER;
  }

  @Override
  protected void doInitialize() throws InitializationException {
    super.doInitialize();
    sliceContexts.clear();
  }

  /**
   * As the loop actor manages a complete loop execution "in the background" for each received trigger msg, i.e. we do not block the process method during the
   * complete loop execution, this must be marked as "asynchronous" processing.
   */
  @Override
  public ProcessingMode getProcessingMode(ActorContext ctxt, ProcessRequest request) {
    if (request.getMessage(inputPort) != null) {
      return ProcessingMode.ASYNCHRONOUS;
    } else {
      return super.getProcessingMode(ctxt, request);
    }
  }

  /**
   * Intercept each "next" message while it's being pushed via the nextPort, so we can use this as indication that a next step message can be sent out.
   */
  @Override
  public void offer(MessageInputContext ctxt) throws PasserelleException {
    if (nextPort.getName().equals(ctxt.getPortName())) {
      sendSliceMessage();
    }
    super.offer(ctxt);
  }

  @Override
  public void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    ManagedMessage msg = request.getMessage(inputPort);
    if (msg != null) {
      int rank = 0;
      try {
        getLogger().debug("{} - received dataItem msg {}", getFullName(), msg);
        boolean wasIdle = sliceContexts.isEmpty();

        IDataItem dataItem = (IDataItem) msg.getBodyContent();
        rank = ((IntToken) sliceRankParameter.getToken()).intValue();
        ISliceIterator sliceIterator = dataItem.getData().getSliceIterator(rank);
        sliceContexts.add(new SliceContext(dataItem, sliceIterator, response));
        if (wasIdle) {
          // only if there's no slicing going on yet, can we send a first one just like that...
          sendSliceMessage();
        }
      } catch (ShapeNotMatchException e) {
        throw new ProcessingException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Received a DataItem with invalid shape for slicer rank " + rank, this, msg, e);
      } catch (Exception e) {
        throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "", this, msg, e);
      }
    }
  }

  private void sendSliceMessage() throws ProcessingException {
    SliceContext slCtxt = sliceContexts.peek();
    if (slCtxt != null) {
      try {
        if (!slCtxt.sliceIterator.hasNext()) {
          ExecutionTracerService.trace(this, "Slicing completed for item " + slCtxt.dataItem.getName());
          // now is the time to drop the current slicing context
          sliceContexts.poll();
          ManagedMessage outputMsg = createMessageFromCauses(slCtxt.response.getRequest().getMessage(inputPort));
          outputMsg.setBodyContent(slCtxt.dataItem, ManagedMessage.objectContentType);
          sendOutputMsg(endPort, outputMsg);
          processFinished(slCtxt.response.getContext(), slCtxt.response.getRequest(), slCtxt.response);
          if (sliceContexts.isEmpty()) {
            if (!isFinishRequested() && inputPort.getActiveSources().isEmpty()) {
              requestFinish();
            }
          } else {
            // continue with next slicing that seems to be on the queue
            sendSliceMessage();
          }
        } else if (isFinishRequested()) {
          ExecutionTracerService.trace(this, "Slicing has been interrupted at slice " + Arrays.toString(slCtxt.sliceIterator.getSlicePosition()) + " for item "
              + slCtxt.dataItem.getName());
          if (getDirectorAdapter().isActorBusy(this)) {
            while (!sliceContexts.isEmpty()) {
              // clear all busy work
              ProcessResponse response = sliceContexts.poll().response;
              if (response != null)
                processFinished(response.getContext(), response.getRequest(), response);
            }
          }
        } else {
          IArray arrayNext = slCtxt.sliceIterator.getArrayNext();
          ExecutionTracerService.trace(this,
              "Slicing at slice " + Arrays.toString(slCtxt.sliceIterator.getSlicePosition()) + " for item " + slCtxt.dataItem.getName());
          ManagedMessage outputMsg = createMessageFromCauses(slCtxt.response.getRequest().getMessage(inputPort));
          outputMsg.setBodyContent(arrayNext, ManagedMessage.objectContentType);
          sendOutputMsg(outputPort, outputMsg);
        }
      } catch (MessageException e) {
        throw new ProcessingException(ErrorCode.MSG_CONSTRUCTION_ERROR, "Error setting msg content", this, e);
      } catch (InvalidRangeException e) {
        throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error getting next slice", this, e);
      }
    } else {
      // we're in trouble, this should not happen???
      // we can't just pretend we didn't notice and all's spiffy!?
      throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Illegal state - empty slicing context Q", this, null);
    }
  }

  static class SliceContext {
    ProcessResponse response;
    ISliceIterator sliceIterator;
    IDataItem dataItem;

    public SliceContext(IDataItem dataItem, ISliceIterator sliceIterator, ProcessResponse response) {
      super();
      this.dataItem = dataItem;
      this.sliceIterator = sliceIterator;
      this.response = response;
    }
  }

}
