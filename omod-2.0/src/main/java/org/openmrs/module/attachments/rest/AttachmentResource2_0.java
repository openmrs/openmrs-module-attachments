package org.openmrs.module.attachments.rest;

import org.openmrs.api.context.Context;
import org.openmrs.module.attachments.AttachmentsService;
import org.openmrs.module.attachments.obs.Attachment;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;

@Resource(name = RestConstants.VERSION_1 + "/attachment", supportedClass = Attachment.class, supportedOpenmrsVersions = {
        "2.0.0" })
public class AttachmentResource2_0 extends AttachmentResource1_10 {
	
	@Override
	public Attachment save(Attachment delegate) {
		return Context.getService(AttachmentsService.class).save(delegate, AttachmentResource1_10.REASON);
	}
}
