package org.openmrs.module.attachments.obs;

import static org.openmrs.module.attachments.AttachmentsContext.isMimeTypeHandled;

import java.util.Arrays;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.openmrs.module.attachments.AttachmentsConstants;
import org.openmrs.module.attachments.AttachmentsContext;

public class ValueComplex {
	
	public static final String INSTRUCTIONS_NONE = AttachmentsConstants.INSTRUCTIONS_PREFIX + ".none";
	
	public static final String INSTRUCTIONS_DEFAULT = AttachmentsConstants.INSTRUCTIONS_PREFIX + ".default";
	
	protected static final String FILENAME_DEFAULT = AttachmentsConstants.MODULE_SHORT_ID.toLowerCase() + "_file";
	
	protected String instructions = INSTRUCTIONS_NONE;
	
	protected String mimeType = AttachmentsConstants.UNKNOWN_MIME_TYPE;
	
	protected String fileName = AttachmentsConstants.MODULE_SHORT_ID.toLowerCase() + "_file.dat";
	
	protected final static String UNIQUE_PREFIX = "m3ks"; // This is used to identify our implementation from saved
	                                                      // valueComplex.
	
	protected final static String SEP = " | ";
	
	protected final static int METADATA_PARTS_COUNT = StringUtils.countMatches(buildValueComplex("", "", ""), SEP);
	
	public ValueComplex(String valueComplex) {
		
		if (StringUtils.substringBefore(valueComplex, SEP).equals(UNIQUE_PREFIX) == false) {
			this.instructions = INSTRUCTIONS_NONE;
			return;
		}
		
		String metaData = StringUtils.substringAfter(valueComplex, SEP);
		String[] metaParts = metaData.split(Pattern.quote(SEP));
		
		if (metaParts.length > 0) {
			instructions = metaParts[0];
			if (!isValidInstructions(instructions)) {
				instructions = INSTRUCTIONS_NONE;
			}
		}
		if (metaParts.length > 1) {
			mimeType = metaParts[1];
			if (!isValidMimeType(mimeType)) {
				mimeType = AttachmentsConstants.UNKNOWN_MIME_TYPE;
			}
		}
		if (metaParts.length > 2) {
			String[] fileNameParts = Arrays.copyOfRange(metaParts, 2, metaParts.length);
			fileName = StringUtils.join(fileNameParts, SEP);
		} else {
			int sepCount = StringUtils.countMatches(valueComplex, SEP);
			if (sepCount >= METADATA_PARTS_COUNT) {
				int pos = StringUtils.ordinalIndexOf(valueComplex, SEP, METADATA_PARTS_COUNT);
				pos += SEP.length();
				fileName = StringUtils.substring(valueComplex, pos);
			} else { // That'd be a case where the file name is not even part of the valueComplex
			         // String, anything else looking valid.
				fileName = FilenameUtils.removeExtension(fileName) + "." + AttachmentsContext.getExtension(mimeType);
			}
		}
	}
	
	public ValueComplex(String instructions, String mimeType, String fileName) {
		this(buildValueComplex(instructions, mimeType, fileName));
	}
	
	@Override
	public String toString() {
		return buildValueComplex(instructions, mimeType, fileName);
	}
	
	public boolean isOwnImplementation() {
		return instructions != INSTRUCTIONS_NONE;
	}
	
	public String getInstructions() {
		return instructions;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public String getMimeType() {
		return mimeType;
	}
	
	public String getValueComplex() {
		return buildValueComplex(instructions, mimeType, fileName);
	}
	
	public static String buildValueComplex(String instructions, String mimeType, String savedFileName) {
		return UNIQUE_PREFIX + SEP + instructions + SEP + mimeType + SEP + savedFileName;
	}
	
	protected static boolean isValidInstructions(String str) {
		return StringUtils.startsWith(str, AttachmentsConstants.INSTRUCTIONS_PREFIX + ".");
	}
	
	protected static boolean isValidMimeType(String str) {
		return isMimeTypeHandled(str);
	}
}
