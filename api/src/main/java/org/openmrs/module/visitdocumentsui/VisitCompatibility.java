package org.openmrs.module.visitdocumentsui;

import java.util.List;

import org.openmrs.Encounter;
import org.openmrs.Visit;

public interface VisitCompatibility {

   public List<Encounter> getNonVoidedEncounters(Visit visit);
   
}
