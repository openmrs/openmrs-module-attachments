package org.openmrs.module.attachments;

import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.api.VisitService;
import org.openmrs.module.attachments.obs.Attachment;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class AttachmentsServiceTest extends BaseModuleContextSensitiveTest {
	
	@Autowired
	private AttachmentsService as;
	
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
	@Verifies(value = "the parameters should return the corresponding attachments", method = "getAttachments(patient, visit, encounter, includeRetired)")
	public void getAttachments_shouldReturnAttachments() {
		
		// Setup
		Patient patient = ps.getPatient(2);
		Visit visit = vs.getVisit(1);
		Encounter encounter = es.getEncountersByPatient(patient).get(0);
		
		// Replay
		List<Attachment> attachmentList = as.getAttachments(patient, visit, encounter, true);
		
		// Verify
		for (Attachment attachment : attachmentList) {
			assertEquals(attachment.getObs().getPerson(), patient);
			assertEquals(attachment.getObs().getEncounter().getVisit(), visit);
			assertEquals(attachment.getObs().getEncounter(), encounter);
		}
		
	}
}
