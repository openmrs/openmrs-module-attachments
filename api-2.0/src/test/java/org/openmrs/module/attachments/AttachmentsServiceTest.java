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
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
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
	
	@Test
	public void getAttachments_shouldReturnEncounterAttachments() throws Exception {
		
		Encounter encounter = testHelper.getTestEncounter();
		List<Attachment> expectedAttachments = testHelper.saveComplexObs(encounter, 2, 2);
		Patient patient = encounter.getPatient();
		
		{
			// Createing some other obs ( not complex Obs ) during the same encounter
			Obs otherObs = new Obs();
			otherObs.setConcept(cs.getConcept(3));
			otherObs.setObsDatetime(new Date());
			otherObs.setEncounter(encounter);
			otherObs.setPerson(patient);
			otherObs.setValueText("Some text value for a test obs.");
			otherObs = os.saveObs(otherObs, null);
		}
		
		List<Attachment> actualAttachments = as.getAttachments(patient, encounter, true);
		
		Assert.assertArrayEquals(
		    expectedAttachments.stream().map(Attachment::getUuid).collect(Collectors.toList()).toArray(),
		    actualAttachments.stream().map(Attachment::getUuid).collect(Collectors.toList()).toArray());
	}
	
	@Test
	public void getAttachments_shouldReturnVisitAttachments() throws Exception {
		
		Encounter encounter = testHelper.getTestEncounter();
		Visit visit = encounter.getVisit();
		Patient patient = encounter.getPatient();
		Provider provider = prs.getProvider(1);
		
		Encounter encounter2 = context.getAttachmentEncounter(patient, visit, provider);
		Encounter encounter3 = context.getAttachmentEncounter(patient, visit, provider);
		
		List<Attachment> expectedAttachments = testHelper.saveComplexObs(encounter, 1, 1);
		expectedAttachments.addAll(testHelper.saveComplexObs(encounter2, 2, 2));
		expectedAttachments.addAll(testHelper.saveComplexObs(encounter3, 3, 3));
		
		List<Attachment> actualAttachments = as.getAttachments(patient, visit, true);
		
		Assert.assertArrayEquals(
		    expectedAttachments.stream().map(Attachment::getUuid).collect(Collectors.toList()).toArray(),
		    actualAttachments.stream().map(Attachment::getUuid).collect(Collectors.toList()).toArray());
	}
}
