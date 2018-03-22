package org.openmrs.module.attachments;

import org.junit.Test;
import org.openmrs.*;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.api.VisitService;
import org.openmrs.module.attachments.obs.Attachment;
import org.openmrs.test.Verifies;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class AttachmentsServiceTest {
	
	@Autowired
	private AttachmentsService as;
	
	@Autowired
	private PatientService ps;
	
	@Autowired
	private VisitService vs;
	
	@Autowired
	private EncounterService es;
	
	@Test
	@Verifies(value = "the parameters should return the corresponding attachments", method = "getAttachments(patient, visit, encounter, includeRetired)")
	public void getAttachments_shouldReturnAttachments() {
		
		// Setup
		Patient patient = ps.getPatient(1);
		Visit visit = vs.getActiveVisitsByPatient(patient).get(0);
		Encounter encounter = es.getEncountersByPatient(patient).get(0);
		
		//Replay
		List<Attachment> attachmentList = as.getAttachments(patient, visit, encounter, true);
		
		//Verify
		for (Attachment attachment : attachmentList) {
			assertEquals(attachment.getObs().getPerson(),(Person)patient);
			assertEquals(attachment.getObs().getEncounter().getVisit(),visit);
			assertEquals(attachment.getObs().getEncounter(),encounter);
		}
		
	}
}
