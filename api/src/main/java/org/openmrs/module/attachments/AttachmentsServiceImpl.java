package org.openmrs.module.attachments;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.Visit;

import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;

import org.openmrs.module.attachments.obs.Attachment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.List;

public class AttachmentsServiceImpl implements AttachmentsService {
	
	private final Log log = LogFactory.getLog(getClass());
	
	@Autowired
	@Qualifier(AttachmentsConstants.COMPONENT_ATT_CONTEXT)
	private AttachmentsContext context;
	
	@Autowired
	@Qualifier("conceptService")
	private ConceptService cs;
	
	@Override
	public List<Attachment> getAttachments(Patient patient, Encounter encounter, boolean includeRetired) {
		
		List<Person> personList = new ArrayList<>();
		List<Attachment> attachmentsList = new ArrayList<>();
		personList.add(patient);
		List<String> conceptComplexList = context.getConceptComplexList();
		List<Concept> questionConcepts = new ArrayList<>();
		for (String uuid : conceptComplexList) {
			Concept concept = cs.getConceptByUuid(uuid);
			if (concept == null) {
				log.error("The Concept with UUID " + uuid + " was not found");
			} else {
				questionConcepts.add(concept);
			}
		}
		List<Encounter> encounterList = new ArrayList<>();
		encounterList.add(encounter);
		List<Obs> obs = context.getObsService().getObservations(personList, encounterList, questionConcepts, null, null,
		    null, null, null, null, null, null, includeRetired);
		for (Obs observation : obs) {
			if (observation.isComplex()) {
				attachmentsList.add(new Attachment(observation));
			}
		}
		return attachmentsList;
	}
	
	@Override
	public List<Attachment> getAttachments(Patient patient, Visit visit, boolean includeRetired) {
		List<Visit> visitList = new ArrayList<>();
		visitList.add(visit);
		List<Attachment> attachmentsList = new ArrayList<>();
		List<Encounter> encounters = Context.getEncounterService().getEncounters(patient, null, null, null, null, null, null,
		    null, visitList, includeRetired);
		for (Encounter encounter : encounters) {
			attachmentsList.addAll(getAttachments(patient, encounter, includeRetired));
		}
		return attachmentsList;
	}
	
	public List<Attachment> getAttachments(Patient patient, boolean includeRetired) {
		List<Attachment> attachmentsList = new ArrayList<>();
		List<Encounter> encounters = context.getEncounterService()
		        .getEncountersByPatient(patient.getPatientIdentifier().toString(), includeRetired);
		for (Encounter encounter : encounters) {
			attachmentsList.addAll(getAttachments(patient, encounter, includeRetired));
		}
		return attachmentsList;
	}
}
