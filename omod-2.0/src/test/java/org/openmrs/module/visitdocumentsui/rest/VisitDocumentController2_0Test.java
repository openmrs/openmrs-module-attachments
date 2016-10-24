package org.openmrs.module.visitdocumentsui.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Obs;
import org.openmrs.api.ObsService;
import org.openmrs.module.visitdocumentsui.VisitDocumentsConstants;
import org.openmrs.module.visitdocumentsui.obs.TestHelper;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.beans.factory.annotation.Autowired;

public class VisitDocumentController2_0Test extends MainResourceControllerTest {
   
   @Autowired
   protected ObsService obsService;
   
   @Autowired
   protected TestHelper testHelper;
   
   private Obs obs;
   
   @Before
   public void setup() throws IOException {
      obs = testHelper.getTestComplexObs();
   }

   @Override
   public String getURI() {
      return VisitDocumentsConstants.VISIT_DOCUMENT_URI;
   }

   @Override
   public String getUuid() {
      return obs.getUuid();
   }

   @Override
   public long getAllCount() {
      return 0;
   }
   
   @Override @Test public void shouldGetAll() {}
   @Override @Test public void shouldGetDefaultByUuid() {}
   @Override @Test public void shouldGetRefByUuid() {}
   @Override @Test public void shouldGetFullByUuid() {}
   
   @Test
   public void postVisitDocument_shouldUpdateObsComment() throws Exception {
      // Setup
      String editedComment = "Hello world!";
      String json = "{\"uuid\":\"" + getUuid() + "\",\"comment\":\"" + editedComment + "\"}";
      
      // Replay
      Object doc = deserialize( handle( newPostRequest(getURI() + "/" + getUuid(), json) ) );
      
      // Verif
      String comment = (String) PropertyUtils.getProperty(doc, "comment");
      assertEquals(editedComment, comment);
   }
   
   @Test
   public void deleteVisitDocument_shouldPurgeObs() throws Exception {
      // Setup
      File file = new File(testHelper.getLastSavedFilePath());
      assertTrue(file.exists());
      
      // Replay
      handle( newDeleteRequest(getURI() + "/" + getUuid(), new Parameter("purge", "")) );
      
      // Verif
      assertNull(obsService.getObsByUuid(getUuid()));
      assertFalse(file.exists());
   }
}