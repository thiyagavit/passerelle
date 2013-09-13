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
import org.cdma.dictionary.LogicalGroup;
import org.cdma.interfaces.IDataItem;
import org.cdma.interfaces.IDataset;
import org.cdma.interfaces.IGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.actor.gui.style.CheckBoxStyle;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v5.Actor;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.message.ManagedMessage;

/**
 * @author delerw
 */
public class CDMADataItemSelector extends Actor {

  private static final long serialVersionUID = 1L;

  private final static Logger LOGGER = LoggerFactory.getLogger(CDMADataItemSelector.class);

  public Port input;
  public Port output;

  public StringParameter itemNameParameter;
  public Parameter itemSelectionModeParameter;

  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public CDMADataItemSelector(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    input = PortFactory.getInstance().createInputPort(this, IDataset.class);
    output = PortFactory.getInstance().createOutputPort(this);

    itemNameParameter = new StringParameter(this, "Item name");
    itemNameParameter.setExpression("scan:data:images");

    itemSelectionModeParameter = new Parameter(this, "Logical selection mode", BooleanToken.TRUE);
    new CheckBoxStyle(itemSelectionModeParameter, "chb");
  }

  public Logger getLogger() {
    return LOGGER;
  }

  @Override
  public void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    ManagedMessage msg = request.getMessage(input);

    try {
      boolean logicalMode = ((BooleanToken) itemSelectionModeParameter.getToken()).booleanValue();
      String itemName = ((StringToken)itemNameParameter.getToken()).stringValue();
      IDataset dataSet = (IDataset) msg.getBodyContent();
      IDataItem dataItem = null;
      if (logicalMode) {
        LogicalGroup logicalRoot = dataSet.getLogicalRoot();
        dataItem = logicalRoot.getDataItem(itemName);
      } else {
        IGroup physRoot = dataSet.getRootGroup();
        String[] pathParts = itemName.split("/");
        IGroup grp = physRoot;
        for (int i=0; i< pathParts.length - 1 ; ++i) {
          grp = grp.getGroup(pathParts[i]);
        }
        dataItem = (IDataItem) grp.getDataItem(pathParts[pathParts.length-1]);
      }

      if (dataItem != null) {
        getLogger().info("item {} has shape: {}", itemName, Arrays.toString(dataItem.getShape()));
        ManagedMessage outputMsg = createMessageFromCauses(msg);
        outputMsg.setBodyContent(dataItem, ManagedMessage.objectContentType);
        response.addOutputMessage(output, outputMsg);
      } else {
        throw new ProcessingException(ErrorCode.FLOW_CONFIGURATION_ERROR, "Item " + itemName + " not found", this, msg, null);
      }
    } catch (ProcessingException e) {
      throw e;
    } catch (Exception e) {
      throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "", this, e);
    }

  }

}
