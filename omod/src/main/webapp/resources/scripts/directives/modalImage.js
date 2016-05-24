angular.module('vdui.widget.modalImage', [])

  .directive('vduiModalImage', function() {

    return {

      restrict: 'E',
      scope: {
      },
      templateUrl: 'templates/modalImage.page',

    	controller : function($scope, $rootScope) {
  			$scope.cfg = {};
  			
  			$scope.hide = function() {
  				$scope.cfg = {};
  			}

  			$rootScope.$on('vdui_event_diplayComplexObs', function(event, cfg) {
		      $scope.cfg = cfg;
		    });
    	}

    };
  });