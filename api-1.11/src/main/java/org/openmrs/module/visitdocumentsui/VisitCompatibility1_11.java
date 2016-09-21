package org.openmrs.module.visitdocumentsui;

import java.util.List;

import org.openmrs.Encounter;
import org.openmrs.Visit;
import org.openmrs.annotation.OpenmrsProfile;
import org.springframework.stereotype.Component;

@Component(VisitDocumentsConstants.COMPONENT_VISIT_COMPATIBILITY)
@OpenmrsProfile(openmrsPlatformVersion = "1.11.4 - 2.0.*")
public class VisitCompatibility1_11 implements VisitCompatibility {

   @Override
   public List<Encounter> getNonVoidedEncounters(Visit visit) {
      return visit.getNonVoidedEncounters();
   }

}