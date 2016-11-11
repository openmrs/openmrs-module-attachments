angular.module('vdui.service.visitDocumentService')
  .factory('VisitDocument', function($resource) {
    return $resource("/" + OPENMRS_CONTEXT_PATH  + "/ws/rest/v1/visitdocument/:uuid", {
      uuid: '@uuid'
    },{
        query: { method:'GET', isArray:false } // OpenMRS RESTWS returns { "results": [] }
      });
  });