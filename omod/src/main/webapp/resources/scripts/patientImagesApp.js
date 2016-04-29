angular.module('dropzoneModule', []).directive('dropzoneDirective',
  function () {
    return function (scope, element, attrs) {
      var config, dropzone;

      config = scope[attrs.dropzoneDirective];

      // create a Dropzone for the element with the given options
      dropzone = new Dropzone(element[0], config.options);

      scope.processDropzone = function() {
        dropzone.processQueue();
      };

      // bind the given event handlers
      angular.forEach(config.eventHandlers, function (handler, event) {
        dropzone.on(event, handler);
      });
    };
  });

angular.module('patientImagesApp', ['dropzoneModule', 'obsService', 'session']);

angular.module('patientImagesApp').controller('FileUploadCtrl', ['$scope', '$window', 'SessionInfo',
  function ($scope, $window, SessionInfo) {

    // This happens on page load, by the time any file is dropped in, the provider would have been fetched.
    var providerUuid = "";
    SessionInfo.get().$promise.then(function(info) {
      providerUuid = info.currentProvider.uuid;
    });

    $scope.dropzoneConfig = {
      
      'options': // passed into the Dropzone constructor
      { 
        'url': $window.config.uploadUrl,
        'paramName': 'patientimagefile',
        'maxFiles': 1,
        'maxFilesize': $window.config.maxFileSize,
        'acceptedFiles': 'image/*', 
        'autoProcessQueue': false
      },
      'eventHandlers':
      {
        'addedfile': function(file) {
          $scope.file = file;
          if (this.files[1]!=null) {
            this.removeFile(this.files[0]);
          }
          $scope.$apply(function() {
            $scope.fileAdded = true;
          });
        },
        'sending': function (file, xhr, formData) {
          formData.append('providerUuid', providerUuid);
          formData.append('obsText', $scope.obsText);
        },
        'success': function (file, response) {
          $scope.$broadcast('newObsEvent', response);
        }
      }
    };

    $scope.uploadFile = function() {
      $scope.processDropzone();
    };

  }]);

angular.module('patientImagesApp').controller('ListObsCtrl', ['$scope', '$window', 'ObsService',
  function($scope, $window, ObsService) {

    ObsService.getObs({
      patient: $window.config.patient.uuid,
      concept: $window.config.conceptComplexUuid,
      v: $window.config.obsRep
    }).then(function(obs) {
      $scope.obsArray = obs;
    })

    $scope.$on('newObsEvent', function(event, obs) {
      $scope.obsArray.unshift(obs);
      $scope.$apply();
    });

  }]);









