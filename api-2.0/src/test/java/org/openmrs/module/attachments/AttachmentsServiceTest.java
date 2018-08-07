package org.openmrs.module.attachments;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.api.APIException;
import org.openmrs.module.attachments.obs.Attachment;
import org.openmrs.module.attachments.obs.TestHelper;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class AttachmentsServiceTest extends BaseModuleContextSensitiveTest {
	
	@Autowired
	private AttachmentsService as;
	
	@Autowired
	@Qualifier(AttachmentsConstants.COMPONENT_ATT_CONTEXT)
	protected AttachmentsContext ctx;
	
	@Autowired
	private TestHelper testHelper;
	
	@Autowired
	@Qualifier(AttachmentsConstants.COMPONENT_COMPLEXOBS_SAVER)
	private ComplexObsSaver obsSaver;
	
	@Before
	public void setup() throws IOException {
		testHelper.init();
	}
	
	@After
	public void tearDown() throws IOException {
		testHelper.tearDown();
	}
	
	protected static List<Attachment> asSortedAttachments(List<Obs> obsList) {
		List<Attachment> attachments = obsList.stream().map(obs -> new Attachment(obs)).collect(Collectors.toList());
		Collections.sort(attachments, Comparator.comparing(Attachment::getDateTime).reversed());
		return attachments;
	}
	
	@Test
	public void getAttachments_shouldReturnEncounterAttachments() throws Exception {
		//
		// setup
		//
		Encounter encounter = testHelper.getTestEncounter();
		List<Attachment> expectedAttachments = asSortedAttachments(testHelper.saveComplexObs(encounter, 2, 2));
		Patient patient = encounter.getPatient();
		
		// adding a non-complex obs
		Obs otherObs = new Obs();
		otherObs.setConcept(ctx.getConceptService().getConcept(3));
		otherObs.setObsDatetime(new Date());
		otherObs.setEncounter(encounter);
		otherObs.setPerson(patient);
		otherObs.setValueText("Some text value for a test obs.");
		otherObs = ctx.getObsService().saveObs(otherObs, null);
		
		//
		// replay
		//
		List<Attachment> actualAttachments = as.getAttachments(patient, encounter, true);
		
		//
		// verify
		//
		Assert.assertArrayEquals(
		    expectedAttachments.stream().map(Attachment::getUuid).collect(Collectors.toList()).toArray(),
		    actualAttachments.stream().map(Attachment::getUuid).collect(Collectors.toList()).toArray());
	}
	
	@Test
	public void getAttachments_shouldReturnVisitAttachments() throws Exception {
		//
		// setup
		//
		Encounter encounter1 = testHelper.getTestEncounter();
		Visit visit = encounter1.getVisit();
		Patient patient = encounter1.getPatient();
		Provider provider = ctx.getProviderService().getProvider(1);
		
		Encounter encounter2 = ctx.getAttachmentEncounter(patient, visit, provider);
		Encounter encounter3 = ctx.getAttachmentEncounter(patient, visit, provider);
		
		// adding a non-complex obs to encounter1
		Obs otherObs1 = new Obs();
		otherObs1.setConcept(ctx.getConceptService().getConcept(3));
		otherObs1.setObsDatetime(new Date());
		otherObs1.setEncounter(encounter1);
		otherObs1.setPerson(patient);
		otherObs1.setValueText("Some text value for a test obs.");
		otherObs1 = ctx.getObsService().saveObs(otherObs1, null);
		
		// adding a non-complex obs to encounter2
		Obs otherObs2 = new Obs();
		otherObs2.setConcept(ctx.getConceptService().getConcept(3));
		otherObs2.setObsDatetime(new Date());
		otherObs2.setEncounter(encounter2);
		otherObs2.setPerson(patient);
		otherObs2.setValueText("Some text value for a test obs.");
		otherObs2 = ctx.getObsService().saveObs(otherObs2, null);
		
		List<Obs> obsList = testHelper.saveComplexObs(encounter1, 1, 1);
		obsList.addAll(testHelper.saveComplexObs(encounter2, 2, 2));
		obsList.addAll(testHelper.saveComplexObs(encounter3, 3, 3));
		List<Attachment> expectedAttachments = asSortedAttachments(obsList);
		
		//
		// replay
		//
		List<Attachment> actualAttachments = as.getAttachments(patient, visit, true);
		
		//
		// verify
		//
		Assert.assertArrayEquals(
		    expectedAttachments.stream().map(Attachment::getUuid).collect(Collectors.toList()).toArray(),
		    actualAttachments.stream().map(Attachment::getUuid).collect(Collectors.toList()).toArray());
	}
	
	@Test
	public void getAttachments_shouldReturnAllAttachments() throws Exception {
		//
		// setup
		//
		Encounter encounter = testHelper.getTestEncounter();
		List<Obs> obsList = testHelper.saveComplexObs(encounter, 3, 2);
		Patient patient = ctx.getPatientService().getPatient(2);
		
		// attachments not bound to any visits/encounters
		obsList.addAll(testHelper.saveComplexObs(null, 2, 0));
		
		// adding some non-complex obs
		for (int i = 0; i < 2; i++) {
			Obs otherObs = new Obs();
			otherObs.setConcept(ctx.getConceptService().getConcept(3));
			otherObs.setObsDatetime(new Date());
			otherObs.setPerson(patient);
			otherObs.setValueText("Some text value for a test obs #" + i + 1);
			otherObs = ctx.getObsService().saveObs(otherObs, null);
		}
		
		//
		// replay
		//
		List<Attachment> actualAttachments = as.getAttachments(patient, true);
		
		//
		// verify
		//
		Assert.assertArrayEquals(
		    asSortedAttachments(obsList).stream().map(Attachment::getUuid).collect(Collectors.toList()).toArray(),
		    actualAttachments.stream().map(Attachment::getUuid).collect(Collectors.toList()).toArray());
	}
	
	@Test
	public void getAttachments_shouldNotReturnEncounterlessAttachments() throws Exception {
		//
		// setup
		//
		Encounter encounter = testHelper.getTestEncounter();
		Patient patient = ctx.getPatientService().getPatient(2);
		List<Attachment> expectedAttachments = asSortedAttachments(testHelper.saveComplexObs(encounter, 3, 2));
		
		// attachments not bound to any visits/encounters
		testHelper.saveComplexObs(null, 2, 0);
		
		// adding some non-complex obs
		for (int i = 0; i < 2; i++) {
			Obs otherObs = new Obs();
			otherObs.setConcept(ctx.getConceptService().getConcept(3));
			otherObs.setObsDatetime(new Date());
			otherObs.setPerson(patient);
			otherObs.setValueText("Some text value for a test obs #" + i + 1);
			otherObs = ctx.getObsService().saveObs(otherObs, null);
		}
		
		//
		// replay
		//
		List<Attachment> actualAttachments = as.getAttachments(patient, false, true);
		
		//
		// verify
		//
		Assert.assertArrayEquals(
		    expectedAttachments.stream().map(Attachment::getUuid).collect(Collectors.toList()).toArray(),
		    actualAttachments.stream().map(Attachment::getUuid).collect(Collectors.toList()).toArray());
	}
	
	@Test
	public void getAttachments_shouldReturnEncounterlessAttachments() throws Exception {
		//
		// setup
		//
		Encounter encounter = testHelper.getTestEncounter();
		testHelper.saveComplexObs(encounter, 3, 2);
		Patient patient = ctx.getPatientService().getPatient(2);
		
		// attachments not bound to any visits/encounters
		List<Attachment> expectedAttachments = asSortedAttachments(testHelper.saveComplexObs(null, 2, 0));
		
		// adding some non-complex obs
		for (int i = 0; i < 2; i++) {
			Obs otherObs = new Obs();
			otherObs.setConcept(ctx.getConceptService().getConcept(3));
			otherObs.setObsDatetime(new Date());
			otherObs.setPerson(patient);
			otherObs.setValueText("Some text value for a test obs #" + i + 1);
			otherObs = ctx.getObsService().saveObs(otherObs, null);
		}
		
		//
		// replay
		//
		List<Attachment> actualAttachments = as.getEncounterlessAttachments(patient, true);
		
		//
		// verify
		//
		Assert.assertArrayEquals(
		    expectedAttachments.stream().map(Attachment::getUuid).collect(Collectors.toList()).toArray(),
		    actualAttachments.stream().map(Attachment::getUuid).collect(Collectors.toList()).toArray());
	}
	
	@Test
	public void getAttachments_shouldBeConsistentAcrossServiceApi() throws Exception {
		//
		// setup
		//
		Encounter encounter = testHelper.getTestEncounter();
		testHelper.saveComplexObs(encounter, 3, 2);
		Patient patient = ctx.getPatientService().getPatient(2);
		
		// attachments not bound to any visits/encounters
		testHelper.saveComplexObs(null, 2, 0);
		
		// adding some non-complex obs
		for (int i = 0; i < 2; i++) {
			Obs otherObs = new Obs();
			otherObs.setConcept(ctx.getConceptService().getConcept(3));
			otherObs.setObsDatetime(new Date());
			otherObs.setPerson(patient);
			otherObs.setValueText("Some text value for a test obs # " + i + 1);
			otherObs = ctx.getObsService().saveObs(otherObs, null);
		}
		
		//
		// replay
		//
		List<Attachment> expectedAllAttachments = as.getAttachments(patient, false, true);
		expectedAllAttachments.addAll(as.getEncounterlessAttachments(patient, true));
		Collections.sort(expectedAllAttachments, Comparator.comparing(Attachment::getDateTime).reversed());
		List<Attachment> actualAllAttachments = as.getAttachments(patient, true, true);
		
		//
		// verify
		//
		Assert.assertArrayEquals(
		    expectedAllAttachments.stream().map(Attachment::getUuid).collect(Collectors.toList()).toArray(),
		    actualAllAttachments.stream().map(Attachment::getUuid).collect(Collectors.toList()).toArray());
	}
	
	@Test(expected = APIException.class)
	public void getAttachments_shouldThrowWhenFetchingNonComplexObs() throws Exception {
		//
		// setup
		//
		Concept nonComplexConcept = ctx.getConceptService().getConcept(5);
		Encounter encounter = testHelper.getTestEncounter();
		Patient patient = ctx.getPatientService().getPatient(2);
		for (int i = 0; i < 2; i++) {
			Obs obs = new Obs();
			obs.setConcept(nonComplexConcept);
			obs.setObsDatetime(new Date());
			obs.setEncounter(encounter);
			obs.setPerson(patient);
			obs.setValueText("Some text value for a test obs #" + i + 1);
			obs = ctx.getObsService().saveObs(obs, null);
		}
		// pointing the configuration to the non-complex concept
		ctx.getAdministrationService().updateGlobalProperty(AttachmentsConstants.GP_CONCEPT_COMPLEX_UUID_LIST,
		    "[\"" + nonComplexConcept.getUuid() + "\"]");
		
		//
		// replay
		//
		as.getAttachments(patient, encounter, true);
	}
}
