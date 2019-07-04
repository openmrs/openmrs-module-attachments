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
import org.openmrs.api.context.Context;
import org.openmrs.module.attachments.AttachmentsConstants;
import org.openmrs.module.attachments.AttachmentsService;
import org.openmrs.module.attachments.obs.Attachment;
import org.openmrs.module.attachments.obs.TestHelper;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.BasePageableResult;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;

public class AttachmentController2_0Test extends MainResourceControllerTest {
	
	@Autowired
	protected ObsService obsService;
	
	@Autowired
	protected TestHelper testHelper;
	
	private Obs obs;
	
	@Before
	public void setup() throws IOException {
		testHelper.init();
		obs = testHelper.getTestComplexObs();
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
		Object doc = deserialize(handle(newPostRequest(getURI() + "/" + getUuid(), json)));
		
		// Verif
		String comment = (String) PropertyUtils.getProperty(doc, "comment");
		assertEquals(editedComment, comment);
	}
	
	@Test
	public void doSearch_shouldReturnResult() {
		
		// Setup
		AttachmentResource1_10 res = new AttachmentResource1_10();
		Patient patient = Context.getPatientService().getPatient(2);
		
		AttachmentsService as = Context.getService(AttachmentsService.class);
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
}
