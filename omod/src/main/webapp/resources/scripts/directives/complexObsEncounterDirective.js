angular.module('vdui.widget.complexObsEncounter')
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