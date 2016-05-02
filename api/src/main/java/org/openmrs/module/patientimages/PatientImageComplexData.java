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

import org.openmrs.obs.ComplexData;

public class PatientImageComplexData extends ComplexData {

	private static final long serialVersionUID = 1L;

	private String type = "";
	
	public PatientImageComplexData(String title, Object data) {
		this(title, data, "");
	}
	
	public PatientImageComplexData(String title, Object data, String type) {
		super(title, data);
		this.type = type;
	}

	public String getType() {
		return type;
	}
}