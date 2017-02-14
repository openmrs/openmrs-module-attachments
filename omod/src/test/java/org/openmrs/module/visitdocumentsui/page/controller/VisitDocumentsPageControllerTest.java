package org.openmrs.module.visitdocumentsui.page.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.visitdocumentsui.VisitDocumentsConstants;
import org.openmrs.module.visitdocumentsui.VisitDocumentsContext;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.ui.framework.BasicUiUtils;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

public class VisitDocumentsPageControllerTest extends BaseModuleWebContextSensitiveTest {
	
	@Autowired
	private VisitDocumentsPageController controller;
	
	@Autowired
	private VisitDocumentsContext context;
	
	private UiSessionContext sessionContext = mock(UiSessionContext.class);
	
	private UiUtils ui = new BasicUiUtils();
	
	private PageModel model = new PageModel();
	
	@Before
	public void setup() {
		when(sessionContext.getSessionLocation()).thenReturn(new Location());
		
		context.getAdministrationService().saveGlobalProperty( new GlobalProperty(VisitDocumentsConstants.GP_CONCEPT_COMPLEX_UUID_LIST, "[\"7cac8397-53cd-4f00-a6fe-028e8d743f8e\",\"42ed45fd-f3f6-44b6-bfc2-8bde1bb41e00\"]" ) );
		context.getAdministrationService().saveGlobalProperty( new GlobalProperty(VisitDocumentsConstants.GP_MAX_STORAGE_FILE_SIZE, "1.2" ) );
		context.getAdministrationService().saveGlobalProperty( new GlobalProperty(VisitDocumentsConstants.GP_MAX_UPLOAD_FILE_SIZE, "5.0" ) );
		context.getAdministrationService().saveGlobalProperty( new GlobalProperty(VisitDocumentsConstants.GP_ALLOW_NO_CAPTION, "false" ) );
		context.getAdministrationService().saveGlobalProperty( new GlobalProperty(VisitDocumentsConstants.GP_WEBCAM_ALLOWED, "true" ) );
		context.getAdministrationService().saveGlobalProperty( new GlobalProperty(RestConstants.MAX_RESULTS_DEFAULT_GLOBAL_PROPERTY_NAME, "50" ) );
	}
	
	@Test
	public void shouldSendParamVisitToModel() throws JsonParseException, JsonMappingException, IOException {
		
		Patient patient = Context.getPatientService().getPatient(2);
		Visit visit = Context.getVisitService().getVisit(1);
		controller.controller(patient, visit, sessionContext, ui, context, model);
		
		HashMap<String,Object> jsonConfig = (new ObjectMapper()).readValue((String) model.get("jsonConfig"), HashMap.class);
		Visit modelVisit = (Visit) ConversionUtil.convertMap( (LinkedHashMap<String,Object>) jsonConfig.get("visit") , Visit.class);
		
		assertEquals(visit.getId(), modelVisit.getId());
	}
}
