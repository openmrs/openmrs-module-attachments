package org.openmrs.module.visitdocumentsui.rest;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptComplex;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.GlobalProperty;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.module.visitdocumentsui.VisitDocumentsActivator;
import org.openmrs.module.visitdocumentsui.VisitDocumentsConstants;
import org.openmrs.module.visitdocumentsui.obs.ComplexDataHelper;
import org.openmrs.module.visitdocumentsui.obs.ValueComplex;
import org.openmrs.module.visitdocumentsui.web.controller.VisitDocumentsController;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockMultipartFile;

public class ComplexObsResource2_0Test extends BaseDelegatingResourceTest<ComplexObsResource2_0, Obs> {
   
   @Autowired
   protected VisitDocumentsActivator activator;
   
   @Autowired
   protected VisitDocumentsController controller;
   
   @Autowired
   @Qualifier(VisitDocumentsConstants.COMPONENT_COMPLEXDATA_HELPER)
   protected ComplexDataHelper complexDataHelper;
   
   private Obs obs = new Obs();
   
   @Before
   public void setup() throws IOException {
      activator.started(); // so our concepts complex are created
      
      Patient patient = Context.getPatientService().getPatient(2);
      Visit visit = Context.getVisitService().getVisit(1);
      EncounterService encounterService = Context.getEncounterService();
      EncounterType encounterType = encounterService.getEncounterType(1);
      Provider provider = Context.getProviderService().getProvider(1);
      EncounterRole encounterRole = encounterService.getEncounterRoleByUuid(EncounterRole.UNKNOWN_ENCOUNTER_ROLE_UUID);
      
      Encounter encounter = controller.getVisitDocumentEncounter(patient, visit, encounterType, provider, encounterRole, encounterService);
      
      String comment = RandomStringUtils.randomAlphabetic(12);
      Concept concept = Context.getConceptService().getConceptByUuid(VisitDocumentsConstants.CONCEPT_DEFAULT_UUID);
      obs = controller.prepareComplexObs(patient, encounter, comment, (ConceptComplex) concept);
      
      Context.getAdministrationService().saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_COMPLEX_OBS_DIR, (Files.createTempDirectory(null)).toAbsolutePath().toString()));
      
      MockMultipartFile multipartFile = new MockMultipartFile("filename", "filename.dat", "application/octet-stream", "mock_content".getBytes());
      obs = controller.saveOtherDocument(obs, multipartFile, ValueComplex.INSTRUCTIONS_DEFAULT, Context.getObsService(), complexDataHelper);
   }
   
   @Override
   public Obs newObject() {
      return obs;
   }

   @Override
   public String getDisplayProperty() {
      return (new ValueComplex(obs.getValueComplex())).toString();
   }

   @Override
   public String getUuidProperty() {
      return obs.getUuid();
   }
   
   @Override @Test public void asRepresentation_shouldReturnValidRefRepresentation() {}
   
   @Test
   public void postComplexObs_shouldCommentBeSaved() {
      ComplexObsResource2_0 resource = getResource();
      String newComment = "Hello world!"; 
      obs.setComment(newComment);
      obs = resource.save(obs);
      
      assertEquals(newComment, obs.getComment());
   }
}
