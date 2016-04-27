package org.openmrs.module.patientimages.web.controller;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ConceptComplex;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.module.patientimages.PatientImagesConstants;
import org.openmrs.module.patientimages.PatientImagesContext;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.CustomRepresentation;
import org.openmrs.obs.ComplexData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Controller
public class VisitImagesController {
    
	@Autowired
    @Qualifier("patientImagesContext")
    protected PatientImagesContext context;
	
	protected final Log log = LogFactory.getLog(getClass());
	
    @RequestMapping(value = PatientImagesConstants.UPLOAD_IMAGE_URL, method = RequestMethod.POST)
    @ResponseBody
    public Object uploadImage(
    		@RequestParam("patient") Patient patient,
			@RequestParam("visit") String visitUuid,
    		MultipartHttpServletRequest request) 
    {
    	String providerUuid = request.getParameter("providerUuid");
    	String obsText = request.getParameter("obsText");

    	Provider provider = context.getProviderService().getProviderByUuid(providerUuid);
    	
    	Visit visit = context.getVisitService().getVisitByUuid(visitUuid);
    	Encounter encounter = saveImageUploadEncounter(patient, visit, context.getEncounterType(), provider, context.getEncounterRole(), context.getEncounterService());
    	
    	Obs obs = new Obs();
    	try {
            Iterator<String> fileNameIterator = request.getFileNames();	// Looping through the uploaded file names.

            while (fileNameIterator.hasNext()) {
                String uploadedFileName = fileNameIterator.next();
                MultipartFile uploadedFile = request.getFile(uploadedFileName);
                
                ConceptComplex conceptComplex = context.getConceptComplex();
                obs = saveUploadedImageObs(patient.getPerson(), encounter, uploadedFile, obsText, conceptComplex, context.getObsService());
            }
        }
        catch (Exception e) {
        	log.error(e.getMessage(), e);
        }
    	
        return ConversionUtil.convertToRepresentation(obs, new CustomRepresentation(PatientImagesConstants.REPRESENTATION_OBS));
    }

    /*
     * @see https://wiki.openmrs.org/display/docs/Complex+Obs+Support
     */
    protected Obs saveUploadedImageObs(Person person, Encounter encounter, MultipartFile file, String obsText, ConceptComplex conceptComplex, ObsService obsService) throws IOException {
    	Obs obs = new Obs(person, conceptComplex, encounter.getEncounterDatetime(), encounter.getLocation());
    	obs.setEncounter(encounter);
    	obs.setComment(obsText);
    	obs.setComment((obsText == null) ? "" : obsText);
    	obs.setComplexData( new ComplexData(file.getOriginalFilename(), file.getInputStream()) );
    	return obsService.saveObs(obs, null);
    }
    
    protected Encounter saveImageUploadEncounter(Patient patient, Visit visit, EncounterType encounterType, Provider provider, EncounterRole encounterRole, EncounterService encounterService) {
    	Encounter encounter = new Encounter();
    	encounter.setVisit(visit);
    	encounter.setProvider(encounterRole, provider);
		encounter.setEncounterType(encounterType);
		encounter.setEncounterDatetime(new Date());
		encounter.setPatient(visit.getPatient());
		encounter.setLocation(visit.getLocation());
		return encounterService.saveEncounter(encounter);
    }
}
