<%
	ui.includeJavascript("uicommons", "angular.min.js")
	ui.includeJavascript("uicommons", "angular-resource.min.js")
	ui.includeJavascript("uicommons", "angular-common.js")
	ui.includeJavascript("uicommons", "angular-app.js")

	ui.includeJavascript("visitdocumentsui", "dashboardWidget.js")
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