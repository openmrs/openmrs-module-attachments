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
	 * Get a patient's attachments that are associated with encounters.
	 *
	 * @param includeRetired Specifies whether the underlying complex obs that are fetched should
	 *            include retired ones or not.
	 * @throws APIException if no concepts complex were configured or found to query attachments
	 */
	List<Attachment> getAttachments(Patient patient, boolean includeRetired);
	
	/**
	 * Get a patient's attachments that are associated with a specified encounter.
	 *
	 * @param encounter the encounter which attachment to get
	 * @param includeRetired Specifies whether the underlying complex obs that are fetched should
	 *            include retired ones or not.
	 * @throws APIException if no concepts complex were configured or found to query attachments
	 */
	List<Attachment> getAttachments(Patient patient, Encounter encounter, boolean includeRetired);
	
	/**
	 * Get a patient's attachments that are associated with a specified visit.
	 *
	 * @param visit the visit which attachments to get
	 * @param includeRetired Specifies whether the underlying complex obs that are fetched should
	 *            include retired ones or not.
	 * @throws APIException if no concepts complex were configured or found to query attachments
	 */
	List<Attachment> getAttachments(Patient patient, Visit visit, boolean includeRetired);
	
	/**
	 * Get a patient's attachments that are not associated with any visits or encounters.
	 *
	 * @param includeRetired Specifies whether the underlying complex obs that are fetched should
	 *            include retired ones or not.
	 * @throws APIException if no concepts complex were configured or found to query attachments
	 */
	List<Attachment> getIsolatedAttachments(Patient patient, boolean includeRetired);
	
}
