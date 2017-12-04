package org.openmrs.module.attachments.obs;

import java.io.File;

import org.openmrs.Obs;
import org.openmrs.module.attachments.AttachmentsConstants;
import org.openmrs.obs.ComplexData;
import org.openmrs.obs.handler.AbstractHandler;
import org.openmrs.obs.handler.BinaryDataHandler;

public class DefaultAttachmentHandler extends AbstractAttachmentHandler {
	
	public DefaultAttachmentHandler() {
		super();
	}
	
	protected void setParentComplexObsHandler() {
		setParent(new BinaryDataHandler());
	}
	
	protected ComplexData readComplexData(Obs obs, ValueComplex valueComplex, String view) {
		// We invoke the parent to inherit from the file reading routines.
		Obs tmpObs = new Obs();
		tmpObs.setValueComplex(valueComplex.getFileName()); // Temp obs used as a safety
		
		ComplexData complexData;
		if (view.equals(AttachmentsConstants.ATT_VIEW_THUMBNAIL)) {
			// This handler doesn't have data for thumbnails, we return a null content
			complexData = new ComplexData(valueComplex.getFileName(), null);
		} else {
			tmpObs = getParent().getObs(tmpObs, AttachmentsConstants.BINARYDATA_HANDLER_VIEW); // BinaryDataHandler
			                                                                                   // doesn't handle
			                                                                                   // several views
			complexData = tmpObs.getComplexData();
		}
		
		// Then we build our own custom complex data
		return getComplexDataHelper().build(valueComplex.getInstructions(), complexData.getTitle(), complexData.getData(),
		    valueComplex.getMimeType()).asComplexData();
	}
	
	protected boolean deleteComplexData(Obs obs, AttachmentComplexData complexData) {
		// We use a temp obs whose value complex points to the file name
		Obs tmpObs = new Obs();
		tmpObs.setValueComplex(complexData.asComplexData().getTitle()); // Temp obs used as a safety
		return getParent().purgeComplexData(tmpObs);
	}
	
	protected ValueComplex saveComplexData(Obs obs, AttachmentComplexData complexData) {
		// We invoke the parent to inherit from the file saving routines.
		obs = getParent().saveObs(obs);
		
		File savedFile = AbstractHandler.getComplexDataFile(obs);
		String savedFileName = savedFile.getName();
		
		return new ValueComplex(complexData.getInstructions(), complexData.getMimeType(), savedFileName);
	}
}
