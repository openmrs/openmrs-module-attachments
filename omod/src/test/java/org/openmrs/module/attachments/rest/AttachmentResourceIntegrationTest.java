package org.openmrs.module.attachments.rest;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.attachments.obs.Attachment;
import org.openmrs.module.attachments.obs.TestHelper;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;
import org.springframework.beans.factory.annotation.Autowired;

public class AttachmentResourceIntegrationTest extends BaseDelegatingResourceTest<AttachmentResource, Attachment> {

	@Autowired
	protected TestHelper testHelper;

	private Obs obs;

	@BeforeEach
	public void before() throws Exception {
		testHelper.init();
		obs = testHelper.saveNormalSizeImageAttachment();
	}

	@AfterEach
	public void tearDown() throws IOException {
		testHelper.tearDown();
	}

	@Override
	public Attachment newObject() {
		return new Attachment(obs);
	}

	@Override
	public String getDisplayProperty() {
		return null;
	}

	@Override
	public String getUuidProperty() {
		return obs.getUuid();
	}

	@Override
	public void validateDefaultRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		assertPropEquals("dateTime", getObject().getDateTime());
		assertPropEquals("comment", getObject().getComment());
		assertPropEquals("bytesMimeType", getObject().getBytesMimeType());
		assertPropEquals("bytesContentFamily", getObject().getBytesContentFamily());
	}
}
