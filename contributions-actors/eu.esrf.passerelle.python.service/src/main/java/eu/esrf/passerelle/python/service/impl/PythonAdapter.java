/* Copyright 2014 - iSencia Belgium NV - ESRF

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
package eu.esrf.passerelle.python.service.impl;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.map.ObjectMapper;

import uk.ac.diamond.python.service.PythonService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.edm.backend.service.common.AbstractAdapter;
import com.isencia.passerelle.edm.backend.service.common.Result;
import com.isencia.passerelle.edm.backend.service.common.Result.ResultType;
import com.isencia.passerelle.edm.request.service.ServiceException;
import com.isencia.passerelle.process.model.ResultBlock;
import com.isencia.passerelle.process.model.Task;
import com.isencia.passerelle.process.service.ServiceRegistry;

import eu.esrf.passerelle.python.api.EsrfRepositoryService;
import eu.esrf.passerelle.python.api.PythonScript;
import eu.esrf.passerelle.python.service.Constants;
import eu.esrf.passerelle.python.service.activator.Activator;

/**
 * Remark : depends on a base class that is not yet in open source.
 * When we migrate the ESRF Python actor to the latest Passerelle EDM, this will change.
 * 
 * @author erwindl
 *
 */
public class PythonAdapter extends AbstractAdapter {

  public static final String RESOURCE_NAME = "PYTHON";
  public static final String SERVICE_NAME = "eu.esrf.services.python.v2.6";

  private final static Logger LOGGER = LoggerFactory.getLogger(PythonAdapter.class);

  // TODO make this configurable
  private String pythonInterpreter = Constants.PYTHONINTERPRETER_DEFAULT;

  @Override
  public String getServiceName() {
    return SERVICE_NAME;
  }

  @Override
  public String getPreferencesNodeName() {
    return "eu.esrf.passerelle.python.service";
  }

  @Override
  public void notifyConfigurationChanged() {
    // TODO Auto-generated method stub

  }

  @Override
  public Result process(Task task) throws ServiceException {
    PythonService service = null;
    File temp = null;
    try {
      String assetCode = getOptionalParameterValue(Constants.ASSET_CODE_ATTRNAME, task);
      String scriptPath = null;
      if (assetCode != null && !assetCode.isEmpty()) {
        EsrfRepositoryService repoService = Activator.getDefaultInstance().getRepositoryService();
        PythonScript pythonScript = repoService.getPythonScript(assetCode);
        if (pythonScript != null) {
          temp = File.createTempFile(pythonScript.getFileName(), ".py");
          FileUtils.writeByteArrayToFile(temp, pythonScript.getData());
          scriptPath = temp.getPath();
        } else {
          throw new ServiceException(ErrorCode.ACTOR_EXECUTION_ERROR, "Python with  asset code " + assetCode + " doesn't exist");
        }
      } else {
        scriptPath = getOptionalParameterValue(Constants.PATH_ATTRNAME, task);
      }

      String resultType = "python";

      Map<String, String> data = new HashMap<String, String>();
      Iterator<String> attributeNames = task.getAttributeNames();
      while (attributeNames.hasNext()) {
        String attrName = (String) attributeNames.next();
        // These attribute names refer to "technical" attributes in the Task and should
        // probably not be transfered to the script.
        if (!Constants.PATH_ATTRNAME.equalsIgnoreCase(attrName)) {
          data.put(attrName, task.getAttributeValue(attrName));
        }
      }
      try {
        service = PythonService.openConnection(pythonInterpreter);
        final Map<String, ? extends Object> result = service.runScript(scriptPath, data);
        if (result != null && !result.isEmpty()) {
          ResultBlock rb = ServiceRegistry.getInstance().getEntityFactory().createResultBlock(task, resultType);
          for (String resultKey : result.keySet()) {
            Object resultValue = result.get(resultKey);
            String resultValueStr = null;
            if (resultValue instanceof String) {
              resultValueStr = (String) resultValue;
            } else {
              ObjectMapper jacksonMapper = new ObjectMapper();
              resultValueStr = jacksonMapper.writeValueAsString(resultValue);
            }
            ServiceRegistry.getInstance().getEntityFactory().createResultItem(rb, resultKey, resultValueStr, null);
          }
        }
      } catch (Exception e) {
        throw new ServiceException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error from python script", e);
      }
      return new Result(ResultType.SYNCH);
    } catch (ServiceException e) {
      throw e;
    } catch (Throwable e) {
      throw new ServiceException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error preparing for python script", e);
    } finally {
      if (temp != null) {
        try {
          temp.delete();
        } catch (Exception e) {
          LOGGER.error("Error deleting temp file", e);
        }
      }
      if (service != null)
        service.stop();
    }
  }

}
