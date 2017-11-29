package org.openmrs.module.attachments.obs;

import static org.hamcrest.Matchers.lessThan;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import org.apache.commons.io.FilenameUtils;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.attachments.AttachmentsConstants;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

public class ImageAttachmentHandlerIT extends BaseModuleContextSensitiveTest {

	@Autowired
	protected TestHelper testHelper;

	@Test
	public void saveComplexData_shouldSaveThumbnailToDiskAndRename() throws IOException {

		// Replay
		Obs obs = testHelper.saveImageAttachment();
		ValueComplex vc = new ValueComplex(obs.getValueComplex());

		// Verif
		File file = new File(testHelper.getComplexObsDir() + "/" + vc.getFileName());
		Assert.assertTrue(file.exists());
		File thumbnail = new File(testHelper.getComplexObsDir() + "/" + ImageAttachmentHandler.buildThumbnailFileName(vc.getFileName()));
		Assert.assertTrue(thumbnail.exists());
		
		Assert.assertThat(thumbnail.length(), lessThan(file.length()));
		BufferedImage img = ImageIO.read(thumbnail);
		Assert.assertEquals(ImageAttachmentHandler.THUMBNAIL_MAX_HEIGHT, Math.max(img.getHeight(), img.getWidth()));

		// Now check rename the file by appending NO_THUMBNAIL_SUFFIX to the file
	    String fileName = ImageAttachmentHandler.saveThumbnailOrRename(file, img.getHeight(), img.getWidth());
	    Assert.assertTrue(StringUtils.endsWith(FilenameUtils.removeExtension(fileName), ImageAttachmentHandler.NO_THUMBNAIL_SUFFIX));
	}

	@Test
	public void deleteComplexData_shouldDeleteThumbnailFromDisk() throws IOException {

		// Setup
		Obs obs = testHelper.saveImageAttachment();
		ValueComplex vc = new ValueComplex(obs.getValueComplex());

		// Replay
		obs = Context.getObsService().getComplexObs(obs.getId(), AttachmentsConstants.ATT_VIEW_CRUD);
		Context.getObsService().purgeObs(obs);

		// Verif
		File file = new File(testHelper.getComplexObsDir() + "/" + vc.getFileName());
		Assert.assertFalse(file.exists());
		File thumbnail = new File(testHelper.getComplexObsDir() + "/" + ImageAttachmentHandler.buildThumbnailFileName(vc.getFileName()));
		Assert.assertFalse(thumbnail.exists());
	}

	@Test
	public void readComplexData_shouldFetchThumbnail() throws IOException {

		// Setup
		Obs obs = testHelper.saveImageAttachment();
		ValueComplex vc = new ValueComplex(obs.getValueComplex());
		File thumbnail = new File(testHelper.getComplexObsDir() + "/" + ImageAttachmentHandler.buildThumbnailFileName(vc.getFileName()));
		Assert.assertTrue(thumbnail.exists());
		byte[] expectedBytes = new BaseComplexData(thumbnail.getName(), ImageIO.read(thumbnail)).asByteArray();
		
		// Replay
		obs = Context.getObsService().getComplexObs(obs.getId(), AttachmentsConstants.ATT_VIEW_THUMBNAIL);
		
		// Verif
		Assert.assertArrayEquals(expectedBytes, BaseComplexData.getByteArray(obs.getComplexData()));
	}
}