package org.openmrs.module.visitdocumentsui.obs;

import org.openmrs.obs.ComplexData;

/**
 * This class brings all the complex data 2.0 features.
 * So it fills the gap between versions 1.11.x and 2.0.x the class. 
 */
/*
 * TODO This class should go when the module will depend on Platform 2.0
 */
public class ComplexData_2_0 extends ComplexData {
   
   private static final long serialVersionUID = 1L;

   private String mimeType;
   private Long length;
   
   public ComplexData_2_0(String title, Object data) {
      super(title, data);
   }
   
   public void setMimeType(String mimeType) {
      this.mimeType = mimeType;
   }
   
   /**
    * @return data MIME type
    */
   public String getMimeType() {
      return this.mimeType;
   }
   
   public void setLength(Long length) {
      this.length = length;
   }
   
   /**
    * @return data length
    */
   public Long getLength() {
      return this.length;
   }
}
