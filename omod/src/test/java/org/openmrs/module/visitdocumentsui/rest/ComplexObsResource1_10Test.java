package org.openmrs.module.visitdocumentsui.rest;

import org.junit.Test;
import org.openmrs.Obs;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;

public class ComplexObsResource1_10Test extends BaseDelegatingResourceTest<ComplexObsResource1_10, Obs> {

   private Obs obs = new Obs();
   
   @Override
   public Obs newObject() {
      return obs;
   }

   @Override
   public String getDisplayProperty() {
      return "";
   }

   @Override
   public String getUuidProperty() {
      return obs.getUuid();
   }
   
   @Test
   public void shouldRunTest() throws Exception {
      Object resource = getResource();
   }
}
