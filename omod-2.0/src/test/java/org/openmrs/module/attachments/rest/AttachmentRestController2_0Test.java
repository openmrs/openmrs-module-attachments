package org.openmrs.module.attachments.rest;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.PropertyUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Assert;
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
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.resource.impl.BasePageableResult;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.openmrs.obs.ComplexObsHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.bind.annotation.RequestMethod;

public class AttachmentRestController2_0Test extends MainResourceControllerTest {
	
	private static final Logger log = LoggerFactory.getLogger(Attachment.class);
	
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
	public void createAttachment_shouldCreateANewAttachment() throws Exception {
		
		SimpleObject attachment = new SimpleObject();
		attachment.add("uuid", obs.getUuid());
		attachment.add("comment", obs.getComment());
		attachment.add("complexData", obs.getComplexData());
		
		String json = "{\"uuid\":\"" + getUuid() + "\",\"attachment\":\"" + attachment + "\"}";
		
		try {
			json = new ObjectMapper().writeValueAsString(attachment);
		}
		catch (IOException e) {
			log.error("attachment created", e);
		}
		
		MockHttpServletRequest req = request(RequestMethod.POST, getURI());
		req.setContent(json.getBytes());
		
		SimpleObject result = null;
		try {
			result = deserialize(handle(req));
		}
		catch (Exception e) {
			log.error("Attachment failed to be created!", e);
		}
		
		Util.log("Created attachment", result);
		
		// Check existence in database
		String uuid = (String) attachment.get("uuid");
		Assert.assertNull(result);
		String createdAttachment = obs.getUuid();
		Assert.assertNotEquals("Created complexData ", createdAttachment.equalsIgnoreCase(createdAttachment));
		
	}
	
	@Test
	public void getAttachment_shouldGetADefaultRepresentationOfAttachment() throws Exception {
		
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		SimpleObject result = deserialize(handle(req));
		
		Assert.assertNotNull(result);
		Util.log("Attachment fetched (default)", result);
		Assert.assertEquals(getUuid(), result.get("uuid"));
	}
	
	@Test
	public void updateAttachment_shouldChangeAPropertyOnAttachment() throws Exception {
		
		SimpleObject attributes = new SimpleObject();
		attributes.add("complexData", "update complextData");
		attributes.add("deathDate", "Updated deathDate");
		attributes.add("prefferedName", "Updated   prefferedName");
		attributes.add("gender", "updated gender");
		attributes.add("preferredAddress", "updated preferredAddress");
		
		String json = "{\"uuid\":\"" + getUuid() + "\",\"attributes\":\"" + attributes + "\"}";
		
		try {
			json = new ObjectMapper().writeValueAsString(attributes);
		}
		catch (IOException e) {
			log.error("Attachments deserialised!", e);
		}
		
		MockHttpServletRequest req = request(RequestMethod.POST, getURI() + "/" + getUuid());
		req.setContent(json.getBytes());
		
		SimpleObject result = null;
		try {
			result = deserialize(handle(req));
		}
		catch (Exception e) {
			log.error("attachment failed to be updated!", e);
		}
		
		Assert.assertNull(result);
		String editedAttachment = obs.getUuid();
		Assert.assertNotEquals("Updated complexData ", editedAttachment.equalsIgnoreCase(editedAttachment));
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
