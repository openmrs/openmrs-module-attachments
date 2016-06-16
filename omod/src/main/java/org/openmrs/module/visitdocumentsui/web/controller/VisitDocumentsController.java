package org.openmrs.module.visitdocumentsui.web.controller;

import java.awt.image.RenderedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.servlet.http.HttpServletResponse;

import net.coobird.thumbnailator.Thumbnails;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
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
import org.openmrs.module.visitdocumentsui.VisitDocumentsConstants;
import org.openmrs.module.visitdocumentsui.VisitDocumentsContext;
import org.openmrs.module.visitdocumentsui.obs.ComplexData_2_0;
import org.openmrs.module.visitdocumentsui.obs.PatientImageComplexData;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.CustomRepresentation;
import org.openmrs.obs.ComplexData;
import org.openmrs.web.servlet.ComplexObsServlet;
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
   @Qualifier(VisitDocumentsConstants.MODULE_CONTEXT_QUALIFIER)
   protected VisitDocumentsContext context;

   protected final Log log = LogFactory.getLog(getClass());
   
   public static enum ContentFamily {
      IMAGES,
      OTHER
   }
   
   /**
    * @param mimeType The MIME type of the uploaded content.
    * @return The type/family of uploaded content based on the MIME type.
    */
   public static ContentFamily getContentFamily(String mimeType) {
      ContentFamily contentFamily = ContentFamily.OTHER;
      if (StringUtils.startsWith(mimeType, "image/")) {
         contentFamily = ContentFamily.IMAGES;
      }
      return contentFamily;
   }

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
      try {
         Iterator<String> fileNameIterator = request.getFileNames();	// Looping through the uploaded file names.

         while (fileNameIterator.hasNext()) {
            String uploadedFileName = fileNameIterator.next();
            MultipartFile uploadedFile = request.getFile(uploadedFileName);
            
            switch (getContentFamily(uploadedFile.getContentType())) {
               case IMAGES:
                  
                  Encounter encounter = saveVisitDocumentEncounter(patient, visit, context.getEncounterType(), provider, context.getEncounterRole(), context.getEncounterService());
                  ConceptComplex conceptComplex = context.getConceptComplex();
                  obs = saveUploadedImageObs(patient.getPerson(), encounter, uploadedFile, fileCaption, conceptComplex, instructions, context.getObsService());
                  break;
                  
               case OTHER:
               default:
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

   /*
    * @see https://wiki.openmrs.org/display/docs/Complex+Obs+Support
    */
   protected Obs saveUploadedImageObs(Person person, Encounter encounter, MultipartFile file, String fileCaption, ConceptComplex conceptComplex, String instructions, ObsService obsService)
         throws IOException
   {
      Object image = file.getInputStream();

      double compressionRatio = VisitDocumentsContext.getCompressionRatio(file.getSize(), 1000000 * context.getMaxStorageFileSize());
      if (compressionRatio < 1) {
         image = Thumbnails.of(file.getInputStream()).scale(compressionRatio).asBufferedImage();
      }

      Obs obs = new Obs(person, conceptComplex, encounter.getEncounterDatetime(), encounter.getLocation());
      obs.setEncounter(encounter);
      obs.setComment(fileCaption);
      if (StringUtils.isEmpty(instructions)) {
         instructions = PatientImageComplexData.INSTRUCTIONS_DEFAULT;
      }
      obs.setComplexData( new PatientImageComplexData(instructions, file.getOriginalFilename(), image, file.getContentType()) );
      return obsService.saveObs(obs, getClass().toString());
   }

   protected Encounter saveVisitDocumentEncounter(Patient patient, Visit visit, EncounterType encounterType, Provider provider, EncounterRole encounterRole, EncounterService encounterService) {

      Encounter encounter = new Encounter();
      encounter.setVisit(visit);
      encounter.setEncounterType(encounterType);
      encounter.setPatient(visit.getPatient());
      encounter.setLocation(visit.getLocation());
      if (context.isOneEncounterPerVisit()) {
         List<Encounter> encounters = visit.getNonVoidedEncounters();
         for (Encounter e : encounters) {
            if (e.getEncounterType().getUuid() == encounterType.getUuid()) {
               encounter = e;
               break;
            }
         }
      }
      encounter.setProvider(encounterRole, provider);
      encounter.setEncounterDatetime(new Date());
      return encounterService.saveEncounter(encounter);
   }
   
   @RequestMapping(value = VisitDocumentsConstants.DOWNLOAD_DOCUMENT_URL, method = RequestMethod.GET)
   public void downloadDocument(@RequestParam("obs") String obsUuid, @RequestParam(value="view", required=false) String view,
         HttpServletResponse response)
   {
      if (StringUtils.isEmpty(view))
         view = VisitDocumentsConstants.DOC_VIEW_ORIGINAL;

      Obs obs = context.getObsService().getObsByUuid(obsUuid);
      Obs complexObs = context.getObsService().getComplexObs(obs.getObsId(), view);
      ComplexData complexData = complexObs.getComplexData();
      
      String mimeType = getContentType(complexData);
      try {
         response.setContentType(mimeType);
         switch (getContentFamily(mimeType)) {
            case IMAGES:
               response.getOutputStream().write( getByteArray(complexData) );
               break;
               
            case OTHER:
            default:
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
    * Extracts the MIME type of a {@link ComplexData} instance.
    * @param complexData
    * @return The MIME type (or content type).
    */
   public static String getContentType(ComplexData complexData) {
      
      if (complexData instanceof ComplexData_2_0)
         return ((ComplexData_2_0) complexData).getMimeType();
      
      byte[] bytes = getByteArray(complexData);
      if (ArrayUtils.isEmpty(bytes))
         return VisitDocumentsConstants.UNKNOWN_MIME_TYPE;
      
      InputStream stream = new BufferedInputStream(new ByteArrayInputStream(bytes));
      try {
         return URLConnection.guessContentTypeFromStream(stream);
      } catch (IOException e) {
         return VisitDocumentsConstants.UNKNOWN_MIME_TYPE;
      }
   }
   
   /**
    * This returns the byte array out of the complex data's inner data.
    * This is borrowed from {@link ComplexObsServlet}.
    * @return The byte array, or an empty array if an error occurred.
    */
   public static byte[] getByteArray(ComplexData complexData)
   {
      Object data = (complexData != null) ? complexData.getData() : new byte[0];
      
      if (data == null) {
         return new byte[0];
      }
      if (data instanceof byte[]) {
         return (byte[]) data;         
      }
      else if (RenderedImage.class.isAssignableFrom(data.getClass())) {
         RenderedImage image = (RenderedImage) data;

         ByteArrayOutputStream bytesOutStream = new ByteArrayOutputStream();
         try {
            ImageOutputStream imgOutStream = ImageIO.createImageOutputStream(bytesOutStream);
            String extension = FilenameUtils.getExtension(complexData.getTitle());
            ImageIO.write(image, extension, imgOutStream);
            imgOutStream.close();
         }
         catch (IOException e) {
            return new byte[0];
         }
         
         return bytesOutStream.toByteArray();
      }
      else if (InputStream.class.isAssignableFrom(data.getClass())) {
         try {
            return IOUtils.toByteArray((InputStream) data);
         } catch (IOException e) {
            return new byte[0];
         }
      }
      else {
         return new byte[0];
      }
   }
}
