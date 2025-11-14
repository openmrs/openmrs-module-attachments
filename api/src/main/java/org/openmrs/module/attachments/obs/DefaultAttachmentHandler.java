package org.openmrs.module.attachments.obs;

import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.attachments.AttachmentsConstants;
import org.openmrs.obs.ComplexData;
import org.openmrs.obs.ComplexObsHandler;
import org.openmrs.obs.handler.BinaryDataHandler;
import org.springframework.beans.factory.annotation.Autowired;

public class DefaultAttachmentHandler extends AbstractAttachmentHandler {

	@Autowired
	protected BinaryDataHandler binaryDataHandler;

	public DefaultAttachmentHandler() {
		super();
	}

	@Override
	protected ComplexObsHandler getParent() {
		return binaryDataHandler;
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
			tmpObs = getParent().getObs(tmpObs, AttachmentsConstants.BINARYDATA_HANDLER_VIEW); // BinaryDataHan doesn't
																								// handle several views
			complexData = tmpObs.getComplexData();
		}

		// Then we build our own custom complex data
		return getComplexDataHelper().build(valueComplex.getInstructions(), complexData.getTitle(),
				complexData.getData(), valueComplex.getMimeType()).asComplexData();
	}

	protected boolean deleteComplexData(Obs obs) {
		return getParent().purgeComplexData(obs);
	}

	protected ValueComplex saveComplexData(Obs obs, AttachmentComplexData complexData) {
		// We invoke the parent to inherit from the file saving routines.
		obs = getParent().saveObs(obs);
		return new ValueComplex(complexData.getInstructions(), complexData.getMimeType(), obs.getValueComplex());
	}
}
