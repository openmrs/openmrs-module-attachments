angular.module('att.service.complexObsCacheService')

.service('ComplexObsCacheService', [ 'ModuleUtils', '$http', '$q', function(module, $http, $q) {

    var setComplexObs = function(obs) {
        if (!module.complexObsCache) {
            module.complexObsCache = {};
        }
        module.complexObsCache[obs.uuid] = obs;
    }

    var setComplexData = function(uuid, complexData, view) {
        if (!module.complexDataCache) {
            module.complexDataCache = {};
        }
        if (!(view in module.complexDataCache)) {
            module.complexDataCache[view] = {};
        }
        module.complexDataCache[view][uuid] = complexData;
    }

    this.getComplexObs = function(obs, url, view) {
        var deferred = $q.defer();
        // Create a copy of the 'obs' function parameter to avoid modifying the parent $scope.obs variable
        obs = angular.copy(obs);

        var uuid = obs.uuid;
        if (!isViewInCache(uuid, view)) {
            var url = url + '?' + 'view=' + view + '&' + 'obs=' + uuid;
            $http.get(url, {
                cache : true,
                responseType : "arraybuffer"
            }).success(function(data, status, headers) {
                setComplexData(uuid, data, view);

                obs.mimeType = headers('Content-Type');
                obs.contentFamily = headers('Content-Family'); // Custom header
                obs.fileName = headers('File-Name'); // Custom header
                obs.fileExt = headers('File-Ext'); // Custom header
                setComplexObs(obs);

                deferred.resolve({
                    obs : obs,
                    complexData : data
                });
            }).error(function(msg, code) {
                deferred.reject(null);
            });
        } else {
            deferred.resolve({
                obs : module.complexObsCache[uuid],
                complexData : module.complexDataCache[view][uuid]
            });
        }
        return deferred.promise;
    }

    var isViewInCache = function(uuid, view) {
        if (!module.complexDataCache || !(view in module.complexDataCache)) {
            return false;
        }
        if (!(view in module.complexDataCache)) {
            return false;
        }
        if (!(uuid in module.complexDataCache[view])) {
            return false;
        }
        return true;
    }

} ]);