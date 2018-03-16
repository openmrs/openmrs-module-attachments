package org.openmrs.module.attachments.rest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.module.attachments.AttachmentsConstants;
import org.openmrs.module.attachments.AttachmentsContext;
import org.openmrs.module.attachments.ComplexObsSaver;
import org.openmrs.module.attachments.obs.Attachment;
import org.openmrs.module.attachments.obs.ValueComplex;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.api.RestService;
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
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.EncounterResource1_8;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.PatientResource1_8;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.openmrs.module.attachments.AttachmentsContext.getContentFamily;

@Resource(name = RestConstants.VERSION_1 + "/attachment", supportedClass = Attachment.class, supportedOpenmrsVersions = {
        "1.10.*", "1.11.*", "1.12.*" })
public class AttachmentResource1_10 extends DataDelegatingCrudResource<Attachment> implements Uploadable {
	
	protected static final String REASON = "REST web service";
	
	protected final Log log = LogFactory.getLog(getClass());
	
	ComplexObsSaver obsSaver = Context.getRegisteredComponent(AttachmentsConstants.COMPONENT_COMPLEXOBS_SAVER,
	    ComplexObsSaver.class);
	
	AttachmentsContext attachmentsContext = Context.getRegisteredComponent(AttachmentsConstants.COMPONENT_ATT_CONTEXT,
	    AttachmentsContext.class);
	
	@Override
	public Attachment newDelegate() {
		return new Attachment();
	}
	
	@Override
	public Attachment save(Attachment delegate) {
		Obs obs = Context.getObsService().saveObs(delegate.getObs(), REASON);
		return new Attachment(obs);
	}
	
	@Override
	public Attachment getByUniqueId(String uniqueId) {
		Obs obs = Context.getObsService().getObsByUuid(uniqueId);
		if (!obs.isComplex())
			throw new GenericRestException(uniqueId + " does not identify a complex obs.", null);
		else {
			obs = Context.getObsService().getComplexObs(obs.getId(), AttachmentsConstants.ATT_VIEW_CRUD);
			return new Attachment(obs);
		}
	}
	
	/**
	 * Gets attachements by patient or encounter (paged according to context if necessary) only if a
	 * patient or encounter parameter exists respectively in the request set on the
	 * {@link RequestContext} otherwise searches for obs that match the specified query
	 *
	 * @param context
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doSearch(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected PageableResult doSearch(RequestContext context) {
		String patientUuid = context.getRequest().getParameter("patient");
		if (patientUuid != null) {
			Patient patient = ((PatientResource1_8) Context.getService(RestService.class)
			        .getResourceBySupportedClass(Patient.class)).getByUniqueId(patientUuid);
			if (patient == null)
				return new EmptySearchResult();
			List<Obs> obs = Context.getObsService().getObservationsByPerson(patient);
			return new NeedsPaging<Obs>(obs, context);
		}
		
		String encounterUuid = context.getRequest().getParameter("encounter");
		if (encounterUuid != null) {
			Encounter enc = ((EncounterResource1_8) Context.getService(RestService.class)
			        .getResourceBySupportedClass(Encounter.class)).getByUniqueId(encounterUuid);
			if (enc == null)
				return new EmptySearchResult();
			List<Obs> obs = new ArrayList<Obs>(enc.getAllObs());
			return new NeedsPaging<Obs>(obs, context);
		}
		
		return new NeedsPaging<Obs>(Context.getObsService().getObservations(context.getParameter("q")), context);
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
		Provider provider = Context.getProviderService().getProviderByUuid(context.getParameter("provider"));
		String fileCaption = context.getParameter("fileCaption");
		String instructions = context.getParameter("instructions");
		
		// Verify Parameters
		if (patient == null) {
			throw new IllegalRequestException("A patient parameter must be provided when uploading an attachment.");
		}
		
		if (StringUtils.isEmpty(instructions))
			instructions = ValueComplex.INSTRUCTIONS_DEFAULT;
		
		Encounter encounter = null;
		if (visit != null) {
			encounter = attachmentsContext.getAttachmentEncounter(patient, visit, provider);
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
		description.addProperty("comment");
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
}
