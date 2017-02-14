package org.openmrs.module.visitdocumentsui.page.controller;

import static org.openmrs.module.visitdocumentsui.VisitDocumentsContext.getContentFamilyMap;

import java.util.Map;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.domainwrapper.DomainWrapperFactory;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.openmrs.module.visitdocumentsui.VisitDocumentsContext;
import org.openmrs.module.visitdocumentsui.fragment.controller.ClientConfigFragmentController;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.InjectBeans;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

@Component
public class VisitDocumentsPageController {
	
	@Autowired
	private DomainWrapperFactory domainWrapperFactory; 

	public void controller(
			@RequestParam("patient") Patient patient,
			@RequestParam(value = "visit", required = false) Visit visit,
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

		VisitDomainWrapper visitWrapper = getVisitDomainWrapper(patient, visit, context.getAdtService(), sessionContext.getSessionLocation());
		jsonConfig.put("visit", visitWrapper == null ? "" : convertToRef(visitWrapper.getVisit()));

		jsonConfig.put("contentFamilyMap", getContentFamilyMap());

		model.put("jsonConfig", ui.toJson(jsonConfig));


		// For Core Apps's patient header.
		model.put("patient", patient);
	}
	
	protected VisitDomainWrapper getVisitDomainWrapper(Patient patient, Visit visit, AdtService adtService, Location sessionLocation) {
		VisitDomainWrapper visitWrapper = null;
		if (visit == null) {
			// Fetching the active visit, if any.
			Location visitLocation = adtService.getLocationThatSupportsVisits(sessionLocation);
			visitWrapper = adtService.getActiveVisit(patient, visitLocation);
		}
		else {
			visitWrapper = domainWrapperFactory.newVisitDomainWrapper(visit);
		}
		return visitWrapper;
	}

	private Object convertToRef(Object object) {
		return object == null ? null : ConversionUtil.convertToRepresentation(object, Representation.REF);
	}
}