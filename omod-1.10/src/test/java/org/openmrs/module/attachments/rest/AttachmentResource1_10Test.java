package org.openmrs.module.attachments.rest;

import org.junit.Before;
import org.openmrs.api.context.Context;
import org.openmrs.module.attachments.obs.Attachment;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;

public class AttachmentResource1_10Test extends BaseDelegatingResourceTest<AttachmentResource1_10, Attachment> {
	
	@Before
	public void before() throws Exception {
		executeDataSet("org/openmrs/api/include/ObsServiceTest-complex.xml");
	}
	
	@Override
	public Attachment newObject() {
		return new Attachment(Context.getObsService().getObsByUuid("9b6639b2-5785-4603-a364-075c2d61cd51"));
	}
	
	@Override
	public String getDisplayProperty() {
		return null;
	}
	
	@Override
	public String getUuidProperty() {
		return "9b6639b2-5785-4603-a364-075c2d61cd51";
	}
	
	@Override
	public void validateDefaultRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		
		assertPropEquals("dateTime", getObject().getDateTime());
		assertPropEquals("comment", getObject().getComment());
		assertPropEquals("complexData", getObject().getComplexData());
		assertPropEquals("bytesMimeType", getObject().getBytesMimeType());
		assertPropEquals("bytesContentFamily", getObject().getBytesContentFamily());
	}
}
