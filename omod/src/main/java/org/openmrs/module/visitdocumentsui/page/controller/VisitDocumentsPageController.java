package org.openmrs.module.visitdocumentsui.page.controller;

import java.util.Map;

import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.visitdocumentsui.VisitDocumentsContext;
import org.openmrs.module.visitdocumentsui.fragment.controller.ClientConfigFragmentController;
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
		Map<String, Object> jsonConfig = ClientConfigFragmentController.getClientConfig(context, ui);
		jsonConfig.put("patient", convertToFull(patient));
		jsonConfig.put("visit", convertToFull(visit));
		
		model.put("jsonConfig", ui.toJson(jsonConfig));
		model.put("patient", patient); // For Core Apps's patient header.
	}
	
    private Object convertToFull(Object object) {
        return object == null ? null : ConversionUtil.convertToRepresentation(object, Representation.FULL);
    }
}