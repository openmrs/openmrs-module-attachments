package org.openmrs.module.visitdocumentsui.obs;

import static org.openmrs.module.visitdocumentsui.VisitDocumentsContext.isMimeTypeHandled;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import org.apache.commons.lang.ArrayUtils;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.visitdocumentsui.VisitDocumentsConstants;
import org.openmrs.obs.ComplexData;
import org.springframework.stereotype.Component;

@Component(VisitDocumentsConstants.COMPONENT_COMPLEXDATA_HELPER)
@OpenmrsProfile(openmrsPlatformVersion = "1.11.4 - 1.12.*")
public class ComplexDataHelper1_11 implements ComplexDataHelper {
   
   @Override
   public DocumentComplexData build(String instructions, String title, Object data, String mimeType) {
      return new DocumentComplexData1_11(instructions, title, data, mimeType);
   }
   
   @Override
   public DocumentComplexData build(String instructions, ComplexData complexData) {
      return build(instructions, complexData.getTitle(), complexData.getData(), getContentType(complexData));
   }

   @Override
   public String getContentType(ComplexData complexData) {
      
      if (complexData instanceof DocumentComplexData) {  // In case it's our module's implementation
         DocumentComplexData docComplexData = (DocumentComplexData) complexData;
         if (isMimeTypeHandled(docComplexData.getMimeType()))   // Perhaps too restrictive
            return docComplexData.getMimeType();
      }
      
      byte[] bytes = BaseComplexData.getByteArray(complexData);
      if (ArrayUtils.isEmpty(bytes))
         return VisitDocumentsConstants.UNKNOWN_MIME_TYPE;
      
      // guessing the content type
      InputStream stream = new BufferedInputStream(new ByteArrayInputStream(bytes));
      try {
         String mimeType = URLConnection.guessContentTypeFromStream(stream); 
         return mimeType == null ? VisitDocumentsConstants.UNKNOWN_MIME_TYPE : mimeType;
      } catch (IOException e) {
         return VisitDocumentsConstants.UNKNOWN_MIME_TYPE;
      }
   }
}