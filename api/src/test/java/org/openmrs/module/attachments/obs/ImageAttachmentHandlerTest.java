package org.openmrs.module.attachments.obs;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.Obs;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.obs.ComplexData;
import org.openmrs.obs.ComplexObsHandler;
import org.openmrs.obs.handler.ImageHandler;
import org.openmrs.test.Verifies;
import org.openmrs.util.OpenmrsConstants;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class ImageAttachmentHandlerTest {
	
	// Various facets of the test input image resource
	private String imgFilePath = TestHelper.ATTACHMENTS_FOLDER + "/" + "OpenMRS_logo.png";
	
	private String imgFileName = FilenameUtils.getName(imgFilePath);
	
	private String imgExt = FilenameUtils.getExtension(imgFileName);
	
	private URL imageUrl = getClass().getClassLoader().getResource(imgFilePath);
	
	private BufferedImage originalBufferedImg;
	
	private File imageFile = new File(imgFileName);
	
	private final String randomView = RandomStringUtils.random(10);
	
	private ComplexObsHandler imageHandler = new ImageHandler();
	
	private ComplexObsHandler imageDocumentHandler = new ImageAttachmentHandler();
	
	@Before
	public void setUp() throws IOException, APIException, URISyntaxException {
		
		originalBufferedImg = ImageIO.read(imageUrl);
		ImageIO.write(originalBufferedImg, imgExt, imageFile);
		
		AdministrationService adminService = mock(AdministrationService.class);
		when(adminService.getGlobalProperty(eq(OpenmrsConstants.GLOBAL_PROPERTY_COMPLEX_OBS_DIR)))
		        .thenReturn(imageUrl.toURI().resolve(".").getPath());
		
		PowerMockito.mockStatic(Context.class);
		when(Context.getAdministrationService()).thenReturn(adminService);
	}
	
	@Test
	@Verifies(value = "Images saved with ImageDocumentHandler with no additional valueComplex metadata should be fetchable by ImageHandler.", method = "saveObs(obs)")
	public void imageDocumentHandler_shouldSaveLikeImageHandler() throws FileNotFoundException {
		
		// Replay
		Obs savedObs = new Obs();
		savedObs.setComplexData(
		    (new AttachmentComplexData1_10(imgFileName, new FileInputStream(imageFile))).asComplexData());
		imageDocumentHandler.saveObs(savedObs);
		Obs fetchedObs = imageHandler.getObs(savedObs, randomView);
		ComplexData complexData = fetchedObs.getComplexData();
		
		// Verification
		byte[] expectedByteArray = ((DataBufferByte) originalBufferedImg.getData().getDataBuffer()).getData();
		byte[] actualByteArray = ((DataBufferByte) ((BufferedImage) complexData.getData()).getData().getDataBuffer())
		        .getData();
		assertArrayEquals(expectedByteArray, actualByteArray);
	}
	
	@Test
	@Verifies(value = "Images saved with ImageHandler should be fetchable by ImageDocumentHandler.", method = "getObs(obs, view)")
	public void imageDocumentHandler_shouldFetchLikeImageHandler() throws FileNotFoundException {
		
		// Replay
		Obs savedObs = new Obs();
		savedObs.setComplexData(new ComplexData(imgFileName, new FileInputStream(imageFile)));
		imageHandler.saveObs(savedObs); // Saving with core's handler
		Obs fetchedObs = imageDocumentHandler.getObs(savedObs, randomView);
		ComplexData complexData = fetchedObs.getComplexData(); // Fetching with our handler
		
		// Verification
		byte[] expectedByteArray = ((DataBufferByte) originalBufferedImg.getData().getDataBuffer()).getData();
		byte[] actualByteArray = ((DataBufferByte) ((BufferedImage) complexData.getData()).getData().getDataBuffer())
		        .getData();
		assertArrayEquals(expectedByteArray, actualByteArray);
	}
}
