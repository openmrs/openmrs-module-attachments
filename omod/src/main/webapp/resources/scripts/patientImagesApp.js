angular.module('dropzoneModule', []).directive('dropzoneDirective', function () {
  return function (scope, element, attrs) {
    var config, dropzone;

    config = scope[attrs.dropzoneDirective];

    // create a Dropzone for the element with the given options
    dropzone = new Dropzone(element[0], config.options);

    // bind the given event handlers
    angular.forEach(config.eventHandlers, function (handler, event) {
      dropzone.on(event, handler);
    });
  };
});

angular.module('patientImagesApp', ['dropzoneModule']);

angular.module('patientImagesApp').controller('FileUploadCtrl', function ($scope) {
  
  $scope.dropzoneConfig = {
    
    'options': { // passed into the Dropzone constructor
      'url': '/upload',
      'paramName': 'patientimagefile',
      'maxFiles': 1,
      'autoProcessQueue': false
    },

    'eventHandlers': {
        'addedfile': function(file) {
            $scope.file = file;
            if (this.files[1]!=null) {
                this.removeFile(this.files[0]);
            }
            $scope.$apply(function() {
                $scope.fileAdded = true;
            });
        },
        'sending': function (file, xhr, formData) {},
        'success': function (file, response) {}
    }
  };

});