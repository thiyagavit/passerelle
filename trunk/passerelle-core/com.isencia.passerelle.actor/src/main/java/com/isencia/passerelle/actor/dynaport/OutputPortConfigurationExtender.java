/* Copyright 2011 - iSencia Belgium NV

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

package com.isencia.passerelle.actor.dynaport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.ValueListener;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.ext.ConfigurationExtender;

/**
 * @author delerw
 *
 */
public class OutputPortConfigurationExtender extends Attribute implements ConfigurationExtender, ValueListener {
  private static Logger LOGGER = LoggerFactory.getLogger(OutputPortConfigurationExtender.class);

  private static final String OUTPUT_PORTNAMES = "Output port names (comma-separated)";
  public StringParameter outputPortNamesParameter = null;
  
  private Set<String> outputPortNames = new HashSet<String>();
  // this will deliver a secured interface on the available output port names
  private Set<String> outputPortNamesForContainerAccess = Collections.unmodifiableSet(outputPortNames);
  private Entity container;

  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public OutputPortConfigurationExtender(Entity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    this.container = container;
    outputPortNamesParameter = new StringParameter(container, OUTPUT_PORTNAMES);
    outputPortNamesParameter.addValueListener(this);
  }
  
  public Collection<String> getOutputPortNames() {
    return outputPortNamesForContainerAccess; 
  }

  // TODO : problem is that there's no way to pass error info to the source of the change
  // so no possibility to warn user that the configured ports could not be created correctly
  // Maybe need to do this change via the attributeChanged() of the containing actor after all?
  // But then this config extender can only work on some specific Passerelle actors that 
  // have adapted attributeChanged()...
  @Override
  public void valueChanged(Settable settable) {
    // should always be our output port parameter, but still...
    if (settable == outputPortNamesParameter) {
      String outputPortNames = outputPortNamesParameter.getExpression();
      changeOutputPorts(outputPortNames);
    }
  }

  /**
   * @return Returns the outputPorts.
   */
  @SuppressWarnings("unchecked")
  public List<Port> getOutputPorts() {
    // in order to avoid cloning issues
    // when we would maintain the list of dynamically cfg-ed
    // output ports in an instance variable,
    // we build this list dynamically here from
    // Ptolemy's internal port list
    List<Port> ports = new ArrayList<Port>();
    for (String portName : outputPortNames) {
      Port p = (Port) container.getPort(portName);
      if (p != null)
        ports.add(p);
      else {
        LOGGER.error("{} - internal error - configured port not found with name {}", container.getFullName(), portName);
      }
    }
    return ports;
  }

  /**
   * @param portNames comma-separated
   * @throws IllegalActionException
   * @throws IllegalArgumentException
   */
  protected void changeOutputPorts(String portNames) {
    LOGGER.trace("{} - changeOutputPorts() - entry - portNames : {}", container.getFullName(), portNames);

    Set<String> previousPortNames = new HashSet<String>(outputPortNames);
    outputPortNames.clear();
    String[] newPortNames = portNames.split(",");

    // first add new ports
    for (String portName : newPortNames) {
      Port aPort = (Port) container.getPort(portName);
      if (aPort == null) {
        // create a new one
        try {
          createPort(portName);
        } catch (IllegalActionException e) {
          LOGGER.error("{} - internal error - failed to create port with name {}", container.getFullName(), portName);
        }
      }
      previousPortNames.remove(portName);
      outputPortNames.add(portName);
    }
    // then remove removed ports, based on remaining names in the old port names list
    for (String portName : previousPortNames) {
      try {
        container.getPort(portName).setContainer(null);
      } catch (Exception e) {
        LOGGER.error("{} - internal error - failed to remove port with name {}", container.getFullName(), portName);
      }
    }

    LOGGER.trace("{} - changeOutputPorts() - exit", container.getFullName());
  }

  /**
   * @param portName
   * @return
   * @throws IllegalActionException
   */
  protected Port createPort(String portName) throws IllegalActionException {
    LOGGER.trace("{} - createPort() - entry - name : {}", container.getFullName(), portName);
    
    Port aPort = null;
    try {
      aPort = (Port) container.getPort(portName);

      if (aPort == null) {
        LOGGER.debug("{} - createPort() - port {} will be constructed", container.getFullName(), portName);
        aPort = PortFactory.getInstance().createOutputPort(container, portName);
        aPort.setMultiport(true);
      } else {
        throw new IllegalActionException(container, "port " + portName + " already exists");
      }
    } catch (Exception e) {
      throw new IllegalActionException(this, e, "failed to create port " + portName);
    }
    LOGGER.trace("{} - createPort() - exit - port : {}", container.getFullName(), portName);
    return aPort;
  }
}
