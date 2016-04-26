/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.patientimages;


import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptComplex;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptDescription;
import org.openmrs.ConceptName;
import org.openmrs.EncounterType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.module.ModuleActivator;

/**
 * This class contains the logic that is run every time this module is either started or stopped.
 */
public class PatientImagesActivator implements ModuleActivator {
	
	protected Log log = LogFactory.getLog(getClass());
		
	/**
	 * @see ModuleActivator${symbol_pound}willRefreshContext()
	 */
	public void willRefreshContext() {
		log.info("Refreshing Patient Images Module");
	}
	
	/**
	 * @see ModuleActivator${symbol_pound}contextRefreshed()
	 */
	public void contextRefreshed() {
		log.info("Patient Images Module refreshed");
	}
	
	/**
	 * @see ModuleActivator${symbol_pound}willStart()
	 */
	public void willStart() {
		log.info("Starting Patient Images Module");
	}
	
	/**
	 * @see ModuleActivator${symbol_pound}started()
	 */
	public void started() {
		
		{
			final String name = "PATIENT IMAGE FOR UPLOAD";
			final String desc = "Concept complex used as a question for obs wrapping patient images.";
			final String uuid = "7cac8397-53cd-4f00-a6fe-028e8d743f8e";	// this is also the default GP value set in config.xml
			
			ConceptService conceptService = Context.getConceptService();
			
			if(null == conceptService.getConceptByUuid(uuid)) {
				
				ConceptComplex conceptComplex = new ConceptComplex();
				conceptComplex.setUuid(uuid);
				conceptComplex.setHandler("ImageHandler");
				ConceptName conceptName = new ConceptName(name, Locale.ENGLISH);
				conceptComplex.setFullySpecifiedName(conceptName);
				conceptComplex.setPreferredName(conceptName);
				conceptComplex.setConceptClass( conceptService.getConceptClassByUuid(ConceptClass.QUESTION_UUID) );
				conceptComplex.setDatatype( conceptService.getConceptDatatypeByUuid(ConceptDatatype.COMPLEX_UUID) );
				conceptComplex.addDescription(new ConceptDescription(desc, Locale.ENGLISH));
				
				conceptService.saveConcept(conceptComplex);
			}
		}

		{
			final String name = "Image Upload Encounter";
			final String desc = "Encounters used to record (complex) obs wrapping patient images.";
			final String uuid = "5021b1a1-e7f6-44b4-ba02-da2f2bcf8718";	// this is also the default GP value set in config.xml
			
			EncounterService es = Context.getEncounterService();
			EncounterType encounterType = es.getEncounterTypeByUuid(uuid);

			if(encounterType == null) {
				encounterType = new EncounterType(name, desc);
				encounterType.setUuid(uuid);
				es.saveEncounterType(encounterType);
			}
		}
		
		log.info("Patient Images Module started");
	}
	
	/**
	 * @see ModuleActivator${symbol_pound}willStop()
	 */
	public void willStop() {
		log.info("Stopping Patient Images Module");
	}
	
	/**
	 * @see ModuleActivator${symbol_pound}stopped()
	 */
	public void stopped() {
		log.info("Patient Images Module stopped");
	}
		
}
