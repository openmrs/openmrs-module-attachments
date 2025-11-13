package org.openmrs.module.attachments.obs;

import static org.openmrs.module.attachments.AttachmentsConstants.ATT_VIEW_ORIGINAL;
import static org.openmrs.module.attachments.AttachmentsConstants.ATT_VIEW_THUMBNAIL;

import java.util.Arrays;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Obs;
import org.openmrs.api.StorageService;
import org.openmrs.module.attachments.AttachmentsConstants;
import org.openmrs.obs.ComplexData;
import org.openmrs.obs.ComplexObsHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Double inheritance class. The actual implementation parent must be set
 * through {@link #setParentComplexObsHandler()}.
 */
public abstract class AbstractAttachmentHandler implements ComplexObsHandler {

	private static final String[] supportedViews = {ATT_VIEW_ORIGINAL, ATT_VIEW_THUMBNAIL};

	public final static String THUMBNAIL_SUFFIX = "_thumb";

	public final static int THUMBNAIL_MAX_HEIGHT = 200;

	public final static int THUMBNAIL_MAX_WIDTH = THUMBNAIL_MAX_HEIGHT;

	protected final Log log = LogFactory.getLog(getClass());

	private ComplexObsHandler parent;

	@Autowired
	protected StorageService storageService;

	@Autowired
	@Qualifier(AttachmentsConstants.COMPONENT_COMPLEXDATA_HELPER)
	private ComplexDataHelper complexDataHelper;

	@Autowired
	@Qualifier(AttachmentsConstants.COMPONENT_COMPLEXVIEW_HELPER)
	private ComplexViewHelper complexViewHelper;

	/*
	 * Appends THUMBNAIL_SUFFIX to a file name or storage service key
	 */
	public static String appendThumbnailSuffix(String fileNameOrKey) {
		String fileName = FilenameUtils.removeExtension(fileNameOrKey);
		String ext = FilenameUtils.getExtension(fileNameOrKey);
		return fileName + THUMBNAIL_SUFFIX + (StringUtils.isEmpty(ext) ? "" : "." + ext);
	}

	public void setComplexViewHelper(ComplexViewHelper complexViewHelper) {
		this.complexViewHelper = complexViewHelper;
	}

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
	abstract protected boolean deleteComplexData(Obs obs);

	/*
	 * Complex data CRUD - Save (Update)
	 */
	abstract protected ValueComplex saveComplexData(Obs obs, AttachmentComplexData complexData);

	public String[] getSupportedViews() {
		return supportedViews;
	}

	public boolean supportsView(String view) {
		return Arrays.asList(getSupportedViews()).contains(view);
	}

	protected void setParent(ComplexObsHandler complexObsHandler) {
		this.parent = complexObsHandler;
	}

	final protected ComplexObsHandler getParent() {
		return parent;
	}

	public AttachmentComplexData getAttachmentComplexData(ComplexData complexData) {

		if (!(complexData instanceof AttachmentComplexData)) {
			return complexDataHelper.build(ValueComplex.INSTRUCTIONS_DEFAULT, complexData.getTitle(),
					complexData.getData(), complexDataHelper.getContentType(complexData));
		}

		return (AttachmentComplexData) complexData;
	}

	/*
	 * Drifts to our own CRUD overloadable routine when it is our implementation.
	 */
	@Override
	final public Obs getObs(Obs obs, String view) {

		ValueComplex valueComplex = new ValueComplex(obs.getValueComplex());

		if (StringUtils.isEmpty(view)) {
			view = AttachmentsConstants.ATT_VIEW_ORIGINAL;
		}

		ComplexData attData = readComplexData(obs, valueComplex, view);
		obs.setComplexData(attData);
		return obs;
	}

	/*
	 * Drifts to our own CRUD overloadable routine when it is our implementation.
	 */
	@Override
	final public boolean purgeComplexData(Obs obs) {
		return deleteComplexData(obs);
	}

	/*
	 * Drifts to our own CRUD overloadable routine when it is our implementation.
	 */
	@Override
	final public Obs saveObs(Obs obs) {

		AttachmentComplexData complexData = getAttachmentComplexData(obs.getComplexData());

		ValueComplex valueComplex = saveComplexData(obs, complexData);
		obs.setValueComplex(valueComplex.getValueComplex());
		return obs;
	}
}
