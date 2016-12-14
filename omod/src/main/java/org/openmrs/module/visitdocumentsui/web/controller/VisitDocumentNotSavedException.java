package org.openmrs.module.visitdocumentsui.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class VisitDocumentNotSavedException extends RuntimeException {
   
   private static final long serialVersionUID = -2820372462268285747L;
   
   public VisitDocumentNotSavedException(String message, Throwable cause) {
      super(message, cause);
   }
}