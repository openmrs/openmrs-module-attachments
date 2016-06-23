/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.visitdocumentsui.obs;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.visitdocumentsui.VisitDocumentsConstants;

public class DocumentComplexData extends ComplexData_2_0 {

	protected final Log log = LogFactory.getLog(getClass());
	private static final long serialVersionUID = 1L;
	
	private String instructions = ValueComplex.INSTRUCTIONS_NONE;
	
	/** 
	 * @param instructions Custom instructions to be processed by {@link DefaultDocumentHandler#DefaultDocumentHandler() DefaultDocumentHandler}
	 * @param mimeType Same as HTTP content type, @see <a href="http://www.sitepoint.com/web-foundations/mime-types-complete-list/"/>
	 */
	public DocumentComplexData(String instructions, String title, Object data, String mimeType) {
		super(title, data);
		if (!StringUtils.isEmpty(mimeType)) {
         this.setMimeType(mimeType);
		}
		else {
		   this.setMimeType(VisitDocumentsConstants.UNKNOWN_MIME_TYPE);
		}
		if (!StringUtils.isEmpty(instructions))
			this.instructions = instructions;
	}
	
	/**
	 * Constructor defaulting to parent's behaviour.
	 * @param stream The parent's data argument.
	 */
	public DocumentComplexData(String title, Object data) {
		this("", title, data, "");
	}

	public String getInstructions() {
		return instructions;
	}
}