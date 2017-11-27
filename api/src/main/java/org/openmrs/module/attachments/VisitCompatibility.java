package org.openmrs.module.attachments;

import java.util.List;

import org.openmrs.Encounter;
import org.openmrs.Visit;

public interface VisitCompatibility {
	
	public List<Encounter> getNonVoidedEncounters(Visit visit);
	
}
