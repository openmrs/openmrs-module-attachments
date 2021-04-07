package org.openmrs.module.attachments.rest;

import org.junit.Before;
import org.mockito.Mock;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.module.attachments.AttachmentsService;
import org.openmrs.module.attachments.obs.Attachment;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;
import org.springframework.beans.factory.annotation.Autowired;

public class AttachmentResource2_0Test extends BaseDelegatingResourceTest<AttachmentResource2_0, Attachment> {
	
	private static final String ATTACHMENTRESOURCE_UUID = "9b6639b2-5785-4603-a364-075c2d61cd51";
	
	@Mock
	AttachmentsService attachmentService;
	
	Attachment attachment;
	
	@Autowired
	private ObsService obsService;
	
	@Mock
	RequestContext requestContext;
	
	@Before
	public void before() throws Exception {
		executeDataSet("org/openmrs/api/include/ObsServiceTest-complex.xml");
	}
	
	@Override
	public String getDisplayProperty() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String getUuidProperty() {
		return ATTACHMENTRESOURCE_UUID;
	}
	
	@Override
	public Attachment newObject() {
		return new Attachment(Context.getObsService().getObsByUuid(ATTACHMENTRESOURCE_UUID));
		
	}
	
	@Override
	public void validateDefaultRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		assertPropEquals("uuid", getObject().getUuid());
		assertPropEquals("dateTime", getObject().getDateTime());
		assertPropEquals("comment", getObject().getComment());
		assertPropEquals("complexData", getObject().getComplexData());
		
	}
	
	@Override
	public void validateFullRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		
		assertPropEquals("uuid", getObject().getUuid());
		assertPropEquals("dateTime", getObject().getDateTime());
		assertPropEquals("comment", getObject().getComment());
		assertPropEquals("datechanged", getObject().getDateChanged());
		assertPropEquals("complexData", getObject().getComplexData());
		
	}
	
}
