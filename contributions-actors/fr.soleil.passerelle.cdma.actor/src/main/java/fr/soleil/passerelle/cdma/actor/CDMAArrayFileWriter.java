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
import org.cdma.interfaces.IArray;
import org.cdma.plugin.soleil.nexus.array.NxsArray;
import org.cdma.plugin.soleil.nexus.navigation.NxsDataItem;
import org.cdma.plugin.soleil.nexus.navigation.NxsDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.actor.gui.style.CheckBoxStyle;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.ValidationException;
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
  public FileParameter skeletonFileParameter;
  public StringParameter outputFileParameter;
  public StringParameter itemNameParameter;
  public Parameter appendModeParameter;
  private File outputFile;
  private File skeletonFile;

  public CDMAArrayFileWriter(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    input = PortFactory.getInstance().createInputPort(this, IArray.class);
    skeletonFileParameter = new FileParameter(this, "CDMA write skeleton file");
    skeletonFileParameter.setExpression("$HOME/cdma_trials/testSkeleton.nxs");
    outputFileParameter = new StringParameter(this, "Output file");
    outputFileParameter.setExpression("C:/Users/delerw/cdma_trials/testOutput.nxs");
    itemNameParameter = new StringParameter(this, "Result item name");
    itemNameParameter.setExpression("myItem");
    appendModeParameter = new Parameter(this, "Append mode", BooleanToken.TRUE);
    new CheckBoxStyle(appendModeParameter, "checkbox");
    registerConfigurableParameter(outputFileParameter);
    registerConfigurableParameter(itemNameParameter);
    registerConfigurableParameter(appendModeParameter);
    registerExpertParameter(skeletonFileParameter);
  }

  public Logger getLogger() {
    return LOGGER;
  }

  @Override
  protected void validateInitialization() throws ValidationException {
    super.validateInitialization();
    try {
      if (!skeletonFileParameter.asFile().exists()) {
        throw new ValidationException(ErrorCode.ACTOR_INITIALISATION_ERROR, "Invalid skeleton file " + skeletonFileParameter.stringValue(), this, null);
      }
    } catch (Exception e) {
      throw new ValidationException(ErrorCode.ACTOR_INITIALISATION_ERROR, "Invalid skeleton file " + skeletonFileParameter, this, e);
    }
  }

  @Override
  protected void doInitialize() throws InitializationException {
    super.doInitialize();
    outputFile = null;
    skeletonFile = null;
  }

  @Override
  public void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    ManagedMessage msg = request.getMessage(input);
    try {
      boolean appendMode = ((BooleanToken) appendModeParameter.getToken()).booleanValue();
      boolean doAppend = false;
      String itemName = itemNameParameter.stringValue();
      if (skeletonFile == null) {
        skeletonFile = skeletonFileParameter.asFile();
      }
      if (appendMode) {
        itemName += request.getIterationCount();
        if (outputFile != null) {
          // need to pick this one as skeleton now to continue appending to it
          skeletonFile = outputFile;
          doAppend = true;
        }
      }
      if (outputFile == null) {
        outputFile = new File(outputFileParameter.stringValue());
      }
      IArray rcvdArray = (IArray) msg.getBodyContent();
      NxsDataset nxsDataset = NxsDataset.instanciate(skeletonFile.toURI());
      NxsDataItem dataItem = new NxsDataItem();
      dataItem.setName(itemName);
      NxsArray array = new NxsArray((NxsArray) rcvdArray);
      dataItem.setCachedData(array, false);
      nxsDataset.getRootGroup().addDataItem(dataItem);
      nxsDataset.getRootGroup().addStringAttribute("hello", "world");
//      if (doAppend) {
//        nxsDataset.save();
//      } else {
        nxsDataset.saveTo(outputFile.getAbsolutePath());
//      }
    } catch (Throwable e) {
      throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "", this, e);
    }
  }
}
