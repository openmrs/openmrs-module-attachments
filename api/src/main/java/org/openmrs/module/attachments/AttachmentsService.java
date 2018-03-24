package org.openmrs.module.attachments;

import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.module.attachments.obs.Attachment;

import java.util.List;

/**
 * Used for managing attachments
 */
public interface AttachmentsService {
	
	List<Attachment> getAttachments(Patient patient, Visit visit, Encounter encounter, boolean includeRetired);
	
}
