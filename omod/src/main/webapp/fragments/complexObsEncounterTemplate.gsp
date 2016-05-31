<%	
	ui.includeJavascript("uicommons", "angular.min.js")
  ui.includeJavascript("uicommons", "angular-resource.min.js")
  ui.includeJavascript("uicommons", "angular-common.js")
  ui.includeJavascript("uicommons", "angular-app.js")

  ui.includeJavascript("visitdocumentsui", "directives/thumbnail.js")
  ui.includeJavascript("visitdocumentsui", "services/complexObsService.js")

  ui.includeJavascript("visitdocumentsui", "directives/modalImage.js")

  ui.includeJavascript("visitdocumentsui", "directives/complexObsEncounterDirective.js")

  ui.includeJavascript("visitdocumentsui", "fragments/complexObsEncounterTemplate.js")
%>

<div ng-app="vdui.fragment.encounterTemplate" ng-controller="EncounterTemplateCtrl" ng-init="init()">

<script type="text/template" id="complexObsEncounterTemplate">
<li>
	<div class="encounter-date">
	    <i class="icon-time"></i>
	    <strong>
	        {{- encounter.encounterTime }}
	    </strong>
	    {{- encounter.encounterDate }}
	</div>
	<ul class="encounter-details">
	    <li> 
	        <div class="encounter-type">
	            <strong>
	                <i class="{{- config.icon }}"></i>
	                <span class="encounter-name" data-encounter-id="{{- encounter.encounterId }}">{{- encounter.encounterType.name }}</span>
	            </strong>
	        </div>
	    </li>
	    <li>
	        <div>
	            ${ ui.message("coreapps.by") }
	            <strong class="provider">
	                {{- encounter.primaryProvider ? encounter.primaryProvider : '' }}
	            </strong>
	            ${ ui.message("coreapps.in") }
	            <strong class="location">{{- encounter.location }}</strong>
	        </div>
	    </li>
	    <li>
	        <div class="details-action">
	            <a class="view-details collapsed" href='javascript:void(0);' data-encounter-id="{{- encounter.encounterId }}" data-encounter-form="{{- encounter.form != null}}" data-display-with-html-form="{{- config.displayWithHtmlForm }}" data-target="#encounter-summary{{- encounter.encounterId }}" data-toggle="collapse"
	            	data-encounter-type-uuid="{{- encounter.encounterType.uuid }}">
	                <span class="show-details">${ ui.message("coreapps.patientDashBoard.showDetails")}</span>
	                <span class="hide-details">${ ui.message("coreapps.patientDashBoard.hideDetails")}</span>
	                <i class="icon-caret-right"></i>
	            </a>
	        </div>
	    </li>
	</ul>

	<span>
        {{ if ( config.editable && encounter.canEdit) { }}
            <i class="editEncounter delete-item icon-pencil" data-patient-id="{{- patient.id }}" data-encounter-id="{{- encounter.encounterId }}" {{ if (config.editUrl) { }} data-edit-url="{{- config.editUrl }}" {{ } }} title="${ ui.message("coreapps.edit") }"></i>
        {{ } }}
        {{ if ( encounter.canDelete ) { }}
	       <i class="deleteEncounterId delete-item icon-remove" data-visit-id="{{- encounter.visitId }}" data-encounter-id="{{- encounter.encounterId }}" title="${ ui.message("coreapps.delete") }"></i>
        {{  } }}
	</span>

	<div id="encounter-summary{{- encounter.encounterId }}">
			<div class="encounter-summary-container"></div>
	</div>

</li>
</script>

</div>