package org.openmrs.module.visitdocumentsui.web.controller;

import static org.openmrs.module.visitdocumentsui.VisitDocumentsContext.getCompressionRatio;
import static org.openmrs.module.visitdocumentsui.VisitDocumentsContext.getContentFamily;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import net.coobird.thumbnailator.Thumbnails;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
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
import org.openmrs.module.visitdocumentsui.VisitCompatibility;
import org.openmrs.module.visitdocumentsui.VisitDocumentsConstants;
import org.openmrs.module.visitdocumentsui.VisitDocumentsConstants.ContentFamily;
import org.openmrs.module.visitdocumentsui.VisitDocumentsContext;
import org.openmrs.module.visitdocumentsui.obs.ComplexDataHelper;
import org.openmrs.module.visitdocumentsui.obs.DocumentComplexData;
import org.openmrs.module.visitdocumentsui.obs.ValueComplex;
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
public class VisitDocumentsController {

   @Autowired
   @Qualifier(VisitDocumentsConstants.COMPONENT_VDUI_CONTEXT)
   protected VisitDocumentsContext context;
   
   @Autowired
   @Qualifier(VisitDocumentsConstants.COMPONENT_VISIT_COMPATIBILITY)
   protected VisitCompatibility visitCompatibility;

   protected final Log log = LogFactory.getLog(getClass());

   @RequestMapping(value = VisitDocumentsConstants.UPLOAD_DOCUMENT_URL, method = RequestMethod.POST)
   @ResponseBody
   public Object uploadDocuments(
         @RequestParam("patient") Patient patient,
         @RequestParam("visit") Visit visit,
         @RequestParam("provider") Provider provider,
         @RequestParam("fileCaption") String fileCaption,
         @RequestParam(value="instructions", required=false) String instructions,
         MultipartHttpServletRequest request) 
   {
      Obs obs = new Obs();
      try
      {
         Iterator<String> fileNameIterator = request.getFileNames();	// Looping through the uploaded file names.

         while (fileNameIterator.hasNext()) {
            String uploadedFileName = fileNameIterator.next();
            MultipartFile multipartFile = request.getFile(uploadedFileName);
            
            Encounter encounter = getVisitDocumentEncounter(patient, visit, context.getEncounterType(), provider, context.getEncounterRole(), context.getEncounterService());
            ConceptComplex conceptComplex = null;
            if (StringUtils.isEmpty(instructions))
               instructions = ValueComplex.INSTRUCTIONS_DEFAULT;
            
            switch (getContentFamily(multipartFile.getContentType())) {
               case IMAGE:
                  conceptComplex = context.getConceptComplex(ContentFamily.IMAGE);
                  obs = prepareComplexObs(patient, encounter, fileCaption, conceptComplex);
                  obs = saveImageDocument(obs, multipartFile, instructions, context.getObsService(), context.getComplexDataHelper());
                  break;
                  
               case OTHER:
               default:
                  conceptComplex = context.getConceptComplex(ContentFamily.OTHER);
                  obs = prepareComplexObs(patient, encounter, fileCaption, conceptComplex);
                  obs = saveOtherDocument(obs, multipartFile, instructions, context.getObsService(), context.getComplexDataHelper());
                  break;
            }
         }
      }
      catch (IOException e) {
         // TODO Some error info should be returned to the client (perhaps via the Dropzone widget?)
         log.error(e.getMessage(), e);
      }

      return ConversionUtil.convertToRepresentation(obs, new CustomRepresentation(VisitDocumentsConstants.REPRESENTATION_OBS));
   }
   
   public Obs prepareComplexObs(Person person, Encounter encounter, String fileCaption, ConceptComplex conceptComplex) {
      Obs obs = new Obs(person, conceptComplex, new Date(), encounter.getLocation());
      obs.setEncounter(encounter);
      obs.setComment(fileCaption);
      return obs;
   }

   public Obs saveImageDocument(Obs obs, MultipartFile multipartFile, String instructions, ObsService obsService, ComplexDataHelper complexDataHelper)
         throws IOException
   {
      Object image = multipartFile.getInputStream();
      double compressionRatio = getCompressionRatio(multipartFile.getSize(), 1000000 * context.getMaxStorageFileSize());
      if (compressionRatio < 1) {
         image = Thumbnails.of(multipartFile.getInputStream()).scale(compressionRatio).asBufferedImage();
      }
      obs.setComplexData( complexDataHelper.build(instructions, multipartFile.getOriginalFilename(), image, multipartFile.getContentType()).asComplexData() );
      return obsService.saveObs(obs, getClass().toString());
   }
   
   public Obs saveOtherDocument(Obs obs, MultipartFile multipartFile, String instructions, ObsService obsService, ComplexDataHelper complexDataHelper)
         throws IOException
   {
      obs.setComplexData( complexDataHelper.build(instructions, multipartFile.getOriginalFilename(), multipartFile.getBytes(), multipartFile.getContentType()).asComplexData() );
      return obsService.saveObs(obs, getClass().toString());
   }

   public Encounter getVisitDocumentEncounter(Patient patient, Visit visit, EncounterType encounterType, Provider provider, EncounterRole encounterRole, EncounterService encounterService)
   {
      Encounter encounter = new Encounter();
      encounter.setVisit(visit);
      encounter.setEncounterType(encounterType);
      encounter.setPatient(visit.getPatient());
      encounter.setLocation(visit.getLocation());
      boolean saveEncounter = true;
      if (context.isOneEncounterPerVisit()) {
         List<Encounter> encounters = visitCompatibility.getNonVoidedEncounters(visit);
         for (Encounter e : encounters) {
            if (e.getEncounterType().getUuid() == encounterType.getUuid()) {
               encounter = e;
               saveEncounter = false;
               break;
            }
         }
      }
      encounter.setProvider(encounterRole, provider);
      encounter.setEncounterDatetime(new Date());
      if (saveEncounter) {
         encounter = encounterService.saveEncounter(encounter);
      }
      return encounter;
   }
   
   @RequestMapping(value = VisitDocumentsConstants.DOWNLOAD_DOCUMENT_URL, method = RequestMethod.GET)
   public void downloadDocument(@RequestParam("obs") String obsUuid, @RequestParam(value="view", required=false) String view, HttpServletResponse response)
   {
      if (StringUtils.isEmpty(view))
         view = VisitDocumentsConstants.DOC_VIEW_ORIGINAL;

      // Getting the Core/Platform complex data object
      Obs obs = context.getObsService().getObsByUuid(obsUuid);
      Obs complexObs = context.getObsService().getComplexObs(obs.getObsId(), view);
      ComplexData complexData = complexObs.getComplexData();
      
      // Switching to our complex data object
      ValueComplex valueComplex = new ValueComplex(obs.getValueComplex());
      DocumentComplexData docComplexData = context.getComplexDataHelper().build(valueComplex.getInstructions(), complexData);
      
      String mimeType = docComplexData.getMimeType();
      try {
         // The document meta data is sent as HTTP headers.
         response.setContentType(mimeType);
         response.addHeader("Content-Family", getContentFamily(mimeType).name());   // custom header
         response.addHeader("File-Name", docComplexData.getTitle());   // custom header
         response.addHeader("File-Ext", getExtension(docComplexData.getTitle(), mimeType));   // custom header
         switch (getContentFamily(mimeType)) {
            default:
               response.getOutputStream().write(docComplexData.asByteArray());
               break;
         }
      }
      catch (IOException e) {
         response.setStatus(500);
         log.error("Could not write to HTTP response for when fetching obs with"
               + " VALUE_COMPLEX='" + complexObs.getValueComplex() + "',"
               + " OBS_ID='" + complexObs.getId() + "',"
               + " OBS_UUID='" + complexObs.getUuid() + "'"
               , e);
      }
   }
   
   /**
    * @param fileName
    * @param mimeType
    * @return The best guess extension for the file, preferring it coming out from the original file name if possible.
    */
   public static String getExtension(String fileName, String mimeType) {
      String ext = FilenameUtils.getExtension(fileName);
      String extFromMimeType = VisitDocumentsContext.getExtension(mimeType);
      if (!StringUtils.isEmpty(ext)) {
         if (ext.length() > 6) {  // this is a bit arbitrary, just to discriminate funny named files such as "uiohdz.iuhezuidhuih"
            ext = extFromMimeType;
         }
      }
      else {
         ext = extFromMimeType;
      }
      return ext;
   }
}
