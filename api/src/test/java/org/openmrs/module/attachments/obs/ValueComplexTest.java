package org.openmrs.module.attachments.obs;

import static org.junit.Assert.assertEquals;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.openmrs.module.attachments.AttachmentsConstants;
import org.openmrs.module.attachments.obs.ValueComplex;
import org.openmrs.test.Verifies;

public class ValueComplexTest {
	
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
		fileName = "my file" + ValueComplex.SEP + "name .ext";
		valueComplex = new ValueComplex(instr, mimeType, fileName);
		
		// Verification
		assertEquals(instr, valueComplex.getInstructions());
		assertEquals(mimeType, valueComplex.getMimeType());
		assertEquals(fileName, valueComplex.getFileName());
		
		// Replay
		fileName = ValueComplex.SEP + ValueComplex.SEP + ValueComplex.SEP + ValueComplex.SEP + ValueComplex.SEP + ".ext";
		valueComplex = new ValueComplex(instr, mimeType, fileName);
		
		// Verification
		assertEquals(instr, valueComplex.getInstructions());
		assertEquals(mimeType, valueComplex.getMimeType());
		assertEquals(fileName, valueComplex.getFileName());
		
		// Replay
		fileName = ValueComplex.SEP;
		valueComplex = new ValueComplex(instr, mimeType, fileName);
		
		// Verification
		assertEquals(instr, valueComplex.getInstructions());
		assertEquals(mimeType, valueComplex.getMimeType());
		assertEquals(fileName, valueComplex.getFileName());
		
		// Replay
		String sepNoBlanks = StringUtils.remove(ValueComplex.SEP, " ");
		fileName = ValueComplex.SEP + sepNoBlanks + ValueComplex.SEP + sepNoBlanks;
		valueComplex = new ValueComplex(instr, mimeType, fileName);
		
		// Verification
		assertEquals(instr, valueComplex.getInstructions());
		assertEquals(mimeType, valueComplex.getMimeType());
		assertEquals(fileName, valueComplex.getFileName());
	}
}
