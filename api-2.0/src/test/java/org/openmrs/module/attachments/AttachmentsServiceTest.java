package org.openmrs.module.attachments;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
import org.openmrs.api.VisitService;
import org.openmrs.module.attachments.obs.Attachment;
import org.openmrs.module.attachments.obs.TestHelper;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;
import java.util.stream.Collectors;

public class AttachmentsServiceTest extends BaseModuleContextSensitiveTest {
	
	@Autowired
	private AttachmentsService as;
	
	@Autowired
	private TestHelper testHelper;
	
	@Autowired
	@Qualifier("patientService")
	private PatientService ps;
	
	@Autowired
	@Qualifier("visitService")
	private VisitService vs;
	
	@Autowired
	@Qualifier("encounterService")
	private EncounterService es;
	
	@Autowired
	@Qualifier("conceptService")
	private ConceptService cs;
	
	@Autowired
	@Qualifier("obsService")
	private ObsService os;
	
	@Test
	public void getAttachments_shouldReturnEncounterAttachments() throws Exception {
		
		// Setup
		List<Obs> complexObsList = testHelper.saveComplexObs(3);
		
		Obs obs = complexObsList.get(0);
		Patient patient = obs.getEncounter().getPatient();
		Encounter encounter = obs.getEncounter();
		
		// Replay
		List<Attachment> actualAttachments = as.getAttachments(patient, null, encounter, true);
		
		// Verify
		List<Attachment> expectedAttachments = complexObsList.stream().map(Attachment::new).collect(Collectors.toList());
		
		Assert.assertArrayEquals(
		    expectedAttachments.stream().map(Attachment::getUuid).collect(Collectors.toList()).toArray(),
		    actualAttachments.stream().map(Attachment::getUuid).collect(Collectors.toList()).toArray());
	}
}
