package com.isencia.passerelle.runtime.ws.rest.server;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.runtime.repository.EntryNotFoundException;
import com.isencia.passerelle.runtime.ws.rest.ErrorInfo;

@Provider
public class EntryNotFoundExceptionMapper implements ExceptionMapper<EntryNotFoundException> {

  @Override
  public Response toResponse(EntryNotFoundException exception) {
    ErrorCode errCode = exception.getErrorCode();
    ErrorInfo errItem = new ErrorInfo(errCode.getSeverity(), errCode.getFormattedCode(), errCode.name(), exception.getSimpleMessage());
    
    return Response.status(Status.NOT_FOUND).entity(errItem).build();
  }

}
