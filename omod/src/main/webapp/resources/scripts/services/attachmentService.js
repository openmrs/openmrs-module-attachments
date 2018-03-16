angular.module('att.service.attachmentService').factory('Attachment', function($resource) {
    return $resource("/" + OPENMRS_CONTEXT_PATH + "/ws/rest/v1/attachment/:uuid", {
        uuid : '@uuid'
    }, {
        query : {
            method : 'GET',
            isArray : false
        }
    // OpenMRS RESTWS returns { "results": [] }
    });
}).factory('AttachmentService', function(Attachment) {

    return {

        /**
         * Fetches Obs
         *
         * @param params to search against
         * @returns $promise of array of matching Obs (REST ref representation by default)
         */
        getAttachments : function(params) {
            return Attachment.query(params).$promise.then(function(res) {
                return res.results;
            });
        }
    }
});
