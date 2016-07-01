angular.module('vdui.widget.complexObsEncounter', ['obsService', 'vdui.widget.gallery', 'vdui.widget.thumbnail', 'vdui.widget.modalImage'])
  .directive('vduiComplexObsEncounter', function() {
    return {
      restrict: 'E',
      scope: {
        encounter: '='
      },
      template: '<vdui-gallery obs-query="obsQuery"></vdui-gallery>',
      controller: function($scope) {
        $scope.obsQuery = {
          encounter: $scope.encounter.uuid
        };
      }  
    };
  });