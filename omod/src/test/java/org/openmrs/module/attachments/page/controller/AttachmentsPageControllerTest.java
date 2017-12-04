package org.openmrs.module.attachments.page.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.attachments.AttachmentsContext;
import org.openmrs.module.attachments.obs.TestHelper;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.domainwrapper.DomainWrapperFactory;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class AttachmentsPageControllerTest extends BaseModuleWebContextSensitiveTest {
	
	@Autowired
	protected TestHelper testHelper;
	
	@Autowired
	private AttachmentsPageController controller;
	
	@Autowired
	private AttachmentsContext context;
	
	@Autowired
	private DomainWrapperFactory domainWrapperFactory;
	
	private UiSessionContext sessionContext = mock(UiSessionContext.class);
	
	@Autowired
	@Qualifier("uiUtils")
	private UiUtils ui;
	
	private Location location;
	
	private PageModel model = new PageModel();
	
	@Before
	public void setup() throws IOException {
		testHelper.init();
		
		LocationTag tag = new LocationTag(EmrApiConstants.LOCATION_TAG_SUPPORTS_VISITS, "");
		context.getLocationService().saveLocationTag(tag);
		location = new Location();
		location.setName("test_location");
		location.addTag(tag);
		context.getLocationService().saveLocation(location);
		when(sessionContext.getSessionLocation()).thenReturn(location);
	}
	
	public static Visit getModelVisit(PageModel model) throws JsonParseException, JsonMappingException, IOException {
		HashMap<String, Object> jsonConfig = (new ObjectMapper()).readValue((String) model.get("jsonConfig"), HashMap.class);
		Object visitObj = jsonConfig.get("visit");
		Visit modelVisit = null;
		if (visitObj != null) {
			modelVisit = (Visit) ConversionUtil.convertMap((LinkedHashMap<String, Object>) jsonConfig.get("visit"),
			    Visit.class);
		}
		return modelVisit;
	}
	
	@Test
	public void shouldPassVisitToModel_WhenClosedVisitProvided()
	        throws JsonParseException, JsonMappingException, IOException {
		
		// Setup
		Patient patient = context.getPatientService().getPatient(2);
		Visit visit = context.getVisitService().getActiveVisitsByPatient(patient).get(0);
		visit.setLocation(location); // associating the first active visit with our location
		visit.setStopDatetime(new DateTime(visit.getStartDatetime()).plusDays(3).toDate());
		context.getVisitService().saveVisit(visit);
		
		// Replay
		controller.controller(patient, visit, sessionContext, ui, context, domainWrapperFactory, model);
		
		// Verif
		Visit modelVisit = getModelVisit(model);
		assertEquals(visit.getId(), modelVisit.getId());
	}
	
	@Test
	public void shouldPassLocationActiveVisitToModel_WhenNoVisitProvided()
	        throws JsonParseException, JsonMappingException, IOException {
		
		// Setup
		Patient patient = context.getPatientService().getPatient(2);
		List<Visit> visits = context.getVisitService().getActiveVisitsByPatient(patient);
		Visit activeVisit = context.getVisitService().getActiveVisitsByPatient(patient).get(0);
		activeVisit.setLocation(location); // associating the first active visit with our location
		context.getVisitService().saveVisit(activeVisit);
		
		// Replay
		controller.controller(patient, null, sessionContext, ui, context, domainWrapperFactory, model);
		
		// Verif
		Visit modelVisit = getModelVisit(model);
		assertEquals(activeVisit.getId(), modelVisit.getId());
	}
	
	@Test
	public void shouldPassNullVisitToModel_WhenNoVisitProvidedAndNoActiveVisitAtLocation()
	        throws JsonParseException, JsonMappingException, IOException {
		
		// Setup
		Patient patient = context.getPatientService().getPatient(2);
		
		// Replay
		controller.controller(patient, null, sessionContext, ui, context, domainWrapperFactory, model);
		
		// Verif
		Visit modelVisit = getModelVisit(model);
		assertNull(modelVisit);
	}
}
