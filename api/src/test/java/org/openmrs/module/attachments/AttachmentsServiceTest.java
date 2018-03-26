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
import org.openmrs.test.Verifies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;

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
	@Verifies(value = "the parameters should return the corresponding attachments", method = "getAttachments(patient, visit, encounter, includeRetired)")
	public void getAttachments_shouldReturnAttachments() throws Exception {
		
		// Setup
		List<Obs> obsList = testHelper.saveComplexObs(3);
		
		Obs obs = obsList.get(0);
		Patient patient = obs.getEncounter().getPatient();
		Visit visit = obs.getEncounter().getVisit();
		Encounter encounter = obs.getEncounter();
		
		// Replay
		List<Attachment> attachmentList = as.getAttachments(patient, visit, encounter, true);
		
		// Verify
		List<Attachment> originalAttachmentsList = new ArrayList<>();
		for (Obs observation : obsList) {
				originalAttachmentsList.add(new Attachment(observation));
		}
		assertEquals(new HashSet<>(originalAttachmentsList), new HashSet<>(attachmentList));
		
	}
}
