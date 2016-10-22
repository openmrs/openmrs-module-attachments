package org.openmrs.module.visitdocumentsui.obs;

import org.openmrs.BaseOpenmrsData;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.obs.ComplexData;

/**
 * Visit Document represents all the part of a complex obs that make for a "visit document".
 * 
 * @author Mekom Solutions
 */
public class VisitDocument extends BaseOpenmrsData implements java.io.Serializable {
   
   private static final long serialVersionUID = -3552798988737497690L;
   
   protected Integer id = null;
   protected String uuid = "";
   protected String comment = "";
   protected ComplexData complexData = null;
   
   public VisitDocument() {}
   
   /**
    * @param obs A complex obs
    */
   public VisitDocument(Obs obs) {
      super();
      setUuid(obs.getUuid());
      setId(obs.getId());
      setComment(obs.getComment());
      setComplexData(obs.getComplexData());
   }
   
   public Obs getObs() {
      Obs obs = Context.getObsService().getObsByUuid(getUuid());
      if (obs == null) {
         obs = new Obs();
         obs.setUuid(getUuid());
         obs.setId(getId());
      }
      obs.setComment(getComment());
      obs.setComplexData(getComplexData());
      return obs;
   }

   @Override
   public Integer getId() {
      return id;
   }

   @Override
   public void setId(Integer id) {
      this.id = id;
   }
   
   public String getUuid() {
      return uuid;
   }

   public void setUuid(String uuid) {
      this.uuid = uuid;
   }

   public String getComment() {
      return comment;
   }

   public void setComment(String comment) {
      this.comment = comment;
   }

   public ComplexData getComplexData() {
      return complexData;
   }

   public void setComplexData(ComplexData complexData) {
      this.complexData = complexData;
   }
}
