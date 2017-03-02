angular.module('vdui.widget.modalImage')
  .directive('vduiModalImage', [ 'ModuleUtils', function(module) {
    return {
      restrict: 'E',
      scope: {
        config: '='
      },
      templateUrl: '/' + module.getPartialsPath(OPENMRS_CONTEXT_PATH) + '/modalImage.html',
      controller: function($scope) {
        $scope.hide = function() {
          $scope.config = null;
        }

        $scope.getImageUrl = function() {
          return 'data:' + $scope.config.mimeType + ';base64,' + $scope.config.bytes;
        }
      }
    };
  }]);