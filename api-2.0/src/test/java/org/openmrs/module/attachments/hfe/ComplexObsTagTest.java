package org.openmrs.module.attachments.hfe;

import static org.hamcrest.CoreMatchers.is;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.module.attachments.obs.TestHelper;
import org.openmrs.module.htmlformentry.MockFileInputStreamMultipartFile;
import org.openmrs.module.htmlformentry.RegressionTestHelper;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartHttpServletRequest;

/*
 * This tests replays an image file upload involving FileInputStream.
 */
public class ComplexObsTagTest extends BaseModuleContextSensitiveTest {
	
	@Autowired
	private TestHelper testHelper;
	
	private File file;
	
	@Before
	public void setup() throws Exception {
		testHelper.init();
		executeDataSet("org/openmrs/api/include/ObsServiceTest-complex.xml");
		
		file = File.createTempFile("OpenMRS_banner", ".jpg");
		FileUtils.copyURLToFile(getClass().getClassLoader().getResource("attachments/OpenMRS_banner.jpg"), file);
	}
	
	@After
	public void tearDown() throws IOException {
		testHelper.tearDown();
	}
	
	protected abstract static class ComplexObsHfeTestHelper extends RegressionTestHelper {
		
		private File file;
		
		public ComplexObsHfeTestHelper(File file) {
			this.file = file;
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
			
			String ulWidgetName = widgets.get("Upload:");
			((MockMultipartHttpServletRequest) request)
			        .addFile(new MockFileInputStreamMultipartFile(ulWidgetName, "OpenMRS_banner.jpg", "image/jpeg", file));
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
	public void handleSubmission_shouldUploadWithImageHandler() throws Exception {
		
		new ComplexObsHfeTestHelper(file) {
			
			@Override
			protected String getXmlDatasetPath() {
				return "org/openmrs/module/htmlformentry/include/";
			}
			
			@Override
			public String getFormName() {
				return "complexObsForm";
			}
			
			@Override
			public void assertObs(Obs obs) {
				Assert.assertEquals("jpg image |OpenMRS_banner.jpg", obs.getValueComplex());
			}
			
		}.run();
	}
	
	@Test
	public void handleSubmission_shouldUploadWithImageAttachmentHandler() throws Exception {
		
		new ComplexObsHfeTestHelper(file) {
			
			@Override
			protected String getXmlDatasetPath() {
				return "hfe/";
			}
			
			@Override
			public String getFormName() {
				return "complexObsForm-att";
			}
			
			@Override
			public void assertObs(Obs obs) {
				Assert.assertEquals("m3ks | instructions.default | image/jpeg | OpenMRS_banner.jpg", obs.getValueComplex());
			}
			
		}.run();
	}
	
}
