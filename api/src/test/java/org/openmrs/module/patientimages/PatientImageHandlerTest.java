package org.openmrs.module.patientimages;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.Obs;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.obs.ComplexData;
import org.openmrs.obs.handler.ImageHandler;
import org.openmrs.test.Verifies;
import org.openmrs.util.OpenmrsConstants;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class PatientImageHandlerTest {

	@Test
	@Verifies(value = "PatientImageHandler should behave as core's ImageHandler when no additional input are provided.", method = "saveObs(obs)")
	public void patientImageHandler_shouldDefaultOnImageHandler() throws IOException, URISyntaxException {
		
		// Setup
		ImageHandler patientImageHandler = new PatientImageHandler();
		
		String imgFileName = "OpenMRS_logo.png";
		String imgExt = patientImageHandler.getExtension(imgFileName);
		URL url = getClass().getClassLoader().getResource(imgFileName);
		BufferedImage originalBufferedImg = ImageIO.read(url);
		File imageFile = new File(imgFileName);
		ImageIO.write(originalBufferedImg, imgExt, imageFile);
		
		AdministrationService adminService = mock(AdministrationService.class);
		when(adminService.getGlobalProperty(eq(OpenmrsConstants.GLOBAL_PROPERTY_COMPLEX_OBS_DIR))).thenReturn( url.toURI().resolve(".").getPath() );
		PowerMockito.mockStatic(Context.class);
		when(Context.getAdministrationService()).thenReturn(adminService);
		
		
		// Replay
		Obs savedObs = new Obs();
		savedObs.setComplexData( new PatientImageComplexData(imgFileName, new FileInputStream(imageFile)) );
		patientImageHandler.saveObs(savedObs);
		
		
		// Verification
		final String VIEW = RandomStringUtils.random(10);
		Obs fetchedObs = patientImageHandler.getObs(savedObs, VIEW);
		ComplexData complexData = fetchedObs.getComplexData();
		
		byte[] expectedByteArray = ((DataBufferByte) originalBufferedImg.getData().getDataBuffer()).getData();
		byte[] actualByteArray = ((DataBufferByte) ((BufferedImage) complexData.getData()).getData().getDataBuffer()).getData();
		assertArrayEquals(expectedByteArray, actualByteArray);
	}
}
