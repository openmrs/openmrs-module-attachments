package org.openmrs.module.attachments.obs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openmrs.Obs;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.attachments.AttachmentsConstants;
import org.openmrs.module.attachments.AttachmentsContext;
import org.openmrs.obs.ComplexObsHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component(AttachmentsConstants.COMPONENT_COMPLEXVIEW_HELPER)
@OpenmrsProfile(openmrsPlatformVersion = "2.0.0")
public class ComplexViewHelper2_0 implements ComplexViewHelper {
	
	@Autowired
	@Qualifier(AttachmentsConstants.COMPONENT_ATT_CONTEXT)
	protected AttachmentsContext context;
	
	private static final Logger log = LoggerFactory.getLogger(ComplexViewHelper2_0.class);
	
	@Override
	public String getView(Obs obs, String view) {
		// TODO: Obs obs will help the 2.x implementation support fetching the
		// supported views for that obs,
		// See:
		// https://issues.openmrs.org/browse/ATT-34
		
		ComplexObsHandler complexObsHandler = context.getObsService().getHandler(obs);
		
		if (complexObsHandler.supportsView(view)) {
			return view;
		} else {
			log.warn("Supported view for this Obs not found.Obs will now use the default view");
			return ComplexObsHandler.RAW_VIEW;
		}
		
		/*
		 * String[] supportedViews = complexObsHandler.getSupportedViews();
		 * 
		 * for (int x = 0; x < supportedViews.length; x++) { if
		 * (supportedViews[x].equalsIgnoreCase(view)) { return view; } } log.
		 * warn("Supported view for this Obs not found.Obs will now use the default view"
		 * ); return ComplexObsHandler.RAW_VIEW;
		 */
	}
}
