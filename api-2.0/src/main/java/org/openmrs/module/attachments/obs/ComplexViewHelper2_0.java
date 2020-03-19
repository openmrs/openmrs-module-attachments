package org.openmrs.module.attachments.obs;

import org.openmrs.Obs;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.attachments.AttachmentsConstants;
import org.openmrs.obs.ComplexObsHandler;
import org.springframework.stereotype.Component;

@Component(AttachmentsConstants.COMPONENT_COMPLEXVIEW_HELPER)
@OpenmrsProfile(openmrsPlatformVersion = "2.0.0")
public class ComplexViewHelper2_0 implements ComplexViewHelper {
	
	@Override
	public String getView(Obs obs, String view) {
		// TODO: Obs obs will help the 2.x implementation support fetching the
		// supported views for that obs,
		// See:
		// https://issues.openmrs.org/browse/ATT-34
		
		return ComplexObsHandler.RAW_VIEW;
	}
}
