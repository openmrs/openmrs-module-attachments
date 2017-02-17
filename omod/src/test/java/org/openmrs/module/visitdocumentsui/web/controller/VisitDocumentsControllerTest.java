package org.openmrs.module.visitdocumentsui.web.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.module.visitdocumentsui.VisitDocumentsContext;
import org.openmrs.module.visitdocumentsui.obs.TestHelper;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartHttpServletRequest;

public class VisitDocumentsControllerTest extends BaseModuleWebContextSensitiveTest {

	@Autowired
	private VisitDocumentsController controller;

	@Autowired
	private VisitDocumentsContext context;

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
		when(request.getFile(eq(helper.getTestFileNameWithExt()))).thenReturn(helper.getTestMultipartFile());
	}

	@Test
	public void shouldSaveFile_WhenVisitIsActive() throws JsonParseException, JsonMappingException, IOException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		// Setup
		Visit visit = context.getVisitService().getActiveVisitsByPatient(patient).get(0);

		// Replay
		Object obsRep = controller.uploadDocuments(patient, visit, provider, fileCaption, instructions, request);

		// Verif
		String uuid = (String) PropertyUtils.getProperty(obsRep, "uuid");
		Obs obs = context.getObsService().getObsByUuid(uuid);
		assertEquals(visit.getId(), obs.getEncounter().getVisit().getId());
	}
	
	@Test
	public void shouldSaveFile_WhenVisitIsClosed() throws JsonParseException, JsonMappingException, IOException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
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
	}
}