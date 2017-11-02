package org.openmrs.module.attachments.obs;

import org.apache.commons.lang.StringUtils;
import org.openmrs.module.attachments.AttachmentsConstants;
import org.openmrs.obs.ComplexData;

public class DocumentComplexData2_0 extends BaseComplexData implements DocumentComplexData {
   
   private static final long serialVersionUID = 1L;

   private String instructions = ValueComplex.INSTRUCTIONS_NONE;
   
   /** 
    * @param instructions Custom instructions to be processed by {@link DefaultDocumentHandler#DefaultDocumentHandler() DefaultDocumentHandler}
    * @param mimeType Same as HTTP content type, @see <a href="http://www.sitepoint.com/web-foundations/mime-types-complete-list/"/>
    */
   public DocumentComplexData2_0(String instructions, String title, Object data, String mimeType) {
      super(title, data);
      if (!StringUtils.isEmpty(mimeType)) {
         this.setMimeType(mimeType);
      }
      else {
         this.setMimeType(AttachmentsConstants.UNKNOWN_MIME_TYPE);
      }
      if (!StringUtils.isEmpty(instructions))
         this.instructions = instructions;
   }
   
   public DocumentComplexData2_0(String title, Object data) {
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
}
