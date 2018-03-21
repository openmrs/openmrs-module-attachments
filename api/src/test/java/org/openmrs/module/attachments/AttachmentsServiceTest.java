package org.openmrs.module.attachments;

import org.junit.Test;

import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.Visit;

import org.openmrs.api.context.Context;
import org.openmrs.module.attachments.obs.Attachment;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class AttachmentsServiceTest {
	
	@Autowired
	private AttachmentsServiceImpl as ;
	
	@Test
	public void getAttachments_shouldReturnAttachments() {
		
		// Setup
		Patient patient = Context.getPatientService().getPatient(2);
		Visit activeVisit = Context.getVisitService().getActiveVisitsByPatient(patient).get(0);
		Encounter encounter = Context.getEncounterService().getEncountersByPatient(patient).get(0);
		
		List<Attachment> attachmentList = as.getAttachments(patient, activeVisit, encounter,true);
	}
	
}
