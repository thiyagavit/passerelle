/**
 * 
 */
package com.isencia.passerelle.process.common.configurable;

import com.isencia.passerelle.process.common.exception.ProcessException;

/**
 * Basic contract for EDM system components that are configurable,
 * typically from OSGi preferences.
 * 
 * @author erwin
 *
 */
public interface NamedConfigurable {

	/**
	 * 
	 * @return the name of the component
	 */
	String getName();
	
	/**
	 * 
	 * @return the name/path of the node where the configuration info
	 * for this component can be found.
	 */
	String getConfigurationNodeName();
	
	/**
	 * (re)configure the component based on the current configuration info
	 * 
	 * @throws ConfigurationException
	 */
	void configure() throws ConfigurationException;

}
