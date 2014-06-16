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
import java.net.URI;
import java.util.List;
import java.util.logging.Level;

import org.cdma.Factory;
import org.cdma.engine.hdf.navigation.HdfDataset;
import org.cdma.interfaces.IDataset;
import org.cdma.interfaces.IDatasource;
import org.cdma.plugin.soleil.nexus.navigation.NxsDataset;

import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.TerminationException;
import com.isencia.passerelle.actor.v5.Actor;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;

/**
 * @author delerw
 */
public class CDMADataSetReader extends Actor {
  private static final long serialVersionUID = 1L;

  public Port output;

  public StringParameter dataSetURIParameter;

  private IDataset dataSet;

  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public CDMADataSetReader(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    output = PortFactory.getInstance().createOutputPort(this);

    dataSetURIParameter = new StringParameter(this, "DataSet URI");
  }
  
  @Override
  protected void doInitialize() throws InitializationException {
    super.doInitialize();
    
    List<IDatasource> list = Factory.getDatasources();
    System.out.println("available plugins ");
    for (IDatasource iDatasource : list) {
      System.out.println("plugin name " + iDatasource.getFactoryName());
    }

    Factory.getLogger().setLevel(Level.WARNING);
    Factory.setDictionariesFolder("C:/data/workspaces/passerelle-edm-branch-1_3/fr.soleil.passerelle.cdma.actor.test/CDMA_Dictionaries");
    Factory.setActiveView("DATA_REDUCTION");
  }

  @Override
  public void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {

    try {
      URI dataSetURI = new URI(dataSetURIParameter.getExpression());

      dataSet = NxsDataset.instanciate(dataSetURI);  //Factory.openDataset(dataSetURI);

      if (dataSet == null) {
        throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "No data set found for " + dataSetURI, this, null);
      }
      ManagedMessage message = createMessage();
      try {
        message.setBodyContent(dataSet, ManagedMessage.objectContentType);
      } catch (MessageException e) {
        throw new ProcessingException(ErrorCode.MSG_CONSTRUCTION_ERROR, "", this, e);
      }
      response.addOutputMessage(output, message);
    } catch (ProcessingException e) {
      throw e;
    } catch (Exception e) {
      throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error opening dataset " + dataSetURIParameter.getExpression(), this, e);
    } finally {
      requestFinish();
    }
  }

  @Override
  protected void doWrapUp() throws TerminationException {
    super.doWrapUp();

    if (dataSet != null) {
      try {
        dataSet.close();
      } catch (IOException e) {
        throw new TerminationException(ErrorCode.ERROR, "Error closing dataset " + dataSet.getLocation(), this, e);
      }
    }
  }
}
