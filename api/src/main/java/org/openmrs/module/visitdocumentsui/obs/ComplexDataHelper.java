package org.openmrs.module.visitdocumentsui.obs;

import org.openmrs.obs.ComplexData;

/**
 * Core/Platform independent helper interface to manipulate complex data.
 * 
 * @author Mekom Solutions
 */
public interface ComplexDataHelper {

   public DocumentComplexData build(String instructions, String title, Object data, String mimeType);
   
   public DocumentComplexData build(String instructions, ComplexData complexData);
   
   public String getContentType(ComplexData complexData);
   
}