package org.openmrs.module.attachments.obs;

import static org.openmrs.module.attachments.AttachmentsConstants.ATT_VIEW_ORIGINAL;
import static org.openmrs.module.attachments.AttachmentsConstants.ATT_VIEW_THUMBNAIL;

import java.util.Arrays;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Obs;
import org.openmrs.module.attachments.AttachmentsService;
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

	@Autowired
	protected AttachmentsService attachmentsService;

	@Autowired
	@Qualifier(AttachmentsConstants.COMPONENT_COMPLEXDATA_HELPER)
	private ComplexDataHelper complexDataHelper;

	@Autowired
	@Qualifier(AttachmentsConstants.COMPONENT_COMPLEXVIEW_HELPER)
	private ComplexViewHelper complexViewHelper;

	protected ComplexDataHelper getComplexDataHelper() {
		return complexDataHelper;
	}

	protected ComplexViewHelper getComplexViewHelper() {
		return complexViewHelper;
	}

	@Override
	public String[] getSupportedViews() {
		return supportedViews;
	}

	@Override
	public boolean supportsView(String view) {
		return Arrays.asList(supportedViews).contains(view);
	}

	protected abstract ComplexObsHandler getParent();

	protected abstract ComplexData readComplexData(Obs obs, ValueComplex valueComplex, String view);

	protected abstract boolean deleteComplexData(Obs obs);

	protected abstract ValueComplex saveComplexData(Obs obs, AttachmentComplexData complexData);

	@Override
	public Obs getObs(Obs obs, String view) {
		if (!supportsView(view)) {
			obs.setValueComplex(FilenameUtils.getName(obs.getValueComplex()));
			return obs;
		}
		ValueComplex valueComplex = new ValueComplex(obs.getValueComplex());
		ComplexData complexData = readComplexData(obs, valueComplex, view);
		obs.setComplexData(complexData);
		return obs;
	}

	@Override
	public Obs saveObs(Obs obs) {
		ComplexData complexData = obs.getComplexData();
		if (complexData == null || complexData.getData() == null) {
			throw new IllegalArgumentException("Complex data is required");
		}
		AttachmentComplexData attachmentComplexData = (AttachmentComplexData) complexData.getData();
		ValueComplex valueComplex = saveComplexData(obs, attachmentComplexData);
		obs.setValueComplex(valueComplex.toString());
		obs = getParent().saveObs(obs);
		return obs;
	}

	@Override
	public boolean purgeComplexData(Obs obs) {
		return deleteComplexData(obs);
	}
}