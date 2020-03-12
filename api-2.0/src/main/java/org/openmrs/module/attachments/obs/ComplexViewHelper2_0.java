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
		// Obs obs will help the 2.x implementation support fetching the
		// supported views for that obs,
		// using for example:
		// https://github.com/openmrs/openmrs-core/blob/7da5be1bc34fc4928779f303cd48d42b8a3cae0a/api/src/main/java/org/openmrs/api/ObsService.java#L417-L428
		// and AbstractHandler derived handlers method getSupportedViews()
		
		return ComplexObsHandler.RAW_VIEW;
	}
}
