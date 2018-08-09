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

import static org.mockito.Mockito.*;
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
	public void search_shouldInvokeApiForEncounterAttachments() {
		// Setup
		AttachmentResource1_10 res = new AttachmentResource1_10();
		AttachmentsService attachmentsService = mock(AttachmentsService.class);
		Patient patient = new Patient();
		Encounter encounter = new Encounter();
		
		// Replay
		res.search(attachmentsService, patient, null, encounter, null, true);
		
		// Verify
		verify(attachmentsService, times(1)).getAttachments(patient, encounter, true);
		verifyNoMoreInteractions(attachmentsService);
	}
	
	@Test
	public void search_shouldInvokeApiForVisitAttachments() {
		// Setup
		AttachmentResource1_10 res = new AttachmentResource1_10();
		AttachmentsService attachmentsService = mock(AttachmentsService.class);
		Patient patient = new Patient();
		Visit visit = new Visit();
		
		// Replay
		res.search(attachmentsService, patient, visit, null, null, true);
		
		// Verify
		verify(attachmentsService, times(1)).getAttachments(patient, visit, true);
		verifyNoMoreInteractions(attachmentsService);
	}
	
	@Test
	public void search_shouldInvokeApiForAllAttachments() {
		// Setup
		AttachmentResource1_10 res = new AttachmentResource1_10();
		AttachmentsService attachmentsService = mock(AttachmentsService.class);
		Patient patient = new Patient();
		
		// Replay
		res.search(attachmentsService, patient, null, null, null, true);
		
		// Verify
		verify(attachmentsService, times(1)).getAttachments(patient, true);
		verifyNoMoreInteractions(attachmentsService);
	}
	
	@Test
	public void search_shouldInvokeApiForEncounterlessAttachments() {
		// Setup
		AttachmentResource1_10 res = new AttachmentResource1_10();
		AttachmentsService attachmentsService = mock(AttachmentsService.class);
		Patient patient = new Patient();
		
		// Replay
		res.search(attachmentsService, patient, null, null, "only", true);
		
		// Verify
		verify(attachmentsService, times(1)).getEncounterlessAttachments(patient, true);
		verifyNoMoreInteractions(attachmentsService);
	}
	
	@Test
	public void search_shouldInvokeApiForAllAttachmentsButEncounterless() {
		// Setup
		AttachmentResource1_10 res = new AttachmentResource1_10();
		AttachmentsService attachmentsService = mock(AttachmentsService.class);
		Patient patient = new Patient();
		
		// Replay
		res.search(attachmentsService, patient, null, null, "false", true);
		
		// Verify
		verify(attachmentsService, times(1)).getAttachments(patient, false, true);
		verifyNoMoreInteractions(attachmentsService);
	}
}
