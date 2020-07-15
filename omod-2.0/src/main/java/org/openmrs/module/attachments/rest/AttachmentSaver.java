package org.openmrs.module.attachments.rest;

import org.hibernate.FlushMode;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.attachments.AttachmentsConstants;
import org.openmrs.module.attachments.obs.Attachment;
import org.openmrs.module.emrapi.db.DbSessionUtil;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * A transactional snippet to save an attachment.
 */
@Component(AttachmentsConstants.COMPONENT_ATTACHMENT_SAVER)
public class AttachmentSaver {
	
	@Transactional(readOnly = false)
	public Attachment save(Attachment delegate) {
		FlushMode flushMode = DbSessionUtil.getCurrentFlushMode();
		DbSessionUtil.setManualFlushMode();
		Attachment attachment = new Attachment();
		try {
			Obs obs = Context.getObsService().saveObs(delegate.getObs(), AttachmentResource1_10.REASON);
			attachment = new Attachment(obs);
		}
		finally {
			DbSessionUtil.setFlushMode(flushMode);
		}
		return attachment;
	}
}
