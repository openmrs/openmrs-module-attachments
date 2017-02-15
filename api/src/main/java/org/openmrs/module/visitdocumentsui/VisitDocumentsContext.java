/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.visitdocumentsui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.openmrs.Concept;
import org.openmrs.ConceptComplex;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.LocationService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.VisitService;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.utils.ModuleProperties;
import org.openmrs.module.visitdocumentsui.VisitDocumentsConstants.ContentFamily;
import org.openmrs.module.visitdocumentsui.obs.ComplexDataHelper;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Inject this class to access services and global properties.
 */
@Component(VisitDocumentsConstants.COMPONENT_VDUI_CONTEXT)
public class VisitDocumentsContext extends ModuleProperties
{
	protected final Log log = LogFactory.getLog(getClass());

	@Autowired
	@Qualifier("obsService")
	protected ObsService obsService;

	@Autowired
	@Qualifier("adtService")
	protected AdtService adtService;

	@Autowired
	@Qualifier("locationService")
	protected LocationService locationService;
	
	@Autowired
	@Qualifier(VisitDocumentsConstants.COMPONENT_COMPLEXDATA_HELPER)
	protected ComplexDataHelper complexDataHelper;

	@Autowired
	@Qualifier(VisitDocumentsConstants.COMPONENT_VISIT_COMPATIBILITY)
	protected VisitCompatibility visitCompatibility;

	/*
	 * Exposing all needed services through OUR context
	 */

	public ConceptService getConceptService() {
		return conceptService;
	}

	public ObsService getObsService() {
		return obsService;
	}

	public VisitService getVisitService() {
		return visitService;
	}

	public ProviderService getProviderService() {
		return providerService;
	}

	public PatientService getPatientService() {
		return patientService;
	}

	public EncounterService getEncounterService() {
		return encounterService;
	}

	public AdtService getAdtService() {
		return adtService;
	}

	public LocationService getLocationService() {
		return locationService;
	}
	
	public ComplexDataHelper getComplexDataHelper() {
		return complexDataHelper;
	}

	public AdministrationService getAdministrationService() {
		return administrationService;
	}

	public boolean doAllowEmptyCaption() {
		return this.getBooleanByGlobalProperty(VisitDocumentsConstants.GP_ALLOW_NO_CAPTION);
	}

	public boolean isOneEncounterPerVisit() {
		String flowStr = getAdministrationService().getGlobalProperty(VisitDocumentsConstants.GP_ENCOUNTER_SAVING_FLOW);
		return StringUtils.equalsIgnoreCase(flowStr, "unique");
	}

	public Encounter getVisitDocumentEncounter(Patient patient, Visit visit, Provider provider)
	{
		Encounter encounter = new Encounter();
		encounter.setVisit(visit);
		encounter.setEncounterType(getEncounterType());
		encounter.setPatient(visit.getPatient());
		encounter.setLocation(visit.getLocation());
		boolean saveEncounter = true;
		if (isOneEncounterPerVisit()) {
			List<Encounter> encounters = visitCompatibility.getNonVoidedEncounters(visit);
			for (Encounter e : encounters) {
				if (e.getEncounterType().getUuid() == getEncounterType().getUuid()) {
					encounter = e;
					saveEncounter = false;
					break;
				}
			}
		}
		encounter.setProvider(getEncounterRole(), provider);
		encounter.setEncounterDatetime(new Date());
		if (saveEncounter) {
			encounter = getEncounterService().saveEncounter(encounter);
		}
		return encounter;
	}

	/*
	 * See super#getIntegerByGlobalProperty(String globalPropertyName)
	 */
	protected Double getDoubleByGlobalProperty(String globalPropertyName) {
		String globalProperty = administrationService.getGlobalProperty(globalPropertyName);
		try {
			return Double.valueOf(globalProperty);
		}
		catch (Exception e) {
			throw new IllegalStateException("Global property " + globalPropertyName + " value of " + globalProperty + " is not parsable as a Double");
		}
	}

	/*
	 * See super#getIntegerByGlobalProperty(String globalPropertyName)
	 */
	protected Boolean getBooleanByGlobalProperty(String globalPropertyName) {
		String globalProperty = administrationService.getGlobalProperty(globalPropertyName);
		return BooleanUtils.toBoolean(globalProperty);
	}

	/**
	 * @return The concept complex used to save uploaded image-obs.
	 */
	public ConceptComplex getDefaultConceptComplex() {
		String globalPropertyName = VisitDocumentsConstants.GP_DEFAULT_CONCEPT_COMPLEX_UUID;
		Concept concept = getConceptByGlobalProperty(globalPropertyName);
		ConceptComplex conceptComplex = getConceptService().getConceptComplex(concept.getConceptId());
		if (conceptComplex == null) {
			throw new IllegalStateException("Configuration required: " + globalPropertyName);
		}
		return conceptComplex;
	}

	/**
	 * Returns a simple String-String map from its JSON representation saved as a global property.
	 */
	protected Map<String, String> getMapByGlobalProperty(String globalPropertyName) {
		Map<String, String> map = Collections.<String,String>emptyMap();
		String globalProperty = administrationService.getGlobalProperty(globalPropertyName);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		TypeReference<HashMap<String, String>> typeRef = new TypeReference<HashMap<String, String>>() {};
		try {
			map = mapper.readValue(globalProperty, typeRef);
		} catch (Exception e) {
			log.error("Could not parse global property '" + globalPropertyName + "' into a Map<String, String>.", e);
		}
		return map;
	}

	/**
	 * @param contentFamily The content family ('IMAGE', 'PDF', 'OTHER', ... etc).
	 * @return The concept complex configured to save files belonging to the content family,
	 * and if none is found the default concept complex is returned.
	 */
	public ConceptComplex getConceptComplex(ContentFamily contentFamily) {
		Map<String, String> map = getMapByGlobalProperty(VisitDocumentsConstants.GP_CONCEPT_COMPLEX_UUID_MAP);
		Concept concept = getConceptService().getConceptByUuid(map.get(contentFamily.toString()));
		if (concept != null) {
			return getConceptService().getConceptComplex(concept.getConceptId());
		}
		return getDefaultConceptComplex();
	}

	public List<String> getConceptComplexList() {
		List<String> list = Collections.<String>emptyList();
		final String globalPropertyName = VisitDocumentsConstants.GP_CONCEPT_COMPLEX_UUID_LIST;
		String globalProperty = administrationService.getGlobalProperty(globalPropertyName);

		ObjectMapper mapper = new ObjectMapper();
		TypeReference<ArrayList<String>> typeRef = new TypeReference<ArrayList<String>>() {};
		try {
			list = mapper.readValue(globalProperty, typeRef);
		} catch (Exception e) {
			log.error("Could not parse global property '" + globalPropertyName + "' into a List<String>.", e);
		}
		return list;  
	}

	/**
	 * @return The encounter type for encounters recording the upload of an image.
	 */
	public EncounterType getEncounterType() {
		EncounterType encounterType = getEncounterTypeByGlobalProperty(VisitDocumentsConstants.GP_ENCOUNTER_TYPE_UUID);
		return encounterType;
	}

	/**
	 * @return The max file size allowed to be uploaded (in Megabytes).
	 */
	public Double getMaxUploadFileSize() {
		return getDoubleByGlobalProperty(VisitDocumentsConstants.GP_MAX_UPLOAD_FILE_SIZE);
	}

	/**
	 * @return The max file size allowed to be stored (in Megabytes).
	 */
	public Double getMaxStorageFileSize() {
		return getDoubleByGlobalProperty(VisitDocumentsConstants.GP_MAX_STORAGE_FILE_SIZE);
	}

	public boolean isWebcamAllowed() {
		return getBooleanByGlobalProperty(VisitDocumentsConstants.GP_WEBCAM_ALLOWED);
	}

	public double getMaxCompressionRatio() {
		double maxStorageSize = getMaxStorageFileSize();
		double maxUploadSize = getMaxUploadFileSize();
		if (maxStorageSize > 0)
			return Math.max(maxStorageSize, maxUploadSize) / maxStorageSize;
		else
			return 1;
	}

	public Integer getDashboardThumbnailCount() {
		return getIntegerByGlobalProperty(VisitDocumentsConstants.GP_DASHBOARD_THUMBNAIL_COUNT);
	}

	public Integer getMaxRestResultsCount() {
		return getIntegerByGlobalProperty(RestConstants.MAX_RESULTS_DEFAULT_GLOBAL_PROPERTY_NAME);
	}

	// TODO: Figure out if this is good enough
	public EncounterRole getEncounterRole() {
		EncounterRole unknownRole = getEncounterService().getEncounterRoleByUuid(EncounterRole.UNKNOWN_ENCOUNTER_ROLE_UUID);
		if (unknownRole == null) {
			throw new IllegalStateException("No 'Unknown' encounter role with uuid "
					+ EncounterRole.UNKNOWN_ENCOUNTER_ROLE_UUID + ".");
		}
		return unknownRole;
	}

	public static String getExtension(String mimeType) {
		String ext = "bin";
		if (mimeTypes.containsKey(mimeType)) {
			ext = mimeTypes.get(mimeType);
		}
		return ext;
	}

	/**
	 * @return The 'view' that should be affected when performing CRUD operation on complex obs.
	 */
	public String getCRUDDocumentView() {
		return VisitDocumentsConstants.DOC_VIEW_ORIGINAL;
	}

	public static double getCompressionRatio(double fileByteSize, double maxByteSize) {
		double compressionRatio = 1;
		if (fileByteSize > 0) {
			compressionRatio = Math.min(1, maxByteSize / fileByteSize);
		}
		return compressionRatio;
	}

	/**
	 * @param mimeType The MIME type of the uploaded content.
	 * @return The type/family of uploaded content based on the MIME type.
	 */
	public static ContentFamily getContentFamily(String mimeType) {
		ContentFamily contentFamily = ContentFamily.OTHER;
		if (StringUtils.equals(mimeType, "application/pdf")) {
			contentFamily = ContentFamily.PDF;
		}
		if (StringUtils.startsWith(mimeType, "image/")) {
			contentFamily = ContentFamily.IMAGE;
		}
		return contentFamily;
	}

	public static Map<String, ContentFamily> getContentFamilyMap() {
		Map<String, ContentFamily> map = new HashMap<String, ContentFamily>();
		for (String mimeType : mimeTypes.keySet()) {
			map.put(mimeType, getContentFamily(mimeType));
		}
		return map;
	}

	public static boolean isMimeTypeHandled(String mimeType) {
		return mimeTypes.containsKey(mimeType);
	}

	private static final Map<String, String> mimeTypes;
	static
	{
		mimeTypes = new HashMap<String, String>();
		mimeTypes.put("x-world/x-3dmf","3dm");
		mimeTypes.put("x-world/x-3dmf","3dmf");
		mimeTypes.put("application/octet-stream","a");
		mimeTypes.put("application/x-authorware-bin","aab");
		mimeTypes.put("application/x-authorware-map","aam");
		mimeTypes.put("application/x-authorware-seg","aas");
		mimeTypes.put("text/vnd.abc","abc");
		mimeTypes.put("text/html","acgi");
		mimeTypes.put("video/animaflex","afl");
		mimeTypes.put("application/postscript","ai");
		mimeTypes.put("audio/aiff","aif");
		mimeTypes.put("audio/x-aiff","aif");
		mimeTypes.put("audio/aiff","aifc");
		mimeTypes.put("audio/x-aiff","aifc");
		mimeTypes.put("audio/aiff","aiff");
		mimeTypes.put("audio/x-aiff","aiff");
		mimeTypes.put("application/x-aim","aim");
		mimeTypes.put("text/x-audiosoft-intra","aip");
		mimeTypes.put("application/x-navi-animation","ani");
		mimeTypes.put("application/x-nokia-9000-communicator-add-on-software","aos");
		mimeTypes.put("application/mime","aps");
		mimeTypes.put("application/octet-stream","arc");
		mimeTypes.put("application/arj","arj");
		mimeTypes.put("application/octet-stream","arj");
		mimeTypes.put("image/x-jg","art");
		mimeTypes.put("video/x-ms-asf","asf");
		mimeTypes.put("text/x-asm","asm");
		mimeTypes.put("text/asp","asp");
		mimeTypes.put("application/x-mplayer2","asx");
		mimeTypes.put("video/x-ms-asf","asx");
		mimeTypes.put("video/x-ms-asf-plugin","asx");
		mimeTypes.put("audio/basic","au");
		mimeTypes.put("audio/x-au","au");
		mimeTypes.put("application/x-troff-msvideo","avi");
		mimeTypes.put("video/avi","avi");
		mimeTypes.put("video/msvideo","avi");
		mimeTypes.put("video/x-msvideo","avi");
		mimeTypes.put("video/avs-video","avs");
		mimeTypes.put("application/x-bcpio","bcpio");
		mimeTypes.put("application/mac-binary","bin");
		mimeTypes.put("application/macbinary","bin");
		mimeTypes.put("application/octet-stream","bin");
		mimeTypes.put("application/x-binary","bin");
		mimeTypes.put("application/x-macbinary","bin");
		mimeTypes.put("image/bmp","bm");
		mimeTypes.put("image/bmp","bmp");
		mimeTypes.put("image/x-windows-bmp","bmp");
		mimeTypes.put("application/book","boo");
		mimeTypes.put("application/book","book");
		mimeTypes.put("application/x-bzip2","boz");
		mimeTypes.put("application/x-bsh","bsh");
		mimeTypes.put("application/x-bzip","bz");
		mimeTypes.put("application/x-bzip2","bz2");
		mimeTypes.put("text/plain","c");
		mimeTypes.put("text/x-c","c");
		mimeTypes.put("text/plain","c++");
		mimeTypes.put("application/vnd.ms-pki.seccat","cat");
		mimeTypes.put("text/plain","cc");
		mimeTypes.put("text/x-c","cc");
		mimeTypes.put("application/clariscad","ccad");
		mimeTypes.put("application/x-cocoa","cco");
		mimeTypes.put("application/cdf","cdf");
		mimeTypes.put("application/x-cdf","cdf");
		mimeTypes.put("application/x-netcdf","cdf");
		mimeTypes.put("application/pkix-cert","cer");
		mimeTypes.put("application/x-x509-ca-cert","cer");
		mimeTypes.put("application/x-chat","cha");
		mimeTypes.put("application/x-chat","chat");
		mimeTypes.put("application/java","class");
		mimeTypes.put("application/java-byte-code","class");
		mimeTypes.put("application/x-java-class","class");
		mimeTypes.put("application/octet-stream","com");
		mimeTypes.put("text/plain","com");
		mimeTypes.put("text/plain","conf");
		mimeTypes.put("application/x-cpio","cpio");
		mimeTypes.put("text/x-c","cpp");
		mimeTypes.put("application/mac-compactpro","cpt");
		mimeTypes.put("application/x-compactpro","cpt");
		mimeTypes.put("application/x-cpt","cpt");
		mimeTypes.put("application/pkcs-crl","crl");
		mimeTypes.put("application/pkix-crl","crl");
		mimeTypes.put("application/pkix-cert","crt");
		mimeTypes.put("application/x-x509-ca-cert","crt");
		mimeTypes.put("application/x-x509-user-cert","crt");
		mimeTypes.put("application/x-csh","csh");
		mimeTypes.put("text/x-script.csh","csh");
		mimeTypes.put("application/x-pointplus","css");
		mimeTypes.put("text/css","css");
		mimeTypes.put("text/plain","cxx");
		mimeTypes.put("application/x-director","dcr");
		mimeTypes.put("application/x-deepv","deepv");
		mimeTypes.put("text/plain","def");
		mimeTypes.put("application/x-x509-ca-cert","der");
		mimeTypes.put("video/x-dv","dif");
		mimeTypes.put("application/x-director","dir");
		mimeTypes.put("video/dl","dl");
		mimeTypes.put("video/x-dl","dl");
		mimeTypes.put("application/msword","doc");
		mimeTypes.put("application/msword","dot");
		mimeTypes.put("application/commonground","dp");
		mimeTypes.put("application/drafting","drw");
		mimeTypes.put("application/octet-stream","dump");
		mimeTypes.put("video/x-dv","dv");
		mimeTypes.put("application/x-dvi","dvi");
		mimeTypes.put("drawing/x-dwf (old)","dwf");
		mimeTypes.put("model/vnd.dwf","dwf");
		mimeTypes.put("application/acad","dwg");
		mimeTypes.put("image/vnd.dwg","dwg");
		mimeTypes.put("image/x-dwg","dwg");
		mimeTypes.put("application/dxf","dxf");
		mimeTypes.put("image/vnd.dwg","dxf");
		mimeTypes.put("image/x-dwg","dxf");
		mimeTypes.put("application/x-director","dxr");
		mimeTypes.put("text/x-script.elisp","el");
		mimeTypes.put("application/x-bytecode.elisp (compiled elisp)","elc");
		mimeTypes.put("application/x-elc","elc");
		mimeTypes.put("application/x-envoy","env");
		mimeTypes.put("application/postscript","eps");
		mimeTypes.put("application/x-esrehber","es");
		mimeTypes.put("text/x-setext","etx");
		mimeTypes.put("application/envoy","evy");
		mimeTypes.put("application/x-envoy","evy");
		mimeTypes.put("application/octet-stream","exe");
		mimeTypes.put("text/plain","f");
		mimeTypes.put("text/x-fortran","f");
		mimeTypes.put("text/x-fortran","f77");
		mimeTypes.put("text/plain","f90");
		mimeTypes.put("text/x-fortran","f90");
		mimeTypes.put("application/vnd.fdf","fdf");
		mimeTypes.put("application/fractals","fif");
		mimeTypes.put("image/fif","fif");
		mimeTypes.put("video/fli","fli");
		mimeTypes.put("video/x-fli","fli");
		mimeTypes.put("image/florian","flo");
		mimeTypes.put("text/vnd.fmi.flexstor","flx");
		mimeTypes.put("video/x-atomic3d-feature","fmf");
		mimeTypes.put("text/plain","for");
		mimeTypes.put("text/x-fortran","for");
		mimeTypes.put("image/vnd.fpx","fpx");
		mimeTypes.put("image/vnd.net-fpx","fpx");
		mimeTypes.put("application/freeloader","frl");
		mimeTypes.put("audio/make","funk");
		mimeTypes.put("text/plain","g");
		mimeTypes.put("image/g3fax","g3");
		mimeTypes.put("image/gif","gif");
		mimeTypes.put("video/gl","gl");
		mimeTypes.put("video/x-gl","gl");
		mimeTypes.put("audio/x-gsm","gsd");
		mimeTypes.put("audio/x-gsm","gsm");
		mimeTypes.put("application/x-gsp","gsp");
		mimeTypes.put("application/x-gss","gss");
		mimeTypes.put("application/x-gtar","gtar");
		mimeTypes.put("application/x-compressed","gz");
		mimeTypes.put("application/x-gzip","gz");
		mimeTypes.put("application/x-gzip","gzip");
		mimeTypes.put("multipart/x-gzip","gzip");
		mimeTypes.put("text/plain","h");
		mimeTypes.put("text/x-h","h");
		mimeTypes.put("application/x-hdf","hdf");
		mimeTypes.put("application/x-helpfile","help");
		mimeTypes.put("application/vnd.hp-hpgl","hgl");
		mimeTypes.put("text/plain","hh");
		mimeTypes.put("text/x-h","hh");
		mimeTypes.put("text/x-script","hlb");
		mimeTypes.put("application/hlp","hlp");
		mimeTypes.put("application/x-helpfile","hlp");
		mimeTypes.put("application/x-winhelp","hlp");
		mimeTypes.put("application/vnd.hp-hpgl","hpg");
		mimeTypes.put("application/vnd.hp-hpgl","hpgl");
		mimeTypes.put("application/binhex","hqx");
		mimeTypes.put("application/binhex4","hqx");
		mimeTypes.put("application/mac-binhex","hqx");
		mimeTypes.put("application/mac-binhex40","hqx");
		mimeTypes.put("application/x-binhex40","hqx");
		mimeTypes.put("application/x-mac-binhex40","hqx");
		mimeTypes.put("application/hta","hta");
		mimeTypes.put("text/x-component","htc");
		mimeTypes.put("text/html","htm");
		mimeTypes.put("text/html","html");
		mimeTypes.put("text/html","htmls");
		mimeTypes.put("text/webviewhtml","htt");
		mimeTypes.put("text/html","htx");
		mimeTypes.put("x-conference/x-cooltalk","ice");
		mimeTypes.put("image/x-icon","ico");
		mimeTypes.put("text/plain","idc");
		mimeTypes.put("image/ief","ief");
		mimeTypes.put("image/ief","iefs");
		mimeTypes.put("application/iges","iges");
		mimeTypes.put("model/iges","iges");
		mimeTypes.put("application/iges","igs");
		mimeTypes.put("model/iges","igs");
		mimeTypes.put("application/x-ima","ima");
		mimeTypes.put("application/x-httpd-imap","imap");
		mimeTypes.put("application/inf","inf");
		mimeTypes.put("application/x-internett-signup","ins");
		mimeTypes.put("application/x-ip2","ip");
		mimeTypes.put("video/x-isvideo","isu");
		mimeTypes.put("audio/it","it");
		mimeTypes.put("application/x-inventor","iv");
		mimeTypes.put("i-world/i-vrml","ivr");
		mimeTypes.put("application/x-livescreen","ivy");
		mimeTypes.put("audio/x-jam","jam");
		mimeTypes.put("text/plain","jav");
		mimeTypes.put("text/x-java-source","jav");
		mimeTypes.put("text/plain","java");
		mimeTypes.put("text/x-java-source","java");
		mimeTypes.put("application/x-java-commerce","jcm");
		mimeTypes.put("image/jpeg","jfif");
		mimeTypes.put("image/pjpeg","jfif");
		mimeTypes.put("image/jpeg","jfif-tbnl");
		mimeTypes.put("image/jpeg","jpe");
		mimeTypes.put("image/pjpeg","jpe");
		mimeTypes.put("image/jpeg","jpeg");
		mimeTypes.put("image/pjpeg","jpeg");
		mimeTypes.put("image/jpeg","jpg");
		mimeTypes.put("image/pjpeg","jpg");
		mimeTypes.put("image/x-jps","jps");
		mimeTypes.put("application/x-javascript","js");
		mimeTypes.put("application/javascript","js");
		mimeTypes.put("application/ecmascript","js");
		mimeTypes.put("text/javascript","js");
		mimeTypes.put("text/ecmascript","js");
		mimeTypes.put("image/jutvision","jut");
		mimeTypes.put("audio/midi","kar");
		mimeTypes.put("music/x-karaoke","kar");
		mimeTypes.put("application/x-ksh","ksh");
		mimeTypes.put("text/x-script.ksh","ksh");
		mimeTypes.put("audio/nspaudio","la");
		mimeTypes.put("audio/x-nspaudio","la");
		mimeTypes.put("audio/x-liveaudio","lam");
		mimeTypes.put("application/x-latex","latex");
		mimeTypes.put("application/lha","lha");
		mimeTypes.put("application/octet-stream","lha");
		mimeTypes.put("application/x-lha","lha");
		mimeTypes.put("application/octet-stream","lhx");
		mimeTypes.put("text/plain","list");
		mimeTypes.put("audio/nspaudio","lma");
		mimeTypes.put("audio/x-nspaudio","lma");
		mimeTypes.put("text/plain","log");
		mimeTypes.put("application/x-lisp","lsp");
		mimeTypes.put("text/x-script.lisp","lsp");
		mimeTypes.put("text/plain","lst");
		mimeTypes.put("text/x-la-asf","lsx");
		mimeTypes.put("application/x-latex","ltx");
		mimeTypes.put("application/octet-stream","lzh");
		mimeTypes.put("application/x-lzh","lzh");
		mimeTypes.put("application/lzx","lzx");
		mimeTypes.put("application/octet-stream","lzx");
		mimeTypes.put("application/x-lzx","lzx");
		mimeTypes.put("text/plain","m");
		mimeTypes.put("text/x-m","m");
		mimeTypes.put("video/mpeg","m1v");
		mimeTypes.put("audio/mpeg","m2a");
		mimeTypes.put("video/mpeg","m2v");
		mimeTypes.put("audio/x-mpequrl","m3u");
		mimeTypes.put("application/x-troff-man","man");
		mimeTypes.put("application/x-navimap","map");
		mimeTypes.put("text/plain","mar");
		mimeTypes.put("application/mbedlet","mbd");
		mimeTypes.put("application/x-magic-cap-package-1.0","mc$");
		mimeTypes.put("application/mcad","mcd");
		mimeTypes.put("application/x-mathcad","mcd");
		mimeTypes.put("image/vasa","mcf");
		mimeTypes.put("text/mcf","mcf");
		mimeTypes.put("application/netmc","mcp");
		mimeTypes.put("application/x-troff-me","me");
		mimeTypes.put("message/rfc822","mht");
		mimeTypes.put("message/rfc822","mhtml");
		mimeTypes.put("application/x-midi","mid");
		mimeTypes.put("audio/midi","mid");
		mimeTypes.put("audio/x-mid","mid");
		mimeTypes.put("audio/x-midi","mid");
		mimeTypes.put("music/crescendo","mid");
		mimeTypes.put("x-music/x-midi","mid");
		mimeTypes.put("application/x-midi","midi");
		mimeTypes.put("audio/midi","midi");
		mimeTypes.put("audio/x-mid","midi");
		mimeTypes.put("audio/x-midi","midi");
		mimeTypes.put("music/crescendo","midi");
		mimeTypes.put("x-music/x-midi","midi");
		mimeTypes.put("application/x-frame","mif");
		mimeTypes.put("application/x-mif","mif");
		mimeTypes.put("message/rfc822","mime");
		mimeTypes.put("www/mime","mime");
		mimeTypes.put("audio/x-vnd.audioexplosion.mjuicemediafile","mjf");
		mimeTypes.put("video/x-motion-jpeg","mjpg");
		mimeTypes.put("application/base64","mm");
		mimeTypes.put("application/x-meme","mm");
		mimeTypes.put("application/base64","mme");
		mimeTypes.put("audio/mod","mod");
		mimeTypes.put("audio/x-mod","mod");
		mimeTypes.put("video/quicktime","moov");
		mimeTypes.put("video/quicktime","mov");
		mimeTypes.put("video/x-sgi-movie","movie");
		mimeTypes.put("audio/mpeg","mp2");
		mimeTypes.put("audio/x-mpeg","mp2");
		mimeTypes.put("video/mpeg","mp2");
		mimeTypes.put("video/x-mpeg","mp2");
		mimeTypes.put("video/x-mpeq2a","mp2");
		mimeTypes.put("audio/mpeg3","mp3");
		mimeTypes.put("audio/x-mpeg-3","mp3");
		mimeTypes.put("video/mpeg","mp3");
		mimeTypes.put("video/x-mpeg","mp3");
		mimeTypes.put("audio/mpeg","mpa");
		mimeTypes.put("video/mpeg","mpa");
		mimeTypes.put("application/x-project","mpc");
		mimeTypes.put("video/mpeg","mpe");
		mimeTypes.put("video/mpeg","mpeg");
		mimeTypes.put("audio/mpeg","mpg");
		mimeTypes.put("video/mpeg","mpg");
		mimeTypes.put("audio/mpeg","mpga");
		mimeTypes.put("application/vnd.ms-project","mpp");
		mimeTypes.put("application/x-project","mpt");
		mimeTypes.put("application/x-project","mpv");
		mimeTypes.put("application/x-project","mpx");
		mimeTypes.put("application/marc","mrc");
		mimeTypes.put("application/x-troff-ms","ms");
		mimeTypes.put("video/x-sgi-movie","mv");
		mimeTypes.put("audio/make","my");
		mimeTypes.put("application/x-vnd.audioexplosion.mzz","mzz");
		mimeTypes.put("image/naplps","nap");
		mimeTypes.put("image/naplps","naplps");
		mimeTypes.put("application/x-netcdf","nc");
		mimeTypes.put("application/vnd.nokia.configuration-message","ncm");
		mimeTypes.put("image/x-niff","nif");
		mimeTypes.put("image/x-niff","niff");
		mimeTypes.put("application/x-mix-transfer","nix");
		mimeTypes.put("application/x-conference","nsc");
		mimeTypes.put("application/x-navidoc","nvd");
		mimeTypes.put("application/octet-stream","o");
		mimeTypes.put("application/oda","oda");
		mimeTypes.put("application/x-omc","omc");
		mimeTypes.put("application/x-omcdatamaker","omcd");
		mimeTypes.put("application/x-omcregerator","omcr");
		mimeTypes.put("text/x-pascal","p");
		mimeTypes.put("application/pkcs10","p10");
		mimeTypes.put("application/x-pkcs10","p10");
		mimeTypes.put("application/pkcs-12","p12");
		mimeTypes.put("application/x-pkcs12","p12");
		mimeTypes.put("application/x-pkcs7-signature","p7a");
		mimeTypes.put("application/pkcs7-mime","p7c");
		mimeTypes.put("application/x-pkcs7-mime","p7c");
		mimeTypes.put("application/pkcs7-mime","p7m");
		mimeTypes.put("application/x-pkcs7-mime","p7m");
		mimeTypes.put("application/x-pkcs7-certreqresp","p7r");
		mimeTypes.put("application/pkcs7-signature","p7s");
		mimeTypes.put("application/pro_eng","part");
		mimeTypes.put("text/pascal","pas");
		mimeTypes.put("image/x-portable-bitmap","pbm");
		mimeTypes.put("application/vnd.hp-pcl","pcl");
		mimeTypes.put("application/x-pcl","pcl");
		mimeTypes.put("image/x-pict","pct");
		mimeTypes.put("image/x-pcx","pcx");
		mimeTypes.put("chemical/x-pdb","pdb");
		mimeTypes.put("application/pdf","pdf");
		mimeTypes.put("audio/make","pfunk");
		mimeTypes.put("audio/make.my.funk","pfunk");
		mimeTypes.put("image/x-portable-graymap","pgm");
		mimeTypes.put("image/x-portable-greymap","pgm");
		mimeTypes.put("image/pict","pic");
		mimeTypes.put("image/pict","pict");
		mimeTypes.put("application/x-newton-compatible-pkg","pkg");
		mimeTypes.put("application/vnd.ms-pki.pko","pko");
		mimeTypes.put("text/plain","pl");
		mimeTypes.put("text/x-script.perl","pl");
		mimeTypes.put("application/x-pixclscript","plx");
		mimeTypes.put("image/x-xpixmap","pm");
		mimeTypes.put("text/x-script.perl-module","pm");
		mimeTypes.put("application/x-pagemaker","pm4");
		mimeTypes.put("application/x-pagemaker","pm5");
		mimeTypes.put("image/png","png");
		mimeTypes.put("application/x-portable-anymap","pnm");
		mimeTypes.put("image/x-portable-anymap","pnm");
		mimeTypes.put("application/mspowerpoint","pot");
		mimeTypes.put("application/vnd.ms-powerpoint","pot");
		mimeTypes.put("model/x-pov","pov");
		mimeTypes.put("application/vnd.ms-powerpoint","ppa");
		mimeTypes.put("image/x-portable-pixmap","ppm");
		mimeTypes.put("application/mspowerpoint","pps");
		mimeTypes.put("application/vnd.ms-powerpoint","pps");
		mimeTypes.put("application/mspowerpoint","ppt");
		mimeTypes.put("application/powerpoint","ppt");
		mimeTypes.put("application/vnd.ms-powerpoint","ppt");
		mimeTypes.put("application/x-mspowerpoint","ppt");
		mimeTypes.put("application/mspowerpoint","ppz");
		mimeTypes.put("application/x-freelance","pre");
		mimeTypes.put("application/pro_eng","prt");
		mimeTypes.put("application/postscript","ps");
		mimeTypes.put("application/octet-stream","psd");
		mimeTypes.put("paleovu/x-pv","pvu");
		mimeTypes.put("application/vnd.ms-powerpoint","pwz");
		mimeTypes.put("text/x-script.phyton","py");
		mimeTypes.put("application/x-bytecode.python","pyc");
		mimeTypes.put("audio/vnd.qcelp","qcp");
		mimeTypes.put("x-world/x-3dmf","qd3");
		mimeTypes.put("x-world/x-3dmf","qd3d");
		mimeTypes.put("image/x-quicktime","qif");
		mimeTypes.put("video/quicktime","qt");
		mimeTypes.put("video/x-qtc","qtc");
		mimeTypes.put("image/x-quicktime","qti");
		mimeTypes.put("image/x-quicktime","qtif");
		mimeTypes.put("audio/x-pn-realaudio","ra");
		mimeTypes.put("audio/x-pn-realaudio-plugin","ra");
		mimeTypes.put("audio/x-realaudio","ra");
		mimeTypes.put("audio/x-pn-realaudio","ram");
		mimeTypes.put("application/x-cmu-raster","ras");
		mimeTypes.put("image/cmu-raster","ras");
		mimeTypes.put("image/x-cmu-raster","ras");
		mimeTypes.put("image/cmu-raster","rast");
		mimeTypes.put("text/x-script.rexx","rexx");
		mimeTypes.put("image/vnd.rn-realflash","rf");
		mimeTypes.put("image/x-rgb","rgb");
		mimeTypes.put("application/vnd.rn-realmedia","rm");
		mimeTypes.put("audio/x-pn-realaudio","rm");
		mimeTypes.put("audio/mid","rmi");
		mimeTypes.put("audio/x-pn-realaudio","rmm");
		mimeTypes.put("audio/x-pn-realaudio","rmp");
		mimeTypes.put("audio/x-pn-realaudio-plugin","rmp");
		mimeTypes.put("application/ringing-tones","rng");
		mimeTypes.put("application/vnd.nokia.ringing-tone","rng");
		mimeTypes.put("application/vnd.rn-realplayer","rnx");
		mimeTypes.put("application/x-troff","roff");
		mimeTypes.put("image/vnd.rn-realpix","rp");
		mimeTypes.put("audio/x-pn-realaudio-plugin","rpm");
		mimeTypes.put("text/richtext","rt");
		mimeTypes.put("text/vnd.rn-realtext","rt");
		mimeTypes.put("application/rtf","rtf");
		mimeTypes.put("application/x-rtf","rtf");
		mimeTypes.put("text/richtext","rtf");
		mimeTypes.put("application/rtf","rtx");
		mimeTypes.put("text/richtext","rtx");
		mimeTypes.put("video/vnd.rn-realvideo","rv");
		mimeTypes.put("text/x-asm","s");
		mimeTypes.put("audio/s3m","s3m");
		mimeTypes.put("application/octet-stream","saveme");
		mimeTypes.put("application/x-tbook","sbk");
		mimeTypes.put("application/x-lotusscreencam","scm");
		mimeTypes.put("text/x-script.guile","scm");
		mimeTypes.put("text/x-script.scheme","scm");
		mimeTypes.put("video/x-scm","scm");
		mimeTypes.put("text/plain","sdml");
		mimeTypes.put("application/sdp","sdp");
		mimeTypes.put("application/x-sdp","sdp");
		mimeTypes.put("application/sounder","sdr");
		mimeTypes.put("application/sea","sea");
		mimeTypes.put("application/x-sea","sea");
		mimeTypes.put("application/set","set");
		mimeTypes.put("text/sgml","sgm");
		mimeTypes.put("text/x-sgml","sgm");
		mimeTypes.put("text/sgml","sgml");
		mimeTypes.put("text/x-sgml","sgml");
		mimeTypes.put("application/x-bsh","sh");
		mimeTypes.put("application/x-sh","sh");
		mimeTypes.put("application/x-shar","sh");
		mimeTypes.put("text/x-script.sh","sh");
		mimeTypes.put("application/x-bsh","shar");
		mimeTypes.put("application/x-shar","shar");
		mimeTypes.put("text/html","shtml");
		mimeTypes.put("text/x-server-parsed-html","shtml");
		mimeTypes.put("audio/x-psid","sid");
		mimeTypes.put("application/x-sit","sit");
		mimeTypes.put("application/x-stuffit","sit");
		mimeTypes.put("application/x-koan","skd");
		mimeTypes.put("application/x-koan","skm");
		mimeTypes.put("application/x-koan","skp");
		mimeTypes.put("application/x-koan","skt");
		mimeTypes.put("application/x-seelogo","sl");
		mimeTypes.put("application/smil","smi");
		mimeTypes.put("application/smil","smil");
		mimeTypes.put("audio/basic","snd");
		mimeTypes.put("audio/x-adpcm","snd");
		mimeTypes.put("application/solids","sol");
		mimeTypes.put("application/x-pkcs7-certificates","spc");
		mimeTypes.put("text/x-speech","spc");
		mimeTypes.put("application/futuresplash","spl");
		mimeTypes.put("application/x-sprite","spr");
		mimeTypes.put("application/x-sprite","sprite");
		mimeTypes.put("application/x-wais-source","src");
		mimeTypes.put("text/x-server-parsed-html","ssi");
		mimeTypes.put("application/streamingmedia","ssm");
		mimeTypes.put("application/vnd.ms-pki.certstore","sst");
		mimeTypes.put("application/step","step");
		mimeTypes.put("application/sla","stl");
		mimeTypes.put("application/vnd.ms-pki.stl","stl");
		mimeTypes.put("application/x-navistyle","stl");
		mimeTypes.put("application/step","stp");
		mimeTypes.put("application/x-sv4cpio","sv4cpio");
		mimeTypes.put("application/x-sv4crc","sv4crc");
		mimeTypes.put("image/vnd.dwg","svf");
		mimeTypes.put("image/x-dwg","svf");
		mimeTypes.put("application/x-world","svr");
		mimeTypes.put("x-world/x-svr","svr");
		mimeTypes.put("application/x-shockwave-flash","swf");
		mimeTypes.put("application/x-troff","t");
		mimeTypes.put("text/x-speech","talk");
		mimeTypes.put("application/x-tar","tar");
		mimeTypes.put("application/toolbook","tbk");
		mimeTypes.put("application/x-tbook","tbk");
		mimeTypes.put("application/x-tcl","tcl");
		mimeTypes.put("text/x-script.tcl","tcl");
		mimeTypes.put("text/x-script.tcsh","tcsh");
		mimeTypes.put("application/x-tex","tex");
		mimeTypes.put("application/x-texinfo","texi");
		mimeTypes.put("application/x-texinfo","texinfo");
		mimeTypes.put("application/plain","text");
		mimeTypes.put("text/plain","text");
		mimeTypes.put("application/gnutar","tgz");
		mimeTypes.put("application/x-compressed","tgz");
		mimeTypes.put("image/tiff","tif");
		mimeTypes.put("image/x-tiff","tif");
		mimeTypes.put("image/tiff","tiff");
		mimeTypes.put("image/x-tiff","tiff");
		mimeTypes.put("application/x-troff","tr");
		mimeTypes.put("audio/tsp-audio","tsi");
		mimeTypes.put("application/dsptype","tsp");
		mimeTypes.put("audio/tsplayer","tsp");
		mimeTypes.put("text/tab-separated-values","tsv");
		mimeTypes.put("image/florian","turbot");
		mimeTypes.put("text/plain","txt");
		mimeTypes.put("text/x-uil","uil");
		mimeTypes.put("text/uri-list","uni");
		mimeTypes.put("text/uri-list","unis");
		mimeTypes.put("application/i-deas","unv");
		mimeTypes.put("text/uri-list","uri");
		mimeTypes.put("text/uri-list","uris");
		mimeTypes.put("application/x-ustar","ustar");
		mimeTypes.put("multipart/x-ustar","ustar");
		mimeTypes.put("application/octet-stream","uu");
		mimeTypes.put("text/x-uuencode","uu");
		mimeTypes.put("text/x-uuencode","uue");
		mimeTypes.put("application/x-cdlink","vcd");
		mimeTypes.put("text/x-vcalendar","vcs");
		mimeTypes.put("application/vda","vda");
		mimeTypes.put("video/vdo","vdo");
		mimeTypes.put("application/groupwise","vew");
		mimeTypes.put("video/vivo","viv");
		mimeTypes.put("video/vnd.vivo","viv");
		mimeTypes.put("video/vivo","vivo");
		mimeTypes.put("video/vnd.vivo","vivo");
		mimeTypes.put("application/vocaltec-media-desc","vmd");
		mimeTypes.put("application/vocaltec-media-file","vmf");
		mimeTypes.put("audio/voc","voc");
		mimeTypes.put("audio/x-voc","voc");
		mimeTypes.put("video/vosaic","vos");
		mimeTypes.put("audio/voxware","vox");
		mimeTypes.put("audio/x-twinvq-plugin","vqe");
		mimeTypes.put("audio/x-twinvq","vqf");
		mimeTypes.put("audio/x-twinvq-plugin","vql");
		mimeTypes.put("application/x-vrml","vrml");
		mimeTypes.put("model/vrml","vrml");
		mimeTypes.put("x-world/x-vrml","vrml");
		mimeTypes.put("x-world/x-vrt","vrt");
		mimeTypes.put("application/x-visio","vsd");
		mimeTypes.put("application/x-visio","vst");
		mimeTypes.put("application/x-visio","vsw");
		mimeTypes.put("application/wordperfect6.0","w60");
		mimeTypes.put("application/wordperfect6.1","w61");
		mimeTypes.put("application/msword","w6w");
		mimeTypes.put("audio/wav","wav");
		mimeTypes.put("audio/x-wav","wav");
		mimeTypes.put("application/x-qpro","wb1");
		mimeTypes.put("image/vnd.wap.wbmp","wbmp");
		mimeTypes.put("application/vnd.xara","web");
		mimeTypes.put("application/msword","wiz");
		mimeTypes.put("application/x-123","wk1");
		mimeTypes.put("windows/metafile","wmf");
		mimeTypes.put("text/vnd.wap.wml","wml");
		mimeTypes.put("application/vnd.wap.wmlc","wmlc");
		mimeTypes.put("text/vnd.wap.wmlscript","wmls");
		mimeTypes.put("application/vnd.wap.wmlscriptc","wmlsc");
		mimeTypes.put("application/msword","word");
		mimeTypes.put("application/wordperfect","wp");
		mimeTypes.put("application/wordperfect","wp5");
		mimeTypes.put("application/wordperfect6.0","wp5");
		mimeTypes.put("application/wordperfect","wp6");
		mimeTypes.put("application/wordperfect","wpd");
		mimeTypes.put("application/x-wpwin","wpd");
		mimeTypes.put("application/x-lotus","wq1");
		mimeTypes.put("application/mswrite","wri");
		mimeTypes.put("application/x-wri","wri");
		mimeTypes.put("application/x-world","wrl");
		mimeTypes.put("model/vrml","wrl");
		mimeTypes.put("x-world/x-vrml","wrl");
		mimeTypes.put("model/vrml","wrz");
		mimeTypes.put("x-world/x-vrml","wrz");
		mimeTypes.put("text/scriplet","wsc");
		mimeTypes.put("application/x-wais-source","wsrc");
		mimeTypes.put("application/x-wintalk","wtk");
		mimeTypes.put("image/x-xbitmap","xbm");
		mimeTypes.put("image/x-xbm","xbm");
		mimeTypes.put("image/xbm","xbm");
		mimeTypes.put("video/x-amt-demorun","xdr");
		mimeTypes.put("xgl/drawing","xgz");
		mimeTypes.put("image/vnd.xiff","xif");
		mimeTypes.put("application/excel","xl");
		mimeTypes.put("application/excel","xla");
		mimeTypes.put("application/x-excel","xla");
		mimeTypes.put("application/x-msexcel","xla");
		mimeTypes.put("application/excel","xlb");
		mimeTypes.put("application/vnd.ms-excel","xlb");
		mimeTypes.put("application/x-excel","xlb");
		mimeTypes.put("application/excel","xlc");
		mimeTypes.put("application/vnd.ms-excel","xlc");
		mimeTypes.put("application/x-excel","xlc");
		mimeTypes.put("application/excel","xld");
		mimeTypes.put("application/x-excel","xld");
		mimeTypes.put("application/excel","xlk");
		mimeTypes.put("application/x-excel","xlk");
		mimeTypes.put("application/excel","xll");
		mimeTypes.put("application/vnd.ms-excel","xll");
		mimeTypes.put("application/x-excel","xll");
		mimeTypes.put("application/excel","xlm");
		mimeTypes.put("application/vnd.ms-excel","xlm");
		mimeTypes.put("application/x-excel","xlm");
		mimeTypes.put("application/excel","xls");
		mimeTypes.put("application/vnd.ms-excel","xls");
		mimeTypes.put("application/x-excel","xls");
		mimeTypes.put("application/x-msexcel","xls");
		mimeTypes.put("application/excel","xlt");
		mimeTypes.put("application/x-excel","xlt");
		mimeTypes.put("application/excel","xlv");
		mimeTypes.put("application/x-excel","xlv");
		mimeTypes.put("application/excel","xlw");
		mimeTypes.put("application/vnd.ms-excel","xlw");
		mimeTypes.put("application/x-excel","xlw");
		mimeTypes.put("application/x-msexcel","xlw");
		mimeTypes.put("audio/xm","xm");
		mimeTypes.put("application/xml","xml");
		mimeTypes.put("text/xml","xml");
		mimeTypes.put("xgl/movie","xmz");
		mimeTypes.put("application/x-vnd.ls-xpix","xpix");
		mimeTypes.put("image/x-xpixmap","xpm");
		mimeTypes.put("image/xpm","xpm");
		mimeTypes.put("image/png","x-png");
		mimeTypes.put("video/x-amt-showrun","xsr");
		mimeTypes.put("image/x-xwd","xwd");
		mimeTypes.put("image/x-xwindowdump","xwd");
		mimeTypes.put("chemical/x-pdb","xyz");
		mimeTypes.put("application/x-compress","z");
		mimeTypes.put("application/x-compressed","z");
		mimeTypes.put("application/x-compressed","zip");
		mimeTypes.put("application/x-zip-compressed","zip");
		mimeTypes.put("application/zip","zip");
		mimeTypes.put("multipart/x-zip","zip");
		mimeTypes.put("application/octet-stream","zoo");
		mimeTypes.put("text/x-script.zsh","zsh");
	}
}