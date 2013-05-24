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
package com.isencia.passerelle.runtime.ws.rest.server;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.runtime.ws.rest.ErrorInfo;
import com.isencia.passerelle.runtime.ws.rest.InvalidRequestException;

@Provider
public class InvalidRequestExceptionMapper implements ExceptionMapper<InvalidRequestException> {

  @Override
  public Response toResponse(InvalidRequestException exception) {
    ErrorCode errCode = exception.getErrorCode();
    ErrorInfo errItem = new ErrorInfo(errCode.getSeverity(), errCode.getFormattedCode(), errCode.name(), exception.getSimpleMessage());
    
    return Response.status(Status.BAD_REQUEST).entity(errItem).build();
  }

}
