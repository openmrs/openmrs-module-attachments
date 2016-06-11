package org.openmrs.module.visitdocumentsui.web.controller;

import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.visitdocumentsui.VisitDocumentsConstants;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_11.ObsResource1_11;

@Resource(name = RestConstants.VERSION_1 + "/complexobs", order = 1, supportedClass = Obs.class, supportedOpenmrsVersions = {"1.11.*", "1.12.*"})
public class ComplexObsResource extends ObsResource1_11 {
   
   @Override
   public Obs save(Obs delegate) {
      return super.save(complexify(delegate));
   }
   
   @Override
   public void purge(Obs delegate, RequestContext context) throws ResponseException {
      super.purge(complexify(delegate), context);
   }
   
   public static Obs complexify(Obs delegate) {
      if (isComplex(delegate) && null == delegate.getComplexData()) {
         String valueComplex = delegate.getValueComplex();
         delegate = Context.getObsService().getComplexObs(delegate.getId(), VisitDocumentsConstants.DOC_VIEW_CRUD);
         delegate.setValueComplex(valueComplex);
      }
      return delegate;
   }
   
   public static boolean isComplex(Obs obs) {
      return (null != obs && null != obs.getConcept() && obs.getConcept().isComplex());
   }
}
