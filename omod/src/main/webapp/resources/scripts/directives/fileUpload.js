angular.module('vdui.widget.fileUpload', [])
  
  .directive('dropzoneDirective',
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
    })

  .directive('vduiFileUpload', ['SessionInfo', 'ObsService', function(sessionInfo, obsService) {
    return {

      restrict: 'E',
      scope: {
        config: '='
      },
      templateUrl: '/' + module.getPath(OPENMRS_CONTEXT_PATH) + '/templates/fileUpload.page',

      controller: function($scope, $rootScope) {

        var msgs = [
          "visitdocumentsui.fileUpload.success",
          "visitdocumentsui.fileUpload.error"
        ]
        emr.loadMessages(msgs.toString());

        var providerUuid = "";
        $scope.visitUuid = "";  // In scope for toggling DOM elements

        $scope.init = function() {
          Dropzone.options.visitDocumentsDropzone = false;

          sessionInfo.get().$promise.then(function(info) {
            providerUuid = info.currentProvider.uuid;
          });
          if (config.visit) {
            $scope.visitUuid = config.visit.uuid;
          }
        }

        $scope.dropzoneConfig = {
          
          'options': // passed into the Dropzone constructor
          { 
            'url': config.uploadUrl,
            'thumbnailHeight': 100,
            'thumbnailWidth': 100,
            'paramName': 'visit_document_file',
            'maxFiles': 1,
            'maxFilesize': config.maxFileSize,
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
              formData.append('patient', config.patient.uuid);
              formData.append('visit', $scope.visitUuid);
              formData.append('provider', providerUuid);
              formData.append('fileCaption', ($scope.fileCaption == null) ? "" : $scope.fileCaption );
            },
            'success': function (file, response) {
              $rootScope.$emit(module.eventNewFile, response);
              $().toastmessage('showToast', { type: 'success', position: 'top-right', text: emr.message("visitdocumentsui.fileUpload.success") });
              $scope.clearForms();
            },
            'error': function (file, response, xhr) {
              $().toastmessage('showToast', { type: 'error', position: 'top-right', text: emr.message("visitdocumentsui.fileUpload.error") });
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
          return !($scope.fileCaption || config.allowNoCaption);
        }        

      }
    };
  }]);