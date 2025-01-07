package org.openmrs.module.attachments.obs;

import org.openmrs.Obs;

/**
 * The contents of this file are subject to the OpenMRS Public License Version
 * 1.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at http://license.openmrs.org
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * Copyright (C) OpenMRS, LLC. All Rights Reserved.
 */
public interface ComplexViewHelper {

	/**
	 * This method handles providing a usable/correct view argument to both OpenMRS
	 * 1.10+ and 2.0+ given an OpenMRS 1.10+ view string
	 * 
	 * @param obs
	 *            This will help the 2.x implementation support fetching the
	 *            supported views for that obs,using for example:
	 *            https://github.com/openmrs/openmrs-core/blob/7da5be1bc34fc4928779f303cd48d42b8a3cae0a/api/src/main/java/org/openmrs/api/ObsService.java#L417-L428
	 * @param view
	 *            the ATT or OpenMRS 1.10+ view string
	 * @return a corresponding 1.10+ or 2.0+ view string
	 */
	public String getView(Obs obs, String view);

}
