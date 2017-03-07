package org.openmrs.module.visitdocumentsui.rest;

import org.hibernate.FlushMode;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.db.DbSessionUtil;
import org.openmrs.module.visitdocumentsui.obs.VisitDocument;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;

@Resource(name = RestConstants.VERSION_1 + "/visitdocument", supportedClass = VisitDocument.class,
supportedOpenmrsVersions = {"2.0.*"})
public class VisitDocumentResource2_0 extends VisitDocumentResource1_10 {

   @Override
   public VisitDocument save(VisitDocument delegate) {
      FlushMode flushMode = DbSessionUtil.getCurrentFlushMode();
      DbSessionUtil.setManualFlushMode();
      VisitDocument visitDoc = new VisitDocument();
      try {
         Obs obs = Context.getObsService().saveObs(delegate.getObs(), REASON);
         visitDoc = new VisitDocument(obs);
      } finally {
         DbSessionUtil.setFlushMode(flushMode);
      }
      return visitDoc;
   }
}