package org.openmrs.module.attachments.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.context.Context;
import org.openmrs.module.attachments.AttachmentsService;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class AttachmentResourceTest {
	
	@Before
	public void setup() {
		initMocks(this);
		PowerMockito.mockStatic(Context.class);
	}
	
	@Test
	public void searchAttachments_shouldReturnEncounterAttachments() {
		
		AttachmentResource1_10 res = new AttachmentResource1_10();
		AttachmentsService attachmentsService = mock(AttachmentsService.class);
		Patient patient = new Patient();
		Encounter encounter = new Encounter();
		res.search(attachmentsService, patient, null, encounter, null, true);
		verify(attachmentsService, times(1)).getAttachments(patient, encounter, true);
		
	}
	
	@Test
	public void searchAttachments_shouldReturnVisitAttachments() {
		
		AttachmentResource1_10 res = new AttachmentResource1_10();
		AttachmentsService attachmentsService = mock(AttachmentsService.class);
		Patient patient = new Patient();
		Visit visit = new Visit();
		res.search(attachmentsService, patient, visit, null, null, true);
		verify(attachmentsService, times(1)).getAttachments(patient, visit, true);
		
	}
	
	@Test
	public void searchAttachments_shouldReturnAllAttachments() {
		
		AttachmentResource1_10 res = new AttachmentResource1_10();
		AttachmentsService attachmentsService = mock(AttachmentsService.class);
		Patient patient = new Patient();
		res.search(attachmentsService, patient, null, null, null, true);
		verify(attachmentsService, times(1)).getAttachments(patient, true);
		
	}
	
	@Test
	public void searchAttachments_shouldReturnEncounterlessAttachments() {
		
		AttachmentResource1_10 res = new AttachmentResource1_10();
		AttachmentsService attachmentsService = mock(AttachmentsService.class);
		Patient patient = new Patient();
		res.search(attachmentsService, patient, null, null, "only", true);
		verify(attachmentsService, times(1)).getEncounterlessAttachments(patient, true);
		
	}
	
	@Test
	public void searchAttachments_shouldNotReturnEncounterlessAttachments() {
		
		AttachmentResource1_10 res = new AttachmentResource1_10();
		AttachmentsService attachmentsService = mock(AttachmentsService.class);
		Patient patient = new Patient();
		res.search(attachmentsService, patient, null, null, "false", true);
		verify(attachmentsService, times(1)).getAttachments(patient, false, true);
	}
}
