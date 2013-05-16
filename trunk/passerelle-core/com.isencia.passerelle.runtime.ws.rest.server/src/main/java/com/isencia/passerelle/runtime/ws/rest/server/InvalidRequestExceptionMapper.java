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
