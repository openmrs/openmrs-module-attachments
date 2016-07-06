<%
  ui.includeJavascript("uicommons", "angular.min.js")
  ui.includeJavascript("uicommons", "angular-resource.min.js")
  ui.includeJavascript("uicommons", "angular-common.js")
  ui.includeJavascript("uicommons", "angular-app.js")

  ui.includeJavascript("visitdocumentsui", "dashboardWidget.js")
%>

<!-- Angular widgets -->
<%
  ui.includeFragment("visitdocumentsui", "dependenciesThumbnail")
  ui.includeFragment("visitdocumentsui", "dependenciesGallery")
%>

<script type="text/javascript">

  window.config = ${jsonConfig}; // Getting the config from the Spring Java controller.

</script>

<style>
</style>

<div class="info-section">
	<div class="info-header">
		<i class="icon-paper-clip"></i>
		<h3>${ ui.message("visitdocumentsui.visitactions.label").toUpperCase() }</h3>

<% if (context.hasPrivilege("App: visitdocumentsui.visitdocuments.page")) { %>

    <a href="${ ui.pageLink("visitdocumentsui", "visitDocuments", [patient: patient.patient.uuid, patientId: patient.id, returnUrl: ui.thisUrl()]) }">
      <i class="icon-share-alt edit-action right" title="${ ui.message("coreapps.edit") }"></i>
    </a>

	</div>
	<div class="info-body">

		<div ng-app="vdui.fragment.dashboardWidget" ng-controller="DashboardWidgetCtrl">
			<vdui-gallery obs-query="obsQuery"></vdui-gallery>
		</div>

	</div>

<% } else { %>

  </div>

<% } %>

</div>