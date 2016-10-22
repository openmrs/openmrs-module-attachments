package org.openmrs.module.visitdocumentsui.obs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.lang.RandomStringUtils;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.GlobalProperty;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.module.visitdocumentsui.ComplexObsSaver;
import org.openmrs.module.visitdocumentsui.VisitDocumentsActivator;
import org.openmrs.module.visitdocumentsui.VisitDocumentsConstants;
import org.openmrs.module.visitdocumentsui.VisitDocumentsContext;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;

@Component
public class TestHelper {
   
   @Autowired
   protected VisitDocumentsActivator activator;
   
   @Autowired
   @Qualifier(VisitDocumentsConstants.COMPONENT_VDUI_CONTEXT)
   protected VisitDocumentsContext context;
   
   @Autowired
   @Qualifier(VisitDocumentsConstants.COMPONENT_COMPLEXDATA_HELPER)
   protected ComplexDataHelper complexDataHelper;
   
   @Autowired
   @Qualifier(VisitDocumentsConstants.COMPONENT_COMPLEXOBS_SAVER)
   protected ComplexObsSaver obsSaver;
   
   protected Path lastTmpDir;
   
   protected String fileName = "filename";
   protected String fileExt = "dat";
   
   public String getLastTmpDirPath() {
      return lastTmpDir.toAbsolutePath().toString();
   }
   
   public String getLastSavedFilePath() {
      return getLastTmpDirPath() + "/" + fileName + "." + fileExt;
   }
   
   public Obs getTestComplexObs() throws IOException {
      activator.started(); // so our concepts complex are created
      
      Patient patient = Context.getPatientService().getPatient(2);
      Visit visit = Context.getVisitService().getVisit(1);
      EncounterService encounterService = Context.getEncounterService();
      EncounterType encounterType = encounterService.getEncounterType(1);
      Provider provider = Context.getProviderService().getProvider(1);

      context.getAdministrationService().saveGlobalProperty( new GlobalProperty(VisitDocumentsConstants.GP_ENCOUNTER_TYPE_UUID, encounterType.getUuid()) );
      Encounter encounter = context.getVisitDocumentEncounter(patient, visit, provider);
      
      MockMultipartFile multipartFile = new MockMultipartFile(fileName, fileName + "." + fileExt, "application/octet-stream", "mock_content".getBytes());

      lastTmpDir = Files.createTempDirectory(null);
      context.getAdministrationService().saveGlobalProperty( new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_COMPLEX_OBS_DIR, lastTmpDir.toAbsolutePath().toString()) );
      String conceptComplexUuidMap = "{\"IMAGE\":\"7cac8397-53cd-4f00-a6fe-028e8d743f8e\",\"OTHER\":\"42ed45fd-f3f6-44b6-bfc2-8bde1bb41e00\"}";
      context.getAdministrationService().saveGlobalProperty( new GlobalProperty(VisitDocumentsConstants.GP_CONCEPT_COMPLEX_UUID_MAP, conceptComplexUuidMap) );
      return obsSaver.saveOtherDocument(patient, encounter, RandomStringUtils.randomAlphabetic(12), multipartFile, ValueComplex.INSTRUCTIONS_DEFAULT);
   }
}
