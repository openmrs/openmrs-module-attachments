<%
ui.decorateWith("appui", "standardEmrPage")

ui.includeJavascript("uicommons", "angular.min.js")
ui.includeJavascript("uicommons", "angular-resource.min.js")
ui.includeJavascript("uicommons", "angular-common.js")
ui.includeJavascript("uicommons", "angular-app.js")

ui.includeJavascript("uicommons", "services/obsService.js")
ui.includeJavascript("uicommons", "services/session.js")

ui.includeJavascript("patientimages", "dropzone/dropzone.js")
ui.includeCss("patientimages", "dropzone/basic.css")
ui.includeCss("patientimages", "dropzone/dropzone.css")

ui.includeJavascript("patientimages", "patientImagesApp.js")
%>

<script type="text/javascript">

  var breadcrumbs = [
    { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
    { label: "${ui.message("patientimages.breadcrumbs.label")}"}
  ];

  window.OpenMRS = window.OpenMRS || {};

  var config = ${jsonConfig}; // Getting the config from the Spring Java controller.
  config.uploadUrl = '/' + OPENMRS_CONTEXT_PATH + config.uploadUrl + '.form'; // Building the service URL.
  config.uploadUrl += '?' + 'patient=' + config.patient.uuid + '&' + 'visit=' + config.visit.uuid;
    
  Dropzone.options.patientImagesDropzone = false; // We turn off auto-discover for our element because our directive adds it programmatically.

</script>

<style>

/* MEDIA QUERIES*/
@media only screen and (max-width : 940px),
only screen and (max-device-width : 940px){
  .galleryItem {width: 21%;}
}

@media only screen and (max-width : 720px),
only screen and (max-device-width : 720px){
  .galleryItem {width: 29.33333%;}
}

@media only screen and (max-width : 530px),
only screen and (max-device-width : 530px){
  .galleryItem {width: 46%;}
}

@media only screen and (max-width : 320px),
only screen and (max-device-width : 320px){
  .galleryItem {width: 96%;}
  .galleryItem img {width: 96%;}
  .galleryItem h3 {font-size: 18px;}
  .galleryItem p, {font-size: 18px;}
}

.container {
  width: 100%;
  margin: 0px auto;
  overflow: hidden;
}

.galleryItem {
  color: #797478;
  font: 10px/1.5 Verdana, Helvetica, sans-serif;
  float: left;  
  
  height: 150px;
  width: 16%;
  margin:  2% 2% 50px 2%; 
}

.galleryItem h3 {
  text-transform: uppercase;
}

.galleryItem img {
  max-width: 100%;
  -webkit-border-radius: 5px;
  -moz-border-radius: 5px;
  border-radius: 5px;
}

img {
  max-height: 120px;
}

</style>

${ ui.includeFragment("coreapps", "patientHeader", [ patient: patient ]) }

<% if (context.hasPrivilege("App: patientimages.page")) { %>

<div ng-app="patientImagesApp">
  
  <div ng-controller="FileUploadCtrl" style="width: 30%"> 
    <form action="" dropzone-directive="dropzoneConfig" class="dropzone" id="patient-images-dropzone">
      <div class="dz-default dz-message">${ui.message("patientimages.dropzone.innerlabel")}</div>
    </form>
  </div>

  <div ng-controller="ListObsCtrl"> 
    <div class="container">
      <div class="galleryItem" ng-repeat="obs in obsArray">
        <a href="#"><img ng-src="http://localhost:8081/openmrs/complexObsServlet?obsId={{obs.obsId}}" alt="" /></a>
        <h3>{{obs.obsId}}</h3>
        <p>Lorem ipsum dolor sit amet...</p>
      </div>
    </div>
  </div>

</div>

<% } %>

