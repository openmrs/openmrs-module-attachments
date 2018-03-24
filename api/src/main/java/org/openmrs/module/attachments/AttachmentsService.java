package org.openmrs.module.attachments;

import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Visit;

import java.util.List;

/**
 * Used for managing attachments
 */
public interface AttachmentsService {
	
	List<Obs> getAttachments(Patient patient, Visit visit, Encounter encounter, boolean includeRetired);
	
}
