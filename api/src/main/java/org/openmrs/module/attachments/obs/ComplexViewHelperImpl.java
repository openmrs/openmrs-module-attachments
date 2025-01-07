package org.openmrs.module.attachments.obs;

import org.openmrs.Obs;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.attachments.AttachmentsConstants;
import org.springframework.stereotype.Component;

@Component(AttachmentsConstants.COMPONENT_COMPLEXVIEW_HELPER)
@OpenmrsProfile(openmrsPlatformVersion = "2.2.* - 9.*")
public class ComplexViewHelperImpl implements ComplexViewHelper {

	public String getView(Obs obs, String view) {

		return view;
	}
}
