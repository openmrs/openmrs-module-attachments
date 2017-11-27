package org.openmrs.module.attachments.obs;

import org.openmrs.obs.ComplexData;

/**
 * <i>Our</i> implementation of {@link ComplexData}. This will either wrap of inherit ComplexData
 * and expose the methods that are needed in the context of our module.
 * 
 * @author Mekom Solutions
 */
public interface AttachmentComplexData {
	
	public ComplexData asComplexData();
	
	public byte[] asByteArray();
	
	public String getTitle();
	
	public Object getData();
	
	public String getMimeType();
	
	public String getInstructions();
	
}
