package org.openmrs.module.attachments.obs;

import org.openmrs.Obs;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.api.context.Context;
import org.openmrs.module.attachments.AttachmentsConstants;
import org.openmrs.obs.ComplexObsHandler;
import org.springframework.stereotype.Component;

@Component(AttachmentsConstants.COMPONENT_COMPLEXVIEW_HELPER)
@OpenmrsProfile(openmrsPlatformVersion = "2.2.* - 9.*")
public class ComplexViewHelperImpl implements ComplexViewHelper {

	/**
	 * Complex obs whose concept uses a handler that does not support the requested
	 * view (eg. Core's BinaryDataHandler or ImageHandler, which return null for
	 * anything but RAW_VIEW) fall back to RAW_VIEW, so that their content can still
	 * be fetched as an attachment.
	 */
	public String getView(Obs obs, String view) {
		ComplexObsHandler handler = Context.getObsService().getHandler(obs);
		if (handler != null && !handler.supportsView(view)) {
			return ComplexObsHandler.RAW_VIEW;
		}
		return view;
	}
}
