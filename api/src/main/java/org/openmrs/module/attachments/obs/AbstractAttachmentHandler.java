package org.openmrs.module.attachments.obs;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Obs;
import org.openmrs.module.attachments.AttachmentsConstants;
import org.openmrs.obs.ComplexData;
import org.openmrs.obs.ComplexObsHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Double inheritance class. The actual implementation parent must be set through
 * {@link #setParentComplexObsHandler()}.
 */
public abstract class AbstractAttachmentHandler implements ComplexObsHandler {
	
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
	
	public static String buildThumbnailFileName(String fileName) {
		return FilenameUtils.removeExtension(fileName) + "_thumb" + "." + FilenameUtils.getExtension(fileName);
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
