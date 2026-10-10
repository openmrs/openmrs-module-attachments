package org.openmrs.module.attachments.obs;

import static org.hamcrest.Matchers.lessThan;
import static org.openmrs.module.attachments.obs.ImageAttachmentHandler.appendThumbnailSuffix;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.AfterEach;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.attachments.AttachmentsConstants;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

public class ImageAttachmentHandlerTest extends BaseModuleContextSensitiveTest {

	@Autowired
	protected TestHelper testHelper;

	@BeforeEach
	public void setup() throws IOException {
		testHelper.init();
	}

	@AfterEach
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
		Assertions.assertTrue(originalFile.exists());

		File thumbnail = new File(testHelper.encode(thumbnailFilePath));
		Assertions.assertTrue(thumbnail.exists());

		MatcherAssert.assertThat(thumbnail.length(), lessThan(originalFile.length()));
		BufferedImage img = ImageIO.read(thumbnail);
		Assertions.assertEquals(ImageAttachmentHandler.THUMBNAIL_MAX_HEIGHT, Math.max(img.getHeight(), img.getWidth()));
	}

	@Test
	public void deleteComplexData_shouldDeleteMainImageAndThumbnailFromDisk() throws IOException {

		// Setup
		Obs obs = testHelper.saveNormalSizeImageAttachment();

		// Sanity check
		String originalFilePath = testHelper.getFilePathFromObs(obs);
		String thumbnailFilePath = appendThumbnailSuffix(originalFilePath);

		File originalFile = new File(testHelper.encode(originalFilePath));
		Assertions.assertTrue(originalFile.exists());

		File thumbnail = new File(testHelper.encode(thumbnailFilePath));
		Assertions.assertTrue(thumbnail.exists());

		// Purge Obs
		obs = Context.getObsService().getComplexObs(obs.getId(), AttachmentsConstants.ATT_VIEW_CRUD);
		Context.getObsService().purgeObs(obs);

		// Verify Deleted
		Assertions.assertFalse(originalFile.exists());
		Assertions.assertFalse(thumbnail.exists());
	}

	@Test
	public void readComplexData_shouldFetchThumbnail() throws IOException {

		// Setup
		Obs obs = testHelper.saveNormalSizeImageAttachment();

		// Verify
		String originalFilePath = testHelper.getFilePathFromObs(obs);
		String thumbnailFilePath = appendThumbnailSuffix(originalFilePath);
		File thumbnail = new File(testHelper.encode(thumbnailFilePath));
		Assertions.assertTrue(thumbnail.exists());

		String thumbnailName = thumbnail.getName();
		byte[] expectedBytes = new BaseComplexData(testHelper.decode(thumbnailName), ImageIO.read(thumbnail))
				.asByteArray();

		// Replay
		obs = Context.getObsService().getComplexObs(obs.getId(), AttachmentsConstants.ATT_VIEW_THUMBNAIL);

		byte[] actualBytes = BaseComplexData.getByteArray(obs.getComplexData());

		// Verify
		Assertions.assertArrayEquals(expectedBytes, actualBytes);
	}

	@Test
	public void saveComplexData_shouldNotSaveThumbnailWhenSmallImage() throws IOException {

		// Setup
		Obs obs = testHelper.saveSmallSizeImageAttachment();

		// Verify
		String originalFilePath = testHelper.getFilePathFromObs(obs);
		String thumbnailFilePath = appendThumbnailSuffix(originalFilePath);

		File originalFile = new File(testHelper.encode(originalFilePath));
		Assertions.assertTrue(originalFile.exists());

		File thumbnail = new File(testHelper.encode(thumbnailFilePath));
		Assertions.assertFalse(thumbnail.exists());

		BufferedImage img = ImageIO.read(originalFile);
		Assertions.assertTrue(ImageAttachmentHandler.THUMBNAIL_MAX_HEIGHT >= Math.max(img.getHeight(), img.getWidth()));

	}

	@Test
	public void deleteComplexData_shouldDeleteRegularFileFromDisk() throws IOException {

		// Setup
		Obs obs = testHelper.saveSmallSizeImageAttachment();

		// Sanity check
		String originalFilePath = testHelper.getFilePathFromObs(obs);
		String thumbnailFilePath = appendThumbnailSuffix(originalFilePath);

		File originalFile = new File(testHelper.encode(originalFilePath));
		Assertions.assertTrue(originalFile.exists());

		File thumbnail = new File(testHelper.encode(thumbnailFilePath));
		Assertions.assertFalse(thumbnail.exists()); // thumbnail should never have been created

		// Purge Obs
		obs = Context.getObsService().getComplexObs(obs.getId(), AttachmentsConstants.ATT_VIEW_CRUD);
		Context.getObsService().purgeObs(obs);

		// Verify Deleted
		Assertions.assertFalse(originalFile.exists());
	}

	@Test
	public void readComplexData_shouldAlwaysFetchOriginalImageWhenSmallImage() throws IOException {

		// Setup
		Obs obs = testHelper.saveSmallSizeImageAttachment();

		String originalFilePath = testHelper.getFilePathFromObs(obs);
		File originalFile = new File(testHelper.encode(originalFilePath));
		Assertions.assertTrue(originalFile.exists());

		byte[] expectedBytes = new BaseComplexData(testHelper.decode(originalFile.getName()),
				ImageIO.read(originalFile)).asByteArray();

		// Replay
		Obs obsThumbnailView = Context.getObsService().getComplexObs(obs.getId(),
				AttachmentsConstants.ATT_VIEW_THUMBNAIL);
		Obs obsOriginalView = Context.getObsService().getComplexObs(obs.getId(),
				AttachmentsConstants.ATT_VIEW_ORIGINAL);

		// Verify
		Assertions.assertArrayEquals(expectedBytes, BaseComplexData.getByteArray(obsThumbnailView.getComplexData()));
		Assertions.assertArrayEquals(expectedBytes, BaseComplexData.getByteArray(obsOriginalView.getComplexData()));
	}

}
