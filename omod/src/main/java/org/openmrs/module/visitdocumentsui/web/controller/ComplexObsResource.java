package org.openmrs.module.visitdocumentsui.web.controller;

import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.visitdocumentsui.VisitDocumentsConstants;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_11.ObsResource1_11;

@Resource(name = RestConstants.VERSION_1 + "/complexobs", order = 1, supportedClass = Obs.class, supportedOpenmrsVersions = {"1.11.*", "1.12.*"})
public class ComplexObsResource extends ObsResource1_11 {
   
   @Override
   public Obs save(Obs delegate) {
      if (isComplex(delegate) && null == delegate.getComplexData()) {
         delegate = Context.getObsService().getComplexObs(delegate.getId(), VisitDocumentsConstants.DOC_VIEW_CRUD);
      }
      return super.save(delegate);
   }
   
   public static boolean isComplex(Obs obs) {
      return (null != obs && null != obs.getConcept() && obs.getConcept().isComplex());
   }
}
