package org.openmrs.module.visitdocumentsui.obs;

import org.openmrs.BaseOpenmrsData;

/**
 * Visit Document represents all the part of a complex obs that make for a "visit document".
 * 
 * @author Mekom Solutions
 */
public class VisitDocument extends BaseOpenmrsData implements java.io.Serializable {
   
   private static final long serialVersionUID = -3552798988737497690L;
   
   protected String comment = "";
   
   public VisitDocument() {}

   @Override
   public Integer getId() {
      return 0;
   }

   @Override
   public void setId(Integer id) {
   }
   
   public String getComment() {
      return comment;
   }

   public void setComment(String comment) {
      this.comment = comment;
   }
}
