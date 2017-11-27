package org.openmrs.module.attachments;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.attachments.AttachmentsConstants;
import org.openmrs.module.attachments.AttachmentsContext;
import org.openmrs.module.attachments.AttachmentsConstants.ContentFamily;
import org.openmrs.test.Verifies;

public class AttachmentsContextTest {
	
	private AttachmentsContext context = new AttachmentsContext();
	
	private AdministrationService adminService = mock(AdministrationService.class);
	
	@Before
	public void setup() {
		context.setAdministrationService(adminService);
	}
	
	@Test
	@Verifies(value = "A simple JSON saved as a global property should be parse into a Map<String, String>.", method = "getMapByGlobalProperty(globalPropertyName)")
	public void context_shouldReturnSimpleMap() {
		
		// Setup
		final String IMAGE_STR = ContentFamily.IMAGE.toString();
		final String IMAGE_UUID = "7cac8397-53cd-4f00-a6fe-028e8d743f8e";
		final String OTHER_STR = ContentFamily.OTHER.toString();
		final String OTHER_UUID = "42ed45fd-f3f6-44b6-bfc2-8bde1bb41e00";
		
		String jsonMap = "{\"" + IMAGE_STR + "\":\"" + IMAGE_UUID + "\",\"" + OTHER_STR + "\":\"" + OTHER_UUID + "\"}";
		String gpName = AttachmentsConstants.GP_CONCEPT_COMPLEX_UUID_MAP;
		when(adminService.getGlobalProperty(eq(gpName))).thenReturn(jsonMap);
		
		// Replay
		Map<String, String> map = context.getMapByGlobalProperty(gpName);
		
		// Verif
		assertEquals(2, map.size());
		assertEquals(IMAGE_UUID, map.get(IMAGE_STR));
		assertEquals(OTHER_UUID, map.get(OTHER_STR));
	}
	
	@Test
	@Verifies(value = "A JSON saved as a global property should be parse into a List<String>.", method = "getMapByGlobalProperty(globalPropertyName)")
	public void context_shouldReturnList() {
		
		// Setup
		final String IMAGE_UUID = "7cac8397-53cd-4f00-a6fe-028e8d743f8e";
		final String OTHER_UUID = "42ed45fd-f3f6-44b6-bfc2-8bde1bb41e00";
		
		String jsonList = "[\"" + IMAGE_UUID + "\",\"" + OTHER_UUID + "\"]";
		String gpName = AttachmentsConstants.GP_CONCEPT_COMPLEX_UUID_LIST;
		when(adminService.getGlobalProperty(eq(gpName))).thenReturn(jsonList);
		
		// Replay
		List<String> list = context.getConceptComplexList();
		
		// Verif
		assertEquals(2, list.size());
		assertEquals(IMAGE_UUID, list.get(0));
		assertEquals(OTHER_UUID, list.get(1));
	}
}
