package org.openmrs.module.attachments.obs;

import static org.openmrs.obs.ComplexObsHandler.RAW_VIEW;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Obs;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.api.ObsService;
import org.openmrs.module.attachments.AttachmentsConstants;
import org.openmrs.obs.ComplexObsHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component(AttachmentsConstants.COMPONENT_COMPLEXVIEW_HELPER)
@OpenmrsProfile(openmrsPlatformVersion = "2.0.0")
public class ComplexViewHelper2_0 implements ComplexViewHelper {
	
	private final Log log = LogFactory.getLog(getClass());
	
	@Autowired
	private ObsService obsService;
	
	@Override
	public String getView(Obs obs, String view) {
		
		ComplexObsHandler handler = obsService.getHandler(obs);
		
		if (handler.supportsView(view)) {
			return view;
		} else {
			log.warn("The requested view '" + view + "' is not supported by the complex obs handler '"
			        + handler.getClass().getCanonicalName() + "', reverting to the default view '" + RAW_VIEW + "'.");
		}
		
		return RAW_VIEW;
		
	}
}
