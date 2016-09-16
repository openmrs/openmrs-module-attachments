package org.openmrs.module.visitdocumentsui.obs;

import org.apache.commons.lang.StringUtils;
import org.openmrs.module.visitdocumentsui.VisitDocumentsConstants;
import org.openmrs.obs.ComplexData;

public class DocumentComplexData1_11 extends BaseComplexData implements DocumentComplexData {
   
   private static final long serialVersionUID = 1L;

   private String instructions = ValueComplex.INSTRUCTIONS_NONE;
   private String mimeType = VisitDocumentsConstants.UNKNOWN_MIME_TYPE;
   
   /** 
    * @param instructions Custom instructions to be processed by {@link DefaultDocumentHandler#DefaultDocumentHandler() DefaultDocumentHandler}
    * @param mimeType Same as HTTP content type, @see <a href="http://www.sitepoint.com/web-foundations/mime-types-complete-list/"/>
    */
   public DocumentComplexData1_11(String instructions, String title, Object data, String mimeType) {
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
   
   public DocumentComplexData1_11(String title, Object data) {
      this("", title, data, "");
   }

   @Override
   public String getInstructions() {
      return instructions;
   }

   @Override
   public ComplexData asComplexData() {
      return this;
   }
   
   private void setMimeType(String mimeType) {
      this.mimeType = mimeType;
   }

   @Override
   public String getMimeType() {
      return this.mimeType;
   }
}
