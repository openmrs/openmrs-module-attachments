package org.openmrs.module.visitdocumentsui.rest;

import static org.openmrs.module.visitdocumentsui.rest.ComplexObsResource1_10.complexify;
import static org.openmrs.module.visitdocumentsui.rest.ComplexObsResource1_10.purgeEncounterIfEmpty;

import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_11.ObsResource1_11;

@Resource(name = RestConstants.VERSION_1 + "/obs", order = 0, supportedClass = Obs.class,
supportedOpenmrsVersions = {"2.0.*"})
public class ComplexObsResource2_0 extends ObsResource1_11 {

	@Override
	public Obs save(Obs delegate) {
	   return super.save(complexify(delegate));
	}
	
	@Override
   public void purge(Obs delegate, RequestContext context) throws ResponseException {
      Obs obs = complexify(delegate);
      String encounterUuid = obs.getEncounter().getUuid();
      super.purge(obs, context);
      
      purgeEncounterIfEmpty(Context.getEncounterService(), encounterUuid);
   }
	
	@Override
   protected void delete(Obs delegate, String reason, RequestContext context) throws ResponseException {
      super.delete(delegate, reason, context);
   }
}
