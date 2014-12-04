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
package eu.esrf.passerelle.python.actor;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.gui.style.TextStyle;
import ptolemy.data.StringToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.edm.backend.actor.common.AsynchronousActorActivator;
import com.isencia.passerelle.edm.backend.actor.common.AsynchronousTaskBasedActor;
import com.isencia.passerelle.process.actor.AttributeNames;
import com.isencia.passerelle.process.common.exception.ErrorCode;
import com.isencia.passerelle.process.model.Context;

import eu.esrf.passerelle.python.actor.activator.Activator;

/**
 * Python actor implementation based on an EDM actor/service design. Compatible with EDM 1.5.
 * <p>
 * Remark : depends on a base class that is not yet in open source.
 * When we migrate the ESRF Python actor to the latest Passerelle EDM, this will change.
 * </p>
 * 
 * @author erwindl
 * 
 */
public class PythonActor extends AsynchronousTaskBasedActor {
  private static final long serialVersionUID = -2906695855929667295L;
  private final static Logger LOGGER = LoggerFactory.getLogger(PythonActor.class);

  public static final String PATH_ATTRNAME = "script_path";
  public static final String ASSET_CODE_ATTRNAME = "script_asset";

  public FileParameter scriptPathParameter;
  public Parameter assetParameter;

  /**
   * Used to configure the attributes that must be added to each new Task.
   * <p>
   * Each new line in the String value of this parameter represents one attribute. Following syntaxes are possible :
   * <ul>
   * <li>attrName : the attrName is used to lookup an item in the parent process context and, iff found, a task
   * attribute is created with the value found and the given attrName</li>
   * <li>attrName=lookupItemName : the lookupItemName is used to lookup an item in the parent process context and, iff
   * found, a task attribute is created with the value found and the given attrName</li>
   * </ul>
   * </p>
   */
  public StringParameter attributeMappingParameter;

  public PythonActor(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    assetParameter = new Parameter(this, ASSET_CODE_ATTRNAME);
    scriptPathParameter = new FileParameter(this, PATH_ATTRNAME);
    attributeMappingParameter = new StringParameter(this, AttributeNames.ATTR_MAPPING);
    new TextStyle(attributeMappingParameter, "textarea");
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  @Override
  protected boolean wantsUppercaseAttributeNames() {
    return false;
  }

  @Override
  protected AsynchronousActorActivator getActivator() {
    return Activator.getDefault();
  }

  @Override
  protected String getServiceName() {
    return Activator.PYTHON_SERVICE_NAME;
  }

  @Override
  protected void initActivatorOutsideOSGi() {
    Activator.initOutsideOSGi();
  }

  @Override
  protected void addActorSpecificTaskAttributes(Context processContext, Map<String, String> taskAttributes) throws ProcessingException {

    try {
      createTaskAttributeForKey(processContext, taskAttributes, PATH_ATTRNAME, scriptPathParameter.stringValue());
      createTaskAttributeForKey(processContext, taskAttributes, ASSET_CODE_ATTRNAME, assetParameter.getExpression());
      Map<String, String> attrMappings = getAttributeMappings();
      for (Entry<String, String> attrEntry : attrMappings.entrySet()) {
        createTaskAttributeForKey(processContext, taskAttributes, attrEntry.getValue(), attrEntry.getKey(), (String) null);
      }
    } catch (Exception e) {
      throw new ProcessingException(ErrorCode.TASK_ERROR, "Unable to obtain task attributes", this, e);
    }
  }

  /**
   * 
   * @return a map with entries (attrName, lookupItemName), i.e. defining the task attributes with their attrName as
   *         key, and as entry value : the name of the item in the parent process context, where to look for the value
   *         that must be assigned to the task attribute.
   * @throws Exception
   *           i.c.o. a failure reading the definition of the attribute mapping
   */
  private Map<String, String> getAttributeMappings() throws Exception {
    String mappingDefs = ((StringToken) attributeMappingParameter.getToken()).stringValue();
    Map<String, String> attrMapping = new HashMap<String, String>();
    BufferedReader reader = new BufferedReader(new StringReader(mappingDefs));
    String mappingDef = null;
    while ((mappingDef = reader.readLine()) != null) {
      String[] mappingParts = mappingDef.split("=");
      if (mappingParts.length == 2) {
        String attrName = mappingParts[0];
        String lookupItemName = mappingParts[1];
        attrMapping.put(attrName, lookupItemName);
      } else if (mappingParts.length == 1) {
        String attrName = mappingParts[0];
        attrMapping.put(attrName, attrName);
      }
    }
    return attrMapping;
  }
}
