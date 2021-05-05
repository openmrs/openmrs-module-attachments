package org.openmrs.module.attachments.fragment.controller;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.PersonName;
import org.openmrs.module.attachments.AttachmentsContext;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.FragmentParam;
import org.openmrs.ui.framework.annotation.InjectBeans;
import org.openmrs.ui.framework.fragment.FragmentModel;

public class DashboardWidgetFragmentController {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	public void controller(FragmentModel model, @FragmentParam("patient") PatientDomainWrapper patientWrapper, UiUtils ui,
	        @InjectBeans AttachmentsContext context) {
		
		// Ensure name is HTML-safe to prevent XSS in info messages
		PersonName htmlSafePersonName = new PersonName(ui.escapeHtml(patientWrapper.getPatient().getGivenName()),
		        ui.escapeHtml(patientWrapper.getPatient().getMiddleName()),
		        ui.escapeHtml(patientWrapper.getPatient().getFamilyName()));
		
		patientWrapper.getPatient().removeName(patientWrapper.getPersonName());
		patientWrapper.getPatient().addName(htmlSafePersonName);
		
		Map<String, Object> jsonConfig = ClientConfigFragmentController.getClientConfig(context, ui);
		jsonConfig.put("patient", ConversionUtil.convertToRepresentation(patientWrapper.getPatient(), Representation.REF));
		jsonConfig.put("thumbnailCount", context.getDashboardThumbnailCount());
		model.addAttribute("jsonConfig", ui.toJson(jsonConfig));
	}
}
