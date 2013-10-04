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
import org.cdma.interfaces.IArray;
import org.cdma.interfaces.IArrayIterator;
import org.cdma.interfaces.IDataItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.actor.v5.Actor;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;

/**
 * Simple trial to dump an IArray's values to System.out and forward the IArray in the flow.
 * 
 * @author delerw
 */
public class CDMAArrayValueDumper extends Actor {
  private static final long serialVersionUID = 1L;
  private final static Logger LOGGER = LoggerFactory.getLogger(CDMAArrayValueDumper.class);

  public Port input;
  public Port output;
  public Parameter wrapLengthParam;
  public Parameter chopLengthParam;

  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public CDMAArrayValueDumper(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    input = PortFactory.getInstance().createInputPort(this, IArray.class);
    output = PortFactory.getInstance().createOutputPort(this);
    
    wrapLengthParam = new Parameter(this, "Wrap lines at #values", new IntToken(50));
    chopLengthParam = new Parameter(this, "Chop output at #values", new IntToken(1000));
  }

  public Logger getLogger() {
    return LOGGER;
  }

  @Override
  public void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    ManagedMessage msg = request.getMessage(input);

    try {
      int wrapLength = ((IntToken)wrapLengthParam.getToken()).intValue();
      int chopLength = ((IntToken)chopLengthParam.getToken()).intValue();
      
      IArray array = null;
      IDataItem item = null;
      if(msg.getBodyContent() instanceof IArray) {
        array = (IArray) msg.getBodyContent();
      } else if (msg.getBodyContent() instanceof IDataItem) {
        item = (IDataItem) msg.getBodyContent();
        array = item.getData();
      }
      IArrayIterator itr = array.getIterator();
      int rowLength=0;
      int nrValues=0;
      if(item!=null) {
        System.out.println("Dumping item "+item.getName()+Arrays.toString(item.getShape()));
      }
      while(itr.hasNext()) {
        System.out.print(itr.next() + " ");
        if(++rowLength >= wrapLength) {
          System.out.println();
          rowLength=0;
        }
        if(++nrValues>=chopLength) {
          System.out.println("Chopped....");
          System.out.println();
          break;
        }
      }
      System.out.println();
      response.addOutputMessage(output, msg);
    } catch (Exception e) {
      throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "", this, e);
    }
  }
}
