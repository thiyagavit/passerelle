/* Copyright 2013 - iSencia Belgium NV

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
package com.isencia.passerelle.process.service;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.isencia.passerelle.process.model.Request;
import com.isencia.util.FutureValue;

/**
 * 
 * @author erwin
 *
 */
public interface RequestProcessingService {
  
  /**
   * Process the given Request within the given timeout, if this service instance is capable of handling it.
   * If the service is unable to handle it, it should simply return <code>null</code> immediately.
   * <p>
   * The service implementation is responsible for notifying the system when the task is finished,
   * or when a timeout or error occurred, using the related methods in {@link ProcessManager}.
   * </p>
   * <p>
   * Service implementations are by preference non-blocking, and should just return a {@link Future}
   * to the finished Request.
   * <br/>
   * Blocking service implementations are of course possible, and could use e.g. {@link FutureValue}
   * to return a pre-filled Future.
   * </p>
   * 
   * @param request the Request/Task that must be processed
   * @param timeout the timeout period; null or <=0 values indicate : no timeout should be set.
   * @param unit the {@link TimeUnit} of the timeout period
   * @return a Future to the request after processing is finished or null if this service is unable to process the given request
   */
  Future<Request> process(Request request, Long timeout, TimeUnit unit);
}
