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
import org.openmrs.module.visitdocumentsui.VisitDocumentsConstants;
import org.openmrs.module.visitdocumentsui.VisitDocumentsContext;
import org.openmrs.module.visitdocumentsui.fragment.controller.ClientConfigFragmentController;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.CustomRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.InjectBeans;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

@Component
public class VisitDocumentsPageController {
	
	public void controller(
			@RequestParam("patient") Patient patient,
			@RequestParam(value = "visit", required = false) Visit visit,
			UiSessionContext sessionContext,
			UiUtils ui,
			@InjectBeans VisitDocumentsContext context,
			@SpringBean DomainWrapperFactory domainWrapperFactory,
			PageModel model)
	{
		//
		// The client-side config specific to the page
		//
		Map<String, Object> jsonConfig = ClientConfigFragmentController.getClientConfig(context, ui);
		jsonConfig.put("patient", convertToRef(patient));

		VisitDomainWrapper visitWrapper = getVisitDomainWrapper(domainWrapperFactory, patient, visit, context.getAdtService(), sessionContext.getSessionLocation());
		jsonConfig.put("visit", visitWrapper == null ? null : convertVisit(visitWrapper.getVisit()));

		jsonConfig.put("contentFamilyMap", getContentFamilyMap());

		model.put("jsonConfig", ui.toJson(jsonConfig));


		// For Core Apps's patient header.
		model.put("patient", patient);
	}
	
	protected VisitDomainWrapper getVisitDomainWrapper(DomainWrapperFactory domainWrapperFactory, Patient patient, Visit visit, AdtService adtService, Location sessionLocation) {
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
	
	protected Object convertVisit(Object object) {
		return object == null ? null : ConversionUtil.convertToRepresentation(object, new CustomRepresentation(VisitDocumentsConstants.REPRESENTATION_VISIT));
	}

	protected Object convertToRef(Object object) {
		return object == null ? null : ConversionUtil.convertToRepresentation(object, Representation.REF);
	}
}