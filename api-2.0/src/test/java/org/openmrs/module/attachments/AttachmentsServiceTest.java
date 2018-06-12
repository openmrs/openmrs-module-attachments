package org.openmrs.module.attachments;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.ConceptComplex;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.attachments.obs.Attachment;
import org.openmrs.module.attachments.obs.TestHelper;
import org.openmrs.module.attachments.obs.ValueComplex;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockMultipartFile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.stream.Collectors;

public class AttachmentsServiceTest extends BaseModuleContextSensitiveTest {
	
	@Autowired
	private AttachmentsService as;
	
	@Autowired
	@Qualifier(AttachmentsConstants.COMPONENT_ATT_CONTEXT)
	protected AttachmentsContext context;
	
	@Autowired
	private TestHelper testHelper;
	
	@Autowired
	@Qualifier("patientService")
	private PatientService ps;
	
	@Autowired
	@Qualifier("visitService")
	private VisitService vs;
	
	@Autowired
	@Qualifier("providerService")
	private ProviderService prs;
	
	@Autowired
	@Qualifier("encounterService")
	private EncounterService es;
	
	@Autowired
	@Qualifier("conceptService")
	private ConceptService cs;
	
	@Autowired
	@Qualifier("obsService")
	private ObsService os;
	
	@Autowired
	@Qualifier(AttachmentsConstants.COMPONENT_COMPLEXOBS_SAVER)
	private ComplexObsSaver obsSaver;
	
	@Test
	public void getAttachments_shouldReturnEncounterAttachments() throws Exception {
		
		// setup
		Encounter encounter = testHelper.getTestEncounter();
		List<Attachment> expectedAttachments = testHelper.saveComplexObs(encounter, 2, 2);
		Patient patient = encounter.getPatient();
		
		// adding a non-complex obs
		Obs otherObs = new Obs();
		otherObs.setConcept(cs.getConcept(3));
		otherObs.setObsDatetime(new Date());
		otherObs.setEncounter(encounter);
		otherObs.setPerson(patient);
		otherObs.setValueText("Some text value for a test obs.");
		otherObs = os.saveObs(otherObs, null);
		
		// replay
		List<Attachment> actualAttachments = as.getAttachments(patient, encounter, true);
		
		// verify
		Assert.assertArrayEquals(
		    expectedAttachments.stream().map(Attachment::getUuid).collect(Collectors.toList()).toArray(),
		    actualAttachments.stream().map(Attachment::getUuid).collect(Collectors.toList()).toArray());
	}
	
	@Test
	public void getAttachments_shouldReturnVisitAttachments() throws Exception {
		
		// setup
		Encounter encounter1 = testHelper.getTestEncounter();
		Visit visit = encounter1.getVisit();
		Patient patient = encounter1.getPatient();
		Provider provider = prs.getProvider(1);
		
		Encounter encounter2 = context.getAttachmentEncounter(patient, visit, provider);
		Encounter encounter3 = context.getAttachmentEncounter(patient, visit, provider);
		
		// adding a non-complex obs from encounter1
		Obs otherObs1 = new Obs();
		otherObs1.setConcept(cs.getConcept(3));
		otherObs1.setObsDatetime(new Date());
		otherObs1.setEncounter(encounter1);
		otherObs1.setPerson(patient);
		otherObs1.setValueText("Some text value for a test obs.");
		otherObs1 = os.saveObs(otherObs1, null);
		
		// adding a non-complex obs from encounter2
		Obs otherObs2 = new Obs();
		otherObs2.setConcept(cs.getConcept(3));
		otherObs2.setObsDatetime(new Date());
		otherObs2.setEncounter(encounter2);
		otherObs2.setPerson(patient);
		otherObs2.setValueText("Some text value for a test obs.");
		otherObs2 = os.saveObs(otherObs2, null);
		
		List<Attachment> expectedAttachments = testHelper.saveComplexObs(encounter1, 1, 1);
		expectedAttachments.addAll(testHelper.saveComplexObs(encounter2, 2, 2));
		expectedAttachments.addAll(testHelper.saveComplexObs(encounter3, 3, 3));
		
		// replay
		List<Attachment> actualAttachments = as.getAttachments(patient, visit, true);
		
		// verify
		Assert.assertArrayEquals(
		    expectedAttachments.stream().map(Attachment::getUuid).collect(Collectors.toList()).toArray(),
		    actualAttachments.stream().map(Attachment::getUuid).collect(Collectors.toList()).toArray());
	}
	
	@Test
	public void getAttachments_shouldReturnAllAttachments() throws Exception {
		
		// setup
		Encounter encounter = testHelper.getTestEncounter();
		List<Attachment> expectedAttachments = testHelper.saveComplexObs(encounter, 3, 2);
		Patient patient = ps.getPatient(2);
		
		// attachments not bound to any visits/encounters
		for (int i = 0; i < 2; i++) {
			byte[] randomData = new byte[20];
			new Random().nextBytes(randomData);
			
			MockMultipartFile multipartRandomFile = new MockMultipartFile("1", "1", "application/octet-stream", randomData);
			Obs obs = obsSaver.saveOtherAttachment(null, patient, null, "File caption #" + i + 1, multipartRandomFile,
			    ValueComplex.INSTRUCTIONS_DEFAULT);
			expectedAttachments.add(new Attachment(obs));
		}
		
		// adding some non-complex obs
		for (int i = 0; i < 2; i++) {
			Obs otherObs = new Obs();
			otherObs.setConcept(cs.getConcept(3));
			otherObs.setObsDatetime(new Date());
			otherObs.setPerson(patient);
			otherObs.setValueText("Some text value for a test obs # " + i + 1);
			otherObs = os.saveObs(otherObs, null);
		}
		
		// replay
		List<Attachment> actualAttachments = as.getAttachments(patient, true);
		
		// verify
		Assert.assertArrayEquals(
		    expectedAttachments.stream().map(Attachment::getUuid).collect(Collectors.toList()).toArray(),
		    actualAttachments.stream().map(Attachment::getUuid).collect(Collectors.toList()).toArray());
	}
	
	@Test
	public void getAttachments_shouldNotReturnIsolatedAttachments() throws Exception {
		
		// setup
		Encounter encounter = testHelper.getTestEncounter();
		List<Attachment> expectedAttachments = testHelper.saveComplexObs(encounter, 3, 2);
		Patient patient = ps.getPatient(2);
		
		// attachments not bound to any visits/encounters
		for (int i = 0; i < 2; i++) {
			byte[] randomData = new byte[20];
			new Random().nextBytes(randomData);
			
			MockMultipartFile multipartRandomFile = new MockMultipartFile("1", "1", "application/octet-stream", randomData);
			Obs obs = obsSaver.saveOtherAttachment(null, patient, null, "File caption #" + i + 1, multipartRandomFile,
			    ValueComplex.INSTRUCTIONS_DEFAULT);
		}
		
		// adding some non-complex obs
		for (int i = 0; i < 2; i++) {
			Obs otherObs = new Obs();
			otherObs.setConcept(cs.getConcept(3));
			otherObs.setObsDatetime(new Date());
			otherObs.setPerson(patient);
			otherObs.setValueText("Some text value for a test obs # " + i + 1);
			otherObs = os.saveObs(otherObs, null);
		}
		
		// replay
		List<Attachment> actualAttachments = as.getAttachments(patient, false, true);
		
		// verify
		Assert.assertArrayEquals(
		    expectedAttachments.stream().map(Attachment::getUuid).collect(Collectors.toList()).toArray(),
		    actualAttachments.stream().map(Attachment::getUuid).collect(Collectors.toList()).toArray());
	}
}
