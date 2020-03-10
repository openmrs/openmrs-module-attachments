package org.openmrs.module.attachments.obs;

import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.attachments.AttachmentsConstants;
import org.openmrs.obs.ComplexObsHandler;
import org.springframework.stereotype.Component;

@Component(AttachmentsConstants.COMPONENT_COMPLEXVIEW_HELPER)
@OpenmrsProfile(openmrsPlatformVersion = "1.*")
public class ComplexViewHelper1_10 implements ComplexViewHelper {
	
	public String getView(String view) {
		
		return view;
	}
}
