package org.openmrs.module.visitdocumentsui.rest;

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.module.visitdocumentsui.VisitDocumentsConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;

public class VisitDocumentController1_10Test extends MainResourceControllerTest {

   @Override
   public String getURI() {
      return VisitDocumentsConstants.VISIT_DOCUMENT_URI + "/" + getUuid();
   }

   @Override
   public String getUuid() {
      return "39fb7f47-e80a-4056-9285-bd798be13c63";
   }

   @Override
   public long getAllCount() {
      // TODO Auto-generated method stub
      return 0;
   }
   
   @Override @Test public void shouldGetAll() {}
   @Override @Test public void shouldGetDefaultByUuid() {}
   @Override @Test public void shouldGetRefByUuid() {}
   @Override @Test public void shouldGetFullByUuid() {}
   
   @Test
   public void updateVisitDocument_shouldSaveComment() throws Exception {
      String json = "{\"uuid\":\"" + getUuid() + "\",\"comment\":\"Hello world!\"}";
//      String json = "{\"uuid\":\"d3322e68-6cef-41f6-b976-a3dec1c73610\",\"comment\":\"Hello world!\"}";
      
      Object doc = deserialize(handle(newPostRequest(getURI(), json)));
      Assert.assertNotNull(PropertyUtils.getProperty(doc, "comment"));
   }
}
