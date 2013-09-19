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

import java.io.File;
import java.io.IOException;
import org.cdma.interfaces.IArray;
import org.cdma.plugin.soleil.nexus.array.NxsArray;
import org.cdma.plugin.soleil.nexus.navigation.NxsDataItem;
import org.cdma.plugin.soleil.nexus.navigation.NxsDataset;
import org.cdma.plugin.soleil.nexus.navigation.NxsGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.actor.gui.style.CheckBoxStyle;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.InitializationException;
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
 * Writes the received IArrays to a destination nexus file. In append mode, each received array is stored in a data item with the iteration counter as appended
 * index in its name.
 * 
 * @author delerw
 */
public class CDMAArrayFileWriter extends Actor {
  private static final long serialVersionUID = 1L;
  private final static Logger LOGGER = LoggerFactory.getLogger(CDMAArrayFileWriter.class);
  public Port input;
  public StringParameter outputFileParameter;
  public StringParameter itemNameParameter;
  public Parameter appendModeParameter;
  private File outputFile;

  public CDMAArrayFileWriter(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    input = PortFactory.getInstance().createInputPort(this, IArray.class);
    outputFileParameter = new StringParameter(this, "Output file");
    outputFileParameter.setExpression("C:/Users/delerw/cdma_trials/testOutput.nxs");
    itemNameParameter = new StringParameter(this, "Result item name");
    itemNameParameter.setExpression("myItem");
    appendModeParameter = new Parameter(this, "Append mode", BooleanToken.TRUE);
    new CheckBoxStyle(appendModeParameter, "checkbox");
    registerConfigurableParameter(outputFileParameter);
    registerConfigurableParameter(itemNameParameter);
    registerConfigurableParameter(appendModeParameter);
  }

  public Logger getLogger() {
    return LOGGER;
  }

  @Override
  protected void doInitialize() throws InitializationException {
    super.doInitialize();
    outputFile = null;
  }

  @Override
  public void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    ManagedMessage msg = request.getMessage(input);
    NxsDataset nxsDataset = null;
    try {
      boolean appendMode = ((BooleanToken) appendModeParameter.getToken()).booleanValue();
      String itemName = itemNameParameter.stringValue();
      if (appendMode) {
        itemName += request.getIterationCount();
      }
      if (outputFile == null) {
        outputFile = new File(outputFileParameter.stringValue());
      }
      IArray rcvdArray = (IArray) msg.getBodyContent();
      nxsDataset = NxsDataset.instanciate(outputFile.toURI(), true);
//      NxsPath nxsPath = new NxsPath((NxsPath.splitStringToNode("/blocks2")));
      NxsGroup blocksGroup = new NxsGroup(nxsDataset,"blocks","/", (NxsGroup) nxsDataset.getRootGroup());
//      NxsGroup blocksGroup = new NxsGroup(nxsDataset.getRootGroup(), nxsPath, nxsDataset);
      NxsDataItem dataItem = new NxsDataItem(itemName, nxsDataset);
      NxsArray array = new NxsArray((NxsArray) rcvdArray);
      dataItem.setCachedData(array, false);
      blocksGroup.addDataItem(dataItem);
      blocksGroup.addStringAttribute("hello", "world");
      nxsDataset.getRootGroup().addSubgroup(blocksGroup);
      nxsDataset.getRootGroup().addStringAttribute("hello", "world");
      dataItem.addStringAttribute("hello", "world2");
      nxsDataset.save();
    } catch (Throwable e) {
      throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "", this, e);
    } finally {
      if (nxsDataset != null) {
        try {
          nxsDataset.close();
        } catch (IOException e) {
          // ignore, at least we tried...
        }
      }
    }
  }
}
