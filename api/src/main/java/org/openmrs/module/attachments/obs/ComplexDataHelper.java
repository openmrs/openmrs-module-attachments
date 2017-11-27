package org.openmrs.module.attachments.obs;

import org.openmrs.obs.ComplexData;

/**
 * Core/Platform independent helper interface to manipulate complex data.
 * 
 * @author Mekom Solutions
 */
public interface ComplexDataHelper {
	
	public AttachmentComplexData build(String instructions, String title, Object data, String mimeType);
	
	public AttachmentComplexData build(String instructions, ComplexData complexData);
	
	public String getContentType(ComplexData complexData);
	
}
