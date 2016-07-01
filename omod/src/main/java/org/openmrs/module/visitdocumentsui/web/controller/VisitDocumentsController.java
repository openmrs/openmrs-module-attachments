package org.openmrs.module.visitdocumentsui.web.controller;

import static org.openmrs.module.visitdocumentsui.VisitDocumentsContext.getCompressionRatio;
import static org.openmrs.module.visitdocumentsui.VisitDocumentsContext.getContentFamily;
import static org.openmrs.module.visitdocumentsui.VisitDocumentsContext.isMimeTypeHandled;

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
import org.openmrs.module.visitdocumentsui.VisitDocumentsConstants.ContentFamily;
import org.openmrs.module.visitdocumentsui.VisitDocumentsContext;
import org.openmrs.module.visitdocumentsui.obs.ComplexData_2_0;
import org.openmrs.module.visitdocumentsui.obs.DocumentComplexData;
import org.openmrs.module.visitdocumentsui.obs.ValueComplex;
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
            MultipartFile uploadedFile = request.getFile(uploadedFileName);
            
            Encounter encounter = getVisitDocumentEncounter(patient, visit, context.getEncounterType(), provider, context.getEncounterRole(), context.getEncounterService());
            ConceptComplex conceptComplex = null;
            if (StringUtils.isEmpty(instructions))
               instructions = ValueComplex.INSTRUCTIONS_DEFAULT;
            
            switch (getContentFamily(uploadedFile.getContentType())) {
               case IMAGE:
                  conceptComplex = context.getConceptComplex(ContentFamily.IMAGE);
                  obs = prepareComplexObs(patient.getPerson(), encounter, fileCaption, conceptComplex);
                  obs = saveImageDocument(obs, uploadedFile, instructions, context.getObsService());
                  break;
                  
               case OTHER:
               default:
                  conceptComplex = context.getConceptComplex(ContentFamily.OTHER);
                  obs = prepareComplexObs(patient.getPerson(), encounter, fileCaption, conceptComplex);
                  obs = saveOtherDocument(obs, uploadedFile, instructions, context.getObsService());
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
   
   protected Obs prepareComplexObs(Person person, Encounter encounter, String fileCaption, ConceptComplex conceptComplex) {
      Obs obs = new Obs(person, conceptComplex, new Date(), encounter.getLocation());
      obs.setEncounter(encounter);
      obs.setComment(fileCaption);
      return obs;
   }

   protected Obs saveImageDocument(Obs obs, MultipartFile file, String instructions, ObsService obsService)
         throws IOException
   {
      Object image = file.getInputStream();
      double compressionRatio = getCompressionRatio(file.getSize(), 1000000 * context.getMaxStorageFileSize());
      if (compressionRatio < 1) {
         image = Thumbnails.of(file.getInputStream()).scale(compressionRatio).asBufferedImage();
      }
      obs.setComplexData( new DocumentComplexData(instructions, file.getOriginalFilename(), image, file.getContentType()) );
      return obsService.saveObs(obs, getClass().toString());
   }
   
   protected Obs saveOtherDocument(Obs obs, MultipartFile file, String instructions, ObsService obsService)
         throws IOException
   {
      obs.setComplexData( new DocumentComplexData(instructions, file.getOriginalFilename(), file.getBytes(), file.getContentType()) );
      return obsService.saveObs(obs, getClass().toString());
   }

   protected Encounter getVisitDocumentEncounter(Patient patient, Visit visit, EncounterType encounterType, Provider provider, EncounterRole encounterRole, EncounterService encounterService)
   {
      Encounter encounter = new Encounter();
      encounter.setVisit(visit);
      encounter.setEncounterType(encounterType);
      encounter.setPatient(visit.getPatient());
      encounter.setLocation(visit.getLocation());
      boolean saveEncounter = true;
      if (context.isOneEncounterPerVisit()) {
         List<Encounter> encounters = visit.getNonVoidedEncounters();
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
         // The document meta data is sent as HTTP headers.
         response.setContentType(mimeType);
         response.addHeader("Content-Family", getContentFamily(mimeType).name());   // custom header
         response.addHeader("File-Name", complexData.getTitle());   // custom header
         response.addHeader("File-Ext", getExtension(complexData.getTitle(), mimeType));   // custom header
         switch (getContentFamily(mimeType)) {
            case IMAGE:
            case OTHER:
            default:
               response.getOutputStream().write(getByteArray(complexData));
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
   
   /**
    * Extracts the MIME type of a {@link ComplexData} instance.
    * @param complexData
    * @return The MIME type (or content type).
    */
   public static String getContentType(ComplexData complexData) {
      
      if (complexData instanceof ComplexData_2_0) {
         ComplexData_2_0 complexData_2_0 = (ComplexData_2_0) complexData;
         if (isMimeTypeHandled(complexData_2_0.getMimeType()))   // Perhaps too restrictive
            return complexData_2_0.getMimeType();
      }
      
      byte[] bytes = getByteArray(complexData);
      if (ArrayUtils.isEmpty(bytes))
         return VisitDocumentsConstants.UNKNOWN_MIME_TYPE;
      
      // guessing the content type
      InputStream stream = new BufferedInputStream(new ByteArrayInputStream(bytes));
      try {
         String mimeType = URLConnection.guessContentTypeFromStream(stream); 
         return mimeType == null ? VisitDocumentsConstants.UNKNOWN_MIME_TYPE : mimeType;
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
      final byte[] emptyContent = new byte[0];
      
      Object data = (complexData != null) ? complexData.getData() : emptyContent;
      
      if (data == null) {
         return emptyContent;
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
            return emptyContent;
         }
         
         return bytesOutStream.toByteArray();
      }
      else if (InputStream.class.isAssignableFrom(data.getClass())) {
         try {
            return IOUtils.toByteArray((InputStream) data);
         } catch (IOException e) {
            return emptyContent;
         }
      }
      else {
         return emptyContent;
      }
   }
}
