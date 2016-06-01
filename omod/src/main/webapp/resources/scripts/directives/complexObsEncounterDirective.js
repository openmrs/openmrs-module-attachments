angular.module('vdui.widget.complexObsEncounter', ['vdui.widget.thumbnail', 'vdui.widget.modalImage'])
  .directive('vduiComplexObsEncounter', [ function() {
    return {

      restrict: 'E',
      scope: {
      	allObs: '=obs'
      },
      templateUrl: '/' + module.getPath(OPENMRS_CONTEXT_PATH) + '/templates/complexObsEncounterDirective.page',

    	controller : function($scope) {

        $scope.thumbnailCfg = {};
        emr.getFragmentActionWithCallback(module.getProvider(), "clientConfig", "get", {}, function(response) {

          // Setting the config for the thumbnail directive
          $scope.thumbnailCfg.url = response.downloadUrl + '?'
              + 'view=' + response.thumbView + '&'
              + 'obs=';
          $scope.thumbnailCfg.afterUrl = response.downloadUrl + '?'
              + 'view=' + response.originalView + '&'
              + 'obs=';

          $scope.$apply();
        });
    	}  

    };
  }]);