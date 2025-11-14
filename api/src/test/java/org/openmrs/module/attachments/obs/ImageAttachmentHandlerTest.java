package org.openmrs.module.attachments.obs;

import static org.hamcrest.Matchers.lessThan;
import static org.openmrs.module.attachments.obs.ImageAttachmentHandler.appendThumbnailSuffix;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.attachments.AttachmentsConstants;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

public class ImageAttachmentHandlerTest extends BaseModuleContextSensitiveTest {

	@Autowired
	protected TestHelper testHelper;

	@Before
	public void setup() throws IOException {
		testHelper.init();
	}

	@After
	public void tearDown() throws IOException {
		testHelper.tearDown();
	}

	@Test
	public void saveComplexData_shouldSaveThumbnailToDisk() throws IOException {

		// Setup
		Obs obs = testHelper.saveNormalSizeImageAttachment();

		// Verify
		String originalFilePath = testHelper.getFilePathFromObs(obs);
		String thumbnailFilePath = appendThumbnailSuffix(originalFilePath);

		File originalFile = new File(testHelper.encode(originalFilePath));
		Assert.assertTrue(originalFile.exists());

		File thumbnail = new File(testHelper.encode(thumbnailFilePath));
		Assert.assertTrue(thumbnail.exists());

		Assert.assertThat(thumbnail.length(), lessThan(originalFile.length()));
		BufferedImage img = ImageIO.read(thumbnail);
		Assert.assertEquals(ImageAttachmentHandler.THUMBNAIL_MAX_HEIGHT, Math.max(img.getHeight(), img.getWidth()));
	}

	@Test
	public void deleteComplexData_shouldDeleteMainImageAndThumbnailFromDisk() throws IOException {

		// Setup
		Obs obs = testHelper.saveNormalSizeImageAttachment();

		// Sanity check
		String originalFilePath = testHelper.getFilePathFromObs(obs);
		String thumbnailFilePath = appendThumbnailSuffix(originalFilePath);

		File originalFile = new File(testHelper.encode(originalFilePath));
		Assert.assertTrue(originalFile.exists());

		File thumbnail = new File(testHelper.encode(thumbnailFilePath));
		Assert.assertTrue(thumbnail.exists());

		// Purge Obs
		obs = Context.getObsService().getComplexObs(obs.getId(), AttachmentsConstants.ATT_VIEW_CRUD);
		Context.getObsService().purgeObs(obs);

		// Verify Deleted
		Assert.assertFalse(originalFile.exists());
		Assert.assertFalse(thumbnail.exists());
	}

	@Test
	public void readComplexData_shouldFetchThumbnail() throws IOException {

		// Setup
		Obs obs = testHelper.saveNormalSizeImageAttachment();

		// Verify
		String originalFilePath = testHelper.getFilePathFromObs(obs);
		String thumbnailFilePath = appendThumbnailSuffix(originalFilePath);
		File thumbnail = new File(testHelper.encode(thumbnailFilePath));
		Assert.assertTrue(thumbnail.exists());

		String thumbnailName = thumbnail.getName();
		byte[] expectedBytes = new BaseComplexData(testHelper.decode(thumbnailName), ImageIO.read(thumbnail))
				.asByteArray();

		// Replay
		obs = Context.getObsService().getComplexObs(obs.getId(), AttachmentsConstants.ATT_VIEW_THUMBNAIL);

		byte[] actualBytes = BaseComplexData.getByteArray(obs.getComplexData());

		// Verify
		Assert.assertArrayEquals(expectedBytes, actualBytes);
	}

	@Test
	public void saveComplexData_shouldNotSaveThumbnailWhenSmallImage() throws IOException {

		// Setup
		Obs obs = testHelper.saveSmallSizeImageAttachment();

		// Verify
		String originalFilePath = testHelper.getFilePathFromObs(obs);
		String thumbnailFilePath = appendThumbnailSuffix(originalFilePath);

		File originalFile = new File(testHelper.encode(originalFilePath));
		Assert.assertTrue(originalFile.exists());

		File thumbnail = new File(testHelper.encode(thumbnailFilePath));
		Assert.assertFalse(thumbnail.exists());

		BufferedImage img = ImageIO.read(originalFile);
		Assert.assertTrue(ImageAttachmentHandler.THUMBNAIL_MAX_HEIGHT >= Math.max(img.getHeight(), img.getWidth()));

	}

	@Test
	public void deleteComplexData_shouldDeleteRegularFileFromDisk() throws IOException {

		// Setup
		Obs obs = testHelper.saveSmallSizeImageAttachment();

		// Sanity check
		String originalFilePath = testHelper.getFilePathFromObs(obs);
		String thumbnailFilePath = appendThumbnailSuffix(originalFilePath);

		File originalFile = new File(testHelper.encode(originalFilePath));
		Assert.assertTrue(originalFile.exists());

		File thumbnail = new File(testHelper.encode(thumbnailFilePath));
		Assert.assertFalse(thumbnail.exists()); // thumbnail should never have been created

		// Purge Obs
		obs = Context.getObsService().getComplexObs(obs.getId(), AttachmentsConstants.ATT_VIEW_CRUD);
		Context.getObsService().purgeObs(obs);

		// Verify Deleted
		Assert.assertFalse(originalFile.exists());
	}

	@Test
	public void readComplexData_shouldAlwaysFetchOriginalImageWhenSmallImage() throws IOException {

		// Setup
		Obs obs = testHelper.saveSmallSizeImageAttachment();

		String originalFilePath = testHelper.getFilePathFromObs(obs);
		File originalFile = new File(testHelper.encode(originalFilePath));
		Assert.assertTrue(originalFile.exists());

		byte[] expectedBytes = new BaseComplexData(testHelper.decode(originalFile.getName()),
				ImageIO.read(originalFile)).asByteArray();

		// Replay
		Obs obsThumbnailView = Context.getObsService().getComplexObs(obs.getId(),
				AttachmentsConstants.ATT_VIEW_THUMBNAIL);
		Obs obsOriginalView = Context.getObsService().getComplexObs(obs.getId(),
				AttachmentsConstants.ATT_VIEW_ORIGINAL);

		// Verify
		Assert.assertArrayEquals(expectedBytes, BaseComplexData.getByteArray(obsThumbnailView.getComplexData()));
		Assert.assertArrayEquals(expectedBytes, BaseComplexData.getByteArray(obsOriginalView.getComplexData()));
	}

}
