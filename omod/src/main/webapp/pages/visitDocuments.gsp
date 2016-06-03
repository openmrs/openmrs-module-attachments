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
    { label: "${ ui.escapeJs(ui.format(patient)) }" ,
            link: '${ ui.urlBind("/" + contextPath + dashboardUrl, [ patientId: patient.uuid ] ) }'},
    { label: "${ui.message("visitdocumentsui.breadcrumbs.label")}"}
  ];

  window.OpenMRS = window.OpenMRS || {};
  var config = ${jsonConfig}; // Getting the config from the Spring Java controller.
  
  // We turn off auto-discover for our DropzoneJS element because our directive adds it programmatically.
  Dropzone.options.visitDocumentsDropzone = false; 

</script>

<style>

  .vdui_mainSection {
    border: 1px solid #EEE;
    background-color: #F9F9F9;
    margin: 15px 0 10px 0;
  }

  .vdui_fileUploadContainer {
    padding-left: 20px;
    height: 200px;
  }

  .vdui_thumbnailsContainer {
    width: 100%;
    overflow: hidden;
  }

  .vdui_captionElement textarea {
    width: 100%;
    -webkit-box-sizing: border-box; /* Safari/Chrome, other WebKit */
    -moz-box-sizing: border-box;    /* Firefox, other Gecko */
    box-sizing: border-box;         /* Opera/IE 8+ */
  }

  .vdui_uploadContainer {
    height: 180px;
  }

  .vdui_uploadElement {
    float: left;
  }

  .vdui_uploadElement.vdui_dropzoneElement {
    width: 30%;
    height: 75%;
  }

  .vdui_uploadElement.vdui_captionElement {
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

  <div ng-show="visitUuid" class="vdui_mainSection vdui_fileUploadContainer" ng-controller="FileUploadCtrl" ng-init="init()">
    <div class="vdui_uploadContainer">
      <div class="vdui_uploadElement vdui_dropzoneElement">
        <h3>${ui.message("visitdocumentsui.visitdocumentspage.fileTitle")}</h3>
        <form action="" dropzone-directive="dropzoneConfig" class="dropzone" id="visit-documents-dropzone">
          <div class="dz-error-message"><span data-dz-errormessage></span></div>
          <div class="dz-default dz-message">${ui.message("visitdocumentsui.dropzone.innerlabel")}</div>
        </form>
      </div>
      <div class="vdui_uploadElement vdui_captionElement">
        <h3>${ui.message("visitdocumentsui.visitdocumentspage.commentTitle")}</h3>
        <textarea ng-model="fileCaption"></textarea>
        <span class="right" style="margin-top: 4%;">
          <button class="confirm" ng-click="uploadFile()">${ui.message("visitdocumentsui.visitdocumentspage.uploadButton")}</button>
          <button class="" ng-click="clearForms()">${ui.message("visitdocumentsui.visitdocumentspage.clearFormsButton")}</button>
        </span>
      </div>
    </div>
  </div>

  <div ng-controller="ListComplexObsCtrl">
    <div ng-show="obsArray.length" class="vdui_mainSection vdui_thumbnailsContainer">
      <vdui-modal-image></vdui-modal-image>
      <vdui-thumbnail ng-repeat="obs in obsArray" obs="obs" config="thumbnailCfg"></vdui-thumbnail>
    </div>
    <div ng-show="!obsArray.length">
      ${ui.message("visitdocumentsui.visitdocumentspage.noDocuments")}
    </div>
  </div>

</div>

<% } %>