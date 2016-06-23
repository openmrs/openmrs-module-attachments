package org.openmrs.module.visitdocumentsui.obs;

import org.apache.commons.lang.StringUtils;
import org.openmrs.module.visitdocumentsui.VisitDocumentsConstants;

public class ValueComplex {
   
   public static final String INSTRUCTIONS_NONE = "instructions.none";
   public static final String INSTRUCTIONS_DEFAULT = "instructions.default";
   
   protected String instructions = INSTRUCTIONS_NONE;
   protected String mimeType = VisitDocumentsConstants.UNKNOWN_MIME_TYPE;
   protected String fileName = "";

   protected final static String UNIQUE_PREFIX = "m3ks";   // This is used to identify our implementation from saved valueComplex.
   protected final static String SEP = " | ";
   protected final static int METADATA_PARTS_COUNT = 3;  // To avoid being out of bounds on metaParts[]
   
   public ValueComplex(String valueComplex) {
      
      if (StringUtils.substringBefore(valueComplex, SEP).equals(UNIQUE_PREFIX) == false) {
         this.instructions = INSTRUCTIONS_NONE;
         return;
      }
      
      String metaData = StringUtils.substringAfter(valueComplex, SEP);
      String[] metaParts = StringUtils.split(metaData, SEP);
      int partCount = StringUtils.countMatches(buildValueComplex("", "", ""), SEP);
      if (METADATA_PARTS_COUNT != partCount || metaParts.length != partCount) {
         // Somehow the metadata is malformed.
         this.instructions = INSTRUCTIONS_NONE;
         return;
      }
      
      instructions = metaParts[0];
      mimeType = metaParts[1];
      fileName = metaParts[2];
   }
   
   public ValueComplex(String instructions, String mimeType, String fileName) {
      this(buildValueComplex(instructions, mimeType, fileName));
   }
   
   public boolean isOwnImplementation() {
      return instructions != INSTRUCTIONS_NONE;
   }

   public String getInstructions() {
      return instructions;
   }

   public String getFileName() {
      return fileName;
   }

   public String getMimeType() {
      return mimeType;
   }
   
   public String getValueComplex() {
      return buildValueComplex(instructions, mimeType, fileName);
   }

   public static String buildValueComplex(String instructions, String mimeType, String savedFileName) {
      return UNIQUE_PREFIX + SEP + instructions + SEP + mimeType + SEP + savedFileName;
   }
}
