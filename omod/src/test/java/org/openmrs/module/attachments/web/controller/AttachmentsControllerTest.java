package org.openmrs.module.attachments.web.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.GlobalProperty;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.module.attachments.AttachmentsConstants;
import org.openmrs.module.attachments.AttachmentsContext;
import org.openmrs.module.attachments.obs.TestHelper;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartHttpServletRequest;

public class AttachmentsControllerTest extends BaseModuleWebContextSensitiveTest {
	
	@Autowired
	private AttachmentsController controller;
	
	@Autowired
	private AttachmentsContext context;
	
	@Autowired
	protected TestHelper helper;
	
	protected Patient patient;
	
	protected Provider provider;
	
	protected String fileCaption = "test file caption";
	
	protected String instructions = null;
	
	protected MultipartHttpServletRequest request = mock(MultipartHttpServletRequest.class);
	
	@Before
	public void setup() throws IOException {
		helper.init();
		
		patient = context.getPatientService().getPatient(2);
		provider = context.getProviderService().getProvider(1);
		
		List<String> fileNames = Arrays.asList(helper.getTestFileNameWithExt());
		when(request.getFileNames()).thenReturn(fileNames.iterator());
		when(request.getFile(eq(helper.getTestFileNameWithExt()))).thenReturn(helper.getTestDefaultFile());
	}
	
	@Test
	public void shouldSaveFile_WhenVisitIsActive() throws JsonParseException, JsonMappingException, IOException,
	        IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		// Setup
		Visit visit = context.getVisitService().getActiveVisitsByPatient(patient).get(0);
		assertNull(visit.getStopDatetime());
		
		// Replay
		Object obsRep = controller.uploadDocuments(patient, visit, provider, fileCaption, instructions, request);
		
		// Verif
		String uuid = (String) PropertyUtils.getProperty(obsRep, "uuid");
		Obs obs = context.getObsService().getObsByUuid(uuid);
		assertEquals(visit.getId(), obs.getEncounter().getVisit().getId());
		assertTrue(obs.getObsDatetime().after(obs.getEncounter().getEncounterDatetime()));
	}
	
	@Test
	public void shouldSaveFile_WhenVisitIsClosed() throws JsonParseException, JsonMappingException, IOException,
	        IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		// Setup
		Visit visit = context.getVisitService().getActiveVisitsByPatient(patient).get(0);
		visit.setStopDatetime(new DateTime(visit.getStartDatetime()).plusDays(3).toDate());
		context.getVisitService().saveVisit(visit);
		
		// Replay
		Object obsRep = controller.uploadDocuments(patient, visit, provider, fileCaption, instructions, request);
		
		// Verif
		String uuid = (String) PropertyUtils.getProperty(obsRep, "uuid");
		Obs obs = context.getObsService().getObsByUuid(uuid);
		assertEquals(visit.getId(), obs.getEncounter().getVisit().getId());
		assertEquals(visit.getStopDatetime(), obs.getEncounter().getEncounterDatetime());
		assertEquals(visit.getStopDatetime(), obs.getObsDatetime());
	}
	
	@Test
	public void shouldSaveFile_WhenVisitIsClosedAndUniqueEncounterWorkflow() throws JsonParseException, JsonMappingException,
	        IOException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		// Setup
		Visit visit = context.getVisitService().getActiveVisitsByPatient(patient).get(0);
		visit.setStopDatetime(new DateTime(visit.getStartDatetime()).plusDays(3).toDate());
		context.getVisitService().saveVisit(visit);
		Date encounterDateTime = new DateTime(visit.getStartDatetime()).plusMinutes(120).toDate();
		
		{ // a pre-existing encounter that started shortly after the visit started
			Encounter encounter = new Encounter();
			encounter.setEncounterType(context.getEncounterType());
			encounter.setPatient(visit.getPatient());
			encounter.setLocation(visit.getLocation());
			encounter.setProvider(context.getEncounterRole(), provider);
			encounter.setEncounterDatetime(encounterDateTime);
			visit.addEncounter(encounter);
		}
		
		context.getAdministrationService() // setting the unique encounter workflow
		        .saveGlobalProperty(new GlobalProperty(AttachmentsConstants.GP_ENCOUNTER_SAVING_FLOW, "unique"));
		
		// Replay
		Object obsRep = controller.uploadDocuments(patient, visit, provider, fileCaption, instructions, request);
		
		// Verif
		String uuid = (String) PropertyUtils.getProperty(obsRep, "uuid");
		Obs obs = context.getObsService().getObsByUuid(uuid);
		assertEquals(visit.getId(), obs.getEncounter().getVisit().getId());
		assertEquals(encounterDateTime, obs.getEncounter().getEncounterDatetime());
		assertEquals(visit.getStopDatetime(), obs.getObsDatetime());
	}
}
