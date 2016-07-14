package org.openmrs.module.visitdocumentsui.obs;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.openmrs.Obs;
import org.openmrs.module.visitdocumentsui.VisitDocumentsConstants;
import org.openmrs.obs.ComplexData;
import org.openmrs.obs.ComplexObsHandler;
import org.openmrs.obs.handler.AbstractHandler;

/**
 * Double inheritance class.
 * The actual implementation parent must be set through {@link #setParentComplexObsHandler()}.
 */
public abstract class AbstractDocumentHandler extends AbstractHandler implements ComplexObsHandler {

  private ComplexObsHandler parent;

  public AbstractDocumentHandler() {
    super();
    setParentComplexObsHandler();
  }

  /*
  * To set the "real" implementation parent.
  */
  abstract protected void setParentComplexObsHandler();

  /*
  * Complex data CRUD - Read
  */
  abstract protected ComplexData readComplexData(Obs obs, ValueComplex valueComplex, String view);

  /*
  * Complex data CRUD - Delete
  */
  abstract protected boolean deleteComplexData(Obs obs, DocumentComplexData docComplexData);

  /*
  * Complex data CRUD - Save (Update)
  */
  abstract protected ValueComplex saveComplexData(Obs obs, DocumentComplexData docComplexData);

  protected void setParent(ComplexObsHandler complexObsHandler) {
    this.parent = complexObsHandler; 
  }

  final protected ComplexObsHandler getParent() {
    return parent;
  }

  protected static String buildThumbnailFileName(String fileName) {
    return FilenameUtils.removeExtension(fileName) + "_thumb" + "." + FilenameUtils.getExtension(fileName);
  }

  /**
  * @param complexData An obs's complex data.
  * @return null if this is not our implementation, the custom {@link DocumentComplexData} otherwise.
  */
  public static DocumentComplexData fetchDocumentComplexData(ComplexData complexData) {

    if ((complexData instanceof DocumentComplexData) == false) {
      return null; 
    }

    DocumentComplexData docData = (DocumentComplexData) complexData;
    String instructions = docData.getInstructions();
    if (instructions.equals(ValueComplex.INSTRUCTIONS_NONE)) {
      return null;
    }

    return docData;
  }

  /*
  * Drifts to our own CRUD overloadable routine when it is our implementation.
  */
  @Override
  final public Obs getObs(Obs obs, String view) {

    ValueComplex valueComplex = new ValueComplex(obs.getValueComplex()); 
    if (valueComplex.isOwnImplementation() == false) {  // not our implementation
      return getParent().getObs(obs, view);
    }

    if (StringUtils.isEmpty(view)) {
      view = VisitDocumentsConstants.DOC_VIEW_ORIGINAL;
    }

    ComplexData docData = readComplexData(obs, valueComplex, view);
    obs.setComplexData(docData);
    return obs;
  }

  /*
  * Drifts to our own CRUD overloadable routine when it is our implementation.
  */
  @Override
  final public boolean purgeComplexData(Obs obs) {

    DocumentComplexData docData = fetchDocumentComplexData(obs.getComplexData());
    if (docData == null) {   // not our implementation
      if (obs.getComplexData() == null) {
        log.error("Complex data was null and hence was not purged for OBS_ID='" + obs.getObsId() + "'.");
        return false;
      }
      else {
        return getParent().purgeComplexData(obs);
      }
    }

    return deleteComplexData(obs, docData);
  }

  /*
  * Drifts to our own CRUD overloadable routine when it is our implementation.
  */
  @Override
  final public Obs saveObs(Obs obs) {

    DocumentComplexData docComplexData = fetchDocumentComplexData(obs.getComplexData());
    if (docComplexData == null) { // not our implementation
      if (obs.getComplexData() == null) {
        log.error("Complex data was null and hence was not saved for OBS_ID='" + obs.getObsId() + "'.");
        return obs;
      }
      else {
        return getParent().saveObs(obs);
      }
    }

    ValueComplex valueComplex = saveComplexData(obs, docComplexData);
    obs.setValueComplex(valueComplex.getValueComplex());
    return obs;
  }
}