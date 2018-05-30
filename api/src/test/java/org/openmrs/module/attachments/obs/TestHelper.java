package org.openmrs.module.attachments.obs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptComplex;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptDescription;
import org.openmrs.ConceptName;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.GlobalProperty;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.module.attachments.AttachmentsActivator;
import org.openmrs.module.attachments.AttachmentsConstants;
import org.openmrs.module.attachments.AttachmentsContext;
import org.openmrs.module.attachments.ComplexObsSaver;
import org.openmrs.obs.handler.BinaryDataHandler;
import org.openmrs.ui.framework.WebConstants;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;

import liquibase.util.file.FilenameUtils;

@Component
public class TestHelper {
	
	public static final String ATTACHMENTS_FOLDER = "attachments";
	
	@Autowired
	protected AttachmentsActivator activator;
	
	@Autowired
	@Qualifier(AttachmentsConstants.COMPONENT_ATT_CONTEXT)
	protected AttachmentsContext context;
	
	@Autowired
	@Qualifier(AttachmentsConstants.COMPONENT_COMPLEXDATA_HELPER)
	protected ComplexDataHelper complexDataHelper;
	
	@Autowired
	@Qualifier(AttachmentsConstants.COMPONENT_COMPLEXOBS_SAVER)
	protected ComplexObsSaver obsSaver;
	
	protected Path complexObsDir;
	
	protected String fileName = "mock_file_name";
	
	protected String fileExt = "ext";
	
	protected MockMultipartFile multipartDefaultFile = new MockMultipartFile(fileName, fileName + "." + fileExt,
	        "application/octet-stream", "mock_content".getBytes());
	
	protected MockMultipartFile lastSavedMultipartImageFile;
	
	protected ConceptComplex ConceptComplexOutAttach;
	
	/**
	 * @return The last saved test image file, null if none was ever saved.
	 */
	public MockMultipartFile getLastSavedTestImageFile() {
		return lastSavedMultipartImageFile;
	}
	
	public MockMultipartFile getTestDefaultFile() {
		return multipartDefaultFile;
	}
	
	public String getTestFileName() {
		return fileName;
	}
	
	public String getTestFileNameWithExt() {
		return getTestFileName() + "." + fileExt;
	}
	
	/**
	 * This initialization routine configure a lot of boilerplate settings to mimic the actual
	 * environment.
	 * 
	 * @throws IOException
	 */
	public void init() throws IOException {
		activator.started(); // so our concepts complex are created
		
		String conceptComplexUuidMap = "{\"IMAGE\":\"" + AttachmentsConstants.CONCEPT_IMAGE_UUID + "\",\"OTHER\":\""
		        + AttachmentsConstants.CONCEPT_DEFAULT_UUID + "\"}";
		context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(AttachmentsConstants.GP_CONCEPT_COMPLEX_UUID_MAP, conceptComplexUuidMap));
		
		complexObsDir = Files.createTempDirectory(null);
		context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_COMPLEX_OBS_DIR, getComplexObsDir()));
		
		WebConstants.CONTEXT_PATH = "openmrs";
		
		context.getAdministrationService()
		        .saveGlobalProperty(new GlobalProperty(AttachmentsConstants.GP_CONCEPT_COMPLEX_UUID_LIST,
		                "[\"7cac8397-53cd-4f00-a6fe-028e8d743f8e\",\"42ed45fd-f3f6-44b6-bfc2-8bde1bb41e00\"]"));
		context.getAdministrationService()
		        .saveGlobalProperty(new GlobalProperty(AttachmentsConstants.GP_MAX_STORAGE_FILE_SIZE, "1.2"));
		context.getAdministrationService()
		        .saveGlobalProperty(new GlobalProperty(AttachmentsConstants.GP_MAX_UPLOAD_FILE_SIZE, "5.0"));
		context.getAdministrationService()
		        .saveGlobalProperty(new GlobalProperty(AttachmentsConstants.GP_ALLOW_NO_CAPTION, "false"));
		context.getAdministrationService()
		        .saveGlobalProperty(new GlobalProperty(AttachmentsConstants.GP_WEBCAM_ALLOWED, "true"));
		context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(AttachmentsConstants.GP_RESTWS_MAX_RESULTS_DEFAULT_GLOBAL_PROPERTY_NAME, "50"));
		
		context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(AttachmentsConstants.GP_ENCOUNTER_TYPE_UUID, AttachmentsConstants.ENCOUNTER_TYPE_UUID));
		
		if (context.getConceptService().getConceptByName("OutOfAttachmentsTestComplex") == null) {
			ConceptComplex conceptComplexOutAttach = new ConceptComplex();
			conceptComplexOutAttach.setHandler(BinaryDataHandler.class.getSimpleName());
			ConceptName conceptName = new ConceptName("OutOfAttachmentsTestComplex", Locale.ENGLISH);
			conceptComplexOutAttach.setFullySpecifiedName(conceptName);
			conceptComplexOutAttach.setPreferredName(conceptName);
			conceptComplexOutAttach
			        .setConceptClass(context.getConceptService().getConceptClassByUuid(ConceptClass.QUESTION_UUID));
			conceptComplexOutAttach
			        .setDatatype(context.getConceptService().getConceptDatatypeByUuid(ConceptDatatype.COMPLEX_UUID));
			conceptComplexOutAttach
			        .addDescription(new ConceptDescription("Out-of-Attachments test concept complex", Locale.ENGLISH));
			context.getConceptService().saveConcept(conceptComplexOutAttach);
			ConceptComplexOutAttach = conceptComplexOutAttach;
			// System.out.println(conceptComplexOutAttach.getUuid());
			
		}
	}
	
	public Obs getTestComplexObs() throws IOException {
		init();
		
		Patient patient = Context.getPatientService().getPatient(2);
		Visit visit = Context.getVisitService().getVisit(1);
		EncounterService encounterService = Context.getEncounterService();
		EncounterType encounterType = encounterService.getEncounterType(1);
		Provider provider = Context.getProviderService().getProvider(1);
		
		context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(AttachmentsConstants.GP_ENCOUNTER_TYPE_UUID, encounterType.getUuid()));
		Encounter encounter = context.getAttachmentEncounter(patient, visit, provider);
		
		String fileCaption = RandomStringUtils.randomAlphabetic(12);
		return obsSaver.saveOtherAttachment(visit, patient, encounter, fileCaption, getTestDefaultFile(),
		    ValueComplex.INSTRUCTIONS_DEFAULT);
	}
	
	/**
	 * Boilerplate method to save an image attachment.
	 * 
	 * @param imagePath The path of the image resource.
	 */
	public Obs saveImageAttachment(String imagePath, String mimeType) throws IOException {
		init();
		
		Patient patient = Context.getPatientService().getPatient(2);
		Visit visit = Context.getVisitService().getVisit(1);
		EncounterService encounterService = Context.getEncounterService();
		EncounterType encounterType = encounterService.getEncounterType(1);
		Provider provider = Context.getProviderService().getProvider(1);
		
		context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(AttachmentsConstants.GP_ENCOUNTER_TYPE_UUID, encounterType.getUuid()));
		Encounter encounter = context.getAttachmentEncounter(patient, visit, provider);
		
		String imageFileName = FilenameUtils.getName(imagePath);
		lastSavedMultipartImageFile = new MockMultipartFile(FilenameUtils.getBaseName(imageFileName), imageFileName,
		        mimeType, IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream(imagePath)));
		
		String fileCaption = RandomStringUtils.randomAlphabetic(12);
		return obsSaver.saveImageAttachment(visit, patient, encounter, fileCaption, lastSavedMultipartImageFile,
		    ValueComplex.INSTRUCTIONS_DEFAULT);
	}
	
	/**
	 * Boilerplate method to save an 'normal sized' image attachment. This method doesn't ensure that
	 * the size is normal, the method just uses an image file that is assumed to fit.
	 */
	public Obs saveNormalSizeImageAttachment() throws IOException {
		return saveImageAttachment(ATTACHMENTS_FOLDER + "/" + "OpenMRS_banner.jpg", "image/jpeg");
	}
	
	/**
	 * Boilerplate method to save an 'small sized' image attachment. Small sized mean already small
	 * enough to be its own thumbnail. This method doesn't ensure that the size is small, the method
	 * just uses an image file that is assumed to fit.
	 */
	public Obs saveSmallSizeImageAttachment() throws IOException {
		return saveImageAttachment(ATTACHMENTS_FOLDER + "/" + "OpenMRS_icon_100x100.png", "image/png");
	}
	
	public Obs getTestComplexObsWithoutAssociatedEncounterOrVisit() throws Exception {
		init();
		
		Patient patient = Context.getPatientService().getPatient(2);
		
		String fileCaption = RandomStringUtils.randomAlphabetic(12);
		return obsSaver.saveOtherAttachment(null, patient, null, fileCaption, getTestDefaultFile(),
		    ValueComplex.INSTRUCTIONS_DEFAULT);
	}
	
	public String getComplexObsDir() {
		return complexObsDir.toAbsolutePath().toString();
	}
	
	/**
	 * @return The file path of the file 'behind' the complex obs.
	 */
	public String getTestComplexObsFilePath() {
		return getComplexObsDir() + "/" + getTestFileNameWithExt();
	}
	
	/**
	 * Boilerplate method to save a collection of complex obs.
	 *
	 * @param count The number of the complex obs to be saved.
	 */
	public List<Obs> saveComplexObs(int count) throws IOException {
		
		init();
		
		List<Obs> obsList = new ArrayList<>();
		byte[] randomData = new byte[20];
		
		Patient patient = Context.getPatientService().getPatient(2);
		Visit visit = Context.getVisitService().getVisit(1);
		
		EncounterService encounterService = Context.getEncounterService();
		EncounterType encounterType = encounterService.getEncounterType(1);
		Provider provider = Context.getProviderService().getProvider(1);
		context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(AttachmentsConstants.GP_ENCOUNTER_TYPE_UUID, encounterType.getUuid()));
		Encounter encounter = context.getAttachmentEncounter(patient, visit, provider);
		
		for (int i = 0; i < count; i++) {
			String fileCaption = RandomStringUtils.randomAlphabetic(12);
			new Random().nextBytes(randomData);
			MockMultipartFile multipartRandomFile = new MockMultipartFile(String.valueOf(i), String.valueOf(i),
			        "application/octet-stream", randomData);
			obsList.add(obsSaver.saveOtherAttachment(visit, patient, encounter, fileCaption, multipartRandomFile,
			    ValueComplex.INSTRUCTIONS_DEFAULT));
		}
		return obsList;
	}
	
	/**
	 * Boilerplate method to save a collection of complex obs.
	 *
	 * @param count The number of the complex obs to be saved.
	 */
	public List<Obs> saveComplexObsForEncounter(int count) throws IOException {
		init();
		
		// Creating the list of Complex Obs relevnt to the attachment module (
		// Attachments ) using ComplexObsSaver class.
		List<Obs> obsList = new ArrayList<>();
		byte[] randomData = new byte[20];
		
		Patient patient = context.getPatientService().getPatient(2);
		Visit visit = context.getVisitService().getVisit(1);
		
		EncounterService encounterService = context.getEncounterService();
		EncounterType encounterType = encounterService.getEncounterType(1);
		Provider provider = context.getProviderService().getProvider(1);
		context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(AttachmentsConstants.GP_ENCOUNTER_TYPE_UUID, encounterType.getUuid()));
		Encounter encounter = context.getAttachmentEncounter(patient, visit, provider);
		
		for (int i = 0; i < count; i++) {
			String fileCaption = RandomStringUtils.randomAlphabetic(12);
			new Random().nextBytes(randomData);
			MockMultipartFile multipartRandomFile = new MockMultipartFile(String.valueOf(i), String.valueOf(i),
			        "application/octet-stream", randomData);
			obsList.add(obsSaver.saveOtherAttachment(visit, patient, encounter, fileCaption, multipartRandomFile,
			    ValueComplex.INSTRUCTIONS_DEFAULT));
		}
		{
			// Creating the complex Obs not relevent / relevent to the attachment module
			// during the same encounter.
			Obs obs = new Obs();
			// set out-of-Attachment test concept complex
			obs.setConcept(ConceptComplexOutAttach);
			
			// set Attachement concept complex
			// ConceptComplex conceptComplex = context.getConceptService()
			// .getConceptComplex(obsList.get(0).getConcept().getConceptId());
			// obs.setConcept(conceptComplex);
			
			obs.setObsDatetime(new Date());
			obs.setPerson(patient);
			obs.setEncounter(encounter);
			
			new Random().nextBytes(randomData);
			MockMultipartFile multipartRandomFile = new MockMultipartFile("1", "1", "application/octet-stream", randomData);
			obs.setComplexData(
			    complexDataHelper.build(ValueComplex.INSTRUCTIONS_DEFAULT, multipartRandomFile.getOriginalFilename(),
			        multipartRandomFile.getBytes(), multipartRandomFile.getContentType()).asComplexData());
			obs = context.getObsService().saveObs(obs, null);
			
			// Add complex obs to ObsList when Obs is created using attachment concept
			// complex
			// obsList.add(obs);
		}
		{
			// Createing some other obs ( not complex Obs ) during the same encounter
			Obs otherObs = new Obs();
			otherObs.setConcept(context.getConceptService().getConcept(3));
			otherObs.setObsDatetime(new Date());
			otherObs.setEncounter(encounter);
			otherObs.setPerson(patient);
			otherObs.setValueText("Some text value for a test obs.");
			otherObs = context.getObsService().saveObs(otherObs, null);
		}
		// Only return the attachment complex obs related to the attachment module.
		return obsList;
	}
	
	public List<Obs> saveComplexObsForVisit(int count) throws IOException {
		init();
		
		List<Obs> obsList = new ArrayList<>();
		byte[] randomData = new byte[20];
		
		Patient patient = context.getPatientService().getPatient(2);
		Visit visit = context.getVisitService().getVisit(1);
		EncounterService encounterService = context.getEncounterService();
		EncounterType encounterType = encounterService.getEncounterType(1);
		Provider provider = context.getProviderService().getProvider(1);
		
		context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(AttachmentsConstants.GP_ENCOUNTER_TYPE_UUID, encounterType.getUuid()));
		
		Encounter encounter = context.getAttachmentEncounter(patient, visit, provider);
		Encounter encounter2 = context.getAttachmentEncounter(patient, visit, provider);
		
		// saving first obs different from the others
		String fileCaption = RandomStringUtils.randomAlphabetic(12);
		new Random().nextBytes(randomData);
		MockMultipartFile multipartRandomFile = new MockMultipartFile(String.valueOf(0), String.valueOf(0),
		        "application/octet-stream", randomData);
		obsList.add(obsSaver.saveOtherAttachment(visit, patient, encounter, fileCaption, multipartRandomFile,
		    ValueComplex.INSTRUCTIONS_DEFAULT));
		
		for (int i = 1; i < count; i++) {
			fileCaption = RandomStringUtils.randomAlphabetic(12);
			new Random().nextBytes(randomData);
			multipartRandomFile = new MockMultipartFile(String.valueOf(i), String.valueOf(i), "application/octet-stream",
			        randomData);
			obsList.add(obsSaver.saveOtherAttachment(visit, patient, encounter2, fileCaption, multipartRandomFile,
			    ValueComplex.INSTRUCTIONS_DEFAULT));
		}
		{
			// Creating the complex Obs not relevent / relevent to the attachment module
			// during the same visit.
			Obs obs = new Obs();
			// set out-of-Attachment test concept complex
			obs.setConcept(ConceptComplexOutAttach);
			
			// // set Attachement concept complex
			// ConceptComplex conceptComplex = context.getConceptService()
			// .getConceptComplex(obsList.get(0).getConcept().getConceptId());
			// obs.setConcept(conceptComplex);
			
			obs.setObsDatetime(new Date());
			obs.setPerson(patient);
			obs.setEncounter(encounter);
			obs.setComplexData(
			    complexDataHelper.build(ValueComplex.INSTRUCTIONS_DEFAULT, multipartRandomFile.getOriginalFilename(),
			        multipartRandomFile.getBytes(), multipartRandomFile.getContentType()).asComplexData());
			obs = context.getObsService().saveObs(obs, null);
			
			// Add complex obs to ObsList when Obs is created using attachment concept
			// complex
			// obsList.add(obs);
		}
		{
			// Create some other Obs ( not complex Obs ) during the same visit .
			Obs otherObs = new Obs();
			otherObs.setConcept(context.getConceptService().getConcept(3));
			otherObs.setObsDatetime(new Date());
			otherObs.setEncounter(encounter2);
			otherObs.setPerson(patient);
			otherObs.setValueText("Some text value for a test obs.");
			otherObs = context.getObsService().saveObs(otherObs, null);
		}
		
		return obsList;
	}
}
