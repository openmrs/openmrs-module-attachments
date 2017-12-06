package org.openmrs.module.attachments.obs;

import java.io.File;
import java.io.IOException;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Obs;
import org.openmrs.api.APIException;
import org.openmrs.module.attachments.AttachmentsConstants;
import static org.openmrs.module.attachments.obs.ImageAttachmentHandler.THUMBNAIL_MAX_HEIGHT;
import static org.openmrs.module.attachments.obs.ImageAttachmentHandler.THUMBNAIL_MAX_WIDTH;
import org.openmrs.obs.ComplexData;
import org.openmrs.obs.ComplexObsHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Double inheritance class. The actual implementation parent must be set through
 * {@link #setParentComplexObsHandler()}.
 */
public abstract class AbstractAttachmentHandler implements ComplexObsHandler {
	
	public final static String NO_THUMBNAIL_SUFFIX = "___nothumb__";
	
	public final static String THUMBNAIL_SUFFIX = "_thumb";
	
	protected final Log log = LogFactory.getLog(getClass());
	
	private ComplexObsHandler parent;
	
	@Autowired
	@Qualifier(AttachmentsConstants.COMPONENT_COMPLEXDATA_HELPER)
	private ComplexDataHelper complexDataHelper;
	
	public AbstractAttachmentHandler() {
		super();
		setParentComplexObsHandler();
	}
	
	protected ComplexDataHelper getComplexDataHelper() {
		return complexDataHelper;
	}
	
	/*
	 * To set the "real" implementation parent.
	 */
	abstract protected void setParentComplexObsHandler();
	
	/*
	 * Complex data CRUD - Read
	 */
	abstract protected ComplexData readComplexData(Obs obs, ValueComplex valueComplex, String view);
	
	/*
	 * Complex data CRUD - Delete
	 */
	abstract protected boolean deleteComplexData(Obs obs, AttachmentComplexData complexData);
	
	/*
	 * Complex data CRUD - Save (Update)
	 */
	abstract protected ValueComplex saveComplexData(Obs obs, AttachmentComplexData complexData);
	
	protected void setParent(ComplexObsHandler complexObsHandler) {
		this.parent = complexObsHandler;
	}
	
	final protected ComplexObsHandler getParent() {
		return parent;
	}
	
	public static boolean isThumbnail(String fileName) {
		return StringUtils.endsWith(FilenameUtils.removeExtension(fileName), NO_THUMBNAIL_SUFFIX);
	}
	
	/*
	 * Appends NO_THUMBNAIL_SUFFIX the file
	 */
	protected static String buildNoThumbnailFileFileName(String fileName) {
		if (StringUtils.endsWith(FilenameUtils.removeExtension(fileName), NO_THUMBNAIL_SUFFIX)) {
			return fileName;
		} else {
			return FilenameUtils.removeExtension(fileName) + NO_THUMBNAIL_SUFFIX + "."
			        + FilenameUtils.getExtension(fileName);
		}
	}
	
	/*
	 * Appends THUMBNAIL_SUFFIX the file
	 */
	public static String buildThumbnailFileName(String fileName) {
		return FilenameUtils.removeExtension(fileName) + "_thumb" + "." + FilenameUtils.getExtension(fileName);
	}
	
	/**
	 * <p>
	 * The saveThumbnailOrRename method checks image dimension to see if the image is small enough to be
	 * its own thumbnail. If so, it will rename the image file by appending the
	 * <b>NO_THUMBNAIL_SUFFIX</b> to the file. Otherewise, it will create a small thumbnail file
	 * alongside the original file to be used as thumbnail image.
	 * </p>
	 *
	 * @param savedFile original file pointer
	 * @param imageHight image hight
	 * @param imageWidth image width
	 * @return savedFileName new renamed file name or orignal file name
	 */
	public static String saveThumbnailOrRename(File savedFile, int imageHeight, int imageWidth) {
		
		String savedFileName = savedFile.getName();
		
		if ((imageHeight <= THUMBNAIL_MAX_HEIGHT) && (imageWidth <= THUMBNAIL_MAX_WIDTH)) {
			String newSavedFileName = buildNoThumbnailFileFileName(savedFile.getAbsolutePath());
			File newSavedFile = new File(newSavedFileName);
			savedFile.renameTo(newSavedFile);
			savedFileName = buildNoThumbnailFileFileName(savedFileName);
		} else {
			File dir = savedFile.getParentFile();
			String thumbnailFileName = buildThumbnailFileName(savedFileName);
			try {
				Thumbnails.of(savedFile.getAbsolutePath()).size(THUMBNAIL_MAX_HEIGHT, THUMBNAIL_MAX_WIDTH)
				        .toFile(new File(dir, thumbnailFileName));
			}
			catch (IOException e) {
				throw new APIException("A thumbnail file could not be saved for obs with", e);
			}
		}
		
		return savedFileName;
	}
	
	/**
	 * @param complexData An obs's complex data.
	 * @return null if this is not our implementation, the custom {@link DocumentComplexData_old}
	 *         otherwise.
	 */
	public static AttachmentComplexData fetchAttachmentComplexData(ComplexData complexData) {
		
		if ((complexData instanceof AttachmentComplexData) == false) {
			return null;
		}
		
		AttachmentComplexData docData = (AttachmentComplexData) complexData;
		String instructions = docData.getInstructions();
		if (instructions.equals(ValueComplex.INSTRUCTIONS_NONE)) {
			return null;
		}
		
		return docData;
	}
	
	/*
	 * Drifts to our own CRUD overloadable routine when it is our implementation.
	 */
	@Override
	final public Obs getObs(Obs obs, String view) {
		
		ValueComplex valueComplex = new ValueComplex(obs.getValueComplex());
		if (valueComplex.isOwnImplementation() == false) { // not our implementation
			return getParent().getObs(obs, view);
		}
		
		if (StringUtils.isEmpty(view)) {
			view = AttachmentsConstants.ATT_VIEW_ORIGINAL;
		}
		
		ComplexData docData = readComplexData(obs, valueComplex, view);
		obs.setComplexData(docData);
		return obs;
	}
	
	/*
	 * Drifts to our own CRUD overloadable routine when it is our implementation.
	 */
	@Override
	final public boolean purgeComplexData(Obs obs) {
		
		AttachmentComplexData complexData = fetchAttachmentComplexData(obs.getComplexData());
		if (complexData == null) { // not our implementation
			if (obs.getComplexData() == null) {
				log.error("Complex data was null and hence was not purged for OBS_ID='" + obs.getObsId() + "'.");
				return false;
			} else {
				return getParent().purgeComplexData(obs);
			}
		}
		
		return deleteComplexData(obs, complexData);
	}
	
	/*
	 * Drifts to our own CRUD overloadable routine when it is our implementation.
	 */
	@Override
	final public Obs saveObs(Obs obs) {
		
		AttachmentComplexData complexData = fetchAttachmentComplexData(obs.getComplexData());
		if (complexData == null) { // not our implementation
			if (obs.getComplexData() == null) {
				log.error("Complex data was null and hence was not saved for OBS_ID='" + obs.getObsId() + "'.");
				return obs;
			} else {
				return getParent().saveObs(obs);
			}
		}
		
		ValueComplex valueComplex = saveComplexData(obs, complexData);
		obs.setValueComplex(valueComplex.getValueComplex());
		return obs;
	}
}
