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


public class PatientImagesConstants {

	public static final String MODULE_BASE_URL = "/patientimages";
	
	public static final String UPLOAD_IMAGE_URL = MODULE_BASE_URL + "/upload";
	
	public static final String DOWNLOAD_IMAGE_URL = MODULE_BASE_URL + "/download";
	
	public static final String GP_CONCEPT_COMPLEX_UUID = "patientimages.conceptComplexUuid";
	
	public static final String GP_ENCOUNTER_TYPE_UUID = "patientimages.encounterTypeUuid";
	
	public static final String GP_MAX_UPLOAD_FILE_SIZE = "patientimages.maxUploadFileSize";
	
	public static final String GP_MAX_STORAGE_FILE_SIZE = "patientimages.maxStorageFileSize";
	
	public static final String REPRESENTATION_OBS = "(uuid:ref,obsId:ref,comment:ref,obsDatetime:ref)";
}