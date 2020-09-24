package org.openmrs.module.attachments.rest;

import java.util.Arrays;
import java.util.List;

import org.hibernate.FlushMode;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.attachments.AttachmentsConstants;
import org.openmrs.module.attachments.obs.Attachment;
import org.openmrs.module.emrapi.db.DbSessionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.GenericRestException;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.DateProperty;
import io.swagger.models.properties.DateTimeProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;

/**
 * {@link Resource} for Attachment, supporting standard CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + "/attachment", supportedClass = Attachment.class, supportedOpenmrsVersions = {
        "2.0.0" })
public class AttachmentResource2_0 extends AttachmentResource1_10 {
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#getRepresentationDescription(org.openmrs.module.webservices.rest.web.representation.Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("dateTime");
			description.addProperty("comment");
			description.addProperty("complexData");
			description.addSelfLink();
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("dateTime");
			description.addProperty("comment");
			description.addProperty("auditInfo");
			description.addProperty("complexData");
			description.addSelfLink();
			return description;
		} else if (rep instanceof RefRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("comment");
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getCreatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addRequiredProperty("uuid");
		description.addRequiredProperty("display");
		description.addProperty("comment");
		description.addProperty("dateTime");
		description.addProperty("complexData");
		
		return description;
	}
	
	/**
	 * @throws org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getUpdatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getUpdatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("uuid");
		description.addProperty("display");
		description.addProperty("comment");
		description.addProperty("dateTime");
		
		description.addRequiredProperty("complexData");
		
		return description;
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = (ModelImpl) super.getGETModel(rep);
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			model.property("uuid", new StringProperty()).property("display", new StringProperty())
			        .property("gender", new StringProperty()._enum("M")._enum("F")).property("age", new IntegerProperty())
			        .property("birthdate", new DateTimeProperty()).property("birthdateEstimated", new BooleanProperty())
			        .property("dead", new BooleanProperty()).property("deathDate", new DateProperty())
			        .property("causeOfDeath", new StringProperty())
			        .property("attributes", new ArrayProperty(new RefProperty("#/definitions/AttachmentAttributeGetRef")))
			        .property("voided", new BooleanProperty());
		}
		if (rep instanceof DefaultRepresentation) {
			model.property("preferredName", new RefProperty("#/definitions/AttachementNameGetRef"))
			        .property("preferredAddress", new RefProperty("#/definitions/AttachementAddressGetRef"));
			
		} else if (rep instanceof FullRepresentation) {
			model.property("preferredName", new RefProperty("#/definitions/AttachmentNameGet"))
			        .property("preferredAddress", new RefProperty("#/definitions/AttachmentAddressGet"))
			        .property("names", new ArrayProperty(new RefProperty("#/definitions/AttachmentNameGet")))
			        .property("addresses", new ArrayProperty(new RefProperty("#/definitions/AttachmentAddressGet")));
		}
		return model;
	}
	
	@Override
	public Model getCREATEModel(Representation representation) {
		ModelImpl model = new ModelImpl()
		        .property("names", new ArrayProperty(new RefProperty("#/definitions/AttachmentNameCreate")))
		        .property("gender", new StringProperty()._enum("M")._enum("F")).property("age", new IntegerProperty())
		        .property("birthdate", new DateProperty())
		        .property("birthdateEstimated", new BooleanProperty()._default(false))
		        .property("dead", new BooleanProperty()._default(false)).property("deathDate", new DateProperty())
		        .property("causeOfDeath", new StringProperty())
		        .property("addresses", new ArrayProperty(new RefProperty("#/definitions/AttachmentAddressCreate")))
		        .property("attributes", new ArrayProperty(new RefProperty("#/definitions/AttachmentAttributeCreate")));
		
		model.setRequired(Arrays.asList("names", "gender"));
		return model;
	}
	
	@Override
	public Model getUPDATEModel(Representation representation) {
		return new ModelImpl().property("dead", new BooleanProperty()).property("causeOfDeath", new StringProperty())
		        .property("deathDate", new DateProperty()).property("age", new IntegerProperty())
		        .property("gender", new StringProperty()._enum("M")._enum("F")).property("birthdate", new DateProperty())
		        .property("birthdateEstimated", new BooleanProperty()._default(false))
		        .property("preferredName", new StringProperty().example("uuid"))
		        .property("preferredAddress", new StringProperty().example("uuid"))
		        .property("attributes", new ArrayProperty(new RefProperty("#/definitions/AttachmentAttributeCreate")))
		        
		        .required("dead").required("causeOfDeath");
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getPropertiesToExposeAsSubResources()
	 */
	@Override
	public List<String> getPropertiesToExposeAsSubResources() {
		return Arrays.asList("comment", "complexData", "Attributes");
	}
	
	@Override
	public Attachment newDelegate() {
		return new Attachment();
	}
	
	@SuppressWarnings("deprecation")
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
	public Attachment save(Attachment delegate) {
		FlushMode flushMode = DbSessionUtil.getCurrentFlushMode();
		DbSessionUtil.setManualFlushMode();
		Attachment attachment = new Attachment();
		try {
			Obs obs = Context.getObsService().saveObs(delegate.getObs(), REASON);
			attachment = new Attachment(obs);
		}
		finally {
			DbSessionUtil.setFlushMode(flushMode);
		}
		return attachment;
	}
	
}
