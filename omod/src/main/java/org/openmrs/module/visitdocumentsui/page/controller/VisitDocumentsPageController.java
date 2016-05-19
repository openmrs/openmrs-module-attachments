package org.openmrs.module.visitdocumentsui.page.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.visitdocumentsui.VisitDocumentsConstants;
import org.openmrs.module.visitdocumentsui.VisitDocumentsContext;
import org.openmrs.module.visitdocumentsui.obs.PatientImageHandler;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.InjectBeans;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

public class VisitDocumentsPageController {

	public void controller(
			@RequestParam("patient") Patient patient,
			@RequestParam("visit") Visit visit,
			UiSessionContext sessionContext,
			UiUtils ui,
			@InjectBeans VisitDocumentsContext context,
			PageModel model)
	{

		Map<String, Object> jsonConfig = new LinkedHashMap<String, Object>();
		
		jsonConfig.put("patient", convertToFull(patient));
		
		jsonConfig.put("visit", convertToFull(visit));
		
		jsonConfig.put("uploadUrl", "/ws" + VisitDocumentsConstants.UPLOAD_DOCUMENT_URL);
		jsonConfig.put("downloadUrl", "/ws" + VisitDocumentsConstants.DOWNLOAD_IMAGE_URL);
		jsonConfig.put("originalView", VisitDocumentsConstants.DOC_VIEW_ORIGINAL);
		jsonConfig.put("thumbView", VisitDocumentsConstants.DOC_VIEW_THUMBNAIL);
		
		jsonConfig.put("conceptComplexUuid", context.getConceptComplex().getUuid());
		
		jsonConfig.put("thumbSize", PatientImageHandler.THUMBNAIL_HEIGHT);
		jsonConfig.put("maxFileSize", context.getMaxUploadFileSize());
		
		jsonConfig.put("obsRep", "custom:" + VisitDocumentsConstants.REPRESENTATION_OBS);
		
		model.put("jsonConfig", ui.toJson(jsonConfig));
		
		// For Core Apps's patient header.
		model.put("patient", patient);
	}
	
    private Object convertToFull(Object object) {
        return object == null ? null : ConversionUtil.convertToRepresentation(object, Representation.FULL);
    }
}