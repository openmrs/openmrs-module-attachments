package org.openmrs.module.attachments.rest;

import static org.openmrs.module.attachments.AttachmentsContext.getContentFamily;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.api.APIException;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.module.attachments.AttachmentsConstants;
import org.openmrs.module.attachments.AttachmentsContext;
import org.openmrs.module.attachments.AttachmentsService;
import org.openmrs.module.attachments.ComplexObsSaver;
import org.openmrs.module.attachments.obs.Attachment;
import org.openmrs.module.attachments.obs.ComplexDataHelper;
import org.openmrs.module.attachments.obs.ComplexDataHelper1_10;
import org.openmrs.module.attachments.obs.ValueComplex;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.CustomRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.api.Uploadable;
import org.openmrs.module.webservices.rest.web.resource.impl.DataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.EmptySearchResult;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.GenericRestException;
import org.openmrs.module.webservices.rest.web.response.IllegalRequestException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.springframework.web.multipart.MultipartFile;

@Resource(name = RestConstants.VERSION_1 + "/"
        + AttachmentsConstants.ATTACHMENT_URI, supportedClass = Attachment.class, supportedOpenmrsVersions = { "1.10.*",
                "1.11.*", "1.12.*" })
public class AttachmentResource1_10 extends DataDelegatingCrudResource<Attachment> implements Uploadable {
	
	protected static final String REASON = "REST web service";
	
	private ComplexObsSaver obsSaver = Context.getRegisteredComponent(AttachmentsConstants.COMPONENT_COMPLEXOBS_SAVER,
	    ComplexObsSaver.class);
	
	private AttachmentsContext ctx = Context.getRegisteredComponent(AttachmentsConstants.COMPONENT_ATT_CONTEXT,
	    AttachmentsContext.class);
	
	@Override
	public Attachment newDelegate() {
		return new Attachment();
	}
	
	@Override
	public Attachment save(Attachment delegate) {
		Obs obs = Context.getObsService().saveObs(delegate.getObs(), REASON);
		return new Attachment(obs, ctx.getComplexDataHelper());
	}
	
	@Override
	public Attachment getByUniqueId(String uniqueId) {
		Obs obs = Context.getObsService().getObsByUuid(uniqueId);
		if (!obs.isComplex())
			throw new GenericRestException(uniqueId + " does not identify a complex obs.", null);
		else {
			obs = Context.getObsService().getComplexObs(obs.getId(), AttachmentsConstants.ATT_VIEW_CRUD);
			return new Attachment(obs, ctx.getComplexDataHelper());
		}
	}
	
	@Override
	protected void delete(Attachment delegate, String reason, RequestContext context) throws ResponseException {
		String encounterUuid = delegate.getObs().getEncounter() != null ? delegate.getObs().getEncounter().getUuid() : null;
		Context.getObsService().voidObs(delegate.getObs(), REASON);
		voidEncounterIfEmpty(Context.getEncounterService(), encounterUuid);
	}
	
	@Override
	public void purge(Attachment delegate, RequestContext context) throws ResponseException {
		String encounterUuid = delegate.getObs().getEncounter() != null ? delegate.getObs().getEncounter().getUuid() : null;
		Context.getObsService().purgeObs(delegate.getObs());
		voidEncounterIfEmpty(Context.getEncounterService(), encounterUuid);
	}
	
	@Override
	public Object upload(MultipartFile file, RequestContext context) throws ResponseException, IOException {
		
		// Prepare Parameters
		Patient patient = Context.getPatientService().getPatientByUuid(context.getParameter("patient"));
		Visit visit = Context.getVisitService().getVisitByUuid(context.getParameter("visit"));
		Encounter encounter = Context.getEncounterService().getEncounterByUuid(context.getParameter("encounter"));
		Provider provider = Context.getProviderService().getProviderByUuid(context.getParameter("provider"));
		String fileCaption = context.getParameter("fileCaption");
		String instructions = context.getParameter("instructions");
		String base64Content = context.getParameter("base64Content");
		
		if (base64Content != null) {
			file = new Base64MultipartFile(base64Content);
		}
		// Verify File Size
		if (ctx.getMaxUploadFileSize() * 1024 * 1024 < (double) file.getSize()) {
			throw new IllegalRequestException("The file  exceeds the maximum size");
		}
		
		// Verify Parameters
		if (patient == null) {
			throw new IllegalRequestException("A patient parameter must be provided when uploading an attachment.");
		}
		
		if (StringUtils.isEmpty(instructions))
			instructions = ValueComplex.INSTRUCTIONS_DEFAULT;
		
		// Verify Parameters
		if (encounter != null && visit != null) {
			if (encounter.getVisit() != visit) {
				throw new IllegalRequestException(
				        "The specified encounter does not belong to the provided visit, upload aborted.");
			}
		}
		
		if (visit != null && encounter == null) {
			encounter = ctx.getAttachmentEncounter(patient, visit, provider);
		}
		
		if (encounter != null && visit == null) {
			visit = encounter.getVisit();
		}
		
		// Save Obs
		Obs obs;
		switch (getContentFamily(file.getContentType())) {
			case IMAGE:
				obs = obsSaver.saveImageAttachment(visit, patient, encounter, fileCaption, file, instructions);
				break;
			
			case OTHER:
			default:
				obs = obsSaver.saveOtherAttachment(visit, patient, encounter, fileCaption, file, instructions);
				break;
		}
		
		return ConversionUtil.convertToRepresentation(obs,
		    new CustomRepresentation(AttachmentsConstants.REPRESENTATION_OBS));
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("comment");
		return description;
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("uuid");
		description.addProperty("dateTime");
		description.addProperty("comment");
		description.addProperty("bytesMimeType");
		description.addProperty("bytesContentFamily");
		description.addSelfLink();
		return description;
	}
	
	/**
	 * Voids the encounter if it contains no non-voided obs.
	 *
	 * @param encounterService
	 * @param encounterUuid
	 */
	public static void voidEncounterIfEmpty(EncounterService encounterService, String encounterUuid) {
		Encounter encounter = encounterService.getEncounterByUuid(encounterUuid);
		if (encounter != null && encounter.getAllObs().size() == 0) {
			encounterService.voidEncounter(encounter, "foo");
		}
	}
	
	/**
	 * Get the Attachments using AttachmentService.
	 *
	 * @param as specifies the AttachmentService instance.
	 * @param patient
	 * @param visit
	 * @param encounter
	 * @param includeEncounterless
	 * @param includeVoided
	 */
	public List<Attachment> search(AttachmentsService as, Patient patient, Visit visit, Encounter encounter,
	        String includeEncounterless, boolean includeVoided) {
		
		List<Attachment> attachmentList = new ArrayList<>();
		
		if (includeEncounterless != null) {
			if (includeEncounterless.equals("only")) {
				attachmentList = as.getEncounterlessAttachments(patient, includeVoided);
				
			} else {
				attachmentList = as.getAttachments(patient, BooleanUtils.toBoolean(includeEncounterless), includeVoided);
			}
		} else {
			if (encounter != null && visit == null) {
				attachmentList = as.getAttachments(patient, encounter, includeVoided);
			}
			if (visit != null && encounter == null) {
				attachmentList = as.getAttachments(patient, visit, includeVoided);
			}
			if (encounter == null && visit == null) {
				attachmentList = as.getAttachments(patient, includeVoided);
			}
			
		}
		return attachmentList;
	}
	
	/**
	 * Get Attachments by given parameters (paged according to context if necessary) only if a patient
	 * parameter exists in the request set on the {@link RequestContext}, optional encounter, visit ,
	 * includeEncounterless , includeVoided request parameters can be specified to filter the
	 * attachments.
	 *
	 * @param context
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doSearch(org.openmrs.module.webservices.rest.web.RequestContext)
	 * @return Attachments based on the user parameters
	 */
	@Override
	protected PageableResult doSearch(RequestContext context) {
		
		// Prepare Parameters
		Patient patient = Context.getPatientService().getPatientByUuid(context.getParameter("patient"));
		Visit visit = Context.getVisitService().getVisitByUuid(context.getParameter("visit"));
		Encounter encounter = Context.getEncounterService().getEncounterByUuid(context.getParameter("encounter"));
		String includeEncounterless = context.getParameter("includeEncounterless");
		Boolean includeVoided = BooleanUtils.toBoolean(context.getParameter("includeVoided"));
		
		// Verify Parameters
		if (patient == null) {
			throw new IllegalRequestException("A patient parameter must be provided when searching the attachments.");
		}
		
		if (includeVoided == null) {
			includeVoided = false;
		}
		
		// Search Attachments
		List<Attachment> attachmentList = search(ctx.getAttachmentsService(), patient, visit, encounter,
		    includeEncounterless, includeVoided);
		
		if (attachmentList != null) {
			return new NeedsPaging<Attachment>(attachmentList, context);
		}
		return new EmptySearchResult();
	}
	
	/**
	 * Wrapper class to be passed to ComplexObsSaver#saveImageAttachment
	 * ComplexObsSaver#saveImageAttachment needs a MultipartFile but the image could either be a
	 * MultipartFile or a base64 encoded image. This class will only implement the methods used by
	 * ComplexObsSaver#saveImageAttachment. This way we won't have to make any changes to the
	 * implementation of ComplexObsSaver#saveImageAttachment and will also make very little change to
	 * the AttachmentResource1_10#upload implementation. This is also helps us avoid adding an extra
	 * dependency to MockMultipartFile for converting the base64 encoded String to a MultipartFile
	 * object.
	 */
	class Base64MultipartFile implements MultipartFile {
		
		private String fileName;
		
		private String contentType;
		
		private long size;
		
		private InputStream in;
		
		private byte[] bytes;
		
		public Base64MultipartFile(String base64Image) throws IOException {
			String[] parts = base64Image.split(",");
			String contentType = parts[0].split(":")[1].split(";")[0].trim();
			String contents = parts[1].trim();
			byte[] decodedImage = Base64.decodeBase64(contents.getBytes());
			final File temp = File.createTempFile("cameracapture", ".png");
			try (OutputStream stream = new FileOutputStream(temp)) {
				stream.write(decodedImage);
			}
			temp.deleteOnExit();
			
			this.fileName = temp.getName();
			this.in = new FileInputStream(temp);
			this.contentType = contentType;
			this.bytes = decodedImage;
			this.size = temp.length();
		}
		
		@Override
		public String getName() {
			return this.fileName;
		}
		
		@Override
		public String getOriginalFilename() {
			return this.fileName;
		}
		
		@Override
		public String getContentType() {
			return this.contentType;
		}
		
		@Override
		public boolean isEmpty() {
			return false;
		}
		
		@Override
		public long getSize() {
			return this.size;
		}
		
		@Override
		public byte[] getBytes() throws IOException {
			return this.bytes;
		}
		
		@Override
		public InputStream getInputStream() throws IOException {
			return this.in;
		}
		
		@Override
		public void transferTo(File dest) throws IOException, IllegalStateException {
			throw new APIException("Operation transferTo is not supported for Base64MultipartFile");
		}
	}
	
}
