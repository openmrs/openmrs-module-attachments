package org.openmrs.module.attachments.obs;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.openmrs.Concept;
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
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;

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

	protected String fileName = "mock_file_name";

	protected String fileExt = "ext";

	protected MockMultipartFile multipartDefaultFile = new MockMultipartFile(fileName, fileName + "." + fileExt,
			"application/octet-stream", "mock_content".getBytes());

	protected MockMultipartFile lastSavedMultipartImageFile;

	protected final static String OTHER_CONCEPT_COMPLEX_NAME = "OutOfAttachmentsTestComplex";

	protected Concept otherConceptComplex;

	/**
	 * @return The last saved test image file, null if none was ever saved.
	 */

	public MockMultipartFile getTestDefaultFile() {
		return multipartDefaultFile;
	}

	public String getTestFileName() {
		return fileName;
	}

	/**
	 * This initialization routine configure a lot of boilerplate settings to mimic
	 * the actual environment. This method should be invoked when setting up unit
	 * tests.
	 * 
	 * @throws IOException
	 */
	public void init() throws IOException {
		activator.started(); // so our concepts complex are created

		String conceptComplexUuidMap = "{\"IMAGE\":\"" + AttachmentsConstants.CONCEPT_IMAGE_UUID + "\",\"OTHER\":\""
				+ AttachmentsConstants.CONCEPT_DEFAULT_UUID + "\"}";
		context.getAdministrationService().saveGlobalProperty(
				new GlobalProperty(AttachmentsConstants.GP_CONCEPT_COMPLEX_UUID_MAP, conceptComplexUuidMap));

		context.getAdministrationService().saveGlobalProperty(
				new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_COMPLEX_OBS_DIR, "complex_obs"));

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

		context.getAdministrationService().saveGlobalProperty(new GlobalProperty(
				AttachmentsConstants.GP_ENCOUNTER_TYPE_UUID, AttachmentsConstants.ENCOUNTER_TYPE_UUID));

		// Create a concept complex that is not managed by Attachments
		if (context.getConceptService().getConceptByName(OTHER_CONCEPT_COMPLEX_NAME) == null) {
			ConceptComplex conceptComplex = new ConceptComplex();
			conceptComplex.setHandler(BinaryDataHandler.class.getSimpleName());
			ConceptName conceptName = new ConceptName(OTHER_CONCEPT_COMPLEX_NAME, Locale.ENGLISH);
			conceptComplex.setFullySpecifiedName(conceptName);
			conceptComplex.setPreferredName(conceptName);
			conceptComplex.setConceptClass(context.getConceptService().getConceptClassByName("Question"));
			conceptComplex
					.setDatatype(context.getConceptService().getConceptDatatypeByUuid(ConceptDatatype.COMPLEX_UUID));
			conceptComplex
					.addDescription(new ConceptDescription("Out-of-Attachments test concept complex", Locale.ENGLISH));
			context.getConceptService().saveConcept(conceptComplex);
			otherConceptComplex = conceptComplex;
		}
	}

	/**
	 * This method should be invoked when tearing down unit tests.
	 */
	public void tearDown() throws IOException {
		context.getConceptService()
				.purgeConcept(context.getConceptService().getConceptByName(OTHER_CONCEPT_COMPLEX_NAME));
		OpenmrsUtil.deleteDirectory(OpenmrsUtil.getDirectoryInApplicationDataDirectory("storage"));
	}

	public String getTestStorageDirPath() {
		return OpenmrsUtil.getDirectoryInApplicationDataDirectory("storage").getAbsolutePath();
	}

	public String getFilePathFromObs(Obs obs) {
		// storage directory + everything after the last | in the value complex
		return getTestStorageDirPath() + File.separator
				+ obs.getValueComplex().split("\\|")[obs.getValueComplex().split("\\|").length - 1];
	}

	/**
	 * Boilerplate method to get an test encounter
	 *
	 * @return Encounter object.
	 */
	public Encounter getTestEncounter() throws IOException {
		Patient patient = Context.getPatientService().getPatient(2);
		Visit visit = Context.getVisitService().getVisit(1);
		EncounterService encounterService = Context.getEncounterService();
		EncounterType encounterType = encounterService.getEncounterType(1);
		Provider provider = Context.getProviderService().getProvider(1);

		context.getAdministrationService().saveGlobalProperty(
				new GlobalProperty(AttachmentsConstants.GP_ENCOUNTER_TYPE_UUID, encounterType.getUuid()));
		Encounter encounter = context.getAttachmentEncounter(patient, visit, provider);
		return encounter;
	}

	public Obs getTestComplexObs() throws IOException {
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
				ValueComplex.INSTRUCTIONS_DEFAULT, null, null);
	}

	/**
	 * Boilerplate method to save an image attachment.
	 * 
	 * @param imagePath
	 *            The path of the image resource.
	 */
	public Obs saveImageAttachment(String imagePath, String mimeType) throws IOException {
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
				ValueComplex.INSTRUCTIONS_DEFAULT, null, null);
	}

	/**
	 * Boilerplate method to save a 'normal sized' image attachment. This method
	 * doesn't ensure that the size is normal, the method just uses an image file
	 * that is assumed to fit.
	 */
	public Obs saveNormalSizeImageAttachment() throws IOException {
		return saveImageAttachment(ATTACHMENTS_FOLDER + "/" + "OpenMRS_banner.jpg", "image/jpeg");
	}

	/**
	 * Boilerplate method to save an 'small sized' image attachment. Small sized
	 * mean already small enough to be its own thumbnail. This method doesn't ensure
	 * that the size is small, the method just uses an image file that is assumed to
	 * fit.
	 */
	public Obs saveSmallSizeImageAttachment() throws IOException {
		return saveImageAttachment(ATTACHMENTS_FOLDER + "/" + "OpenMRS_icon_100x100.png", "image/png");
	}

	public Obs getTestComplexObsWithoutAssociatedEncounterOrVisit() throws Exception {
		Patient patient = Context.getPatientService().getPatient(2);

		String fileCaption = RandomStringUtils.randomAlphabetic(12);
		return obsSaver.saveOtherAttachment(null, patient, null, fileCaption, getTestDefaultFile(),
				ValueComplex.INSTRUCTIONS_DEFAULT, null, null);
	}

	public String decode(String key) {
		try {
			return URLDecoder.decode(key, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public String encode(String key) {
		try {
			return URLEncoder.encode(key, "UTF-8").replace(".", "%2E").replace("*", "%2A").replace("%2F", "/");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
}
