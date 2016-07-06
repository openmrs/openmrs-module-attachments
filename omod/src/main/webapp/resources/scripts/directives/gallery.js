angular.module('vdui.widget.gallery', ['vdui.service.configService'])
  .directive('vduiGallery', ['ObsService', 'ConfigService', function(obsService, configService) {
    return {

      restrict: 'E',
      scope: {
        config: '=?',
        obsQuery: '='
      },
      templateUrl: '/' + module.getPath(OPENMRS_CONTEXT_PATH) + '/templates/gallery.page',

      controller: function($scope, $rootScope) {

        configService.getConfig().then(function(config) {
          $scope.config = angular.extend({}, config, $scope.config);
          init($scope.config);
        });

        $rootScope.$on(module.eventNewFile, function(event, obs) {
          $scope.obsArray.unshift(obs);
          $scope.$apply();
        });

        var init = function(config) {
          $scope.obsQuery.v = config.obsRep;
          obsService.getObs($scope.obsQuery)
          .then(function(obs) {
            $scope.obsArray = obs;
          })
        }
      }
    };
  }]);