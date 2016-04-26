package org.openmrs.module.patientimages.web.controller;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ConceptComplex;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.module.patientimages.PatientImagesConstants;
import org.openmrs.module.patientimages.PatientImagesContext;
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
    public String uploadImage(
    		@RequestParam("patient") Patient patient,
			@RequestParam("visit") String visitUuid,
    		MultipartHttpServletRequest request) 
    {
    	String providerUuid = request.getParameter("providerUuid");
    	String obsComment = request.getParameter("obsComment");

    	Provider provider = context.getProviderService().getProviderByUuid(providerUuid);
    	
    	Visit visit = context.getVisitService().getVisitByUuid(visitUuid);
    	Encounter encounter = saveImageUploadEncounter(patient, visit.getLocation(), context.getEncounterType(), provider, context.getEncounterService());
    	
    	String obsUuid = "";
    	try {
            Iterator<String> fileNameIterator = request.getFileNames();	// Looping through the uploaded file names.

            while (fileNameIterator.hasNext()) {
                String uploadedFileName = fileNameIterator.next();
                MultipartFile uploadedFile = request.getFile(uploadedFileName);
                
                ConceptComplex conceptComplex = context.getConceptComplex();
                Obs obs = saveUploadedImageObs(patient.getPerson(), encounter, uploadedFile, obsComment, conceptComplex, context.getObsService());
                obsUuid = obs.getUuid();
            }
        }
        catch (Exception e) {
        	log.error(e.getMessage(), e);
        }
    	
        return obsUuid;
    }

    /*
     * @see https://wiki.openmrs.org/display/docs/Complex+Obs+Support
     */
    protected Obs saveUploadedImageObs(Person person, Encounter encounter, MultipartFile file, String obsComment, ConceptComplex conceptComplex, ObsService obsService) throws IOException {
    	Obs obs = new Obs(person, conceptComplex, encounter.getEncounterDatetime(), encounter.getLocation());
    	obs.setEncounter(encounter);
    	obs.setComment((obsComment == null) ? "" : obsComment);
    	obs.setComplexData( new ComplexData(file.getOriginalFilename(), file.getInputStream()) );
    	return obsService.saveObs(obs, null);
    }
    
    protected Encounter saveImageUploadEncounter(Patient patient, Location location, EncounterType encounterType, Provider provider, EncounterService encounterService) {
    	Encounter encounter = new Encounter();
		encounter.setEncounterType(encounterType);
		encounter.setEncounterDatetime(new Date());
		encounter.setPatient(patient);
		encounter.setLocation(location);
		return encounterService.saveEncounter(encounter);
    }
}
