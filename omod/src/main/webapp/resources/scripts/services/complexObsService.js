angular.module('vdui.service.complexObsService', ['ngResource', 'uicommons.common'])
  .factory('ComplexObs', function($resource) {
    return $resource("/" + OPENMRS_CONTEXT_PATH  + "/ws/rest/v1/obs/:uuid", {
      uuid: '@uuid'
    },{
        query: { method:'GET', isArray:false } // OpenMRS RESTWS returns { "results": [] }
      });
  });