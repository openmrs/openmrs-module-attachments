<%
  ui.decorateWith("appui", "standardEmrPage")

  ui.includeJavascript("uicommons", "angular.min.js")
  ui.includeJavascript("uicommons", "angular-resource.min.js")
  ui.includeJavascript("uicommons", "angular-common.js")
  ui.includeJavascript("uicommons", "angular-app.js")

  ui.includeJavascript("visitdocumentsui", "visitDocuments.js")
%>

<!-- File Upload directive dependencies -->
<%
  ui.includeJavascript("uicommons", "services/obsService.js")
  ui.includeJavascript("uicommons", "services/session.js")
  ui.includeJavascript("visitdocumentsui", "dropzone/dropzone.js")
  ui.includeCss("visitdocumentsui", "dropzone/basic.css")
  ui.includeCss("visitdocumentsui", "dropzone/dropzone.css")
  ui.includeJavascript("visitdocumentsui", "directives/fileUpload.js")
%>

<!-- Thumbnail directive dependencies -->
<%
  ui.includeJavascript("visitdocumentsui", "directives/thumbnail.js")
  ui.includeJavascript("visitdocumentsui", "services/complexObsService.js")
  ui.includeJavascript("visitdocumentsui", "services/obsCacheService.js")
  ui.includeJavascript("uicommons", "ngDialog/ngDialog.js")
  ui.includeCss("uicommons", "ngDialog/ngDialog.min.css")
  ui.includeJavascript("visitdocumentsui", "directives/modalImage.js")
  ui.includeJavascript("visitdocumentsui", "date/dateformat.js")
%>

<!-- Gallery directive dependencies -->
<%
  ui.includeJavascript("uicommons", "services/obsService.js")
  ui.includeJavascript("visitdocumentsui", "services/configService.js")
  ui.includeJavascript("visitdocumentsui", "directives/gallery.js")
%>

<script type="text/javascript">

  var breadcrumbs = [
    { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
    { label: "${ ui.escapeJs(ui.format(patient)) }" ,
      link: '${ ui.pageLink( "coreapps", "clinicianfacing/patient", [patientId: patient.id] ) }'},
    { label: "${ ui.message("visitdocumentsui.breadcrumbs.label") }"}
  ];

  window.config = ${jsonConfig}; // Getting the config from the Spring Java controller.
  
</script>

<style>

  .vdui_main-section {
    border: 1px solid #EEE;
    background-color: #F9F9F9;
    margin: 15px 0 10px 0;
  }

</style>

${ ui.includeFragment("coreapps", "patientHeader", [ patient: patient ]) }

<% if (context.hasPrivilege("App: visitdocumentsui.visitdocuments.page")) { %>

<div ng-app="vdui.page.main">

  <div ng-controller="FileUploadCtrl" class="vdui_main-section">
    <vdui-file-upload config="config"></vdui-file-upload>
  </div>

  <div ng-controller="GalleryCtrl" class="vdui_main-section">
    <vdui-gallery obs-query="obsQuery" config="{canEdit: true}"></vdui-gallery>
  </div>

</div>

<% } %>