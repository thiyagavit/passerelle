package com.isencia.passerelle.runtime.ws.rest;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.runtime.repository.DuplicateEntryException;

@Provider
public class DuplicateEntryExceptionMapper implements ExceptionMapper<DuplicateEntryException> {

  @Override
  public Response toResponse(DuplicateEntryException exception) {
    ErrorCode errCode = exception.getErrorCode();
    ErrorInfo errItem = new ErrorInfo(errCode.getSeverity(), errCode.getFormattedCode(), errCode.name(), exception.getSimpleMessage());
    
    return Response.status(Status.CONFLICT).entity(errItem).build();
  }

}
