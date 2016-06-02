angular.module('vdui.widget.complexObsEncounter', ['obsService', 'vdui.widget.thumbnail', 'vdui.widget.modalImage'])

  .directive('vduiComplexObsEncounter', ['ObsService', function() {
    return {

      restrict: 'E',
      scope: {
        encounter: '='
      },
      templateUrl: '/' + module.getPath(OPENMRS_CONTEXT_PATH) + '/templates/complexObsEncounterDirective.page',

    	controller : function($scope, ObsService) {

        $scope.thumbnailCfg = null;

        emr.getFragmentActionWithCallback(module.getProvider(), "clientConfig", "get", {}, function(response) {
          
          ObsService.getObs({
            encounter: $scope.encounter.uuid,
            v: response.obsRep
          }).then(function(obs) {
            $scope.allObs = obs;
            $scope.thumbnailCfg = {};
            $scope.thumbnailCfg.url = response.downloadUrl + '?'
                + 'view=' + response.thumbView + '&'
                + 'obs=';
            $scope.thumbnailCfg.afterUrl = response.downloadUrl + '?'
                + 'view=' + response.originalView + '&'
                + 'obs=';
          })

        });
    	}  
    };
  }]);