package org.openmrs.module.attachments;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.FlushMode;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.Visit;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.attachments.obs.Attachment;
import org.openmrs.module.emrapi.db.DbSessionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Transactional(readOnly = true)
public class AttachmentsServiceImpl implements AttachmentsService {
	
	private final Log log = LogFactory.getLog(getClass());
	
	protected final static String NON_COMPLEX_OBS_ERR = "A non-complex obs was returned while fetching attachments, are the concepts complex configured properly?";
	
	@Autowired
	@Qualifier(AttachmentsConstants.COMPONENT_ATT_CONTEXT)
	private AttachmentsContext ctx;
	
	@Override
	public List<Attachment> getAttachments(Patient patient, boolean includeEncounterless, boolean includeVoided) {
		List<Person> persons = new ArrayList<>();
		List<Concept> questionConcepts = getAttachmentConcepts();
		persons.add(patient);
		
		List<Obs> obsList = ctx.getObsService().getObservations(persons, null, questionConcepts, null, null, null, null,
		    null, null, null, null, includeVoided);
		
		List<Attachment> attachments = new ArrayList<>();
		for (Obs obs : obsList) {
			if (!obs.isComplex()) {
				throw new APIException(NON_COMPLEX_OBS_ERR);
			}
			if (includeEncounterless) {
				attachments.add(new Attachment(obs));
			} else if (obs.getEncounter() != null) {
				attachments.add(new Attachment(obs));
			}
		}
		return attachments;
	}
	
	@Override
	public List<Attachment> getAttachments(Patient patient, boolean includeVoided) {
		return getAttachments(patient, true, includeVoided);
	}
	
	@Override
	public List<Attachment> getEncounterlessAttachments(Patient patient, boolean includeVoided) {
		List<Person> persons = new ArrayList<>();
		List<Concept> questionConcepts = getAttachmentConcepts();
		persons.add(patient);
		
		List<Obs> obsList = ctx.getObsService().getObservations(persons, null, questionConcepts, null, null, null, null,
		    null, null, null, null, includeVoided);
		
		List<Attachment> attachments = new ArrayList<>();
		for (Obs obs : obsList) {
			if (!obs.isComplex()) {
				throw new APIException(NON_COMPLEX_OBS_ERR);
			}
			if (obs.getEncounter() == null) {
				attachments.add(new Attachment(obs));
			}
		}
		return attachments;
	}
	
	@Override
	public List<Attachment> getAttachments(Patient patient, Encounter encounter, boolean includeVoided) {
		List<Person> persons = new ArrayList<>();
		List<Encounter> encounters = new ArrayList<>();
		List<Concept> questionConcepts = getAttachmentConcepts();
		persons.add(patient);
		encounters.add(encounter);
		
		List<Obs> obsList = ctx.getObsService().getObservations(persons, encounters, questionConcepts, null, null, null,
		    null, null, null, null, null, includeVoided);
		
		List<Attachment> attachments = new ArrayList<>();
		for (Obs obs : obsList) {
			if (!obs.isComplex()) {
				throw new APIException(NON_COMPLEX_OBS_ERR);
			}
			attachments.add(new Attachment(obs));
		}
		return attachments;
	}
	
	@Override
	public List<Attachment> getAttachments(Patient patient, final Visit visit, boolean includeVoided) {
		List<Visit> visits = new ArrayList<>();
		visits.add(visit);
		List<Encounter> encounters = ctx.getEncounterService().getEncounters(patient, null, null, null, null, null, null,
		    null, visits, includeVoided);
		
		List<Attachment> attachments = new ArrayList<>();
		for (Encounter encounter : encounters) {
			attachments.addAll(getAttachments(patient, encounter, includeVoided));
		}
		return attachments;
	}
	
	// Get list of attachment complex concepts
	protected List<Concept> getAttachmentConcepts() {
		List<String> conceptsComplex = ctx.getConceptComplexList();
		List<Concept> questionConcepts = new ArrayList<>();
		for (String uuid : conceptsComplex) {
			Concept concept = ctx.getConceptService().getConceptByUuid(uuid);
			if (concept == null) {
				log.error("The Concept with UUID " + uuid + " was not found");
			} else {
				questionConcepts.add(concept);
			}
		}
		if (CollectionUtils.isEmpty(questionConcepts)) {
			log.warn("No concepts complex are configured to fetch attachments.");
		}
		return questionConcepts;
	}
	
	@Transactional
	@Override
	public Attachment save(Attachment delegate, String reason) {
		FlushMode flushMode = DbSessionUtil.getCurrentFlushMode();
		DbSessionUtil.setManualFlushMode();
		Attachment attachment = new Attachment();
		try {
			Obs obs = Context.getObsService().saveObs(delegate.getObs(), reason);
			attachment = new Attachment(obs);
		}
		finally {
			DbSessionUtil.setFlushMode(flushMode);
		}
		return attachment;
	}
}
