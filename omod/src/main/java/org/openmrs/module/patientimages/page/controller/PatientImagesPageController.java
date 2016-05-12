package org.openmrs.module.patientimages.page.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.patientimages.PatientImageComplexData;
import org.openmrs.module.patientimages.PatientImageHandler;
import org.openmrs.module.patientimages.PatientImagesConstants;
import org.openmrs.module.patientimages.PatientImagesContext;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.InjectBeans;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

public class PatientImagesPageController {

	public void controller(
			@RequestParam("patient") Patient patient,
			@RequestParam("visit") String visitUuid,
			UiSessionContext sessionContext,
			UiUtils ui,
			@InjectBeans PatientImagesContext context,
			PageModel model)
	{

		Map<String, Object> jsonConfig = new LinkedHashMap<String, Object>();
		
		jsonConfig.put("patient", convertToFull(patient));
		
		Visit visit = context.getVisitService().getVisitByUuid(visitUuid);
		jsonConfig.put("visit", convertToFull(visit));
		
		jsonConfig.put("uploadUrl", "/ws" + PatientImagesConstants.UPLOAD_IMAGE_URL);
		jsonConfig.put("downloadUrl", "/ws" + PatientImagesConstants.DOWNLOAD_IMAGE_URL);
		jsonConfig.put("originalView", PatientImageComplexData.VIEW_ORIGINAL);
		jsonConfig.put("thumbView",PatientImageComplexData.VIEW_THUMBNAIL);
		
		jsonConfig.put("conceptComplexUuid", context.getConceptComplex().getUuid());
		
		jsonConfig.put("thumbSize", PatientImageHandler.THUMBNAIL_HEIGHT);
		jsonConfig.put("maxFileSize", context.getMaxUploadFileSize());
		
		jsonConfig.put("obsRep", "custom:" + PatientImagesConstants.REPRESENTATION_OBS);
		
		model.put("jsonConfig", ui.toJson(jsonConfig));
		
		// For Core Apps's patient header.
		model.put("patient", patient);
	}
	
    private Object convertToFull(Object object) {
        return object == null ? null : ConversionUtil.convertToRepresentation(object, Representation.FULL);
    }
}