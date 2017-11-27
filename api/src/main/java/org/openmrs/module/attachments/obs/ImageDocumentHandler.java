package org.openmrs.module.attachments.obs;

import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import net.coobird.thumbnailator.Thumbnails;

import org.openmrs.Obs;
import org.openmrs.api.APIException;
import org.openmrs.module.attachments.AttachmentsConstants;
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
   protected ComplexData readComplexData(Obs obs, ValueComplex valueComplex, String view) {
      
      String fileName = valueComplex.getFileName();
      
      if (view.equals(AttachmentsConstants.ATT_VIEW_THUMBNAIL) && 
    		  !fileName.contains("_smallfile")) {
         fileName = buildThumbnailFileName(fileName);
      }
      
      // We invoke the parent to inherit from the file reading routines.
      Obs tmpObs = new Obs();
      tmpObs.setValueComplex(fileName);   // Temp obs used as a safety
      tmpObs = getParent().getObs(tmpObs, AttachmentsConstants.IMAGE_HANDLER_VIEW); // ImageHandler doesn't handle several views
      ComplexData complexData = tmpObs.getComplexData();
      
      // Then we build our own custom complex data
      return getComplexDataHelper().build(valueComplex.getInstructions(), complexData.getTitle(), complexData.getData(), valueComplex.getMimeType())
            .asComplexData();
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

	   File newSavedFile = null;
	   int imageHeight = 0;
	   int imageWidth = 0;
	   
      // We invoke the parent to inherit from the file saving routines.
      obs = getParent().saveObs(obs);

      File savedFile = AbstractHandler.getComplexDataFile(obs);
      String savedFileName = savedFile.getName();
      
      // Get image dimensions
      try {
	      BufferedImage image = ImageIO.read(savedFile);
	      imageHeight = image.getHeight();
	      imageWidth = image.getWidth();
      } catch (IOException e) {
		         getParent().purgeComplexData(obs);
		         throw new APIException("Can't read the image file"
		               + "OBS_ID='" + obs.getObsId() + "', "
		               + "FILE='" + docComplexData.getTitle() + "'.", e);
      }    
  
      // Check for small image file
      if ((imageHeight <= THUMBNAIL_HEIGHT) && (imageWidth <= THUMBNAIL_WIDTH)) { 	  
    	  // Rename the file by append "_smallfile" to the file.
    	  // Therefore, we will know this is a small file and no need for thumnail.
    	  String newSavedFileName = buildSmallFileFileName(savedFile.getAbsolutePath());
    	  newSavedFile = new File(newSavedFileName);
    	  savedFile.renameTo(newSavedFile);
    	  savedFileName = buildSmallFileFileName(savedFileName);    	  
      } else {
	      // Saving the thumbnail
	      File dir = savedFile.getParentFile();
	      String thumbnailFileName = buildThumbnailFileName(savedFileName);  
	      try {
	         Thumbnails.of(savedFile.getAbsolutePath()).size(THUMBNAIL_HEIGHT, THUMBNAIL_WIDTH).toFile( new File(dir, thumbnailFileName) );
	      } catch (IOException e) {
	         getParent().purgeComplexData(obs);
	         throw new APIException("A thumbnail file could not be saved for obs with"
	               + "OBS_ID='" + obs.getObsId() + "', "
	               + "FILE='" + docComplexData.getTitle() + "'.", e);
	      }            
      
      } 
      
      return new ValueComplex(docComplexData.getInstructions(), docComplexData.getMimeType(), savedFileName);
   }
}
