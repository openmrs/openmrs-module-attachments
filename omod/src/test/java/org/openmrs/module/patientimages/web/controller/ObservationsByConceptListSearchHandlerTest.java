package org.openmrs.module.patientimages.web.controller;

import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;


@Ignore("Unable to instanciate the Context yet")
@SuppressWarnings("unchecked")
public class ObservationsByConceptListSearchHandlerTest extends MainResourceControllerTest {

	protected String PATIENT_UUID = "5946f880-b197-400b-9caa-a3c661d23041";

	protected String CONCEPT_1_UUID = "a09ab2c5-878e-4905-b25d-5784167d0216";
	protected String CONCEPT_2_UUID = "c607c80f-1ea9-4da3-bb88-6276ce8868dd";
	protected String CONCEPT_2_MAPPING = "CIEL:5497";
	protected String CONCEPT_3_UUID = "96408258-000b-424e-af1a-403919332938";



	@Override
	public String getURI() {
		return "obs";
	}

	@Test
	public void search_shouldReturnObsByPatientAndConceptList() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());

		req.addParameter("patient", PATIENT_UUID);
		req.addParameter("conceptList", CONCEPT_1_UUID + "," + CONCEPT_2_UUID + 
				"," + CONCEPT_3_UUID );

		SimpleObject result = deserialize(handle(req));
		List<Object> hits = (List<Object>) result.get("results");

		Assert.assertEquals(6, hits.size());
	}

	@Test
	public void search_shouldReturnEmptyWhenPatientIsNotFound() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());

		req.addParameter("patient", "abcd-e");
		req.addParameter("conceptList", CONCEPT_1_UUID + "," + CONCEPT_2_UUID + 
				"," + CONCEPT_3_UUID);

		SimpleObject result = deserialize(handle(req));
		List<Object> hits = (List<Object>) result.get("results");

		Assert.assertEquals(0, hits.size());
	}


	@Test
	public void parseConceptList_shouldIgnoreNonExisitingConcept() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());

		req.addParameter("patient", PATIENT_UUID);
		req.addParameter("conceptList", CONCEPT_1_UUID + "," + "abc1-mks-123a" +
				"," + CONCEPT_3_UUID);
		
		SimpleObject result = deserialize(handle(req));
		List<Object> hits = (List<Object>) result.get("results");

		Assert.assertEquals(4, hits.size());
	}

	@Ignore("The standardTestDataset.xml does not create mappings. Skipping this test")
	@Test
	public void parseConceptList_shouldHandleMappingsAndUuids() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());

		req.addParameter("patient", PATIENT_UUID);
		req.addParameter("conceptList", CONCEPT_1_UUID + "," + CONCEPT_2_MAPPING +
				"," + CONCEPT_3_UUID);
		SimpleObject result = deserialize(handle(req));
		List<Object> hits = (List<Object>) result.get("results");

		Assert.assertEquals(6, hits.size());
	}

	@Override
	public String getUuid() {
		return "";
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
	public long getAllCount() {
		// This method is never called since the 'shouldGetAll' test is overridden and returns exception
		return 0;
	}

}