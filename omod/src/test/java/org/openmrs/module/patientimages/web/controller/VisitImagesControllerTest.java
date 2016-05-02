package org.openmrs.module.patientimages.web.controller;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import net.coobird.thumbnailator.Thumbnails;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.openmrs.util.OpenmrsUtil;
import org.powermock.api.mockito.PowerMockito;
import static org.mockito.Matchers.*;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(OpenmrsUtil.class)
public class VisitImagesControllerTest {

//	@Test
//	public void foo() throws IOException {
//		
//		Thumbnails.of(new File("/Volumes/Data/Users/dimitri/MEGA/repos/openmrs-module-patientimages/omod/src/test/resources/sampleJpeg.jpg"))
//        .size(100, 100)
//        .toFile(new File("/Volumes/Data/Users/dimitri/MEGA/repos/openmrs-module-patientimages/omod/src/test/resources/thumbnail.jpg"));
//	}
	
	@Test
	public void test() {
		
		PowerMockito.mockStatic(OpenmrsUtil.class);
        BDDMockito.given(OpenmrsUtil.getDirectoryInApplicationDataDirectory(anyString())).willReturn(new File("foobar.txt"));
		
		String imageName = "myImage.png";
		String valueComplex = "png image |" + imageName;
		valueComplex = "some_new_prefix|" + valueComplex; 
		
		String[] names = valueComplex.split("\\|");
		String filename = names.length < 2 ? names[0] : names[names.length - 1];
		
		assertEquals(imageName, filename);
		
		File file = OpenmrsUtil.getDirectoryInApplicationDataDirectory("whatever");
		
		names.toString();
		
	}
	
}
