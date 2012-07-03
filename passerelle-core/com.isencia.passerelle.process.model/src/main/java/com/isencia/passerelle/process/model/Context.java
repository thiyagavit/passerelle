/* Copyright 2012 - iSencia Belgium NV

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

package com.isencia.passerelle.process.model;


/**
 * @author erwin
 *
 */
public interface Context extends LifeCycleEntity {
	/**
	 * 
	 * @return for unfinished decisions, this returns an estimation
	 * of the expected remaining waiting time, before the processing will be complete.
	 */
	Integer getExpectedWaitingTime();
	/**
	 * <p>
	 * The status code is a 2-digit code, used to represent all kinds of
	 * detailed states.
	 * </p>
	 * <p>
	 * Besides this property, one can also check <code>getRequest().getStatus()</code>
	 * to see, on a less-detailed level, whether the request's processing is finished, 
	 * has resulted in errors etc.
	 * </p>
	 * @return a 2-digit status code.
	 */
	String getStatusCode();
	/**
	 * 
	 * @return an optional description of the above status
	 */
	String getStatusMsg();
	
}
