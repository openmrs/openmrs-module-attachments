package org.openmrs.module.attachments.obs;

import org.openmrs.obs.handler.BinaryDataHandler;

public class DefaultAttachmentHandler2_0 extends DefaultAttachmentHandler {
	
	public DefaultAttachmentHandler2_0() {
		super();
	}
	
	protected void setParentComplexObsHandler() {
		setParent(new BinaryDataHandler());
	}
}
