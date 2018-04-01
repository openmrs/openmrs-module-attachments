package org.openmrs.module.attachments;

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
	
	@Autowired
	@Qualifier(AttachmentsConstants.COMPONENT_ATT_CONTEXT)
	protected AttachmentsContext context;
	
	@Autowired
	@Qualifier("conceptService")
	private ConceptService cs;
	
	@Override
	public List<Attachment> getAttachments(Patient patient, Visit visit, Encounter encounter, boolean includeRetired) {
		
		List<Person> personList = new ArrayList<>();
		List<Attachment> attachmentsList = new ArrayList<>();
		personList.add(patient);
		
		List<String> conceptComplexList = context.getConceptComplexList();
		List<Concept> questionConcepts = new ArrayList<>();
		for (String uuid : conceptComplexList) {
			questionConcepts.add(cs.getConceptByUuid(uuid));
		}
		
		if (visit == null && encounter == null) {
			List<Obs> obs = Context.getObsService().getObservations(personList, null, questionConcepts, null, null, null,
			    null, null, null, null, null, includeRetired);
			for (Obs observation : obs) {
				if (observation.isComplex()) {
					attachmentsList.add(new Attachment(observation));
				}
			}
			return attachmentsList;
		} else if (visit == null) {
			List<Encounter> encounterList = new ArrayList<>();
			encounterList.add(encounter);
			
			List<Obs> obs = Context.getObsService().getObservations(personList, encounterList, questionConcepts, null, null,
			    null, null, null, null, null, null, includeRetired);
			for (Obs observation : obs) {
				if (observation.isComplex()) {
					attachmentsList.add(new Attachment(observation));
				}
			}
			return attachmentsList;
		} else {
			List<Encounter> encounterList = new ArrayList<>();
			encounterList.add(encounter);
			List<Obs> obs = Context.getObsService().getObservations(personList, encounterList, questionConcepts, null, null,
			    null, null, null, null, null, null, includeRetired);
			for (Obs observation : obs) {
				if (observation.getEncounter().getVisit() == visit && observation.isComplex()) {
					attachmentsList.add(new Attachment(observation));
				}
			}
			return attachmentsList;
		}
	}
}
