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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptComplex;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.VisitService;
import org.openmrs.module.emrapi.utils.ModuleProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Inject this class to access services and global properties.
 */
@Component("patientImagesContext")
public class PatientImagesContext extends ModuleProperties
{
	protected final Log log = LogFactory.getLog(getClass());

	@Autowired
    @Qualifier("obsService")
    protected ObsService obsService;
	
	/*
	 * Exposing all needed services through OUR context
	 */
	
	public ConceptService getConceptService() {
		return conceptService;
	}
	
	public ObsService getObsService() {
		return obsService;
	}
	
	public VisitService getVisitService() {
		return visitService;
	}
	
	public ProviderService getProviderService() {
		return providerService;
	}
	
	public EncounterService getEncounterService() {
		return encounterService;
	}
	
	/*
	 * See super#getIntegerByGlobalProperty(String globalPropertyName)
	 */
	protected Double getDoubleByGlobalProperty(String globalPropertyName) {
		String globalProperty = administrationService.getGlobalProperty(globalPropertyName);
        try {
            return Double.valueOf(globalProperty);
        }
        catch (Exception e) {
            throw new IllegalStateException("Global property " + globalPropertyName + " value of " + globalProperty + " is not parsable as a Double");
        }
    }
	
	/**
	 * @return The concept complex used to save uploaded image-obs.
	 */
	public ConceptComplex getConceptComplex() {
		String globalPropertyName = PatientImagesConstants.GP_CONCEPT_COMPLEX_UUID;
		Concept concept = getConceptByGlobalProperty(globalPropertyName);
		ConceptComplex conceptComplex = getConceptService().getConceptComplex(concept.getConceptId());
		if (conceptComplex == null) {
            throw new IllegalStateException("Configuration required: " + globalPropertyName);
        }
		return conceptComplex;
	}
	
	/**
	 * @return The encounter type for encounters recording the upload of an image.
	 */
	public EncounterType getEncounterType() {
		EncounterType encounterType = getEncounterTypeByGlobalProperty(PatientImagesConstants.GP_ENCOUNTER_TYPE_UUID);
		return encounterType;
	}
	
	/**
	 * @return The max image file size allowed to be uploaded (in Megabytes).
	 */
	public Double getMaxImageFileSize() {
		return getDoubleByGlobalProperty(PatientImagesConstants.GP_MAX_IMAGE_FILE_SIZE);
	}
	
	// TODO: Figure out if this is good enough
	public EncounterRole getEncounterRole() {
		EncounterRole unknownRole = getEncounterService().getEncounterRoleByUuid(EncounterRole.UNKNOWN_ENCOUNTER_ROLE_UUID);
		if (unknownRole == null) {
			throw new IllegalStateException("No 'Unknown' encounter role with uuid "
			        + EncounterRole.UNKNOWN_ENCOUNTER_ROLE_UUID + ".");
		}
		return unknownRole;
	}
}