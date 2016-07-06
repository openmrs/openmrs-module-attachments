angular.module('vdui.service.configService', [])
  .factory('ConfigService', function($q) { 
    var getConfig = function() {
      var deferred = $q.defer();
      if (module.clientConfig) {
        deferred.resolve(module.clientConfig);
      }
      else {
        emr.getFragmentActionWithCallback(module.getProvider(), "clientConfig", "get", {}, function(response) {
          deferred.resolve(response);
          module.clientConfig = response;
        });
      }
      return deferred.promise;
    };

    return {
      getConfig: getConfig
    };
  })