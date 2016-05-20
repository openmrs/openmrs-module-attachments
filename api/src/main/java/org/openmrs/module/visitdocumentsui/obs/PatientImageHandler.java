package org.openmrs.module.visitdocumentsui.obs;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.coobird.thumbnailator.Thumbnails;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.openmrs.Obs;
import org.openmrs.api.APIException;
import org.openmrs.module.visitdocumentsui.VisitDocumentsConstants;
import org.openmrs.obs.ComplexData;
import org.openmrs.obs.ComplexObsHandler;
import org.openmrs.obs.handler.AbstractHandler;
import org.openmrs.obs.handler.ImageHandler;

public class PatientImageHandler extends ImageHandler implements ComplexObsHandler {

	/*
	 * This prefixes the valueComplex of our custom complex obs.
	 * This allows to discriminate between our implementation and another.
	 */
	private final static String UNIQUE_PREFIX = "m3ks";
	private final static String SEP = " | ";
	
	public final static int THUMBNAIL_HEIGHT = 200;
	public final static int THUMBNAIL_WIDTH = THUMBNAIL_HEIGHT;
	
	// Keys for the metadata map.
	private final static String KEY_INSTRUCTIONS = "instructions";
	private final static String KEY_FILE_NAME = "file_name";
	private final static String KEY_MIME_TYPE = "mime_type";
	
	protected static String buildValueComplex(String instructions, String mimeType, String savedFileName) {
		return UNIQUE_PREFIX + SEP + instructions + SEP + mimeType + SEP + savedFileName;
	}
	
	protected static String buildThumbnailFileName(String fileName) {
		return FilenameUtils.removeExtension(fileName) + "_thumb" + "." + FilenameUtils.getExtension(fileName);
	}
	
	/**
	 * 
	 * @param valueComplex The obs's value complex.
	 * @return null if this is not our implementation, the parsed value complex metadata otherwise. 
	 */
	public static Map<String, String> parseValueComplex(String valueComplex) {
		
		Map<String, String> metaDataMap = new HashMap<String, String>();
		
		if (false == StringUtils.substringBefore(valueComplex, SEP).equals(UNIQUE_PREFIX)) {
			return null;
		}
		
		String metaData = StringUtils.substringAfter(valueComplex, SEP);
		String[] metaParts = StringUtils.split(metaData, SEP);
		int partCount = StringUtils.countMatches(buildValueComplex("", "", ""), SEP);
		if( 3 != partCount || metaParts.length != partCount) {
			// Somehow the metadata is malformed.
			return null;
		}
		
		String instructions = metaParts[0];
		
		if (instructions.equals(PatientImageComplexData.INSTRUCTIONS_NONE)) {
			return null;
		}
		
		metaDataMap.put(KEY_INSTRUCTIONS, instructions);
		metaDataMap.put(KEY_MIME_TYPE, metaParts[1]);
		metaDataMap.put(KEY_FILE_NAME, metaParts[2]);
		
		return metaDataMap;
	}
	
	/**
	 * @param complexData An obs's complex data.
	 * @return null if this is not our implementation, the custom {@link PatientImageComplexData} otherwise.
	 */
	public static PatientImageComplexData fetchPatientImageComplexData(ComplexData complexData) {
		
		if (false == (complexData instanceof PatientImageComplexData)) {
			return null; 
		}
		
		PatientImageComplexData patientImageData = (PatientImageComplexData) complexData;
		String instructions = patientImageData.getInstructions();
		if (instructions.equals(PatientImageComplexData.INSTRUCTIONS_NONE)) {
			return null;
		}
		
		return patientImageData;
	}
	
	/**
	 * Reads {@link Obs#getValueComplex()} to know how to set (where to fetch) the complex data.
	 * In particular the possibly prepended 'instructions'.
	 */
	@Override
	public Obs getObs(Obs obs, String view) {
		
		Map<String, String> metaDataMap = parseValueComplex(obs.getValueComplex());
		if (metaDataMap == null) {	// not our implementation
			return super.getObs(obs, view);
		}
		
		if (StringUtils.isEmpty(view)) {
		   view = VisitDocumentsConstants.DOC_VIEW_ORIGINAL;
		}
		
		String instructions = metaDataMap.get(KEY_INSTRUCTIONS);
		String fileName = metaDataMap.get(KEY_FILE_NAME);
		String mimeType = metaDataMap.get(KEY_MIME_TYPE);
		
		switch (instructions) {
		default:
			{
				PatientImageComplexData patientImageData = getComplexData_Default(obs, view, instructions, fileName, mimeType);
				obs.setComplexData(patientImageData);
			}
		}
		return obs;
	}
	
	@Override
	public boolean purgeComplexData(Obs obs) {
		
		PatientImageComplexData patientImageData = fetchPatientImageComplexData(obs.getComplexData());
		if (patientImageData == null) {	// not our implementation
			return super.purgeComplexData(obs);
		}

		switch (patientImageData.getInstructions()) {
		default:
			{
				return purgeComplexData_Default(obs, patientImageData);
			}
		}
	}
	
	/**
	 * Saves the (non-persistent) complex data and sets the valueComplex accordingly.
	 * In particular if 'instructions' are provided, they are prepended to the value complex string to be persisted.
	 */
	@Override
	public Obs saveObs(Obs obs) {
		
		PatientImageComplexData patientImageData = fetchPatientImageComplexData(obs.getComplexData());
		if (patientImageData == null) {	// not our implementation
			return super.saveObs(obs);
		}

		switch (patientImageData.getInstructions()) {
		default:
			{
				String valueComplex = getValueComplex_Default(obs, patientImageData);
				obs.setValueComplex(valueComplex);
			}
		}
		return obs;
	}
	
	protected PatientImageComplexData getComplexData_Default(Obs obs, String view, String instructions, String fileName, String mimeType) {
		
		if (view.equals(VisitDocumentsConstants.DOC_VIEW_THUMBNAIL)) {
			fileName = buildThumbnailFileName(fileName);
		}
		
		// We invoke the parent to inherit from the file reading routines.
		obs.setValueComplex(fileName);
		obs = super.getObs(obs, "whatever_view");	// ImageHandler doesn't in fact handle several views
		ComplexData complexData = obs.getComplexData();
		
		// Then we build our own custom complex data
		return new PatientImageComplexData(instructions, complexData.getTitle(), complexData.getData(), mimeType);
	}
	
	public boolean purgeComplexData_Default(Obs obs, PatientImageComplexData patientImageData) {
		
		// We use a temp obs whose complex data points to the thumbnail file.
		String thumbnailFileName = buildThumbnailFileName(patientImageData.getTitle());
		Obs tmpObs = new Obs();
		tmpObs.setValueComplex(thumbnailFileName);
		
		boolean isThumbNailPurged = super.purgeComplexData(tmpObs);
		if (isThumbNailPurged == false) {
			log.warn("Could not delete thumbnail image '" + thumbnailFileName + "'.");
		}
		
		// After purging our custom data, we invoke the parent.
		boolean isImagePurged = super.purgeComplexData(obs);
		
		return isThumbNailPurged && isImagePurged;
	}
	
	protected String getValueComplex_Default(Obs obs, PatientImageComplexData patientImageData) {

		// We invoke the parent to inherit from the file saving routines.
		obs = super.saveObs(obs);	

		// Saving the thumbnail
		File savedFile = AbstractHandler.getComplexDataFile(obs);
		String savedFileName = savedFile.getName();
		File dir = savedFile.getParentFile();
		String thumbnailFileName = buildThumbnailFileName(savedFileName);
		try {
			Thumbnails.of(savedFile.getAbsolutePath()).size(THUMBNAIL_HEIGHT, THUMBNAIL_WIDTH).toFile( new File(dir, thumbnailFileName) );
		} catch (IOException e) {
			throw new APIException("A thumbnail file could not be saved for obs with"
					+ "OBS_ID='" + obs.getObsId() + "', "
					+ "FILE='" + savedFile.getPath() + "'.", e);
		}
		
		return buildValueComplex(patientImageData.getInstructions(), patientImageData.getMimeType(), savedFileName);
	}
}
