package org.openmrs.module.patientimages;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Obs;
import org.openmrs.obs.ComplexData;
import org.openmrs.obs.handler.ImageHandler;

public class PatientImageHandler extends ImageHandler {
	
	/**
	 * Reads {@link Obs#getValueComplex()} to know how to set (where to fetch) the complex data.
	 */
	@Override
	public Obs getObs(Obs obs, String view) {
		
		String[] names = obs.getValueComplex().split("\\|");
		String type = "";
		if (names.length == 3) {
			type = names[0];
		}

		switch (type) {
		case "":
			obs = super.getObs(obs, view);
		}
		
		return obs;
	}
	
	@Override
	public boolean purgeComplexData(Obs obs) {
		return super.purgeComplexData(obs);
	}
	
	/**
	 * Saves the (non-persistent) complex data and sets the valueComplex accordingly.
	 */
	@Override
	public Obs saveObs(Obs obs) {
		
		ComplexData complexData = obs.getComplexData();
		
		String type = "";
		if (complexData instanceof PatientImageComplexData) {
			type = ((PatientImageComplexData) complexData).getType();
		}
		
		switch (type) {
		case "":
			obs = super.saveObs(obs);
		}
		
		if (!StringUtils.isEmpty(type)) {	// if a type is provided we prepend it to the value complex
			obs.setValueComplex(type + "|" + obs.getValueComplex());
		}
		
		return obs;
	}
}
