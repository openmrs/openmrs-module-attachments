package org.openmrs.module.visitdocumentsui.rest;

import org.junit.Test;
import org.openmrs.module.visitdocumentsui.obs.VisitDocument;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;

public class VisitDocumentResource1_10Test extends BaseDelegatingResourceTest<VisitDocumentResource1_10, VisitDocument> {

   private VisitDocument doc = new VisitDocument(); 
   
   @Override
   public VisitDocument newObject() {
      return doc;
   }

   @Override
   public String getDisplayProperty() {
      return null;
   }
   
   @Override
   public String getUuidProperty() {
      return doc.getUuid();
   }
   
   @Test
   public void shouldLoadResource() throws Exception {
      VisitDocumentResource1_10 resource = getResource();
   }
}
