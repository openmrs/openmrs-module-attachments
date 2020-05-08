package org.openmrs.module.attachments.hfe;

import static org.hamcrest.CoreMatchers.is;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.module.attachments.obs.TestHelper;
import org.openmrs.module.htmlformentry.RegressionTestHelper;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

public class ComplexObsTagTest extends BaseModuleContextSensitiveTest {
	
	@Autowired
	private TestHelper testHelper;
	
	@Before
	public void setup() throws Exception {
		testHelper.init();
		executeDataSet("testdata/test-dataset-2.0.xml");
	}
	
	@After
	public void tearDown() throws IOException {
		testHelper.tearDown();
	}
	
	protected abstract static class ComplexObsHfeTestHelper extends RegressionTestHelper {
		
		@Override
		protected String getXmlDatasetPath() {
			return "hfe/";
		}
		
		@Override
		public String[] widgetLabels() {
			return new String[] { "Date:", "Location:", "Provider:", "Upload:" };
		}
		
		@Override
		public void setupRequest(MockHttpServletRequest request, Map<String, String> widgets) {
			request.addParameter(widgets.get("Date:"), dateAsString(new Date()));
			request.addParameter(widgets.get("Location:"), "2");
			request.addParameter(widgets.get("Provider:"), "1");
			
			MockMultipartHttpServletRequest mpRequest = (MockMultipartHttpServletRequest) request;
			try {
				MultipartFile mpFile = new MockMultipartFile("w8", "OpenMRS_banner.jpg", "image/jpeg", IOUtils
				        .toByteArray(getClass().getClassLoader().getResourceAsStream("attachments/OpenMRS_banner.jpg")));
				mpRequest.addFile(mpFile);
			}
			catch (IOException e) {
				Assert.fail();
				e.printStackTrace();
			}
		}
		
		@Override
		public void testResults(SubmissionResults results) {
			Encounter e = results.getEncounterCreated();
			Assert.assertNotNull(e);
			
			List<Obs> obs = new ArrayList<Obs>(e.getAllObs());
			Assert.assertThat(obs.size(), is(1));
			
			Obs o = obs.get(0);
			assertObs(o);
		}
		
		abstract public void assertObs(Obs obs);
	}
	
	@Test
	public void shouldUploadWithAttachmentsImageHandler() throws Exception {
		
		new ComplexObsHfeTestHelper() {
			
			@Override
			public String getFormName() {
				return "ComplexObsForm-1";
			}
			
			@Override
			public void assertObs(Obs obs) {
				// Assert.assertEquals("jpg image |OpenMRS_banner.jpg", obs.getValueComplex());
			}
			
		}.run();
	}
	
	@Test
	public void shouldUploadWithImageHandler() throws Exception {
		
		new ComplexObsHfeTestHelper() {
			
			@Override
			public String getFormName() {
				return "ComplexObsForm-2";
			}
			
			@Override
			public void assertObs(Obs obs) {
				Assert.assertEquals("jpg image |OpenMRS_banner.jpg", obs.getValueComplex());
			}
			
		}.run();
	}
	
}
