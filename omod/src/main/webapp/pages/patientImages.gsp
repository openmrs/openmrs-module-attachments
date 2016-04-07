<%
ui.decorateWith("appui", "standardEmrPage")

ui.includeJavascript("uicommons", "angular.min.js")
ui.includeJavascript("uicommons", "angular-resource.min.js")
ui.includeJavascript("uicommons", "angular-common.js")
ui.includeJavascript("uicommons", "angular-app.js")

ui.includeJavascript("patientimages", "dropzone/dropzone.js")
ui.includeCss("patientimages", "dropzone/basic.css")
ui.includeCss("patientimages", "dropzone/dropzone.css")

ui.includeJavascript("patientimages", "patientImagesApp.js")
%>

<script type="text/javascript">

  var breadcrumbs = [
    { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
    { label: "${ ui.message("patientimages.breadcrumbs.label")}"}
  ];

  window.OpenMRS = window.OpenMRS || {};

  var config = ${ jsonConfig }; // Getting the config from the Spring Java controller.
    
  Dropzone.options.patientImagesDropzone = false; // We turn off auto-discover for our element because our directive adds it programmatically.

</script>


<div ng-app="patientImagesApp">
  <div ng-controller="FileUploadCtrl" style="width: 30%"> 
    <form action="" dropzone-directive="dropzoneConfig" class="dropzone" id="patient-images-dropzone">
      <div class="dz-default dz-message">Click or drop a file here.</div>
    </form>
  </div>
</div>
