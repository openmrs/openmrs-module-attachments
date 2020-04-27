package org.openmrs.module.attachments.rest;

import java.io.IOException;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.attachments.AttachmentsConstants;
import org.openmrs.module.attachments.obs.TestHelper;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;

public class AttachmentBytesResource1_10Test extends MainResourceControllerTest {
	
	@Autowired
	protected TestHelper testHelper;
	
	private byte[] randomData = new byte[20];
	
	private Obs obs;
	
	@Override
	public String getURI() {
		return AttachmentsConstants.ATTACHMENT_URI;
	}
	
	@Override
	public long getAllCount() {
		return 0;
	}
	
	@Override
	public String getUuid() {
		return obs.getUuid();
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
	@Before
	public void setup() throws IOException {
		testHelper.init();
		obs = testHelper.getTestComplexObs();
		new Random().nextBytes(randomData);
	}
	
	@After
	public void tearDown() throws IOException {
		testHelper.tearDown();
	}
	
	@Test
	public void getAttachmentBytes_shouldGetOriginalFileWhenViewPassedEqualToOriginal() throws Exception {
		
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
        
		HttpServletRequest downloadRequest = newGetRequest(getURI() + "/" + uuid + "/bytes",new Parameter("view", "complexdata.view.original"));
		
		// Replay
		MockHttpServletResponse downloadResponse = handle(downloadRequest);
		byte[] bytesContent = downloadResponse.getContentAsByteArray();
		
		// Verify
		Assert.assertArrayEquals(randomData, bytesContent);
		Assert.assertEquals(downloadResponse.getContentType(), mimeType);
		Assert.assertEquals(downloadResponse.getHeader("File-Name"), fileName);
		Assert.assertEquals(downloadResponse.getHeader("File-Ext"), fileExtension);
		
	}
	
	  @Test 
	  public void getAttachmentBytes_shouldGetFileWhenViewIsNotPassed() throws Exception {
	 
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

		@Test
		public void getAttachmentBytes_shouldGetOriginalFileWhenViewIsPassedEqualToUnknown() throws Exception {
			
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
			
			HttpServletRequest downloadRequest = newGetRequest(getURI() + "/" + uuid + "/bytes",new Parameter("view", "unknown"));
			
			// Replay
			MockHttpServletResponse downloadResponse = handle(downloadRequest);
			byte[] bytesContent = downloadResponse.getContentAsByteArray();
			
			// Verify
			Assert.assertArrayEquals(randomData, bytesContent);
			Assert.assertEquals(downloadResponse.getContentType(), mimeType);
			Assert.assertEquals(downloadResponse.getHeader("File-Name"), fileName);
			Assert.assertEquals(downloadResponse.getHeader("File-Ext"), fileExtension);
			
		}
		@Test
		public void getAttachmentBytes_shouldGetThumbNailFileWhenViewIsPassedEqualToThumbnail() throws Exception {
			
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
			
			HttpServletRequest downloadRequest = newGetRequest(getURI() + "/" + uuid + "/bytes",new Parameter("view", "complexdata.view.thumbnail"));
			
			// Replay
			MockHttpServletResponse downloadResponse = handle(downloadRequest);
			byte[] bytesContent = downloadResponse.getContentAsByteArray();
			
			// Verify
			Assert.assertArrayEquals(new byte[0], bytesContent);
			Assert.assertEquals(downloadResponse.getContentType(), mimeType);
			Assert.assertEquals(downloadResponse.getHeader("File-Name"), fileName);
			Assert.assertEquals(downloadResponse.getHeader("File-Ext"), fileExtension);
			
		}
}
