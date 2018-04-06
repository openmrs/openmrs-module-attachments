package org.openmrs.module.attachments.rest;

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Obs;
import org.openmrs.api.ObsService;
import org.openmrs.module.attachments.AttachmentsConstants;
import org.openmrs.module.attachments.obs.TestHelper;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class AttachmentController2_0Test extends MainResourceControllerTest {
	
	@Autowired
	protected ObsService obsService;
	
	@Autowired
	protected TestHelper testHelper;
	
	private Obs obs;
	
	@Before
	public void setup() throws IOException {
		obs = testHelper.getTestComplexObs();
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
}
