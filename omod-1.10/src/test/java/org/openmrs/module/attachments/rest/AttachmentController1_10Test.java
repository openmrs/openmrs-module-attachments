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
import org.openmrs.module.webservices.rest.web.response.IllegalRequestException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.openmrs.obs.ComplexData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import static org.junit.Assert.*;

public class AttachmentController1_10Test extends MainResourceControllerTest {
	
	@Autowired
	protected ObsService obsService;
	
	@Autowired
	protected TestHelper testHelper;
	
	@Autowired
	private AttachmentsContext attachmentsContext;
	
	private byte[] randomData = new byte[20];
	
	private Obs obs;
	
	@Before
	public void setup() throws IOException {
		obs = testHelper.getTestComplexObs();
		new Random().nextBytes(randomData);
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
	public void saveAttachment_shouldUpdateObsComment() throws Exception {
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
	public void deleteAttachment_shouldVoidObs() throws Exception {
		// Setup
		File file = new File(testHelper.getTestComplexObsFilePath());
		
		// Replay
		handle(newDeleteRequest(getURI() + "/" + getUuid()));
		
		// Verif
		assertTrue(obsService.getObsByUuid(getUuid()).isVoided());
	}
	
	@Test
	public void deleteAttachmentWithMissingFile_shouldVoidObs() throws Exception {
		// Setup
		File file = new File(testHelper.getTestComplexObsFilePath());
		file.delete();
		assertFalse(file.exists());
		
		// Replay
		handle(newDeleteRequest(getURI() + "/" + getUuid()));
		
		// Verif
		assertTrue(obsService.getObsByUuid(getUuid()).isVoided());
	}
	
	@Test
	public void purgeAttachment_shouldPurgeObsAndRemoveFile() throws Exception {
		// Setup
		File file = new File(testHelper.getTestComplexObsFilePath());
		assertTrue(file.exists());
		
		// Replay
		handle(newDeleteRequest(getURI() + "/" + getUuid(), new Parameter("purge", "")));
		
		// Verif
		assertNull(obsService.getObsByUuid(getUuid()));
		assertFalse(file.exists());
	}
	
	@Test
	public void purgeAttachmentWithMissingFile_shouldPurgeObs() throws Exception {
		// Setup
		File file = new File(testHelper.getTestComplexObsFilePath());
		file.delete();
		assertFalse(file.exists());
		
		// Replay
		handle(newDeleteRequest(getURI() + "/" + getUuid(), new Parameter("purge", "")));
		
		// Verif
		assertNull(obsService.getObsByUuid(getUuid()));
	}
	
	@Test
	public void deleteAttachment_shouldVoidObsWithoutAssociatedEncounter() throws Exception {
		// Setup
		Obs obs = testHelper.getTestComplexObsWithoutAssociatedEncounterOrVisit();
		
		// Replay
		handle(newDeleteRequest(getURI() + "/" + obs.getUuid()));
		
		// Verify
		assertTrue(obsService.getObsByUuid(obs.getUuid()).isVoided());
	}
	
	@Test
	public void purgeAttachment_shouldPurgeObsWithoutAssociatedEncounter() throws Exception {
		// Setup
		Obs obs = testHelper.getTestComplexObsWithoutAssociatedEncounterOrVisit();
		
		// Replay
		handle(newDeleteRequest(getURI() + "/" + obs.getUuid(), new Parameter("purge", "")));
		
		// Verify
		assertNull(obsService.getObsByUuid(obs.getUuid()));
	}
	
	@Test
	public void postAttachment_shouldUploadFile() throws Exception {
		
		String fileCaption = "Test file caption";
		
		{
			// Setup
			String fileName = "testFile1.dat";
			Patient patient = Context.getPatientService().getPatient(2);
			Visit visit = Context.getVisitService().getVisit(1);
			
			MockMultipartHttpServletRequest request = newUploadRequest(getURI());
			MockMultipartFile file = new MockMultipartFile("file", fileName, "application/octet-stream", randomData);
			
			request.addFile(file);
			request.addParameter("patient", patient.getUuid());
			request.addParameter("visit", visit.getUuid());
			request.addParameter("fileCaption", fileCaption);
			
			// Replay
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
			String fileName = "testFile2.dat";
			Patient patient = Context.getPatientService().getPatient(2);
			
			MockMultipartHttpServletRequest request = newUploadRequest(getURI());
			MockMultipartFile file = new MockMultipartFile("file", fileName, "application/octet-stream", randomData);
			
			request.addFile(file);
			request.addParameter("patient", patient.getUuid());
			request.addParameter("fileCaption", fileCaption);
			
			// Replay
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
	
	@Test(expected = IllegalRequestException.class)
	public void postAttachment_shouldNotUploadFileAboveSizeLimit() throws Exception {
		// Setup
		String fileCaption = "Test file caption";
		String fileName = "testFile1.dat";
		Patient patient = Context.getPatientService().getPatient(2);
		Visit visit = Context.getVisitService().getVisit(1);
		
		byte[] maxData = new byte[(int) ((attachmentsContext.getMaxUploadFileSize() * 1024 * 1024) + 1)];
		
		MockMultipartHttpServletRequest request = newUploadRequest(getURI());
		MockMultipartFile file = new MockMultipartFile("file", fileName, "application/octet-stream", maxData);
		
		request.addFile(file);
		request.addParameter("patient", patient.getUuid());
		request.addParameter("visit", visit.getUuid());
		request.addParameter("fileCaption", fileCaption);
		
		// Replay
		SimpleObject response = deserialize(handle(request));
	}
	
	@Test
	public void getAttachmentBytes_shouldDownloadFile() throws Exception {
		
		// Setup
		String fileCaption = "Test file caption";
		String uuid;
		String mimeType = "application/octet-stream";
		String fileExtension = "dat";
		String fileName = "testFile." + fileExtension;
		
		// Upload Test File
		Patient patient = Context.getPatientService().getPatient(2);
		MockMultipartHttpServletRequest uploadRequest = newUploadRequest(getURI());
		MockMultipartFile file = new MockMultipartFile("file", fileName, mimeType, randomData);
		
		uploadRequest.addFile(file);
		uploadRequest.addParameter("patient", patient.getUuid());
		uploadRequest.addParameter("fileCaption", fileCaption);
		
		SimpleObject uploadResponse = deserialize(handle(uploadRequest));
		uuid = uploadResponse.get("uuid");
		
		HttpServletRequest downloadRequest = newGetRequest(getURI() + "/" + uuid + "/bytes");
		
		// Replay
		MockHttpServletResponse downloadResponse = handle(downloadRequest);
		byte[] bytesContent = downloadResponse.getContentAsByteArray();
		
		// Verify
		Assert.assertArrayEquals(randomData, bytesContent);
		Assert.assertEquals(downloadResponse.getContentType(), mimeType);
		Assert.assertEquals(downloadResponse.getHeader("File-Name"), fileName);
		Assert.assertEquals(downloadResponse.getHeader("File-Ext"), fileExtension);
		
	}
}
