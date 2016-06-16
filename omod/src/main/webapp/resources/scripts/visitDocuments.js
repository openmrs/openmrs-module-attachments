angular.module('vdui.widget.dropzone', []).directive('dropzoneDirective',
  function () {
    return function (scope, element, attrs) {
      var config, dropzone;

      config = scope[attrs.dropzoneDirective];

      // create a Dropzone for the element with the given options
      dropzone = new Dropzone(element[0], config.options);

      scope.processDropzone = function() {
        dropzone.processQueue();
      };

      scope.removeAllFiles = function() {
        dropzone.removeAllFiles();
      };

      // bind the given event handlers
      angular.forEach(config.eventHandlers, function(handler, event) {
        dropzone.on(event, handler);
      });
    };
  });

angular.module('vdui.page.main', ['vdui.widget.dropzone', 'obsService', 'session', 'vdui.widget.thumbnail', 'vdui.widget.modalImage']);

angular.module('vdui.page.main').controller('FileUploadCtrl', ['$scope', '$rootScope', '$window', 'SessionInfo',
  function ($scope, $rootScope, $window, SessionInfo) {

    var providerUuid = "";
    $scope.visitUuid = "";  // In scope for toggling DOM elements

    $scope.init = function() {
      Dropzone.options.visitDocumentsDropzone = false;

      emr.loadMessages("visitdocumentsui.visitdocumentspage.upload.success,visitdocumentsui.visitdocumentspage.upload.error");

      SessionInfo.get().$promise.then(function(info) {
        providerUuid = info.currentProvider.uuid;
      });
      if ($window.config.visit) {
        $scope.visitUuid = $window.config.visit.uuid;
      }
    }

    $scope.dropzoneConfig = {
      
      'options': // passed into the Dropzone constructor
      { 
        'url': $window.config.uploadUrl,
        'thumbnailHeight': 100,
        'thumbnailWidth': 100,
        'paramName': 'visit_document_file',
        'maxFiles': 1,
        'maxFilesize': $window.config.maxFileSize,
        'acceptedFiles': 'image/*', 
        'autoProcessQueue': false
      },
      'eventHandlers':
      {
        'addedfile': function(file) {
          $scope.file = file;
          if (this.files[1] != null) {
            this.removeFile(this.files[0]);
          }
          $scope.$apply(function() {
            $scope.fileAdded = true;
          });
        },
        'sending': function (file, xhr, formData) {
          formData.append('patient', $window.config.patient.uuid);
          formData.append('visit', $scope.visitUuid);
          formData.append('provider', providerUuid);
          formData.append('fileCaption', ($scope.fileCaption == null) ? "" : $scope.fileCaption );
        },
        'success': function (file, response) {
          $rootScope.$emit('vdui_event_newComplexObs', response);
          $().toastmessage('showToast', { type: 'success', position: 'top-right', text: emr.message("visitdocumentsui.visitdocumentspage.upload.success") });
          $scope.clearForms();
        },
        'error': function (file, response, xhr) {
          $().toastmessage('showToast', { type: 'error', position: 'top-right', text: emr.message("visitdocumentsui.visitdocumentspage.upload.error") });
          console.log(response);
        }
      }
    };

    $scope.uploadFile = function() {
      $scope.processDropzone();
    };

    $scope.clearForms = function() {
      $scope.removeAllFiles();
      $scope.fileCaption = "";
      $scope.$apply();  // Not sure why we need this?
    }

    $scope.isUploadBtnDisabled = function() {
      return !($scope.fileCaption || $window.config.allowNoCaption);
    }

  }]);

angular.module('vdui.page.main').controller('ListComplexObsCtrl', ['$scope', '$rootScope', '$window', 'ObsService',
  function($scope, $rootScope, $window, ObsService) {

    $scope.obsArray = null;

    // Setting the config for the thumbnail directive
    $scope.thumbConfig = {};
    $scope.thumbConfig.canEdit = true; // This should be obtained from privileges
    $scope.thumbConfig.allowNoCaption = $window.config.allowNoCaption;
    $scope.thumbConfig.url = $window.config.downloadUrl + '?'
        + 'view=' + $window.config.thumbView + '&'
        + 'obs=';
    $scope.thumbConfig.contentUrl = $window.config.downloadUrl + '?'
        + 'view=' + $window.config.originalView + '&'
        + 'obs=';

    ObsService.getObs({
      patient: $window.config.patient.uuid,
      concept: $window.config.conceptComplexUuid,
      v: $window.config.obsRep
    }).then(function(obs) {
      $scope.obsArray = obs;
    })

    $rootScope.$on('vdui_event_newComplexObs', function(event, obs) {
      $scope.obsArray.unshift(obs);
      $scope.$apply();
    });

  }]);