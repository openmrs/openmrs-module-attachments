/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.visitdocumentsui;

public class VisitDocumentsConstants {

   public static final String MODULE_CONTEXT_QUALIFIER = "visitDocumentsContext";
   
	public static final String MODULE_BASE_URL = "/visitdocumentsui";
	
	public static final String UPLOAD_DOCUMENT_URL = MODULE_BASE_URL + "/upload";
	
	public static final String DOWNLOAD_DOCUMENT_URL = MODULE_BASE_URL + "/download";
	
	public static final String DOC_VIEW_ORIGINAL = "complexdata.view.original";
	
   public static final String DOC_VIEW_THUMBNAIL = "complexdata.view.thumbnail";
   
   public static final String DOC_VIEW_CRUD = DOC_VIEW_ORIGINAL;  // Use this view for CRUD operations where a view must be provided.
	
	public static final String GP_CONCEPT_COMPLEX_UUID = "visitdocumentsui.conceptComplexUuid";
	
	public static final String GP_ENCOUNTER_TYPE_UUID = "visitdocumentsui.encounterTypeUuid";
	
	public static final String GP_MAX_UPLOAD_FILE_SIZE = "visitdocumentsui.maxUploadFileSize";
	
	public static final String GP_MAX_STORAGE_FILE_SIZE = "visitdocumentsui.maxStorageFileSize";
	
	public static final String GP_ENCOUNTER_SAVING_FLOW = "visitdocumentsui.encounterSavingFlow";
	
	public static final String GP_ALLOW_NO_CAPTION = "visitdocumentsui.allowNoCaption";
	
	public static final String REPRESENTATION_OBS = "(uuid:ref,comment:ref,obsDatetime:ref)";
}