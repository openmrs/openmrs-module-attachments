package org.openmrs.module.patientimages.web.controller;

import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import net.coobird.thumbnailator.Thumbnails;

import org.apache.commons.codec.binary.Base64;
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
import org.openmrs.module.patientimages.PatientImageComplexData;
import org.openmrs.module.patientimages.PatientImagesConstants;
import org.openmrs.module.patientimages.PatientImagesContext;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.CustomRepresentation;
import org.openmrs.obs.ComplexData;
import org.openmrs.util.OpenmrsConstants;
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
    	obs.setComplexData( new ComplexData(file.getOriginalFilename(), file.getInputStream()) );
    	obs.setComplexData( new PatientImageComplexData(file.getOriginalFilename(), file.getInputStream()) );
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
    
    @RequestMapping(value = PatientImagesConstants.DOWNLOAD_IMAGE_URL_2, method = RequestMethod.GET)
    public void downloadImage(@RequestParam("obs") String obsUuid, HttpServletResponse response) throws ServletException, IOException 
    {
    	Obs obs = context.getObsService().getObsByUuid(obsUuid);
    	
    	Obs complexObs = context.getObsService().getComplexObs(obs.getObsId(), OpenmrsConstants.RAW_VIEW);
		ComplexData complexData = complexObs.getComplexData();
		Object data = complexData.getData();
		
		final int height = 100;
		final int width = 100;
		final String format = "png";
		
		
		if (data instanceof byte[])
		{
			ByteArrayInputStream stream = new ByteArrayInputStream((byte[]) data);
			Thumbnails.of(stream).size(height, width).outputFormat(format).toOutputStream( response.getOutputStream() );			
		}
		else if (RenderedImage.class.isAssignableFrom(data.getClass()))
		{
			RenderedImage img = (RenderedImage) data;
			String[] parts = complexData.getTitle().split("\\.");
			String extension = "jpg"; // default extension
			if (parts.length > 0) {
				extension = parts[parts.length - 1];
			}
			ByteArrayOutputStream byteArrayOutputStream= new ByteArrayOutputStream();
			ImageOutputStream imageOutputStream=  ImageIO.createImageOutputStream(byteArrayOutputStream);
			ImageIO.write(img, extension, imageOutputStream);
			imageOutputStream.close();
			
			ByteArrayInputStream stream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
			Thumbnails.of(stream).size(height, width).outputFormat(format).toOutputStream( response.getOutputStream() );
			byteArrayOutputStream.close();
		}
		else if (InputStream.class.isAssignableFrom(data.getClass()))
		{
			InputStream stream = (InputStream) data;
			Thumbnails.of(stream).size(height, width).outputFormat(format).toOutputStream( response.getOutputStream() );
			stream.close();
		}
		else {
			throw new ServletException("Couldn't serialize complex obs data for obsId=" + obsUuid + " of type "
			        + data.getClass());
		}
    }
    
    @RequestMapping(value = PatientImagesConstants.DOWNLOAD_IMAGE_BASE64, method = RequestMethod.GET)
    @ResponseBody
    public Object downloadImageBase64(@RequestParam("obs") String obsUuid) throws ServletException, IOException 
    {
    	Obs obs = context.getObsService().getObsByUuid(obsUuid);
    	
    	Obs complexObs = context.getObsService().getComplexObs(obs.getObsId(), OpenmrsConstants.RAW_VIEW);
		ComplexData complexData = complexObs.getComplexData();
		Object data = complexData.getData();
		
		final int height = 100;
		final int width = 100;
		final String format = "png";
		
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		
		if (data instanceof byte[])
		{
			ByteArrayInputStream inStream = new ByteArrayInputStream((byte[]) data);
			Thumbnails.of(inStream).size(height, width).outputFormat(format).toOutputStream(outStream);			
		}
		else if (RenderedImage.class.isAssignableFrom(data.getClass()))
		{
			RenderedImage img = (RenderedImage) data;
			String[] parts = complexData.getTitle().split("\\.");
			String extension = "jpg"; // default extension
			if (parts.length > 0) {
				extension = parts[parts.length - 1];
			}
			ByteArrayOutputStream byteArrayOutputStream= new ByteArrayOutputStream();
			ImageOutputStream imageOutputStream=  ImageIO.createImageOutputStream(byteArrayOutputStream);
			ImageIO.write(img, extension, imageOutputStream);
			imageOutputStream.close();
			
			ByteArrayInputStream stream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
			Thumbnails.of(stream).size(height, width).outputFormat(format).toOutputStream(outStream);
			byteArrayOutputStream.close();
		}
		else if (InputStream.class.isAssignableFrom(data.getClass()))
		{
			InputStream inStream = (InputStream) data;
			Thumbnails.of(inStream).size(height, width).outputFormat(format).toOutputStream(outStream);
			inStream.close();
		}
		else {
			throw new ServletException("Couldn't serialize complex obs data for obsId=" + obsUuid + " of type "
			        + data.getClass());
		}
		
		Map<String, Object> jsonConfig = new LinkedHashMap<String, Object>();
		
		
		jsonConfig.put("metaData", "data:image/" + format + ";base64,");
		jsonConfig.put("data", Base64.encodeBase64String(outStream.toByteArray()));
		
		return jsonConfig;
    }
    
}
