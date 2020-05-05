package org.openmrs.module.attachments.obs;

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
	
	@Autowired
	private ObsService obsService;
	
	@Override
	public String getView(Obs obs, String view) {
		
		if (obsService.getHandler(obs).supportsView(view)) {
			return view;
		}
		
		return ComplexObsHandler.RAW_VIEW;
		
	}
}
