<%
ui.decorateWith("appui", "standardEmrPage")

ui.includeJavascript("uicommons", "angular.min.js")
ui.includeJavascript("uicommons", "angular-resource.min.js")
ui.includeJavascript("uicommons", "angular-common.js")
ui.includeJavascript("uicommons", "angular-app.js")

ui.includeJavascript("patientimages", "patientImages.js")
ui.includeJavascript("uicommons", "services/encounterService.js")

%>

<script type="text/javascript">

    var breadcrumbs = [
      { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
      { label: "${ ui.message("patientimages.breadcrumbs.label")}"}
    ];
    
    window.OpenMRS = window.OpenMRS || {};

    var config = ${ jsonConfig };
    
</script>


<div ng-app="PatientImages">

    <div ng-controller="PatientImagesPageCtrl">
      <div>Patient UUID is: {{patientUuid}}</div>
      <div>Visit UUID is: {{visitUuid}}</div>
  </div>
</div>
