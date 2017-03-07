package org.openmrs.module.visitdocumentsui.rest;

import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.module.visitdocumentsui.VisitDocumentsConstants;
import org.openmrs.module.visitdocumentsui.obs.VisitDocument;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.GenericRestException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

@Resource(name = RestConstants.VERSION_1 + "/visitdocument", supportedClass = VisitDocument.class,
supportedOpenmrsVersions = {"1.10.*", "1.11.*", "1.12.*"})
public class VisitDocumentResource1_10 extends DataDelegatingCrudResource<VisitDocument> {

	protected static final String REASON = "REST web service";

	@Override
	public VisitDocument newDelegate() {
		return new VisitDocument();
	}

	@Override
	public VisitDocument save(VisitDocument delegate) {
		Obs obs = Context.getObsService().saveObs(delegate.getObs(), REASON);
		return new VisitDocument(obs);
	}

	@Override
	public VisitDocument getByUniqueId(String uniqueId) {
		Obs obs = Context.getObsService().getObsByUuid(uniqueId);
		if (!obs.isComplex())
			throw new GenericRestException(uniqueId + " does not identify a complex obs.", null);
		else {
			obs = Context.getObsService().getComplexObs(obs.getId(), VisitDocumentsConstants.DOC_VIEW_CRUD);
			return new VisitDocument(obs);
		}
	}

	@Override
	protected void delete(VisitDocument delegate, String reason, RequestContext context) throws ResponseException {
		String encounterUuid = delegate.getObs().getEncounter().getUuid();
		Context.getObsService().voidObs(delegate.getObs(), REASON);
		voidEncounterIfEmpty(Context.getEncounterService(), encounterUuid);
	}

	@Override
	public void purge(VisitDocument delegate, RequestContext context) throws ResponseException {
		String encounterUuid = delegate.getObs().getEncounter().getUuid();
		Context.getObsService().purgeObs(delegate.getObs());
		voidEncounterIfEmpty(Context.getEncounterService(), encounterUuid);
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