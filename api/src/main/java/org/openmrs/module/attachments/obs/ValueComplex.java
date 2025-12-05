package org.openmrs.module.attachments.obs;

import static org.openmrs.module.attachments.AttachmentsContext.isMimeTypeHandled;

import java.util.Arrays;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.attachments.AttachmentsConstants;

/**
 * Helper class that can be used to convert the "valueComplex" field of an obs
 * into its parts.
 * <p>
 * Basically, in Core, the valueComplex field has always beeb stored in the
 * following pattern:
 * <p>
 * "filename (image|file|null) |path-to-file"
 * <p>
 * for example: "Printed Patient Summary.pdf file |Printed Patient Summary.pdf"
 * <p>
 * Traditionally (prior to making Attachments Core 2.8 compatible), Attachments
 * morphed this into pattern: "m3ks | instructions.default | application/pdf |
 * Printed Patient Summary.pdf"
 * <p>
 * (When "m3ks" is a unique prefix that tells the handler that this is an
 * Attachments-specific valueComplex)
 * <p>
 * This worked prior to Core 2.8, because the filename always (I believe) equals
 * the path-to-file. However, in Core 2.8, the filename is no longer equal to
 * the path-to-file, so we now need to store both in the Attachments-specific
 * valueComplex.
 * <p>
 * The new format we use is:
 * <p>
 * "m3ks | instructions.default | application/pdf | Printed Patient Summary.pdf
 * |/some/path/Printed Patient Summary.pdf"
 * <p>
 * (Note that we still remove the "file" or "image" suffix from the filename
 * field, as we now have a mime-type field)
 */
public class ValueComplex {

	public static final String INSTRUCTIONS_NONE = AttachmentsConstants.INSTRUCTIONS_PREFIX + ".none";

	public static final String INSTRUCTIONS_DEFAULT = AttachmentsConstants.INSTRUCTIONS_PREFIX + ".default";

	protected final static String UNIQUE_PREFIX = "m3ks";

	// attachments uses a pipe with a leading and trailing space to separate the
	// metadata in valueComplex,
	// but core uses a pipe with a leading space but not trailing space to separate
	// filename from key
	protected final static String PIPE_WITH_LEADING_AND_TRAILING_SPACE = " | ";
	protected final static String PIPE_WITH_LEADING_SPACE = " |";
	protected final static Pattern PIPE_WITH_LEADING_SPACE_AND_NO_TRAILING_SPACE_REGEX = Pattern.compile(" \\|\\S");
	protected final static String SPACE = " ";

	protected final static String IMAGE_FILE_TYPE = "image";
	protected final static String BINARY_DATA_FILE_TYPE = "file";

	// Properties of value complex
	protected String instructions = INSTRUCTIONS_NONE;
	protected String mimeType = AttachmentsConstants.UNKNOWN_MIME_TYPE;
	protected String fileName = AttachmentsConstants.MODULE_SHORT_ID.toLowerCase() + "_file.dat";
	// the key used to retrieve the file from the StorageService (as of Core 2.8)
	protected String key = null;

	public ValueComplex(String valueComplex) {

		// this is a value complex that *wasn't* created by the attachment module
		if (!StringUtils.substringBefore(valueComplex, PIPE_WITH_LEADING_AND_TRAILING_SPACE).equals(UNIQUE_PREFIX)) {
			this.instructions = INSTRUCTIONS_NONE;
			return;
		}

		String metaData = StringUtils.substringAfter(valueComplex, PIPE_WITH_LEADING_AND_TRAILING_SPACE);
		String[] metaParts = metaData.split(Pattern.quote(PIPE_WITH_LEADING_AND_TRAILING_SPACE));

		String fileNameAndKey = null;

		// parse instructions
		if (metaParts.length > 0) {
			instructions = metaParts[0];
			if (!isValidInstructions(instructions)) {
				instructions = INSTRUCTIONS_NONE;
			}
		}
		// parse mime type
		if (metaParts.length > 1) {
			mimeType = metaParts[1];
			if (!isValidMimeType(mimeType)) {
				mimeType = AttachmentsConstants.UNKNOWN_MIME_TYPE;
			}
		}
		// this block pulls out the filename and key set
		if (metaParts.length > 2) {
			String[] fileNameParts = Arrays.copyOfRange(metaParts, 2, metaParts.length);
			fileNameAndKey = StringUtils.join(fileNameParts, PIPE_WITH_LEADING_AND_TRAILING_SPACE);
		}

		// now parse the filename and key set
		if (StringUtils.isNotBlank(fileNameAndKey)
				&& PIPE_WITH_LEADING_SPACE_AND_NO_TRAILING_SPACE_REGEX.matcher(fileNameAndKey).find()) {
			// new model, contains filename and path
			String[] fileNameAndKeyParts = fileNameAndKey.split(Pattern.quote(PIPE_WITH_LEADING_SPACE));
			fileName = fileNameAndKeyParts[0];
			key = fileNameAndKeyParts[1];
			// strip off the "file" or "image" suffix, if present
			if (fileName.endsWith(SPACE + BINARY_DATA_FILE_TYPE)) {
				fileName = fileName.replaceFirst(SPACE + BINARY_DATA_FILE_TYPE + "$", "");
			} else if (fileName.endsWith(SPACE + IMAGE_FILE_TYPE)) {
				fileName = fileName.replaceFirst(SPACE + IMAGE_FILE_TYPE + "$", "");
			}
		} else {
			// old model, just filename, no key/path
			fileName = fileNameAndKey;
		}

	}

	public ValueComplex(String instructions, String mimeType, String coreValueComplex) {
		this(buildValueComplex(instructions, mimeType, coreValueComplex, null));
	}

	@Override
	public String toString() {
		return buildValueComplex(instructions, mimeType, fileName, key);
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

	public String getKey() {
		return key;
	}

	// return the valueComplex in the format expected by the Core handlers (without
	// the additional metadata fields for instructions and mimeType)
	public String getSimplifiedValueComplex() {
		return buildSimplifiedValueComplex(fileName, key);
	}

	public static String buildSimplifiedValueComplex(String fileName, String key) {
		return fileName + (key != null ? (PIPE_WITH_LEADING_SPACE + key) : "");
	}

	public String getValueComplex() {
		return buildValueComplex(instructions, mimeType, fileName, key);
	}

	public static String buildValueComplex(String instructions, String mimeType, String savedFileName, String key) {
		if (StringUtils.isBlank(key)) {
			// old format, prior to Core 2.8, no key, just filename
			return UNIQUE_PREFIX + PIPE_WITH_LEADING_AND_TRAILING_SPACE + instructions
					+ PIPE_WITH_LEADING_AND_TRAILING_SPACE + mimeType + PIPE_WITH_LEADING_AND_TRAILING_SPACE
					+ savedFileName;
		} else {
			// new format
			return UNIQUE_PREFIX + PIPE_WITH_LEADING_AND_TRAILING_SPACE + instructions
					+ PIPE_WITH_LEADING_AND_TRAILING_SPACE + mimeType + PIPE_WITH_LEADING_AND_TRAILING_SPACE
					+ savedFileName + PIPE_WITH_LEADING_SPACE + key;
		}

	}

	protected static boolean isValidInstructions(String str) {
		return StringUtils.startsWith(str, AttachmentsConstants.INSTRUCTIONS_PREFIX + ".");
	}

	protected static boolean isValidMimeType(String str) {
		return isMimeTypeHandled(str);
	}
}
