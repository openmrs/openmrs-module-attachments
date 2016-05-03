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

import java.io.InputStream;

import org.openmrs.obs.ComplexData;
import org.springframework.http.MediaType;

public class PatientImageComplexData extends ComplexData {

	private static final long serialVersionUID = 1L;

	private String type = "";
	private MediaType mediaType = MediaType.ALL;
	
	public PatientImageComplexData(String title, InputStream stream, String type, MediaType mediaType) {
		super(title, stream);
		this.type = type;
		if (null != mediaType)
			this.mediaType = mediaType;
	}
	
	public PatientImageComplexData(String title, InputStream stream) {
		this(title, stream, "", null);
	}

	public String getType() {
		return type;
	}
	
	public String getMediaType() {
		return mediaType.getType();
	}
}