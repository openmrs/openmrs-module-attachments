package org.openmrs.module.attachments.obs;

import java.awt.image.BufferedImage;
import java.io.IOException;

import net.coobird.thumbnailator.Thumbnails;
import org.openmrs.Obs;
import org.openmrs.module.attachments.AttachmentsService;
import org.openmrs.module.attachments.AttachmentsConstants;
import org.openmrs.obs.ComplexData;
import org.openmrs.obs.ComplexObsHandler;
import org.openmrs.obs.handler.ImageHandler;
import org.springframework.beans.factory.annotation.Autowired;

public class ImageAttachmentHandler extends AbstractAttachmentHandler {

	public ImageAttachmentHandler() {
		super();
	}

	@Autowired
	protected ImageHandler imageHandler;

	@Autowired
	protected AttachmentsService attachmentsService;

	@Override
	protected ComplexObsHandler getParent() {
		return imageHandler;
	}

	@Override
	protected ComplexData readComplexData(Obs obs, ValueComplex valueComplex, String view) {
		Obs tmpObs = new Obs();
		tmpObs.setValueComplex(valueComplex.getSimplifiedValueComplex());

		ComplexData complexData;
		if (view.equals(AttachmentsConstants.ATT_VIEW_THUMBNAIL)) {
			try {
				complexData = getThumbnailComplexData(obs, valueComplex);
			} catch (IOException e) {
				log.error("Failed to generate thumbnail for image attachment", e);
				complexData = new ComplexData(valueComplex.getFileName(), null);
			}
		} else {
			tmpObs = getParent().getObs(tmpObs, AttachmentsConstants.IMAGE_HANDLER_VIEW);
			complexData = tmpObs.getComplexData();
		}

		return getComplexDataHelper().build(valueComplex.getInstructions(), complexData.getTitle(),
				complexData.getData(), valueComplex.getMimeType()).asComplexData();
	}

	@Override
	protected boolean deleteComplexData(Obs obs) {
		return getParent().purgeComplexData(obs);
	}

	@Override
	protected ValueComplex saveComplexData(Obs obs, AttachmentComplexData complexData) {
		obs = getParent().saveObs(obs);
		return new ValueComplex(complexData.getInstructions(), complexData.getMimeType(), obs.getValueComplex());
	}

	private ComplexData getThumbnailComplexData(Obs obs, ValueComplex valueComplex) throws IOException {
		Obs tmpObs = new Obs();
		tmpObs.setValueComplex(valueComplex.getSimplifiedValueComplex());
		tmpObs = getParent().getObs(tmpObs, AttachmentsConstants.IMAGE_HANDLER_VIEW);

		ComplexData complexData = tmpObs.getComplexData();
		BufferedImage thumbnail = Thumbnails.of((BufferedImage) complexData.getData())
				.size(AbstractAttachmentHandler.THUMBNAIL_MAX_WIDTH, AbstractAttachmentHandler.THUMBNAIL_MAX_HEIGHT)
				.asBufferedImage();

		return new ComplexData(valueComplex.getFileName() + AbstractAttachmentHandler.THUMBNAIL_SUFFIX, thumbnail);
	}

	public static String appendThumbnailSuffix(String filePath) {
		int dotIndex = filePath.lastIndexOf(".");
		if (dotIndex > 0) {
			return filePath.substring(0, dotIndex) + AbstractAttachmentHandler.THUMBNAIL_SUFFIX
					+ filePath.substring(dotIndex);
		}
		return filePath + AbstractAttachmentHandler.THUMBNAIL_SUFFIX;
	}
}