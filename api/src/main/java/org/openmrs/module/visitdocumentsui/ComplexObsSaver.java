/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.visitdocumentsui;

import static org.openmrs.module.visitdocumentsui.VisitDocumentsContext.getCompressionRatio;

import java.io.IOException;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ConceptComplex;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.Visit;
import org.openmrs.module.visitdocumentsui.VisitDocumentsConstants.ContentFamily;
import org.openmrs.module.visitdocumentsui.obs.ComplexDataHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import net.coobird.thumbnailator.Thumbnails;

@Component(VisitDocumentsConstants.COMPONENT_COMPLEXOBS_SAVER)
public class ComplexObsSaver
{
	protected final Log log = LogFactory.getLog(getClass());

	@Autowired
	@Qualifier(VisitDocumentsConstants.COMPONENT_VDUI_CONTEXT)
	protected VisitDocumentsContext context;

	@Autowired
	@Qualifier(VisitDocumentsConstants.COMPONENT_COMPLEXDATA_HELPER)
	protected ComplexDataHelper complexDataHelper;

	@Autowired
	@Qualifier(VisitDocumentsConstants.COMPONENT_VISIT_COMPATIBILITY)
	protected VisitCompatibility visitCompatibility;

	protected Obs obs = new Obs();
	protected ConceptComplex conceptComplex;

	public Obs getObs() {
		return obs;
	}

	protected void prepareComplexObs(Visit visit, Person person, Encounter encounter, String fileCaption) {
		obs = new Obs(person, conceptComplex, visit.getStopDatetime() == null ? new Date() : visit.getStopDatetime(), encounter.getLocation());
		obs.setEncounter(encounter);
		obs.setComment(fileCaption);
	}

	public Obs saveImageDocument(Visit visit, Person person, Encounter encounter, String fileCaption, MultipartFile multipartFile, String instructions)
			throws IOException
	{
		conceptComplex = context.getConceptComplex(ContentFamily.IMAGE);
		prepareComplexObs(visit, person, encounter, fileCaption);

		Object image = multipartFile.getInputStream();
		double compressionRatio = getCompressionRatio(multipartFile.getSize(), 1000000 * context.getMaxStorageFileSize());
		if (compressionRatio < 1) {
			image = Thumbnails.of(multipartFile.getInputStream()).scale(compressionRatio).asBufferedImage();
		}
		obs.setComplexData( complexDataHelper.build(instructions, multipartFile.getOriginalFilename(), image, multipartFile.getContentType()).asComplexData() );
		obs = context.getObsService().saveObs(obs, getClass().toString());
		return obs;
	}

	public Obs saveOtherDocument(Visit visit, Person person, Encounter encounter, String fileCaption, MultipartFile multipartFile, String instructions)
			throws IOException
	{
		conceptComplex = context.getConceptComplex(ContentFamily.OTHER);
		prepareComplexObs(visit, person, encounter, fileCaption);

		obs.setComplexData( complexDataHelper.build(instructions, multipartFile.getOriginalFilename(), multipartFile.getBytes(), multipartFile.getContentType()).asComplexData() );
		obs = context.getObsService().saveObs(obs, getClass().toString());
		return obs;
	}
}