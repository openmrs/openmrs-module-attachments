angular.module('vdui.widget.modalImage', [])
  .directive('vduiModalImage', function() {
    return {
      restrict: 'E',
      scope: {
        config: '='
      },
      templateUrl: '/' + module.getPath(OPENMRS_CONTEXT_PATH) + '/templates/modalImage.page',
      controller: function($scope) {
        $scope.hide = function() {
          $scope.config = null;
        }
      }
    };
  });