angular.module('vdui.widget.modalImage', ['vdui.service.moduleUtils'])
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
      }
    };
  }]);