package org.openmrs.module.attachments.obs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.openmrs.module.attachments.obs.ValueComplex.buildValueComplex;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.openmrs.module.attachments.AttachmentsConstants;
import org.openmrs.test.Verifies;

public class ValueComplexTest {

	// going forward, we are replacing all "|" fields with an underscore, but
	// keeping these tests as we want to try to maintain backwards compatibility
	@Test
	@Verifies(value = "ValueComplex should handle file names even when containing the reserved separator.", method = "ValueComplex(String, String, String)")
	public void valueComplex_shouldHandleFileNamesWithSeparator() {

		// Setup
		String instr = ValueComplex.INSTRUCTIONS_DEFAULT;
		String mimeType = AttachmentsConstants.UNKNOWN_MIME_TYPE;
		String fileName;
		ValueComplex valueComplex;

		// Replay
		fileName = "my file name .ext";
		valueComplex = new ValueComplex(instr, mimeType, fileName);

		// Verification
		assertEquals(instr, valueComplex.getInstructions());
		assertEquals(mimeType, valueComplex.getMimeType());
		assertEquals(fileName, valueComplex.getFileName());

		// Replay
		fileName = "my file" + ValueComplex.PIPE_WITH_LEADING_AND_TRAILING_SPACE + "name .ext";
		valueComplex = new ValueComplex(instr, mimeType, fileName);

		// Verification
		assertEquals(instr, valueComplex.getInstructions());
		assertEquals(mimeType, valueComplex.getMimeType());
		assertEquals(fileName, valueComplex.getFileName());

		// Replay
		fileName = ValueComplex.PIPE_WITH_LEADING_AND_TRAILING_SPACE + ValueComplex.PIPE_WITH_LEADING_AND_TRAILING_SPACE
				+ ValueComplex.PIPE_WITH_LEADING_AND_TRAILING_SPACE + ValueComplex.PIPE_WITH_LEADING_AND_TRAILING_SPACE
				+ ValueComplex.PIPE_WITH_LEADING_AND_TRAILING_SPACE + ".ext";
		valueComplex = new ValueComplex(instr, mimeType, fileName);

		// Verification
		assertEquals(instr, valueComplex.getInstructions());
		assertEquals(mimeType, valueComplex.getMimeType());
		assertEquals(fileName, valueComplex.getFileName());

		// Replay
		String sepNoBlanks = StringUtils.remove(ValueComplex.PIPE_WITH_LEADING_AND_TRAILING_SPACE, " ");
		fileName = ValueComplex.PIPE_WITH_LEADING_AND_TRAILING_SPACE + sepNoBlanks
				+ ValueComplex.PIPE_WITH_LEADING_AND_TRAILING_SPACE + sepNoBlanks;
		valueComplex = new ValueComplex(instr, mimeType, fileName);

		// Verification
		assertEquals(instr, valueComplex.getInstructions());
		assertEquals(mimeType, valueComplex.getMimeType());
		assertEquals(fileName, valueComplex.getFileName());
	}

	@Test
	public void valueComplex_shouldProperlyParsePreCore2_8ValueComplexString() {
		String valueComplexString = "m3ks | instructions.default | application/pdf | sample-local-pdf_20251112_130823_218e5c1b-4091-462b-9e83-c124425e8a91.pdf";
		ValueComplex valueComplex = new ValueComplex(valueComplexString);

		assertEquals("instructions.default", valueComplex.getInstructions());
		assertEquals("application/pdf", valueComplex.getMimeType());
		assertEquals("sample-local-pdf_20251112_130823_218e5c1b-4091-462b-9e83-c124425e8a91.pdf",
				valueComplex.getFileName());
		assertNull(valueComplex.getKey());
	}

	@Test
	public void valueComplex_shouldProperlyParseCore2_8ValueComplexString() {
		String valueComplexString = "m3ks | instructions.default | application/pdf | sample-local-pdf_20251114_093817.pdf file |complex_obs/2025/11-14/2025-11-14-10-45-05-281-dEuO5wCG-sample-local-pdf_20251114_093817.pdf";

		ValueComplex valueComplex = new ValueComplex(valueComplexString);
		assertEquals("instructions.default", valueComplex.getInstructions());
		assertEquals("application/pdf", valueComplex.getMimeType());
		assertEquals("sample-local-pdf_20251114_093817.pdf", valueComplex.getFileName());
		assertEquals("complex_obs/2025/11-14/2025-11-14-10-45-05-281-dEuO5wCG-sample-local-pdf_20251114_093817.pdf",
				valueComplex.getKey());

		String valueComplexStringImage = "m3ks | instructions.default | image/jpeg | IMG_3869.jpg image |complex_obs/2025/12-04/2025-12-04-14-33-53-304-lt1wjrkK-IMG_3869.jpg";

		ValueComplex valueComplexImage = new ValueComplex(valueComplexStringImage);
		assertEquals("instructions.default", valueComplexImage.getInstructions());
		assertEquals("image/jpeg", valueComplexImage.getMimeType());
		assertEquals("IMG_3869.jpg", valueComplexImage.getFileName());
		assertEquals("complex_obs/2025/12-04/2025-12-04-14-33-53-304-lt1wjrkK-IMG_3869.jpg",
				valueComplexImage.getKey());
	}

	@Test
	public void valueComplex_shouldProperlyBuildPreCore2_8ValueComplexStringIfNoKeyDefined() {
		String complexString = buildValueComplex("instructions.default", "application/pdf",
				"sample-local-pdf_20251112_130823_218e5c1b-4091-462b-9e83-c124425e8a91.pdf", null);
		assertEquals(
				"m3ks | instructions.default | application/pdf | sample-local-pdf_20251112_130823_218e5c1b-4091-462b-9e83-c124425e8a91.pdf",
				complexString);
	}

	@Test
	public void valueComplex_shouldProperlyBuild2_8ValueComplexStringIKeyDefined() {
		String complexString = buildValueComplex("instructions.default", "application/pdf",
				"sample-local-pdf_20251114_093817.pdf",
				"complex_obs/2025/11-14/2025-11-14-10-45-05-281-dEuO5wCG-sample-local-pdf_20251114_093817.pdf");
		assertEquals(
				"m3ks | instructions.default | application/pdf | sample-local-pdf_20251114_093817.pdf |complex_obs/2025/11-14/2025-11-14-10-45-05-281-dEuO5wCG-sample-local-pdf_20251114_093817.pdf",
				complexString);
	}

}
