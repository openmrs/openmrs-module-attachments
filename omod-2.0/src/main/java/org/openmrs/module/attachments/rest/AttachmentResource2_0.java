package org.openmrs.module.attachments.rest;

import org.apache.commons.lang.StringUtils;
import org.hibernate.FlushMode;
import org.openmrs.*;
import org.openmrs.api.context.Context;
import org.openmrs.module.attachments.AttachmentsConstants;
import org.openmrs.module.attachments.AttachmentsContext;
import org.openmrs.module.attachments.ComplexObsSaver;
import org.openmrs.module.attachments.obs.Attachment;
import org.openmrs.module.attachments.obs.ValueComplex;
import org.openmrs.module.emrapi.db.DbSessionUtil;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.CustomRepresentation;
import org.openmrs.module.webservices.rest.web.resource.api.Uploadable;
import org.openmrs.module.webservices.rest.web.response.IllegalRequestException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.openmrs.module.attachments.AttachmentsContext.getContentFamily;

@Resource(name = RestConstants.VERSION_1 + "/attachment", supportedClass = Attachment.class, supportedOpenmrsVersions = {
        "2.0.*" })
public class AttachmentResource2_0 extends AttachmentResource1_10 implements Uploadable {
	
	ComplexObsSaver obsSaver = Context.getRegisteredComponent(AttachmentsConstants.COMPONENT_COMPLEXOBS_SAVER,
	    ComplexObsSaver.class);
	
	AttachmentsContext attachmentsContext = Context.getRegisteredComponent(AttachmentsConstants.COMPONENT_ATT_CONTEXT,
	    AttachmentsContext.class);
	
	@Override
	public Attachment save(Attachment delegate) {
		FlushMode flushMode = DbSessionUtil.getCurrentFlushMode();
		DbSessionUtil.setManualFlushMode();
		Attachment attachment = new Attachment();
		try {
			Obs obs = Context.getObsService().saveObs(delegate.getObs(), REASON);
			attachment = new Attachment(obs);
		}
		finally {
			DbSessionUtil.setFlushMode(flushMode);
		}
		return attachment;
	}
	
	@Override
	public Object upload(MultipartFile file, RequestContext context) throws ResponseException, IOException {
		
		// Prepare Parameters
		Patient patient = Context.getPatientService().getPatientByUuid(context.getParameter("patient"));
		Visit visit = Context.getVisitService().getVisitByUuid(context.getParameter("visit"));
		Provider provider = Context.getProviderService().getProviderByUuid(context.getParameter("provider"));
		String fileCaption = context.getParameter("fileCaption");
		String instructions = context.getParameter("instructions");
		
		// Verify Parameters
		if (patient == null) {
			throw new IllegalRequestException("A patient parameter must be provided when uploading an attachment.");
		}
		
		if (StringUtils.isEmpty(instructions))
			instructions = ValueComplex.INSTRUCTIONS_DEFAULT;
		
		Encounter encounter = null;
		if (visit != null) {
			encounter = attachmentsContext.getAttachmentEncounter(patient, visit, provider);
		}
		
		// Save Obs
		Obs obs;
		switch (getContentFamily(file.getContentType())) {
			case IMAGE:
				obs = obsSaver.saveImageAttachment(visit, patient, encounter, fileCaption, file, instructions);
				break;
			
			case OTHER:
			default:
				obs = obsSaver.saveOtherAttachment(visit, patient, encounter, fileCaption, file, instructions);
				break;
		}
		
		return ConversionUtil.convertToRepresentation(obs,
		    new CustomRepresentation(AttachmentsConstants.REPRESENTATION_OBS));
	}
}
