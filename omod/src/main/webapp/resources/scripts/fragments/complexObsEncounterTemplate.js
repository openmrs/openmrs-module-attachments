angular.module('vdui.fragment.encounterTemplate', ['vdui.widget.complexObsEncounter']);

angular.module('vdui.fragment.encounterTemplate').controller('EncounterTemplateCtrl', ['$scope', '$compile',
  function($scope, $compile) {
  	
  	$scope.init = function() {

			$(document).on('click','.view-details.collapsed', function(event) {
				var thisTemplateId = "complexObsEncounterTemplate";

				var jqTarget = $(event.currentTarget);
        var encounterId = jqTarget.data("encounter-id");
        var displayWithHtmlForm = jqTarget.data("encounter-form") && jqTarget.data("display-with-html-form");
        var dataTarget = jqTarget.data("target");
        var encounterTypeUuid = jqTarget.data("encounter-type-uuid");
        var customTemplateId = encounterTemplates.getTemplateId(encounterTypeUuid);
        if (customTemplateId == thisTemplateId) {
					getEncounterDetails(encounterId, dataTarget, customTemplateId);
				}
		  });
			    
			$(document).on('click', '.deleteEncounterId', function(event) {
				var encounterId = $(event.target).attr("data-encounter-id");
				createDeleteEncounterDialog(encounterId, $(this));
				showDeleteEncounterDialog();
			});

		  $(document).on('click', '.editEncounter', function(event) {
	      var encounterId = $(event.target).attr("data-encounter-id");
	      var patientId = $(event.target).attr("data-patient-id");
	      var editUrl = $(event.target).attr("data-edit-url");
	      if (editUrl) {
          editUrl = editUrl.replace("{{patientId}}", patientId).replace("{{patient.uuid}}", patientId)
              .replace("{{encounterId}}", encounterId).replace("{{encounter.id}}", encounterId);
          emr.navigateTo({ applicationUrl: editUrl });
	      } else {
          emr.navigateTo({
            provider: "htmlformentryui",
            page: "htmlform/editHtmlFormWithStandardUi",
            query: { patientId: patientId, encounterId: encounterId }
          });
	      }
		  });
			
			//We cannot assign it here due to Jasmine failure: 
			//net.sourceforge.htmlunit.corejs.javascript.EcmaError: TypeError: Cannot call method "replace" of undefined
			var detailsTemplates = {};

			function getEncounterDetails(id, dataTarget, displayTemplateId) {

				if (!detailsTemplates[displayTemplateId]) {
					detailsTemplates[displayTemplateId] = _.template($('#' + displayTemplateId).html());
        }
        var displayTemplate = detailsTemplates[displayTemplateId];
		    var encounterDetailsSection = $(dataTarget + ' .encounter-summary-container');

	  		if(encounterDetailsSection.html() == "") { encounterDetailsSection.html("<i class=\"icon-spinner icon-spin icon-2x pull-left\"></i>");}
	      $.getJSON(
	          emr.fragmentActionLink("coreapps", "visit/visitDetails", "getEncounterDetails", { encounterId: id })
	      ).success(function(data)
		      {
		          var cfg = {};
		          cfg.obs = data.observations;
		          cfgStr = JSON.stringify(cfg);
		          cfgStr = cfgStr.replace(/\"([^(\")"]+)\":/g,"$1:");

		          var htmlContent = "<vdui-complex-obs-encounter cfg='" + cfgStr + "'></vdui-complex-obs-encounter>";
		          encounterDetailsSection.html( $compile(htmlContent)($scope) );
		          $scope.$apply();
		      }
	      ).error(function(err){
	          emr.errorAlert(err);
	      });
	    }

  	}	// init()
  }]);
