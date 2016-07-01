package org.openmrs.module.visitdocumentsui.page.controller;

import static org.openmrs.module.visitdocumentsui.VisitDocumentsContext.getContentFamilyMap;

import java.util.Map;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
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
			UiSessionContext sessionContext,
			UiUtils ui,
			@InjectBeans VisitDocumentsContext context,
			PageModel model)
	{
	   //
	   // The client-side config specific to the page
	   //
		Map<String, Object> jsonConfig = ClientConfigFragmentController.getClientConfig(context, ui);
		jsonConfig.put("patient", convertToRef(patient));

		// Fetching the active visit, if any.
		VisitDomainWrapper visitWrapper = null;
	   AdtService adtService = context.getAdtService();
	   Location visitLocation = adtService.getLocationThatSupportsVisits(sessionContext.getSessionLocation());
	   visitWrapper = adtService.getActiveVisit(patient, visitLocation);
		jsonConfig.put("visit", visitWrapper == null ? "" : convertToRef(visitWrapper.getVisit()));
		
		jsonConfig.put("contentFamilyMap", getContentFamilyMap());
		
		model.put("jsonConfig", ui.toJson(jsonConfig));
		
		
		// For Core Apps's patient header.
		model.put("patient", patient);
	}
	
    private Object convertToRef(Object object) {
        return object == null ? null : ConversionUtil.convertToRepresentation(object, Representation.REF);
    }
}