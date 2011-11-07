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
package com.isencia.passerelle.core;

import com.isencia.passerelle.actor.Actor;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * PortFactory
 * 
 * TODO: class comment
 * 
 * @author erwin
 */
public class PortFactory {
	private final static PortFactory instance = new PortFactory();
	
	public static PortFactory getInstance() {
		return instance;
	}
	
	private PortFactory() {
	}

	/**
	 * @param container
	 * @return
	 * @throws NameDuplicationException 
	 * @throws IllegalActionException 
	 */
	public Port createOutputPort(Actor container) throws IllegalActionException, NameDuplicationException {
		return createOutputPort(container,"output");
	}
	public Port createOutputPort(Actor container, String name) throws IllegalActionException, NameDuplicationException {
		Port res = new Port(container,name,false,true);
		return res;
	}
	/**
	 * @param container
	 * @return
	 * @throws NameDuplicationException 
	 * @throws IllegalActionException 
	 */
	public ErrorPort createOutputErrorPort(Actor container) throws IllegalActionException, NameDuplicationException {
		return createOutputErrorPort(container,"error");
	}
	public ControlPort createInputControlPort(Actor container, String name) throws IllegalActionException, NameDuplicationException {
		ControlPort res = new ControlPort(container,name,true,false);
		return res;
	}
	public ControlPort createOutputControlPort(Actor container, String name) throws IllegalActionException, NameDuplicationException {
		ControlPort res = new ControlPort(container,name,false,true);
		return res;
	}
	public ErrorPort createInputErrorPort(Actor container, String name) throws IllegalActionException, NameDuplicationException {
		ErrorPort res = new ErrorPort(container,name,true,false);
		return res;
	}
	public ErrorPort createOutputErrorPort(Actor container, String name) throws IllegalActionException, NameDuplicationException {
		ErrorPort res = new ErrorPort(container,name,false,true);
		return res;
	}

	/**
	 * @param container
	 * @param expectedContentType optionally specify the expected body content type
	 * @return
	 * @throws NameDuplicationException 
	 * @throws IllegalActionException 
	 */
	public Port createInputPort(Actor container, Class expectedContentType) throws IllegalActionException, NameDuplicationException {
		return createInputPort(container,"input", PortMode.PULL, expectedContentType);
	}
	 /**
	  * 
	  * @param container
	  * @param name
	  * @param expectedContentType optionally specify the expected body content type
	  * @return
	  * @throws IllegalActionException
	  * @throws NameDuplicationException
	  */
	public Port createInputPort(Actor container, String name, Class expectedContentType) throws IllegalActionException, NameDuplicationException {
		return createInputPort(container, name, PortMode.PULL, expectedContentType);
	}
	/**
	 * @param container
	 * @param mode
	 * @param expectedContentType optionally specify the expected body content type
	 * @return
	 * @throws NameDuplicationException 
	 * @throws IllegalActionException 
	 */
	public Port createInputPort(Actor container, PortMode mode, Class expectedContentType) throws IllegalActionException, NameDuplicationException {
		return createInputPort(container, "input", mode, expectedContentType);
	}
	 /**
	  * 
	  * @param container
	  * @param name
	  * @param mode
	  * @param expectedContentType optionally specify the expected body content type
	  * @return
	  * @throws IllegalActionException
	  * @throws NameDuplicationException
	  */
	public Port createInputPort(Actor container, String name, PortMode mode, Class expectedContentType) throws IllegalActionException, NameDuplicationException {
		Port res = new Port(container,name,mode,true,false);
		res.setExpectedMessageContentType(expectedContentType);
		return res;
	}
}
