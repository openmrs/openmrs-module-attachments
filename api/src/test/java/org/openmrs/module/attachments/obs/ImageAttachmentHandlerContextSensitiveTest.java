package org.openmrs.module.attachments.obs;

import static org.hamcrest.Matchers.lessThan;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.attachments.AttachmentsConstants;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

public class ImageAttachmentHandlerContextSensitiveTest extends BaseModuleContextSensitiveTest {
	
	@Autowired
	protected TestHelper testHelper;
	
	@Test
	public void saveComplexData_shouldSaveThumbnailToDisk() throws IOException {
		
		// Replay
		testHelper.saveNormalSizeImageAttachment();
		
		// Verif
		MockMultipartFile mpFile = testHelper.getLastSavedTestImageFile();
		File file = new File(testHelper.getComplexObsDir() + "/" + mpFile.getOriginalFilename());
		Assert.assertTrue(file.exists());
		File thumbnail = new File(testHelper.getComplexObsDir() + "/"
		        + ImageAttachmentHandler.buildThumbnailFileName(mpFile.getOriginalFilename()));
		Assert.assertTrue(thumbnail.exists());
		
		Assert.assertThat(thumbnail.length(), lessThan(file.length()));
		BufferedImage img = ImageIO.read(thumbnail);
		Assert.assertEquals(ImageAttachmentHandler.THUMBNAIL_MAX_HEIGHT, Math.max(img.getHeight(), img.getWidth()));
		
		File noThumbnailFile = new File(testHelper.getComplexObsDir() + "/"
		        + ImageAttachmentHandler.buildNoThumbnailFileFileName(mpFile.getOriginalFilename()));
		Assert.assertFalse(noThumbnailFile.exists());
	}
	
	@Test
	public void deleteComplexData_shouldDeleteThumbnailFromDisk() throws IOException {
		
		// Setup
		Obs obs = testHelper.saveNormalSizeImageAttachment();
		MockMultipartFile mpFile = testHelper.getLastSavedTestImageFile();
		
		// Replay
		obs = Context.getObsService().getComplexObs(obs.getId(), AttachmentsConstants.ATT_VIEW_CRUD);
		Context.getObsService().purgeObs(obs);
		
		// Verif
		File file = new File(testHelper.getComplexObsDir() + "/" + mpFile.getOriginalFilename());
		Assert.assertFalse(file.exists());
		File thumbnail = new File(testHelper.getComplexObsDir() + "/"
		        + ImageAttachmentHandler.buildThumbnailFileName(mpFile.getOriginalFilename()));
		Assert.assertFalse(thumbnail.exists());
	}
	
	@Test
	public void readComplexData_shouldFetchThumbnail() throws IOException {
		
		// Setup
		Obs obs = testHelper.saveNormalSizeImageAttachment();
		MockMultipartFile mpFile = testHelper.getLastSavedTestImageFile();
		File thumbnail = new File(testHelper.getComplexObsDir() + "/"
		        + ImageAttachmentHandler.buildThumbnailFileName(mpFile.getOriginalFilename()));
		Assert.assertTrue(thumbnail.exists());
		byte[] expectedBytes = new BaseComplexData(thumbnail.getName(), ImageIO.read(thumbnail)).asByteArray();
		
		// Replay
		obs = Context.getObsService().getComplexObs(obs.getId(), AttachmentsConstants.ATT_VIEW_THUMBNAIL);
		
		// Verif
		Assert.assertArrayEquals(expectedBytes, BaseComplexData.getByteArray(obs.getComplexData()));
	}
	
	@Test
	public void saveComplexData_shouldNotSaveThumbnailWhenSmallImage() throws IOException {
		
		// Setup
		testHelper.saveSmallSizeImageAttachment();
		MockMultipartFile mpFile = testHelper.getLastSavedTestImageFile();
		String originalFileName = mpFile.getOriginalFilename();
		
		// Verif the buildNotThumbnailFileFileName()
		File noThumbnailFile = new File(
		        testHelper.getComplexObsDir() + "/" + ImageAttachmentHandler.buildNoThumbnailFileFileName(originalFileName));
		Assert.assertTrue(noThumbnailFile.exists());
		// Not create new file name since it contained NO_THUMBNAIL_SUFFIX
		noThumbnailFile = new File(testHelper.getComplexObsDir() + "/"
		        + ImageAttachmentHandler.buildNoThumbnailFileFileName(noThumbnailFile.getName()));
		Assert.assertTrue(noThumbnailFile.exists());
		
		// Verif the thumbnail file was NOT created
		File thumbnail = new File(
		        testHelper.getComplexObsDir() + "/" + ImageAttachmentHandler.buildThumbnailFileName(originalFileName));
		Assert.assertFalse(thumbnail.exists());
		
		// Assert.assertThat(thumbnail.length(), lessThan(file.length()));
		BufferedImage img = ImageIO.read(noThumbnailFile);
		Assert.assertTrue(ImageAttachmentHandler.THUMBNAIL_MAX_HEIGHT >= Math.max(img.getHeight(), img.getWidth()));
		
		// Check that the file name contains the no thumbnail suffix
		Assert.assertTrue(ImageAttachmentHandler.isThumbnail(FilenameUtils.removeExtension(noThumbnailFile.getName())));
	}
	
	@Test
	public void deleteComplexData_shouldDeleteNoThumbnailFromDisk() throws IOException {
		
		// Setup
		Obs obs = testHelper.saveSmallSizeImageAttachment();
		MockMultipartFile mpFile = testHelper.getLastSavedTestImageFile();
		String originalFileName = mpFile.getOriginalFilename();
		// Replay
		obs = Context.getObsService().getComplexObs(obs.getId(), AttachmentsConstants.ATT_VIEW_CRUD);
		Context.getObsService().purgeObs(obs);
		
		// Verif
		File noThumbnailFile = new File(
		        testHelper.getComplexObsDir() + "/" + ImageAttachmentHandler.buildNoThumbnailFileFileName(originalFileName));
		Assert.assertFalse(noThumbnailFile.exists());
	}
	
	@Test
	public void readComplexData_shouldAlwaysFetchOriginalImageWhenSmallImage() throws IOException {
		
		// Setup
		Obs obs = testHelper.saveSmallSizeImageAttachment();
		MockMultipartFile mpFile = testHelper.getLastSavedTestImageFile();
		String originalFileName = mpFile.getOriginalFilename();
		File noThumbnailFile = new File(
		        testHelper.getComplexObsDir() + "/" + ImageAttachmentHandler.buildNoThumbnailFileFileName(originalFileName));
		Assert.assertTrue(noThumbnailFile.exists());
		byte[] expectedBytes = new BaseComplexData(noThumbnailFile.getName(), ImageIO.read(noThumbnailFile)).asByteArray();
		
		// Replay
		obs = Context.getObsService().getComplexObs(obs.getId(), AttachmentsConstants.ATT_VIEW_THUMBNAIL);
		
		// Verif
		Assert.assertArrayEquals(expectedBytes, BaseComplexData.getByteArray(obs.getComplexData()));
		
		obs = Context.getObsService().getComplexObs(obs.getId(), AttachmentsConstants.ATT_VIEW_ORIGINAL);
		
		// Verif
		Assert.assertArrayEquals(expectedBytes, BaseComplexData.getByteArray(obs.getComplexData()));
	}
}
