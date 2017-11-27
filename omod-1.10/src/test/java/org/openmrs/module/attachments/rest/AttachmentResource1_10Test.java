package org.openmrs.module.attachments.rest;

import org.junit.Test;
import org.openmrs.module.attachments.obs.Attachment;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;

public class AttachmentResource1_10Test extends BaseDelegatingResourceTest<AttachmentResource1_10, Attachment> {
	
	private Attachment doc = new Attachment();
	
	@Override
	public Attachment newObject() {
		return doc;
	}
	
	@Override
	public String getDisplayProperty() {
		return null;
	}
	
	@Override
	public String getUuidProperty() {
		return doc.getUuid();
	}
	
	@Test
	public void shouldLoadResource() throws Exception {
		AttachmentResource1_10 resource = getResource();
	}
}
