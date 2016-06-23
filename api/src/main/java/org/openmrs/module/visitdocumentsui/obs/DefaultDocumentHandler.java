package org.openmrs.module.visitdocumentsui.obs;

import java.io.File;

import org.openmrs.Obs;
import org.openmrs.module.visitdocumentsui.VisitDocumentsConstants;
import org.openmrs.obs.ComplexData;
import org.openmrs.obs.handler.AbstractHandler;
import org.openmrs.obs.handler.BinaryDataHandler;

public class DefaultDocumentHandler extends AbstractDocumentHandler {
   
   public DefaultDocumentHandler() {
      super();
   }
   
   protected void setParentComplexObsHandler() {
      setParent( new BinaryDataHandler() );
   }
	
   protected ComplexData readComplexData(Obs obs, ValueComplex valueComplex, String view) {
      // We invoke the parent to inherit from the file reading routines.
      obs.setValueComplex(valueComplex.getFileName());
      ComplexData complexData;
      if (view.equals(VisitDocumentsConstants.DOC_VIEW_THUMBNAIL)) {
         complexData = new ComplexData(valueComplex.getFileName(), null);
      }
      else {
         obs = getParent().getObs(obs, "whatever_view"); // BinaryDataHandler doesn't in fact handle several views
         complexData = obs.getComplexData();
      }
      
      // Then we build our own custom complex data
      return new DocumentComplexData(valueComplex.getInstructions(), complexData.getTitle(), complexData.getData(), valueComplex.getMimeType());
   }
   
   protected boolean deleteComplexData(Obs obs, DocumentComplexData docComplexData) {
      // We use a temp obs whose value complex points to the file name
      Obs tmpObs = new Obs();
      tmpObs.setValueComplex(docComplexData.getTitle());
      return getParent().purgeComplexData(tmpObs);
   }
   
   protected ValueComplex saveComplexData(Obs obs, DocumentComplexData docComplexData) {
      // We invoke the parent to inherit from the file saving routines.
      obs = getParent().saveObs(obs);

      File savedFile = AbstractHandler.getComplexDataFile(obs);
      String savedFileName = savedFile.getName();
      
      return new ValueComplex(docComplexData.getInstructions(), docComplexData.getMimeType(), savedFileName);
   }
}
