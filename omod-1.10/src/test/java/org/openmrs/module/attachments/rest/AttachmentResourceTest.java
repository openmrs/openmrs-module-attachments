package org.openmrs.module.attachments.rest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.Concept;
import org.openmrs.ConceptComplex;
import org.openmrs.ConceptDatatype;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.module.attachments.AttachmentsConstants;
import org.openmrs.module.attachments.AttachmentsContext;
import org.openmrs.module.attachments.AttachmentsService;
import org.openmrs.module.attachments.obs.Attachment;
import org.openmrs.module.attachments.obs.ComplexDataHelper1_10;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class AttachmentResourceTest {

	@Before
	public void setup() {
		initMocks(this);
		PowerMockito.mockStatic(Context.class);
		AttachmentsContext ctx = mock(AttachmentsContext.class);
		when(ctx.getComplexDataHelper()).thenReturn(new ComplexDataHelper1_10());
		when(Context.getRegisteredComponent(AttachmentsConstants.COMPONENT_ATT_CONTEXT, AttachmentsContext.class))
				.thenReturn(ctx);
	}

	@Test
	public void get_shouldReturnFilenameProperty() {
		// Arrange
		AttachmentResource1_10 res = new AttachmentResource1_10();
		ObsService service = mock(ObsService.class);
		PowerMockito.when(Context.getObsService()).thenReturn(service);
		Obs attachmentObs = new Obs();
		attachmentObs.setUuid("1234");
		attachmentObs.setId(1);
		Concept attachmentConcept = new ConceptComplex();
		ConceptDatatype datatype = new ConceptDatatype();
		datatype.setHl7Abbreviation("ED");
		attachmentConcept.setDatatype(datatype);
		attachmentObs.setConcept(attachmentConcept);
		attachmentObs.setValueComplex("m3ks | instructions.default | text/plain | filename.png");
		when(service.getObsByUuid("1234")).thenReturn(attachmentObs);
		when(service.getComplexObs(1, AttachmentsConstants.ATT_VIEW_CRUD)).thenReturn(attachmentObs);

		// Act
		Attachment attachment = res.getByUniqueId("1234");

		// Assert
		assertThat(attachment.getFilename(), equalTo("filename.png"));
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
