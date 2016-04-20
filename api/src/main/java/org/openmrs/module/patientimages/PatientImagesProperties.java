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
import org.openmrs.api.context.Context;
import org.springframework.stereotype.Component;

/*
 * Copied from {@link org.openmrs.module.emrapi.utils.ModuleProperties}
 * since it wasn't possible to bring in the dependency on emrapi-api
 */
@Component("patientImagesProperties")
public class PatientImagesProperties
{
	protected final Log log = LogFactory.getLog(getClass());
	
	protected Concept getConceptByGlobalProperty(String globalPropertyName) {
        String globalProperty = Context.getAdministrationService().getGlobalProperty(globalPropertyName);
        Concept concept = Context.getConceptService().getConceptByUuid(globalProperty);
        if (concept == null) {
            throw new IllegalStateException("Configuration required: " + globalPropertyName);
        }
        return concept;
    }
	
	public ConceptComplex getConceptComplex() {
		String globalPropertyName = PatientImagesConstants.GP_CONCEPT_COMPLEX_UUID;
		Concept concept = getConceptByGlobalProperty(globalPropertyName);
		ConceptComplex conceptComplex = Context.getConceptService().getConceptComplex(concept.getConceptId());
		if (conceptComplex == null) {
            throw new IllegalStateException("Configuration required: " + globalPropertyName);
        }
		return conceptComplex;
	}
}