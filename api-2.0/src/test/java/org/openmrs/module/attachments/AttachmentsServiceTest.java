package org.openmrs.module.attachments;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.ConceptComplex;
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

import java.util.Date;
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
	
	protected static final String OBS_DATASET_XML = "org/openmrs/module/include/ComplexObsTest.xml";
	
	@Test
	public void getAttachments_shouldReturnEncounterAttachments() throws Exception {
		
		// Setup
		List<Obs> complexObsList = testHelper.saveComplexObsForEncounter(2);
		executeDataSet(OBS_DATASET_XML);
		
		Obs obs = complexObsList.get(0);
		Patient patient = obs.getEncounter().getPatient();
		Encounter encounter = obs.getEncounter();
		System.out.println(encounter.getEncounterId());
		Visit visit = encounter.getVisit();
		
		//Creating the Concept Complex Obs not relevent to the attachments
		//ConceptComplex conceptComplex = cs.getConceptComplex(8474);
		
		// Creating the Concept Complex Obs relevent to the attachments
		ConceptComplex conceptComplex = cs.getConceptComplex(obs.getConcept().getConceptId());


		Obs conceptComplexDefaultObs = testHelper.saveComplexObsForEncounterDefault(patient, conceptComplex, encounter);
		
		// saving some other obs during the same encounter
		Obs otherObs = new Obs();
		otherObs.setConcept(cs.getConcept(3));
		otherObs.setObsDatetime(new Date());
		otherObs.setEncounter(encounter);
		otherObs.setPerson(patient);
		otherObs.setValueText("Some text value for a test obs.");
		otherObs = os.saveObs(otherObs, null);
		Assert.assertNotNull(otherObs.getId());
		
		// Replay
		List<Attachment> actualAttachments = as.getAttachments(patient, encounter, true);
		
		// Verify
		List<Attachment> expectedAttachments = complexObsList.stream().map(Attachment::new).collect(Collectors.toList());
		
		Assert.assertArrayEquals(
		    expectedAttachments.stream().map(Attachment::getUuid).collect(Collectors.toList()).toArray(),
		    actualAttachments.stream().map(Attachment::getUuid).collect(Collectors.toList()).toArray());
	}
	
	@Test
	public void getAttachments_shouldReturnVisitAttachments() throws Exception {
		
		// Setup
		List<Obs> complexObsList = testHelper.saveComplexObsForVisit(4);
		
		Obs obs = complexObsList.get(0);
		Patient patient = obs.getEncounter().getPatient();
		Encounter encounter = obs.getEncounter();
		Visit visit = obs.getEncounter().getVisit();
		
		// saving some other obs during the same visit
		Obs otherObs = new Obs();
		otherObs.setConcept(cs.getConcept(3));
		otherObs.setObsDatetime(new Date());
		otherObs.setEncounter(encounter);
		otherObs.setPerson(patient);
		otherObs.setValueText("Some text value for a test obs.");
		otherObs = os.saveObs(otherObs, null);
		Assert.assertNotNull(otherObs.getId());
		
		// Replay
		List<Attachment> actualAttachments = as.getAttachments(patient, visit, true);
		
		// Verify
		List<Attachment> expectedAttachments = complexObsList.stream().map(Attachment::new).collect(Collectors.toList());
		
		Assert.assertArrayEquals(
		    expectedAttachments.stream().map(Attachment::getUuid).collect(Collectors.toList()).toArray(),
		    actualAttachments.stream().map(Attachment::getUuid).collect(Collectors.toList()).toArray());
	}
}
