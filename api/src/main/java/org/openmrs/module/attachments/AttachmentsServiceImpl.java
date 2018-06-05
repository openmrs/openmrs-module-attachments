package org.openmrs.module.attachments;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.Visit;

import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;

import org.openmrs.module.attachments.obs.Attachment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.IOException;
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
		
		List<Person> persons = new ArrayList<>();
		List<Attachment> attachments = new ArrayList<>();
		List<Encounter> encounters = new ArrayList<>();
		List<Concept> questionConcepts = getAttachmentConcepts();
		persons.add(patient);
		encounters.add(encounter);
		try {
			List<Obs> obs = context.getObsService().getObservations(persons, encounters, questionConcepts, null, null, null,
			    null, null, null, null, null, includeRetired);
			
			for (Obs observation : obs) {
				if (observation.isComplex()) {
					attachments.add(new Attachment(observation));
				}
			}
		}
		catch (Exception e) {
			throw new APIException("No concepts complex were configured or found to query attachments.", e);
		}
		
		return attachments;
	}
	
	@Override
	public List<Attachment> getAttachments(Patient patient, Visit visit, boolean includeRetired) {
		List<Visit> visits = new ArrayList<>();
		List<Attachment> attachments = new ArrayList<>();
		visits.add(visit);
		List<Encounter> encounters = Context.getEncounterService().getEncounters(patient, null, null, null, null, null, null,
		    null, visits, includeRetired);
		for (Encounter encounter : encounters) {
			attachments.addAll(getAttachments(patient, encounter, includeRetired));
		}
		return attachments;
	}
	
	public List<Attachment> getAttachments(Patient patient, boolean includeRetired) {
		List<Attachment> attachments = new ArrayList<>();
		List<Encounter> encounters = context.getEncounterService()
		        .getEncountersByPatient(patient.getPatientIdentifier().toString(), includeRetired);
		for (Encounter encounter : encounters) {
			attachments.addAll(getAttachments(patient, encounter, includeRetired));
		}
		return attachments;
	}
	
	public List<Attachment> getIsolatedAttachments(Patient patient, boolean includeRetired) {
		List<Person> persons = new ArrayList<>();
		List<Attachment> attachments = new ArrayList<>();
		List<Concept> questionConcepts = getAttachmentConcepts();
		persons.add(patient);
		try {
			List<Obs> obs = context.getObsService().getObservations(persons, null, questionConcepts, null, null, null, null,
			    null, null, null, null, includeRetired);
			
			for (Obs observation : obs) {
				if (observation.isComplex()) {
					attachments.add(new Attachment(observation));
				}
			}
		}
		catch (Exception e) {
			throw new APIException("No concepts complex were configured or found to query attachments.", e);
		}
		
		return attachments;
	}
	
	// Get list of attachment complex concepts.
	private List<Concept> getAttachmentConcepts() {
		List<String> conceptComplexes = context.getConceptComplexList();
		List<Concept> questionConcepts = new ArrayList<>();
		for (String uuid : conceptComplexes) {
			Concept concept = cs.getConceptByUuid(uuid);
			if (concept == null) {
				log.error("The Concept with UUID " + uuid + " was not found");
			} else {
				questionConcepts.add(concept);
			}
		}
		return questionConcepts;
	}
}
