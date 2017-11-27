package org.openmrs.module.attachments;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openmrs.Encounter;
import org.openmrs.Visit;
import org.openmrs.annotation.OpenmrsProfile;
import org.springframework.stereotype.Component;

@Component(AttachmentsConstants.COMPONENT_VISIT_COMPATIBILITY)
@OpenmrsProfile(openmrsPlatformVersion = "1.10.2 - 1.10.*")
public class VisitCompatibility1_10 implements VisitCompatibility {
	
	@Override
	public List<Encounter> getNonVoidedEncounters(Visit visit) {
		List<Encounter> nonVoidedEncounters = new ArrayList<Encounter>();
		Set<Encounter> allEncounters = visit.getEncounters();
		if (allEncounters != null) {
			for (Encounter encounter : allEncounters) {
				if (!encounter.isVoided()) {
					nonVoidedEncounters.add(encounter);
				}
			}
		}
		return nonVoidedEncounters;
	}
	
}
