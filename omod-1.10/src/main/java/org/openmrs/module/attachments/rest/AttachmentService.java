package org.openmrs.module.attachments.rest;

import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.module.attachments.obs.Attachment;

import java.util.List;

/**
 * Created by dileka on 3/17/18.
 */
public interface AttachmentService {
	
	List<Attachment> getAttachments(Patient patient, Visit visit, Encounter encounter, boolean includeRetired);
	
}
