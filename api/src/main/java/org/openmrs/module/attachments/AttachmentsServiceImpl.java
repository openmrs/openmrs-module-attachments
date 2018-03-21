package org.openmrs.module.attachments;

import org.openmrs.*;
import org.openmrs.api.context.Context;
import org.openmrs.module.attachments.obs.Attachment;

import java.util.ArrayList;
import java.util.List;

public class AttachmentsServiceImpl implements AttachmentsService {
	
	@Override
	public List<Attachment> getAttachments(Patient patient, Visit visit, Encounter encounter, boolean includeRetired) {
		
		List<Attachment> attachments = new ArrayList<>();
		if (visit == null && encounter == null) {
			List<Obs> obs = Context.getObsService().getObservations((List<Person>) patient, null, null, null, null, null,
			    null, null, null, null, null, includeRetired);
			for (Obs observation : obs) {
				attachments.add(new Attachment(observation));
			}
			return attachments;
		} else if (visit == null) {
			List<Obs> obs = Context.getObsService().getObservations((List<Person>) patient, (List<Encounter>) encounter,
			    null, null, null, null, null, null, null, null, null, includeRetired);
			for (Obs observation : obs) {
				attachments.add(new Attachment(observation));
			}
			return attachments;
		} else {
			List<Obs> obs = Context.getObsService().getObservations((List<Person>) patient, (List<Encounter>) encounter,
			    null, null, null, null, null, null, null, null, null, includeRetired);
			for (Obs observation : obs) {
				if (observation.getEncounter().getVisit() == visit) {
					attachments.add(new Attachment(observation));
				}
			}
			return attachments;
		}
	}
	
}
