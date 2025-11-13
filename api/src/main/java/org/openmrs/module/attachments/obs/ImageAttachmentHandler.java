package org.openmrs.module.attachments.obs;

import java.awt.image.BufferedImage;
import java.io.IOException;

import net.coobird.thumbnailator.Thumbnails;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.attachments.AttachmentsConstants;
import org.openmrs.obs.ComplexData;
import org.openmrs.obs.handler.ImageHandler;

public class ImageAttachmentHandler extends AbstractAttachmentHandler {

	public ImageAttachmentHandler() {
		super();
	}

	@Override
	protected void setParentComplexObsHandler() {
		setParent(Context.getRegisteredComponents(ImageHandler.class).get(0));
	}

	@Override
	protected ComplexData readComplexData(Obs obs, ValueComplex valueComplex, String view) {

		String dataKey = ((ImageHandler) getParent()).parseDataKey(obs);
		String fileName = valueComplex.getFileName();

		if (view.equals(AttachmentsConstants.ATT_VIEW_THUMBNAIL)
				&& storageService.exists(appendThumbnailSuffix(dataKey))) {
			fileName = appendThumbnailSuffix(fileName);
		}

		// We invoke the parent to inherit from the file reading routines.
		Obs tmpObs = new Obs();
		tmpObs.setValueComplex(fileName); // Temp obs used as a safety
		tmpObs = getParent().getObs(tmpObs, AttachmentsConstants.IMAGE_HANDLER_VIEW); // ImageHandler doesn't handle
		// several views
		ComplexData complexData = tmpObs.getComplexData();

		// Then we build our own custom complex data
		return getComplexDataHelper().build(valueComplex.getInstructions(), complexData.getTitle(),
				complexData.getData(), valueComplex.getMimeType()).asComplexData();
	}

	@Override
	protected boolean deleteComplexData(Obs obs) {

		// first delete the thumbnail if it exists
		Boolean isThumbnailPurged = null;
		String thumbnailDataKey = appendThumbnailSuffix(((ImageHandler) getParent()).parseDataKey(obs));

		try {
			if (storageService.exists(thumbnailDataKey)) {
				isThumbnailPurged = storageService.purgeData(thumbnailDataKey);
			} else {
				isThumbnailPurged = true;
			}
		} catch (IOException e) {
			log.error("Failed to purge thumbnail file: " + thumbnailDataKey, e);
			isThumbnailPurged = false;
		}

		boolean isImagePurged = getParent().purgeComplexData(obs);
		return isThumbnailPurged && isImagePurged;
	}

	@Override
	protected ValueComplex saveComplexData(Obs obs, AttachmentComplexData complexData) {

		// We invoke the parent to inherit from the file saving routines
		obs = getParent().saveObs(obs);

		// now use the parent method to fetch the complex data, the assumption is it
		// will return a BufferedImage
		// (since that is what ImageHandler getObs in Core returns)
		obs = getParent().getObs(obs, AttachmentsConstants.IMAGE_HANDLER_VIEW);

		int imageHeight = Integer.MAX_VALUE;
		int imageWidth = Integer.MAX_VALUE;

		// Get image dimensions
		BufferedImage image = (BufferedImage) obs.getComplexData().getData();
		imageHeight = image.getHeight();
		imageWidth = image.getWidth();
		saveThumbnailIfNeeded(obs, imageHeight, imageWidth);

		// We invoke the parent to inherit from the file saving routines.
		return new ValueComplex(complexData.getInstructions(), complexData.getMimeType(), obs.getValueComplex());
	}

	/**
	 * <p>
	 * If the image is over a certain dimension, it will create a small thumbnail
	 * file alongside the original file to be used as thumbnail image.
	 * </p>
	 *
	 * @param obs
	 *            original obs
	 * @param imageHeight
	 *            image height
	 * @param imageWidth
	 *            image width
	 * @return savedFileName new renamed file name or original file name
	 */
	public void saveThumbnailIfNeeded(Obs obs, int imageHeight, int imageWidth) {
		if ((imageHeight <= THUMBNAIL_MAX_HEIGHT) && (imageWidth <= THUMBNAIL_MAX_WIDTH)) {
			return;
		} else {
			String key = appendThumbnailSuffix(((ImageHandler) getParent()).parseDataKey(obs));
			try {
				storageService.saveData(outputStream -> {
					Object data = obs.getComplexData().getData();
					if (!(data instanceof BufferedImage)) {
						throw new IllegalArgumentException(
								"Expected a BufferedImage, but got " + data.getClass().getName());
					}
					BufferedImage image = (BufferedImage) data;
					Thumbnails.of(image).size(THUMBNAIL_MAX_HEIGHT, THUMBNAIL_MAX_WIDTH)
							.outputFormat(obs.getComplexData().getMimeType().split("/")[1].toLowerCase())
							.toOutputStream(outputStream);
				}, null, null, key);
				// the above is a bit of hack... we pass in the entire value we want use as a key instead of specifying the filename,
				// module ID and key suffix and having the storage service to generate the full key for us
				// this is because we need to be able to recreate the key based on the key of the main image
			} catch (IOException e) {
				log.error("Failed to save thumbnail file: " + key, e);
			}
		}
	}
}
