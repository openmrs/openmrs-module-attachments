package org.openmrs.module.attachments;

import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.api.VisitService;
import org.openmrs.module.attachments.obs.Attachment;
import org.openmrs.module.attachments.obs.TestHelper;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AttachmentsServiceTest extends BaseModuleContextSensitiveTest {
	
	@Autowired
	private AttachmentsService as;
	
	@Autowired
	protected TestHelper testHelper;
	
	@Autowired
	@Qualifier("patientService")
	protected PatientService ps;
	
	@Autowired
	@Qualifier("visitService")
	protected VisitService vs;
	
	@Autowired
	@Qualifier("encounterService")
	private EncounterService es;
	
	@Test
	public void getAttachments_shouldReturnAttachments() throws Exception {
		
		// Setup
		List<Obs> obsList = testHelper.saveComplexObs(3);
		
		Obs obs = obsList.get(0);
		Patient patient = obs.getEncounter().getPatient();
		Visit visit = obs.getEncounter().getVisit();
		Encounter encounter = obs.getEncounter();
		
		// Replay
		List<Attachment> attachmentsList = as.getAttachments(patient, visit, encounter, true);
		
		// Verify
		List<Attachment> originalAttachmentsList = obsList.stream().map(Attachment::new).collect(Collectors.toList());
		
		assertEquals(new HashSet<>(originalAttachmentsList), new HashSet<>(attachmentsList));
		
		for (Attachment originalAttachment : originalAttachmentsList) {
			boolean found = false;
			for (Attachment testingAttachment : attachmentsList) {
				if (Objects.equals(originalAttachment.getUuid(), testingAttachment.getUuid())) {
					found = true;
					break;
				}
			}
			assertTrue(found);
		}
	}
}
