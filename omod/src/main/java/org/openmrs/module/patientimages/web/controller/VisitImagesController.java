package org.openmrs.module.patientimages.web.controller;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ConceptComplex;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.Visit;
import org.openmrs.api.ObsService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.patientimages.PatientImagesConstants;
import org.openmrs.module.patientimages.PatientImagesProperties;
import org.openmrs.obs.ComplexData;
import org.openmrs.ui.framework.annotation.InjectBeans;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Controller
public class VisitImagesController {
    
	protected final Log log = LogFactory.getLog(getClass());
	
    @RequestMapping(value = PatientImagesConstants.UPLOAD_IMAGE_URL, method = RequestMethod.POST)
    @ResponseBody
    public String uploadImage(
    		@RequestParam("patient") Patient patient,
			@RequestParam("visit") String visitUuid,
			@InjectBeans PatientImagesProperties properties,
    		MultipartHttpServletRequest request) 
    {
    	VisitService visitService = Context.getVisitService();
    	ObsService obsService = Context.getObsService();
    	
    	Visit visit = visitService.getVisitByUuid(visitUuid);
    	
    	try {
            Iterator<String> itr = request.getFileNames();

            while (itr.hasNext()) {
                String uploadedFile = itr.next();
                MultipartFile file = request.getFile(uploadedFile);
                
                ConceptComplex conceptComplex = properties.getConceptComplex();
                saveComplexObs(patient.getPerson(), visit, file, conceptComplex, obsService);
            }
        }
        catch (Exception e) {
        	log.error(e.getMessage(), e);
        }
    	
        return "";
    }

    /*
     * @see https://wiki.openmrs.org/display/docs/Complex+Obs+Support
     */
    protected void saveComplexObs(Person person, Visit visit, MultipartFile file, ConceptComplex conceptComplex, ObsService obsService) throws IOException {
    	Obs obs = new Obs(person, conceptComplex, new Date(), visit.getLocation());
    	ComplexData complexData = new ComplexData(file.getOriginalFilename(), file.getInputStream());
    	obs.setComplexData(complexData);
    	  
    	obsService.saveObs(obs, null);
    }
}
