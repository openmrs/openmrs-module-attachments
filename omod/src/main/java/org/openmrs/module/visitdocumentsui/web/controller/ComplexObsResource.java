package org.openmrs.module.visitdocumentsui.web.controller;

import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.visitdocumentsui.VisitDocumentsConstants;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_11.ObsResource1_11;
import org.openmrs.obs.ComplexObsHandler;

@Resource(name = RestConstants.VERSION_1 + "/complexobs", order = 1, supportedClass = Obs.class, supportedOpenmrsVersions = {"1.11.*", "1.12.*"})
public class ComplexObsResource extends ObsResource1_11 {
   
   @Override
   public Obs save(Obs delegate) {
      if (isComplex(delegate) && null == delegate.getComplexData()) {
         String valueComplex = delegate.getValueComplex();
         delegate = Context.getObsService().getComplexObs(delegate.getId(), VisitDocumentsConstants.DOC_VIEW_CRUD);
         delegate.setValueComplex(valueComplex);
      }
      return super.save(delegate);
   }
   
   @Override
   protected void delete(Obs delegate, String reason, RequestContext context) throws ResponseException {
      if (isComplex(delegate) && null == delegate.getComplexData()) {
         String valueComplex = delegate.getValueComplex();
         delegate = Context.getObsService().getComplexObs(delegate.getId(), VisitDocumentsConstants.DOC_VIEW_CRUD);
         delegate.setValueComplex(valueComplex);
         
         String handlerString = Context.getConceptService().getConceptComplex(delegate.getConcept().getConceptId()).getHandler();
         ComplexObsHandler handler = Context.getObsService().getHandler(handlerString);
         handler.purgeComplexData(delegate);
      }
      super.delete(delegate, reason, context); 
   }
   
   public static boolean isComplex(Obs obs) {
      return (null != obs && null != obs.getConcept() && obs.getConcept().isComplex());
   }
}
