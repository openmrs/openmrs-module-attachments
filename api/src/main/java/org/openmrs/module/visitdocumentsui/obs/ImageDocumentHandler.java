package org.openmrs.module.visitdocumentsui.obs;

import java.io.File;
import java.io.IOException;

import net.coobird.thumbnailator.Thumbnails;

import org.openmrs.Obs;
import org.openmrs.api.APIException;
import org.openmrs.module.visitdocumentsui.VisitDocumentsConstants;
import org.openmrs.obs.ComplexData;
import org.openmrs.obs.handler.AbstractHandler;
import org.openmrs.obs.handler.ImageHandler;

public class ImageDocumentHandler extends AbstractDocumentHandler {

   public final static int THUMBNAIL_HEIGHT = 200;
   public final static int THUMBNAIL_WIDTH = THUMBNAIL_HEIGHT;

   public ImageDocumentHandler() {
      super();
   }
   
   @Override
   protected void setParentComplexObsHandler() {
      setParent( new ImageHandler() );
   }
   
   @Override
   protected DocumentComplexData readComplexData(Obs obs, ValueComplex valueComplex, String view) {
      
      String fileName = valueComplex.getFileName();
      if (view.equals(VisitDocumentsConstants.DOC_VIEW_THUMBNAIL)) {
         fileName = buildThumbnailFileName(fileName);
      }
      
      // We invoke the parent to inherit from the file reading routines.
      obs.setValueComplex(fileName);
      obs = getParent().getObs(obs, "whatever_view"); // ImageHandler doesn't in fact handle several views
      ComplexData complexData = obs.getComplexData();
      
      // Then we build our own custom complex data
      return new DocumentComplexData(valueComplex.getInstructions(), complexData.getTitle(), complexData.getData(), valueComplex.getMimeType());
   }
   
   @Override
   protected boolean deleteComplexData(Obs obs, DocumentComplexData docComplexData) {
      
      // We use a temp obs whose complex data points to the file names
      String fileName = docComplexData.getTitle();
      String thumbnailFileName = buildThumbnailFileName(fileName);
      
      Obs tmpObs = new Obs();
      
      tmpObs.setValueComplex(thumbnailFileName);
      boolean isThumbNailPurged = getParent().purgeComplexData(tmpObs);
      tmpObs.setValueComplex(fileName);
      boolean isImagePurged = getParent().purgeComplexData(tmpObs);
      
      return isThumbNailPurged && isImagePurged;
   }
   
   @Override
   protected ValueComplex saveComplexData(Obs obs, DocumentComplexData docComplexData) {

      // We invoke the parent to inherit from the file saving routines.
      obs = getParent().saveObs(obs);

      File savedFile = AbstractHandler.getComplexDataFile(obs);
      String savedFileName = savedFile.getName();
      
      // Saving the thumbnail
      File dir = savedFile.getParentFile();
      String thumbnailFileName = buildThumbnailFileName(savedFileName);
      try {
         Thumbnails.of(savedFile.getAbsolutePath()).size(THUMBNAIL_HEIGHT, THUMBNAIL_WIDTH).toFile( new File(dir, thumbnailFileName) );
      } catch (IOException e) {
         throw new APIException("A thumbnail file could not be saved for obs with"
               + "OBS_ID='" + obs.getObsId() + "', "
               + "FILE='" + savedFile.getPath() + "'.", e);
      }
      
      return new ValueComplex(docComplexData.getInstructions(), docComplexData.getMimeType(), savedFileName);
   }
}
