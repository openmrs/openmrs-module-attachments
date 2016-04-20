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
import org.openmrs.ConceptName;
import org.openmrs.api.ConceptService;
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
				
				conceptService.saveConcept(conceptComplex);
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
