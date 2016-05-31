angular.module('vdui.widget.complexObsEncounter', ['vdui.widget.thumbnail', 'vdui.widget.modalImage'])

  .directive('vduiComplexObsEncounter', [ function() {

    return {

      restrict: 'E',
      scope: {
      	cfg: '='
      },

      templateUrl: '/' + OPENMRS_CONTEXT_PATH + '/visitdocumentsui/templates/complexObsEncounterDirective.page',

    	controller : function($scope) {
    	}

    };
  }]);