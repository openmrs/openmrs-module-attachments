package org.openmrs.module.attachments.rest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.module.attachments.AttachmentsContext;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

@SuppressWarnings("unchecked")
public class ObsByConceptListSearchHandler1_10Test extends MainResourceControllerTest {
	
	private String PATIENT_UUID = "5946f880-b197-400b-9caa-a3c661d23041";
	
	private String CONCEPT_1_UUID = "a09ab2c5-878e-4905-b25d-5784167d0216";
	
	private String CONCEPT_2_UUID = "c607c80f-1ea9-4da3-bb88-6276ce8868dd";
	
	private String CONCEPT_2_SOURCE = "CIEL";
	
	private String CONCEPT_2_NUMBER = "5089";
	
	private String CONCEPT_3_UUID = "96408258-000b-424e-af1a-403919332938";
	
	private ObsService obsService;
	
	private ConceptService conceptService;
	
	@Autowired
	private AttachmentsContext context;
	
	@Before
	public void init() throws Exception {
		obsService = context.getObsService();
		conceptService = context.getConceptService();
	}
	
	@Test
	public void search_shouldReturnObsByPatientAndConceptList() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		
		req.addParameter("patient", PATIENT_UUID);
		req.addParameter("conceptList", CONCEPT_1_UUID + "," + CONCEPT_2_UUID + "," + CONCEPT_3_UUID);
		
		SimpleObject result = deserialize(handle(req));
		List<Object> hits = (List<Object>) result.get("results");
		
		Assert.assertEquals(6, hits.size());
	}
	
	@Test
	public void search_shouldReturnEmptyWhenPatientIsNotFound() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		
		req.addParameter("patient", "abcd-e");
		req.addParameter("conceptList", CONCEPT_1_UUID + "," + CONCEPT_2_UUID + "," + CONCEPT_3_UUID);
		
		SimpleObject result = deserialize(handle(req));
		List<Object> hits = (List<Object>) result.get("results");
		
		Assert.assertEquals(0, hits.size());
	}
	
	@Test
	public void search_shouldIgnoreNonExisitingConcept() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		
		req.addParameter("patient", PATIENT_UUID);
		req.addParameter("conceptList", CONCEPT_1_UUID + "," + "abc1-mks-123a" + "," + CONCEPT_3_UUID);
		
		SimpleObject result = deserialize(handle(req));
		List<Object> hits = (List<Object>) result.get("results");
		
		Assert.assertEquals(3, hits.size());
	}
	
	@Test
	public void search_shouldSortByDescendingOrder() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		
		req.addParameter("patient", PATIENT_UUID);
		req.addParameter("conceptList", CONCEPT_1_UUID + "," + CONCEPT_2_UUID + "," + CONCEPT_3_UUID);
		
		SimpleObject result = deserialize(handle(req));
		List<Object> hits = (List<Object>) result.get("results");
		
		for (int i = 0; i < hits.size(); i++) {
			Obs obs1 = obsService.getObsByUuid((String) PropertyUtils.getProperty(hits.get(i), "uuid"));
			if (i > 0) {
				Obs obs2 = obsService.getObsByUuid((String) PropertyUtils.getProperty(hits.get(i - 1), "uuid"));
				Assert.assertTrue(obs2.getObsDatetime().getTime() >= obs1.getObsDatetime().getTime());
			}
		}
	}
	
	// @Ignore("The standardTestDataset.xml does not create mappings. Skipping this
	// test")
	@Test
	public void parseConceptList_shouldHandleMappingsAndUuids() throws Exception {
		
		String conceptListStr = CONCEPT_1_UUID + "," + CONCEPT_2_SOURCE + ":" + CONCEPT_2_NUMBER + "," + CONCEPT_3_UUID;
		
		ConceptService conceptServiceMock = mock(ConceptService.class);
		
		when(conceptServiceMock.getConceptByMapping(CONCEPT_2_NUMBER, CONCEPT_2_SOURCE))
		        .thenReturn(conceptService.getConceptByUuid(CONCEPT_2_UUID));
		when(conceptServiceMock.getConceptByUuid(CONCEPT_1_UUID))
		        .thenReturn(conceptService.getConceptByUuid(CONCEPT_1_UUID));
		when(conceptServiceMock.getConceptByUuid(CONCEPT_3_UUID))
		        .thenReturn(conceptService.getConceptByUuid(CONCEPT_3_UUID));
		
		ObsByConceptListSearchHandler1_10 searchHandler = new ObsByConceptListSearchHandler1_10();
		List<Concept> conceptList = searchHandler.parseConceptList(conceptListStr, conceptServiceMock);
		
		Assert.assertEquals(3, conceptList.size());
	}
	
	@Override
	public String getUuid() {
		return "be48cdcb-6a76-47e3-9f2e-2635032f3a9a";
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#shouldGetAll()
	 */
	@Override
	@Test(expected = ResourceDoesNotSupportOperationException.class)
	public void shouldGetAll() throws Exception {
		super.shouldGetAll();
	}
	
	@Override
	public String getURI() {
		return "obs";
	}
	
	@Override
	public long getAllCount() {
		// This method is never called since the 'shouldGetAll' test is overridden and
		// returns exception
		return 0;
	}
}
