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

import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.obs.ComplexData;
import org.openmrs.web.servlet.ComplexObsServlet;

public class PatientImageComplexData extends ComplexData {

	protected final Log log = LogFactory.getLog(getClass());
	private static final long serialVersionUID = 1L;
	
	public static final String INSTRUCTIONS_NONE = "instructions.none";
	public static final String INSTRUCTIONS_DEFAULT = "instructions.default";
	public static final String UNKNOWN_MIME_TYPE = "application/unknown";
	
	public static final String VIEW_ORIGINAL = "complexdata.view.original";
	public static final String VIEW_THUMBNAIL = "complexdata.view.thumbnail";

	private String instructions = INSTRUCTIONS_NONE;
	private String mimeType = UNKNOWN_MIME_TYPE;
	
	/** 
	 * @param instructions Custom instructions to be processed by {@link PatientImageHandler#PatientImageHandler() PatientImageHandler}
	 * @param mimeType Same as HTTP content type, @see <a href="http://www.sitepoint.com/web-foundations/mime-types-complete-list/"/>
	 */
	public PatientImageComplexData(String instructions, String title, Object data, String mimeType) {
		super(title, data);
		if (!StringUtils.isEmpty(instructions))
			this.instructions = instructions;
		if (!StringUtils.isEmpty(mimeType))
			this.mimeType = mimeType;
	}
	
	/**
	 * Constructor defaulting to parent's behaviour.
	 * @param stream The parent's data argument.
	 */
	public PatientImageComplexData(String title, Object data) {
		this("", title, data, UNKNOWN_MIME_TYPE);
	}

	public String getInstructions() {
		return instructions;
	}

	public String getMimeType() {
		return mimeType;
	}
	
	/**
	 * This returns the image's byte array out of the complex data's inner data.
	 * This is borrowed from {@link ComplexObsServlet} and would therefore work outside of our implementation.
	 * @return The image's byte array, or an empty array if an error occurred.
	 * @throws IOException 
	 */
	public static byte[] getByteArray(ComplexData complexData)
	{
		Object data = complexData.getData();
		
		if (data == null) {
			return new byte[0];
		}
		if (data instanceof byte[]) {
			return (byte[]) data;			
		}
		else if (RenderedImage.class.isAssignableFrom(data.getClass())) {
			RenderedImage image = (RenderedImage) data;

			ByteArrayOutputStream bytesOutStream = new ByteArrayOutputStream();
			try {
			ImageOutputStream imgOutStream = ImageIO.createImageOutputStream(bytesOutStream);
			String extension = FilenameUtils.getExtension(complexData.getTitle());
			ImageIO.write(image, extension, imgOutStream);
			imgOutStream.close();
			}
			catch (IOException e) {
				return new byte[0];
			}
			
			return bytesOutStream.toByteArray();
		}
		else if (InputStream.class.isAssignableFrom(data.getClass())) {
			try {
				return IOUtils.toByteArray((InputStream) data);
			} catch (IOException e) {
				return new byte[0];
			}
		}
		else {
			return new byte[0];
		}
	}
}