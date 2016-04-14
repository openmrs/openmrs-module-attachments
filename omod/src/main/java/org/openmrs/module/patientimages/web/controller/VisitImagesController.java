package org.openmrs.module.patientimages.web.controller;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.patientimages.PatientImagesConstants;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Controller
public class VisitImagesController {
    
	protected final Log log = LogFactory.getLog(getClass());
	
    @RequestMapping(value = PatientImagesConstants.UPLOAD_IMAGE_URL, method = RequestMethod.POST)
    @ResponseBody
    public String uploadImage(MultipartHttpServletRequest request) {
    	try {
            Iterator<String> itr = request.getFileNames();

            while (itr.hasNext()) {
                String uploadedFile = itr.next();
                MultipartFile file = request.getFile(uploadedFile);
                String filename = file.getOriginalFilename();
                byte[] bytes = file.getBytes();
                String mimeType = file.getContentType();
                
            }
        }
        catch (Exception e) {
        	log.error(e.getMessage(), e);
        }
    	
        return "";
    }
}
