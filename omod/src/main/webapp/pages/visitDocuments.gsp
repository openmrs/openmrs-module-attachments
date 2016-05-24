<%
  ui.decorateWith("appui", "standardEmrPage")

  ui.includeJavascript("uicommons", "angular.min.js")
  ui.includeJavascript("uicommons", "angular-resource.min.js")
  ui.includeJavascript("uicommons", "angular-common.js")
  ui.includeJavascript("uicommons", "angular-app.js")

  ui.includeJavascript("uicommons", "services/obsService.js")
  ui.includeJavascript("uicommons", "services/session.js")

  ui.includeJavascript("visitdocumentsui", "dropzone/dropzone.js")
  ui.includeCss("visitdocumentsui", "dropzone/basic.css")
  ui.includeCss("visitdocumentsui", "dropzone/dropzone.css")

  ui.includeJavascript("visitdocumentsui", "visitDocuments.js")

  ui.includeJavascript("visitdocumentsui", "directives/thumbnail.js")
  ui.includeJavascript("visitdocumentsui", "services/complexObsService.js")

  ui.includeJavascript("visitdocumentsui", "directives/modalImage.js")
%>

<script type="text/javascript">

  var breadcrumbs = [
    { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
    { label: "${ui.message("visitdocumentsui.breadcrumbs.label")}"}
  ];

  window.OpenMRS = window.OpenMRS || {};

  var config = ${jsonConfig}; // Getting the config from the Spring Java controller.
  config.downloadUrl = '/' + OPENMRS_CONTEXT_PATH + config.downloadUrl;

  config.uploadUrl = '/' + OPENMRS_CONTEXT_PATH + config.uploadUrl;
  config.uploadUrl += '?' + 'patient=' + config.patient.uuid + '&' + 'visit=' + config.visit.uuid;

  Dropzone.options.visitDocumentsDropzone = false; // We turn off auto-discover for our DropzoneJS element because our directive adds it programmatically.

</script>

<style>

  .vdui_mainSection {
    position: relative;
    border: 1px solid #EEE;
    background-color: #F9F9F9;
  }

  .vdui_fileUploadContainer {
    margin-top: 20px;
    margin-bottom: 20px;
    padding-left: 20px;
  }

  .vdui_thumbnailsContainer {
    width: 100%;
    margin: 0px auto;
    overflow: hidden;
  }

  textarea {
    width: 100%;
    /*height: 50%;*/
    -webkit-box-sizing: border-box; /* Safari/Chrome, other WebKit */
    -moz-box-sizing: border-box;    /* Firefox, other Gecko */
    box-sizing: border-box;         /* Opera/IE 8+ */
  }

  .upload-container {
    display: block;
    height: 180px;
  }

  .upload-element {
    float: left;  
    display: inline;
  }

  .upload-element.dropzone-element {
    width: 30%;
    height: 75%;
  }

  .upload-element.caption-element {
    width: 55%;
    margin-left: 2%;
  }

  .dropzone {
    position: center;
    border: 2px dotted #888;
    border-radius: 10px;
    min-height: 0px;
    height: 100%;
    text-align: center;
  }

  .dropzone.in {
    width: 600px;
    height: 200px;
    line-height: 200px;
    font-size: larger;
  }

</style>

${ ui.includeFragment("coreapps", "patientHeader", [ patient: patient ]) }

<% if (context.hasPrivilege("App: visitdocumentsui.visitdocuments.page")) { %>

<div ng-app="vdui.page.main">

  <!-- The overlay image, hidden at first -->
  <vdui-modal-image></vdui-modal-image>

  <!-- Looping through the thumbnails -->
  <div class="vdui_mainSection vdui_thumbnailsContainer" ng-controller="ListObsCtrl">
    <vdui-thumbnail ng-repeat="obs in obsArray" obs="obs" config="cfg"></vdui-thumbnail>
  </div>

  <div class="vdui_mainSection vdui_fileUploadContainer" ng-controller="FileUploadCtrl">
    
    <div class="upload-container">
      <div class="upload-element dropzone-element">
        <h3>${ui.message("visitdocumentsui.visitdocumentspage.fileTitle")}</h3>
        <form action="" dropzone-directive="dropzoneConfig" class="dropzone" id="visit-documents-dropzone">
          <div class="dz-error-message"><span data-dz-errormessage></span></div>
          <div class="dz-default dz-message">${ui.message("visitdocumentsui.dropzone.innerlabel")}</div>
        </form>
      </div>
      <div class="upload-element caption-element" style="">
        <h3>${ui.message("visitdocumentsui.visitdocumentspage.commentTitle")}</h3>
        <textarea ng-model="fileCaption"></textarea>
        <span class="right" style="margin-top: 4%;">
          <button class="confirm" ng-click="uploadFile()">${ui.message("visitdocumentsui.visitdocumentspage.uploadButton")}</button>
          <button class="" ng-click="clearForms()">${ui.message("visitdocumentsui.visitdocumentspage.clearFormsButton")}</button>
        </span>
      </div>
      <div style="clear:both;"/>
    </div>
    
  </div>  

</div>

<% } %>