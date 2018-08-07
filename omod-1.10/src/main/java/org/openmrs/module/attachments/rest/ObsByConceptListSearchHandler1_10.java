package org.openmrs.module.attachments.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.module.attachments.AttachmentsConstants;
import org.openmrs.module.attachments.AttachmentsContext;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.api.SearchConfig;
import org.openmrs.module.webservices.rest.web.resource.api.SearchHandler;
import org.openmrs.module.webservices.rest.web.resource.api.SearchQuery;
import org.openmrs.module.webservices.rest.web.resource.impl.EmptySearchResult;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * The contents of this file are subject to the OpenMRS Public License Version 1.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://license.openmrs.org Software distributed under the License is distributed on an
 * "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for the
 * specific language governing rights and limitations under the License. Copyright (C) OpenMRS, LLC.
 * All Rights Reserved.
 */
@Component
public class ObsByConceptListSearchHandler1_10 implements SearchHandler {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	@Autowired
	@Qualifier(AttachmentsConstants.COMPONENT_ATT_CONTEXT)
	protected AttachmentsContext context;
	
	private final SearchConfig searchConfig = new SearchConfig("obsByConceptList", RestConstants.VERSION_1 + "/obs",
	        Arrays.asList("1.10.*", "1.11.*", "1.12.*", "2.0.*", "2.1.*"),
	        Arrays.asList(
	            new SearchQuery.Builder("Allows you to retrieve Observations for a patient and a for list of concepts")
	                    .withRequiredParameters("patient", "conceptList").build()));
	
	@Override
	public SearchConfig getSearchConfig() {
		return this.searchConfig;
	}
	
	@Override
	public PageableResult search(RequestContext requestContext) throws ResponseException {
		
		String patientUuid = requestContext.getRequest().getParameter("patient");
		
		String conceptListStr = requestContext.getRequest().getParameter("conceptList");
		
		List<Concept> conceptList = parseConceptList(conceptListStr, context.getConceptService());
		
		if (patientUuid != null) {
			
			Patient patient = context.getPatientService().getPatientByUuid(patientUuid);
			
			if (patient == null) {
				log.warn("Patient \"" + patientUuid + "\" was not found. Returning empty set.");
				return new EmptySearchResult();
			}
			
			List<Obs> obsList = new ArrayList<>();
			
			if (patient != null & conceptList.size() != 0) {
				
				ObsService obsService = context.getObsService();
				
				for (Concept concept : conceptList) {
					obsList.addAll(obsService.getObservationsByPersonAndConcept(patient, concept));
				}
			}
			
			if (obsList.size() != 0) {
				// Sorting obs by descending obsDatetime
				Collections.sort(obsList, new Comparator<Obs>() {
					
					public int compare(Obs obs1, Obs obs2) {
						return obs2.getObsDatetime().compareTo(obs1.getObsDatetime());
					}
				});
				
				return new NeedsPaging<Obs>(obsList, requestContext);
			}
		}
		
		return new EmptySearchResult();
	}
	
	/**
	 * Returns a {@link List} of {@link Concept} from a provided {@link String} of comma separated
	 * concepts. Each concept of the list can be provided either as UUID or as Concept Mapping
	 * 
	 * @param conceptListStr
	 * @param conceptService
	 * @return a list of concepts
	 */
	protected List<Concept> parseConceptList(String conceptListStr, ConceptService conceptService) {
		
		List<String> conceptUuidsList = Arrays.asList(conceptListStr.split("\\s*,\\s*"));
		
		List<Concept> conceptList = new ArrayList<>();
		
		if (conceptUuidsList != null) {
			for (String conceptStr : conceptUuidsList) {
				
				Concept concept = new Concept();
				
				// See if the concept is a mapping or a uuid
				List<String> conceptMapping = Arrays.asList(conceptStr.split(":"));
				
				if (conceptMapping.size() > 1) {
					// it is a mapping
					concept = conceptService.getConceptByMapping(conceptMapping.get(1), conceptMapping.get(0));
				} else {
					// it is a uuid
					concept = conceptService.getConceptByUuid(conceptStr);
				}
				
				if (concept == null) {
					log.warn("Concept \"" + conceptStr + "\" was not found. Ignoring it.");
				} else {
					conceptList.add(concept);
				}
			}
		}
		
		return conceptList;
		
	}
}
