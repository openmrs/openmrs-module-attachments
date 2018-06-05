package org.openmrs.module.attachments;

import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.APIException;
import org.openmrs.module.attachments.obs.Attachment;

import java.util.List;

/**
 * Used for managing attachments
 */
public interface AttachmentsService {


	/**
	 * Get all attachments for given patient.
	 *
	 * @param patient the patient whose attachment to get
	 * @param includeRetired
	 * @return matching attachments
	 * @throws APIException
	 */
	List<Attachment> getAttachments(Patient patient, boolean includeRetired);

	/**
	 * Get all attachments for given encounter.
	 *
	 * @param patient the patient whose attachment to get
	 * @param encounter the encounter which attachment to get
	 * @param includeRetired
	 * @return matching list of attachments
	 * @throws APIException
	 */
	List<Attachment> getAttachments(Patient patient, Encounter encounter, boolean includeRetired);

	/**
	 * Get all attachments for given visit.
	 *
	 * @param patient the patient whose attachments to get
	 * @param visit the visit which attachments to get
	 * @param includeRetired
	 * @return matching list of attachments
	 * @throws APIException
	 */
	List<Attachment> getAttachments(Patient patient, Visit visit, boolean includeRetired);

	/**
	 * Get all attachments for given patient which are not associated with any encounters.
	 *
	 * @param patient the patient whose attachment to get
	 * @param includeRetired
	 * @return matching list of attachments
	 * @throws APIException
	 */
	List<Attachment> getIsolatedAttachments(Patient patient, boolean includeRetired);
	
}
