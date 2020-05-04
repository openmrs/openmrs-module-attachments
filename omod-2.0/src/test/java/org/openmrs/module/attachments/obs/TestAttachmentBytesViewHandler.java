package org.openmrs.module.attachments.obs;

import static org.openmrs.module.attachments.obs.ValueComplex.INSTRUCTIONS_DEFAULT;

import org.openmrs.Obs;
import org.openmrs.obs.ComplexData;
import org.openmrs.obs.handler.BinaryDataHandler;

public class TestAttachmentBytesViewHandler extends AbstractAttachmentHandler {
	
	public static final String DEFAULT_VIEW = "VIEW_1";
	
	public static final String DEFAULT_VIEW_DATA = "This is a string content for VIEW_1.";
	
	public static final String ALTERNATE_VIEW = "VIEW_2";
	
	public static final String ALTERNATE_VIEW_DATA = "This is a string content for VIEW_2.";
	
	private static final String[] supportedViews = { DEFAULT_VIEW, ALTERNATE_VIEW };
	
	@Override
	protected void setParentComplexObsHandler() {
		setParent(new BinaryDataHandler());
	}
	
	@Override
	protected ComplexData readComplexData(Obs obs, ValueComplex valueComplex, String view) {
		
		if (ALTERNATE_VIEW.equals(view)) {
			return new AttachmentComplexData2_0(INSTRUCTIONS_DEFAULT, "extra_view", ALTERNATE_VIEW_DATA, "text/plain");
		}
		
		return new AttachmentComplexData2_0(INSTRUCTIONS_DEFAULT, "default_view", DEFAULT_VIEW_DATA, "text/plain");
		
	}
	
	@Override
	protected boolean deleteComplexData(Obs obs, AttachmentComplexData complexData) {
		return false;
	}
	
	@Override
	protected ValueComplex saveComplexData(Obs obs, AttachmentComplexData complexData) {
		return null;
	}
	
	@Override
	public String[] getSupportedViews() {
		return supportedViews;
	}
}
