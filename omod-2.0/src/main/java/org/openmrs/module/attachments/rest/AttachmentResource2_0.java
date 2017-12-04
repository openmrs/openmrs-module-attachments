package org.openmrs.module.attachments.rest;

import org.hibernate.FlushMode;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.attachments.obs.Attachment;
import org.openmrs.module.emrapi.db.DbSessionUtil;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;

@Resource(name = RestConstants.VERSION_1 + "/attachment", supportedClass = Attachment.class, supportedOpenmrsVersions = {
        "2.0.*" })
public class AttachmentResource2_0 extends AttachmentResource1_10 {
	
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
}
