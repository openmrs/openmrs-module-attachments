package org.openmrs.module.attachments.rest;


import org.junit.Before;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.module.attachments.AttachmentsConstants;
import org.openmrs.module.attachments.AttachmentsService;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class AttachmentResourceTest {

	
	public String getURI() {
		return AttachmentsConstants.ATTACHMENT_URI;
	}
	
	@Before
	public void setup() {
		initMocks(this);
	}
	
	@Test
	public void searchAttachments_shouldGetAttachmentsByPatient() throws Exception {
		
		 MainResourceControllerTest mainResourceControllerTest =
		 mock(MainResourceControllerTest.class);
		 AttachmentsService attachmentsService = mock(AttachmentsService.class);
		 Patient patient = new Patient();
		 patient.setUuid("test");

		 MockHttpServletRequest request1 =
		 mainResourceControllerTest.newGetRequest(getURI());
		 request1.setParameter("patient", patient.getUuid());
		 request1.setParameter("includeVoided", "true");
		 request1.setParameter("includeEncounterless", "false");
		 SimpleObject result =
		 mainResourceControllerTest.deserialize(mainResourceControllerTest.handle(request1));
		 verify(attachmentsService, times(1)).getAttachments(patient, false, true);
		
	}
}
