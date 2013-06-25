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
import org.cdma.engine.nexus.array.NexusArray;
import org.cdma.engine.nexus.navigation.NexusDataItem;
import org.cdma.engine.nexus.navigation.NexusDataset;
import org.cdma.interfaces.IArray;
import org.cdma.plugin.soleil.array.NxsArray;
import org.cdma.plugin.soleil.internal.NexusDatasetImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import fr.soleil.nexus.DataItem;
import fr.soleil.nexus.PathData;


/**
 * Writes an IArray to a destination nexus file.
 * 
 * REMARK : This actor doesn't function yet!!!
 * 
 * @author delerw
 *
 */
public class CDMAArrayFileWriter extends Actor {
  private static final long serialVersionUID = 1L;
  private final static Logger LOGGER = LoggerFactory.getLogger(CDMAArrayFileWriter.class);

  public Port input;

  public CDMAArrayFileWriter(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    input = PortFactory.getInstance().createInputPort(this, IArray.class);
  }

  public Logger getLogger() {
    return LOGGER;
  }

  @Override
  public void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    ManagedMessage msg = request.getMessage(input);

    try {
      NxsArray array = (NxsArray) msg.getBodyContent();
      IArray[] parts = array.getArrayParts();
      NexusArray nxsArr = (NexusArray) parts[0];
////      DataItem dataItem = nxsArr.getDataItem();
//      AcquisitionData writer = new AcquisitionData();
////      PathGroup pathGroup = new PathGroup(PathNexus.splitStringPath("my/attr"));
//      writer.setFile("C:/temp/test.nxs");
//      writer.writeAttr("hello", array.getFactoryName(), PathNexus.ROOT_PATH);
//      writer.writeData(dataItem, PathData.Convert(dataItem.getPath()));
//      writer.finalize();
      
      //NxsDataset nxsDataset = NxsDataset.instanciate(new URI("C:/temp/test.nxs"));
      
     // IFactory nxsFactory = Factory.getFactory("SoleilNeXus");
      
      NexusDataset nxsDataset = new NexusDatasetImpl(new File("C:/temp/test.nxs"),false);
      DataItem item = new DataItem(nxsArr.getStorage());
      item.setPath(PathData.ROOT_PATH);
      NexusDataItem dataItem = new NexusDataItem(array.getFactoryName(),item,nxsDataset.getRootGroup(),nxsDataset);
      dataItem.setParent(nxsDataset.getRootGroup());
      dataItem.setName("mydataitem");
//      NexusAttribute attr = new NexusAttribute(array.getFactoryName(), "hello", "world");
      nxsDataset.getRootGroup().addDataItem(dataItem);
      
      
      nxsDataset.getRootGroup().addStringAttribute("hello", "world");
      
      nxsDataset.save();
    } catch (Throwable e) {
      throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "", this, e);
    }
  }
}
