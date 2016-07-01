angular.module('vdui.widget.modalImage', [])
  .directive('vduiModalImage', function() {
    return {
      restrict: 'E',
      scope: {},
      templateUrl: '/' + module.getPath(OPENMRS_CONTEXT_PATH) + '/templates/modalImage.page',

    	controller: function($scope, $rootScope) {
        /* Storing the current displayed obs globally */
        module.modalImageObs = null;
        $scope.setObs = function(obs) {
          if (obs !== module.modalImageObs) {
            module.modalImageObs = obs;
            $scope.obs = obs;
          }
        }

  			$scope.obs = null;
  			
        $scope.hide = function() {
          $scope.setObs(null);
        }

        $rootScope.$on(module.eventDisplayImage, function(event, obs) {
          $scope.setObs(obs);
        });
    	}
    };
  });