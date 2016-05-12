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
  config.downloadUrl = '/' + OPENMRS_CONTEXT_PATH + config.downloadUrl;

  config.uploadUrl = '/' + OPENMRS_CONTEXT_PATH + config.uploadUrl;
  config.uploadUrl += '?' + 'patient=' + config.patient.uuid + '&' + 'visit=' + config.visit.uuid;
    
  Dropzone.options.patientImagesDropzone = false; // We turn off auto-discover for our DropzoneJS element because our directive adds it programmatically.

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
}

.container {
  width: 100%;
  margin: 0px auto;
  overflow: hidden;
}

.galleryItem {
  color: #797478;
  font: 10px/1.5;
  float: left;  
  
  height: 60px;
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
  max-height: 50px;
}

textarea {
  width: 100%;
  height: 50%;
  -webkit-box-sizing: border-box; /* Safari/Chrome, other WebKit */
  -moz-box-sizing: border-box;    /* Firefox, other Gecko */
  box-sizing: border-box;         /* Opera/IE 8+ */
}

#gallery-container {
  margin: 40px auto;
  border: 1px solid #EEE;
  background-color: #F9F9F9;
}

.upload-container {
  float: left;  
  height: 150px;
  //border: 1px solid red;
  //overflow: hidden;
  display: inline-block;
}

.dropzone {
  position: relative;
  border: 4px dotted #888;
  border-radius: 5px;
  min-height: 0px;
  height: 100%;
  text-align: center;
}

.dropzone.in {
  /*width: 600px;
  height: 200px;
  line-height: 200px;
  font-size: larger;*/
}

</style>

${ ui.includeFragment("coreapps", "patientHeader", [ patient: patient ]) }

<% if (context.hasPrivilege("App: patientimages.page")) { %>

<div ng-app="patientImagesApp">
  
  <div id="image-upload-container" ng-controller="FileUploadCtrl">
    <div>
      <div class="upload-container" style="width: 20%;">
        <h3>${ui.message("patientimages.patientpage.fileTitle")}</h3>
        <form action="" dropzone-directive="dropzoneConfig" class="dropzone" id="patient-images-dropzone">
          <div class="dz-default dz-message">${ui.message("patientimages.dropzone.innerlabel")}</div>
        </form>
      </div>
      <div class="upload-container" style="width: 70%;">
        <h3>${ui.message("patientimages.patientpage.commentTitle")}</h3>
        <textarea ng-model="obsText"></textarea>
        <button class="right" ng-click="uploadFile()" style="margin-top: 2%;">${ui.message("patientimages.patientpage.uploadButton")}</button>
      </div>
      <div style="clear:both;"/>
    </div>
    
  </div>

  <div id="gallery-container">
    <h2>${ui.message("patientimages.patientpage.galleryTitle")}</h2>
    <div ng-controller="ListObsCtrl"> 
      <div class="container">
        <div class="galleryItem" ng-repeat="obs in obsArray">
          <a target="_blank" href="{{getImageSrc(obs.uuid)}}">
            <img ng-src="{{getThumbnailSrc(obs.uuid)}}" alt="" />
          </a>
          <p>{{obs.comment}}</p>
        </div>
      </div>
    </div>
  </div>

</div>

<% } %>

