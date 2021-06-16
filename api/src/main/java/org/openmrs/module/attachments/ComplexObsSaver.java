/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.attachments;

import static org.openmrs.module.attachments.AttachmentsConstants.ContentFamily.IMAGE;
import static org.openmrs.module.attachments.AttachmentsConstants.ContentFamily.OTHER;
import static org.openmrs.module.attachments.AttachmentsContext.getCompressionRatio;

import java.io.IOException;
import java.util.Date;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ConceptComplex;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.Visit;
import org.openmrs.module.attachments.obs.ComplexDataHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import net.coobird.thumbnailator.Thumbnails;

@Component(AttachmentsConstants.COMPONENT_COMPLEXOBS_SAVER)
public class ComplexObsSaver {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	@Autowired
	@Qualifier(AttachmentsConstants.COMPONENT_ATT_CONTEXT)
	protected AttachmentsContext context;
	
	@Autowired
	@Qualifier(AttachmentsConstants.COMPONENT_COMPLEXDATA_HELPER)
	protected ComplexDataHelper complexDataHelper;
	
	@Autowired
	@Qualifier(AttachmentsConstants.COMPONENT_VISIT_COMPATIBILITY)
	protected VisitCompatibility visitCompatibility;
	
	protected Obs newObs(ConceptComplex conceptComplex, Visit visit, Person person, Encounter encounter, String comment) {
		Obs obs = new Obs(person, conceptComplex,
		        visit == null || visit.getStopDatetime() == null ? new Date() : visit.getStopDatetime(),
		        encounter != null ? encounter.getLocation() : null);
		obs.setEncounter(encounter); // may be null
		obs.setComment(comment);
		return obs;
	}
	
	public Obs saveImageObs(Visit visit, Person person, Encounter encounter, String comment, MultipartFile multipartFile,
	        String instructions) throws IOException {
		return saveImageObs(visit, person, encounter, context.getConceptComplex(IMAGE), comment, multipartFile,
		    instructions);
	}
	
	public Obs saveImageObs(Visit visit, Person person, Encounter encounter, ConceptComplex conceptComplex,
	        String fileCaption, MultipartFile multipartFile, String instructions) throws IOException {
		
		if (conceptComplex == null) {
			conceptComplex = context.getConceptComplex(IMAGE);
		}
		
		Obs obs = newObs(conceptComplex, visit, person, encounter, fileCaption);
		Object image = multipartFile.getInputStream();
		double compressionRatio = getCompressionRatio(multipartFile.getSize(), 1000000 * context.getMaxStorageFileSize());
		if (compressionRatio < 1) {
			image = Thumbnails.of(ImageIO.read(multipartFile.getInputStream())).scale(compressionRatio).asBufferedImage();
		}
		obs.setComplexData(
		    complexDataHelper.build(instructions, multipartFile.getOriginalFilename(), image, multipartFile.getContentType())
		            .asComplexData());
		
		return context.getObsService().saveObs(obs, getClass().toString());
	}
	
	public Obs saveOtherObs(Visit visit, Person person, Encounter encounter, String comment, MultipartFile multipartFile,
	        String instructions) throws IOException {
		return saveOtherObs(visit, person, encounter, context.getConceptComplex(OTHER), comment, multipartFile,
		    instructions);
	}
	
	public Obs saveOtherObs(Visit visit, Person person, Encounter encounter, ConceptComplex conceptComplex, String comment,
	        MultipartFile multipartFile, String instructions) throws IOException {
		
		if (conceptComplex == null) {
			conceptComplex = context.getConceptComplex(OTHER);
		}
		
		Obs obs = newObs(conceptComplex, visit, person, encounter, comment);
		obs.setComplexData(complexDataHelper.build(instructions, multipartFile.getOriginalFilename(),
		    multipartFile.getBytes(), multipartFile.getContentType()).asComplexData());
		
		return context.getObsService().saveObs(obs, getClass().toString());
	}
}
