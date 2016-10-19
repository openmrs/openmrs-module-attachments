package org.openmrs.module.visitdocumentsui.rest;

import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.visitdocumentsui.obs.VisitDocument;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.GenericRestException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

@Resource(name = RestConstants.VERSION_1 + "/visitdocument", supportedClass = VisitDocument.class,
supportedOpenmrsVersions = {"1.10.*", "1.11.*", "1.12.*", "2.0.*"})
public class VisitDocumentResource1_10 extends DataDelegatingCrudResource<VisitDocument> {

   @Override
   public VisitDocument newDelegate() {
      return new VisitDocument();
   }

   @Override
   public VisitDocument save(VisitDocument delegate) {
      // TODO Auto-generated method stub
      return delegate;
   }

   @Override
   public VisitDocument getByUniqueId(String uniqueId) {
      Obs obs = Context.getObsService().getObsByUuid(uniqueId);
//      if (!obs.isComplex())
//         throw new GenericRestException(uniqueId + " does not identify a complex obs.", null);
      VisitDocument doc = new VisitDocument();
      doc.setUuid(uniqueId);
      doc.setComment(obs.getComment());
      return doc;
   }

   @Override
   protected void delete(VisitDocument delegate, String reason, RequestContext context) throws ResponseException {
      // TODO Auto-generated method stub
   }

   @Override
   public void purge(VisitDocument delegate, RequestContext context) throws ResponseException {
      // TODO Auto-generated method stub
   }
   
   @Override
   public DelegatingResourceDescription getCreatableProperties() {
      DelegatingResourceDescription description = new DelegatingResourceDescription();
//      description.addRequiredProperty("comment");
      description.addProperty("comment");
      return description;
   }

   @Override
   public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
      DelegatingResourceDescription description = new DelegatingResourceDescription();
      description.addProperty("uuid");
      description.addProperty("comment");
      description.addSelfLink();
      return description;
   }
}
