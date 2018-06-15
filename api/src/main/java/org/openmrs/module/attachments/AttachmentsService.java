package org.openmrs.module.attachments;

import java.util.List;

import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.APIException;
import org.openmrs.module.attachments.obs.Attachment;

public interface AttachmentsService {
	
	/**
	 * Get a patient's attachments including attachments that are not associated with any visits or
	 * encounters.
	 *
	 * @param includeRetired Specifies whether the underlying complex obs that are fetched should
	 *            include retired ones or not.
	 * @throws APIException if non-complex obs are mistakenly returned
	 */
	List<Attachment> getAttachments(Patient patient, boolean includeRetired);
	
	/**
	 * Get a patient's attachments.
	 *
	 * @param includeRetired Specifies whether the underlying complex obs that are fetched should
	 *            include retired ones or not.
	 * @param includeIsolated Specifies whether the underlying attachments that are fetched should
	 *            include attachments that are not associated with any visits or encounters.
	 * @throws APIException if non-complex obs are mistakenly returned
	 */
	List<Attachment> getAttachments(Patient patient, boolean includeIsolated, boolean includeRetired);
	
	/**
	 * Get a patient's attachments that are not associated with any visits or encounters.
	 *
	 * @param includeRetired Specifies whether the underlying complex obs that are fetched should
	 *            include retired ones or not.
	 * @throws APIException if non-complex obs are mistakenly returned
	 */
	List<Attachment> getIsolatedAttachments(Patient patient, boolean includeRetired);
	
	/**
	 * Get a patient's attachments that are associated with a specified encounter.
	 *
	 * @param encounter the encounter which attachment to get
	 * @param includeRetired Specifies whether the underlying complex obs that are fetched should
	 *            include retired ones or not.
	 * @throws APIException if non-complex obs are mistakenly returned
	 */
	List<Attachment> getAttachments(Patient patient, Encounter encounter, boolean includeRetired);
	
	/**
	 * Get a patient's attachments that are associated with a specified visit.
	 *
	 * @param visit the visit which attachments to get
	 * @param includeRetired Specifies whether the underlying complex obs that are fetched should
	 *            include retired ones or not.
	 * @throws APIException if non-complex obs are mistakenly returned
	 */
	List<Attachment> getAttachments(Patient patient, Visit visit, boolean includeRetired);
}
