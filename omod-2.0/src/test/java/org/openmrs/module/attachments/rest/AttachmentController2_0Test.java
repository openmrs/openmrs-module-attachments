package org.openmrs.module.attachments.rest;

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.module.attachments.AttachmentsConstants;
import org.openmrs.module.attachments.AttachmentsContext;
import org.openmrs.module.attachments.obs.TestHelper;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.openmrs.obs.ComplexData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;

import java.io.IOException;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class AttachmentController2_0Test extends MainResourceControllerTest {
	
	@Autowired
	protected ObsService obsService;
	
	@Autowired
	protected TestHelper testHelper;
	
	private AttachmentsContext attachmentsContext;
	
	private Obs obs;
	
	private byte[] randomData = new byte[20];
	
	@Before
	public void setup() throws IOException {
		obs = testHelper.getTestComplexObs();
		new Random().nextBytes(randomData);
		attachmentsContext = Context.getRegisteredComponent(AttachmentsConstants.COMPONENT_ATT_CONTEXT,
		    AttachmentsContext.class);
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
	public void postAttachment_shouldUploadFile() throws Exception {
		
		String fileCaption = "TEXT HERE";
		
		{
			// Setup
			String fileName = "testFile.obj";
			Patient patient = Context.getPatientService().getPatient(2);
			Visit visit = Context.getVisitService().getVisit(1);
			
			MockMultipartHttpServletRequest request = newUploadRequest(getURI());
			MockMultipartFile file = new MockMultipartFile("file", fileName, "application/octet-stream", randomData);
			
			request.addFile(file);
			request.addParameter("patient", patient.getUuid());
			request.addParameter("visit", visit.getUuid());
			request.addParameter("fileCaption", fileCaption);
			
			// Reply
			SimpleObject response = deserialize(handle(request));
			
			Obs obs = Context.getObsService().getObsByUuid((String) response.get("uuid"));
			Obs complexObs = Context.getObsService().getComplexObs(obs.getObsId(), null);
			ComplexData complexData = complexObs.getComplexData();
			
			// Verify
			Assert.assertEquals(obs.getComment(), fileCaption);
			Assert.assertEquals(complexData.getTitle(), fileName);
			Assert.assertArrayEquals(randomData, (byte[]) complexData.getData());
			Assert.assertNotNull(obs.getEncounter());
			Assert.assertEquals(obs.getEncounter().getEncounterType(), attachmentsContext.getEncounterType());
		}
		
		// File upload should not require visit
		{
			// Setup
			String fileName = "noVisitFile.obj";
			Patient patient = Context.getPatientService().getPatient(2);
			
			MockMultipartHttpServletRequest request = newUploadRequest(getURI());
			MockMultipartFile file = new MockMultipartFile("file", fileName, "application/octet-stream", randomData);
			
			request.addFile(file);
			request.addParameter("patient", patient.getUuid());
			request.addParameter("fileCaption", fileCaption);
			
			// Reply
			SimpleObject response = deserialize(handle(request));
			
			Obs obs = Context.getObsService().getObsByUuid((String) response.get("uuid"));
			Obs complexObs = Context.getObsService().getComplexObs(obs.getObsId(), null);
			ComplexData complexData = complexObs.getComplexData();
			
			// Verify
			Assert.assertEquals(obs.getComment(), fileCaption);
			Assert.assertEquals(complexData.getTitle(), fileName);
			Assert.assertArrayEquals(randomData, (byte[]) complexData.getData());
			Assert.assertNull(obs.getEncounter());
		}
	}
}
