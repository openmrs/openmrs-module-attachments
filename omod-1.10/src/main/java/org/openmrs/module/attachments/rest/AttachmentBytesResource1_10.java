package org.openmrs.module.attachments.rest;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.attachments.AttachmentsConstants;
import org.openmrs.module.attachments.AttachmentsContext;
import org.openmrs.module.attachments.obs.AttachmentComplexData;
import org.openmrs.module.attachments.obs.ValueComplex;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.response.GenericRestException;
import org.openmrs.module.webservices.rest.web.response.IllegalRequestException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.openmrs.obs.ComplexData;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.openmrs.module.attachments.AttachmentsContext.getContentFamily;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/" + AttachmentsConstants.ATTACHMENT_URI)
public class AttachmentBytesResource1_10 extends BaseRestController {
	
	protected AttachmentsContext context = Context.getRegisteredComponent(AttachmentsConstants.COMPONENT_ATT_CONTEXT,
	    AttachmentsContext.class);
	
	protected final Log log = LogFactory.getLog(getClass());
	
	@RequestMapping(value = AttachmentsConstants.ATTACHMENT_BYTES_URI, method = RequestMethod.GET)
	public void getFile(@PathVariable("uuid") String uuid, HttpServletResponse response) throws ResponseException {
		// Getting the Core/Platform complex data object
		Obs obs = context.getObsService().getObsByUuid(uuid);
		
		if (!obs.isComplex()) {
			throw new IllegalRequestException("The following obs is not a complex obs, no complex data can be retrieved. "
			        + "Obs UUID: " + obs.getUuid());
		}
		
		Obs complexObs = Context.getObsService().getComplexObs(obs.getObsId(), null);
		ComplexData complexData = complexObs.getComplexData();
		
		// Switching to our complex data object
		ValueComplex valueComplex = new ValueComplex(obs.getValueComplex());
		AttachmentComplexData attComplexData = context.getComplexDataHelper().build(valueComplex.getInstructions(),
		    complexData);
		
		String mimeType = attComplexData.getMimeType();
		
		// The attachment meta data is sent as HTTP headers.
		response.setContentType(mimeType);
		response.addHeader("Content-Family", getContentFamily(mimeType).name());
		response.addHeader("File-Name", attComplexData.getTitle());
		response.addHeader("File-Ext", getExtension(attComplexData.getTitle(), mimeType));
		
		try {
			response.getOutputStream().write(attComplexData.asByteArray());
		}
		catch (IOException ex) {
			response.setStatus(500);
			throw new GenericRestException("There was an error when downloading the attachment's bytes content."
			        + " Perhaps the file content is corrupted.", ex);
		}
		
	}
	
	public static String getExtension(String fileName, String mimeType) {
		String ext = FilenameUtils.getExtension(fileName);
		String extFromMimeType = AttachmentsContext.getExtension(mimeType);
		if (!org.apache.commons.lang.StringUtils.isEmpty(ext)) {
			if (ext.length() > 6) { // this is a bit arbitrary, just to discriminate funny named files such as
				// "uiohdz.iuhezuidhuih"
				ext = extFromMimeType;
			}
		} else {
			ext = extFromMimeType;
		}
		return ext;
	}
}
