package org.openmrs.module.attachments.obs;

import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.attachments.AttachmentsConstants;
import org.openmrs.obs.ComplexData;
import org.springframework.stereotype.Component;

@Component(AttachmentsConstants.COMPONENT_COMPLEXDATA_HELPER)
@OpenmrsProfile(openmrsPlatformVersion = "2.*")
public class ComplexDataHelper2_0 implements ComplexDataHelper {
	
	@Override
	public AttachmentComplexData build(String instructions, String title, Object data, String mimeType) {
		return new AttachmentComplexData2_0(instructions, title, data, mimeType);
	}
	
	@Override
	public AttachmentComplexData build(String instructions, ComplexData complexData) {
		return build(instructions, complexData.getTitle(), complexData.getData(), complexData.getMimeType());
	}
	
	@Override
	public String getContentType(ComplexData complexData) {
		return complexData.getMimeType();
	}
}
