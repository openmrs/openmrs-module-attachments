package org.openmrs.module.visitdocumentsui.fragment.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.visitdocumentsui.VisitDocumentsConstants;
import org.openmrs.module.visitdocumentsui.VisitDocumentsContext;
import org.openmrs.module.visitdocumentsui.obs.ImageDocumentHandler;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.InjectBeans;

public class ClientConfigFragmentController {

   protected final Log log = LogFactory.getLog(getClass());

   /**
    * Static client config builder to be available for other controllers.
    */
   public static Map<String, Object> getClientConfig(VisitDocumentsContext context, UiUtils ui) {
      Map<String, Object> jsonConfig = new LinkedHashMap<String, Object>();
      
      jsonConfig.put("uploadUrl", "/" + ui.contextPath() + "/ws" + VisitDocumentsConstants.UPLOAD_DOCUMENT_URL);
      jsonConfig.put("downloadUrl", "/" + ui.contextPath() + "/ws" + VisitDocumentsConstants.DOWNLOAD_DOCUMENT_URL);
      jsonConfig.put("originalView", VisitDocumentsConstants.DOC_VIEW_ORIGINAL);
      jsonConfig.put("thumbView", VisitDocumentsConstants.DOC_VIEW_THUMBNAIL);
      
      jsonConfig.put("conceptComplexUuidList", context.getConceptComplexList());
      
      jsonConfig.put("thumbSize", ImageDocumentHandler.THUMBNAIL_HEIGHT);
      jsonConfig.put("maxFileSize", context.getMaxUploadFileSize());
      jsonConfig.put("allowNoCaption", context.doAllowEmptyCaption());
      
      jsonConfig.put("obsRep", "custom:" + VisitDocumentsConstants.REPRESENTATION_OBS);
      
      return jsonConfig;
   }
   
   /**
    * The controller method to be invoked from the client-side.
    * For instance with JavaScript 'emr.getFragmentActionWithCallback(...)'
    * @return The JSON config
    */
   public Object get(@InjectBeans VisitDocumentsContext context, UiUtils ui) {
      return getClientConfig(context, ui);
   }
}
