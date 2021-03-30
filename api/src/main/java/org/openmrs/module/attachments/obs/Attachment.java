package org.openmrs.module.attachments.obs;

import java.util.Date;

import org.openmrs.BaseOpenmrsData;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.attachments.AttachmentsConstants.ContentFamily;
import org.openmrs.module.attachments.AttachmentsContext;
import org.openmrs.obs.ComplexData;

/**
 * Attachment represents all the parts of a complex obs that make an "attachment".
 * 
 * @author Mekom Solutions
 */
public class Attachment extends BaseOpenmrsData implements java.io.Serializable {
	
	private static final long serialVersionUID = -3552798988737497690L;
	
	protected Integer id = null;
	
	protected Date dateTime = null;
	
	protected String comment = "";
	
	protected String bytesMimeType = null;
	
	protected ContentFamily bytesContentFamily = null;
	
	protected ComplexData complexData = null;
	
	public Attachment() {
	}
	
	/**
	 * @param obs A complex obs
	 */
	public Attachment(Obs obs) {
		super();
		
		setUuid(obs.getUuid());
		setId(obs.getId());
		setCreator(obs.getCreator());
		setDateCreated(obs.getDateCreated());
		setChangedBy(obs.getChangedBy());
		setDateChanged(obs.getDateChanged());
		setVoided(obs.getVoided());
		setVoidedBy(obs.getVoidedBy());
		setVoidReason(obs.getVoidReason());
		
		setDateTime(obs.getObsDatetime());
		setComment(obs.getComment());
		setComplexData(obs.getComplexData());
	}
	
	/**
	 * @param obs A complex obs
	 */
	public Attachment(Obs obs, ComplexDataHelper complexDataHelper) {
		this(obs);
		
		setBytesMimeType(complexDataHelper.getContentType(obs.getComplexData()));
		setBytesContentFamily(AttachmentsContext.getContentFamily(complexDataHelper.getContentType(obs.getComplexData())));
		
	}
	
	public Obs getObs() {
		Obs obs = Context.getObsService().getObsByUuid(getUuid());
		if (obs == null) {
			obs = new Obs();
			obs.setUuid(getUuid());
			obs.setId(getId());
			obs.setCreator(getCreator());
			obs.setDateCreated(getDateCreated());
			obs.setChangedBy(getChangedBy());
			obs.setDateChanged(getDateChanged());
			obs.setVoided(getVoided());
			obs.setVoidedBy(getVoidedBy());
			obs.setVoidReason(getVoidReason());
		}
		obs.setObsDatetime(getDateTime());
		obs.setComment(getComment());
		obs.setComplexData(getComplexData());
		return obs;
	}
	
	@Override
	public Integer getId() {
		return id;
	}
	
	@Override
	public void setId(Integer id) {
		this.id = id;
	}
	
	public Date getDateTime() {
		return dateTime;
	}
	
	public void setDateTime(Date dateTime) {
		this.dateTime = dateTime;
	}
	
	public String getComment() {
		return comment;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public ComplexData getComplexData() {
		return complexData;
	}
	
	public void setComplexData(ComplexData complexData) {
		this.complexData = complexData;
	}
	
	public void setBytesMimeType(String bytesMimeType) {
		this.bytesMimeType = bytesMimeType;
	}
	
	public String getBytesMimeType() {
		return bytesMimeType;
	}
	
	public ContentFamily getBytesContentFamily() {
		return bytesContentFamily;
	}
	
	public void setBytesContentFamily(ContentFamily bytesContentFamily) {
		this.bytesContentFamily = bytesContentFamily;
	}
}
