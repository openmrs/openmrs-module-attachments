package org.openmrs.module.attachments.rest;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
import org.openmrs.module.attachments.AttachmentsConstants;
import org.openmrs.module.attachments.AttachmentsService;
import org.openmrs.module.attachments.obs.Attachment;
import org.openmrs.module.attachments.obs.TestAttachmentBytesViewHandler;
import org.openmrs.module.attachments.obs.TestHelper;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.resource.impl.BasePageableResult;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.openmrs.obs.ComplexObsHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class AttachmentRestController2_0Test extends MainResourceControllerTest {
	
	@Autowired
	private AttachmentsService as;
	
	@Autowired
	private ObsService obsService;
	
	@Autowired
	private PatientService patientService;
	
	@Autowired
	private TestHelper testHelper;
	
	private Obs obs;
	
	@Before
	public void setup() throws Exception {
		executeDataSet("testdata/test-dataset.xml");
		testHelper.init();
		obs = testHelper.getTestComplexObs();
		
		obsService.registerHandler(TestAttachmentBytesViewHandler.class.getSimpleName(),
		    (ComplexObsHandler) new TestAttachmentBytesViewHandler());
	}
	
	@After
	public void tearDown() throws IOException {
		testHelper.tearDown();
	}
	
	@Override
	public String getURI() {
		return AttachmentsConstants.ATTACHMENT_URI;
	}
	
	@Override
	public String getUuid() {
		return obs.getUuid();
	}
	
	@Override
	public long getAllCount() {
		return 0;
	}
	
	@Override
	@Test
	public void shouldGetAll() {
	}
	
	@Override
	@Test
	public void shouldGetDefaultByUuid() {
	}
	
	@Override
	@Test
	public void shouldGetRefByUuid() {
	}
	
	@Override
	@Test
	public void shouldGetFullByUuid() {
	}
	
	@Test
	public void postAttachment_shouldUpdateObsComment() throws Exception {
		// Setup
		String editedComment = "Hello world!";
		String json = "{\"uuid\":\"" + getUuid() + "\",\"comment\":\"" + editedComment + "\"}";
		
		// Replay
		Object att = deserialize(handle(newPostRequest(getURI() + "/" + getUuid(), json)));
		
		// Verif
		String comment = (String) PropertyUtils.getProperty(att, "comment");
		assertEquals(editedComment, comment);
	}
	
	@Test
	public void doSearch_shouldReturnResults() {
		
		// Setup
		AttachmentResource2_0 res = new AttachmentResource2_0();
		Patient patient = patientService.getPatient(2);
		
		List<Attachment> attachments = as.getAttachments(patient, false);
		
		MockHttpServletRequest request = newGetRequest(getURI() + "/");
		request.addParameter("patient", patient.getUuid());
		request.addParameter("v", "full");
		
		RequestContext requestContext = new RequestContext();
		requestContext.setRequest(request);
		
		// Replay
		BasePageableResult response = (BasePageableResult) res.doSearch(requestContext);
		List<Attachment> actualAttachments = response.getPageOfResults();
		
		// Verify
		assertArrayEquals(attachments.stream().map(Attachment::getUuid).collect(Collectors.toList()).toArray(),
		    actualAttachments.stream().map(Attachment::getUuid).collect(Collectors.toList()).toArray());
		
	}
	
	@Test
	public void getAttachmentBytes_shouldReturnBytesForView() throws Exception {
		// setup
		Patient patient = patientService.getPatient(2);
		String attUuid = "bbec1b32-dbf1-4b7a-bd2b-8898543c1367";
		
		// Requesting the bytes for a specific view
		{
			// replay
			String view = TestAttachmentBytesViewHandler.DEFAULT_VIEW;
			MockHttpServletRequest request = newGetRequest(getURI() + "/" + attUuid + "/bytes", new Parameter("view", view));
			request.addParameter("patient", patient.getUuid());
			request.addParameter("v", "full");
			
			MockHttpServletResponse response = handle(request);
			
			// verif
			assertEquals(TestAttachmentBytesViewHandler.DEFAULT_VIEW_DATA, response.getContentAsString());
		}
		
		// Requesting the bytes for a specific view
		{
			// replay
			String view = TestAttachmentBytesViewHandler.ALTERNATE_VIEW;
			MockHttpServletRequest request = newGetRequest(getURI() + "/" + attUuid + "/bytes", new Parameter("view", view));
			request.addParameter("patient", patient.getUuid());
			request.addParameter("v", "full");
			
			MockHttpServletResponse response = handle(request);
			
			// verif
			assertEquals(TestAttachmentBytesViewHandler.ALTERNATE_VIEW_DATA, response.getContentAsString());
		}
		
		// Requesting the bytes with no view
		{
			// replay
			MockHttpServletRequest request = newGetRequest(getURI() + "/" + attUuid + "/bytes");
			request.addParameter("patient", patient.getUuid());
			request.addParameter("v", "full");
			
			MockHttpServletResponse response = handle(request);
			
			// verif
			assertEquals(TestAttachmentBytesViewHandler.DEFAULT_VIEW_DATA, response.getContentAsString());
		}
		
		// Requesting the bytes for an unknown view
		{
			// replay
			String view = "foobar_unkown_view";
			MockHttpServletRequest request = newGetRequest(getURI() + "/" + attUuid + "/bytes", new Parameter("view", view));
			request.addParameter("patient", patient.getUuid());
			request.addParameter("v", "full");
			
			MockHttpServletResponse response = handle(request);
			
			// verif
			assertEquals(TestAttachmentBytesViewHandler.DEFAULT_VIEW_DATA, response.getContentAsString());
		}
		
	}
}
