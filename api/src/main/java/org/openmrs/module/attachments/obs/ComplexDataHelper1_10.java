package org.openmrs.module.attachments.obs;

import static org.openmrs.module.attachments.AttachmentsContext.isMimeTypeHandled;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import org.apache.commons.lang.ArrayUtils;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.attachments.AttachmentsConstants;
import org.openmrs.obs.ComplexData;
import org.springframework.stereotype.Component;

@Component(AttachmentsConstants.COMPONENT_COMPLEXDATA_HELPER)
@OpenmrsProfile(openmrsPlatformVersion = "1.10.2 - 1.12.*")
public class ComplexDataHelper1_10 implements ComplexDataHelper {
	
	@Override
	public AttachmentComplexData build(String instructions, String title, Object data, String mimeType) {
		return new AttachmentComplexData1_10(instructions, title, data, mimeType);
	}
	
	@Override
	public AttachmentComplexData build(String instructions, ComplexData complexData) {
		return build(instructions, complexData.getTitle(), complexData.getData(), getContentType(complexData));
	}
	
	@Override
	public String getContentType(ComplexData complexData) {
		
		if (complexData instanceof AttachmentComplexData) { // In case it's our module's implementation
			AttachmentComplexData attComplexData = (AttachmentComplexData) complexData;
			if (isMimeTypeHandled(attComplexData.getMimeType())) { // Perhaps too restrictive
				return attComplexData.getMimeType();
			}
		}
		
		byte[] bytes = BaseComplexData.getByteArray(complexData);
		if (ArrayUtils.isEmpty(bytes)) {
			return AttachmentsConstants.UNKNOWN_MIME_TYPE;
		}
		
		// guessing the content type
		InputStream stream = new BufferedInputStream(new ByteArrayInputStream(bytes));
		try {
			String mimeType = URLConnection.guessContentTypeFromStream(stream);
			return mimeType == null ? AttachmentsConstants.UNKNOWN_MIME_TYPE : mimeType;
		}
		catch (IOException e) {
			return AttachmentsConstants.UNKNOWN_MIME_TYPE;
		}
	}
}
