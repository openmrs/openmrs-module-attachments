angular.module('vdui.service.obsCacheService', [])
  .service('ObsCacheService', function() {
    this.get = function(uuid) {
      if (!module.obsCache) {
        module.obsCache = {};
        return null;
      }
      if (uuid in module.obsCache) {
        return module.obsCache[uuid];
      }
      else {
        return null;
      }
    };

    this.set = function(obs) {
      module.obsCache[obs.uuid] = obs;
    }
  });