angular.module('vdui.widget.thumbnail', ['vdui.service.complexObsService', 'ngDialog', 'vdui.widget.modalImage'])

  .directive('vduiEnterKeyDown', function() {
    return function(scope, element, attrs) {
      element.bind("keydown keypress", function(event) {
        if(event.which === 13) {
          scope.$apply(function() {
            scope.$eval(attrs.vduiEnterKeyDown, {'event': event});
          });
          event.preventDefault();
        }
      });
    };
  })

  .directive('vduiEscapeKeyDown', function() {
    return function(scope, element, attrs) {
      element.bind("keydown keypress", function(event) {
        if(event.which === 27) {
          scope.$apply(function() {
            scope.$eval(attrs.vduiEscapeKeyDown, {'event': event});
          });
          event.preventDefault();
        }
      });
    };
  })

  .directive('vduiThumbnail', [ 'ComplexObs', 'ngDialog', '$http', '$window', function(Obs, ngDialog, $http, $window) {
    return {
      restrict: 'E',
      scope: {
      	obs: '=',
        config: '='
      },
      templateUrl: '/' + module.getPath(OPENMRS_CONTEXT_PATH) + '/templates/thumbnail.page',

      controller: function($scope) {

        var msgs = [
          "visitdocumentsui.thumbail.get.error",
          "visitdocumentsui.thumbail.save.success",
          "visitdocumentsui.thumbail.save.error",
          "visitdocumentsui.thumbail.delete.success",
          "visitdocumentsui.thumbail.delete.error"
        ]
        emr.loadMessages(msgs.toString());

        $scope.canEdit = function() {
          if($scope.config.canEdit) {
            if($scope.config.canEdit === true) {
              return true;
            }
          }
          return false;
        }

        $scope.toggleVisible = function(visible) {
          $scope.active = visible;
        }

        $scope.toggleEditMode = function(editMode) {
          if ($scope.canEdit()) {
            $scope.newCaption = $scope.obs.comment;
            $scope.editMode = editMode;
            if ($scope.editMode) {
              $scope.editModeCss = "vdui_thumbnail-edit-mode";
            }
            else {
              $scope.editModeCss = "";
            }
          }
        }

        $scope.toggleEditMode(false);
        $scope.toggleVisible(true);
        $scope.src = "";
        $scope.loading = false;

        $scope.getEditModeCss = function() {
          return $scope.editModeCss;
        }

        $scope.getPrettyDate = function() {
          var timeFormat = "dd mmm yy";
          if ((new Date($scope.obs.obsDatetime)).toDateString() === (new Date()).toDateString()) {
            timeFormat = "H:MM";
          }
          else {
            if ((new Date($scope.obs.obsDatetime)).getYear() === (new Date()).getYear()) {
              timeFormat = "dd mmm";
            }
          }
          return dateFormat($scope.obs.obsDatetime, timeFormat);
        }

        $scope.saveCaption = function() {
          var caption = $scope.obs.comment;
          if ((caption == $scope.newCaption) || ($scope.newCaption == "" && !$scope.config.allowNoCaption)) {
            $scope.toggleEditMode(false);
            return;
          }

          $scope.obs.comment = $scope.newCaption;
          
          var saved = Obs.save({
            uuid: $scope.obs.uuid,
            comment: $scope.obs.comment
          });
          saved.$promise.then(function(obs) {
            $scope.toggleEditMode(false);
            $().toastmessage('showToast', { type: 'success', position: 'top-right', text: emr.message("visitdocumentsui.thumbail.save.success") });
          }, function(reason) {
            $scope.obs.comment = caption;
            $().toastmessage('showToast', { type: 'error', position: 'top-right', text: emr.message("visitdocumentsui.thumbail.save.error") });
            console.log(err);
          });
        }

        $scope.confirmDelete = function() {
          // https://github.com/likeastore/ngDialog/blob/master/README.md
          ngDialog.open({
            template: 'vdui_thumbnail-confirm-dialog',
            scope: $scope,
            controller: ['$scope', function($scope) {
              $scope.showSpinner = false;
              $scope.confirm = function() {
                $scope.showSpinner = true;
                $scope.purge(true, $scope);
              }
            }]
          });
        }

        $scope.purge = function(purge, scope) {
          Obs.delete({
            uuid: scope.obs.uuid,
            purge: purge
          })
          .$promise.then(function(res) {
            scope.toggleVisible(false);
            scope.closeThisDialog();
            $().toastmessage('showToast', { type: 'success', position: 'top-right', text: emr.message("visitdocumentsui.thumbail.delete.success") });
          }, function(err) {
            scope.closeThisDialog();
            if (purge === true) { // We should only do this if error 500 is the cause: https://github.com/openmrs/openmrs-core/blob/1.11.x/api/src/main/java/org/openmrs/api/impl/ObsServiceImpl.java#L213
              scope.purge(null, scope);
            }
            else {
              $().toastmessage('showToast', { type: 'error', position: 'top-right', text: emr.message("visitdocumentsui.thumbail.delete.error") });  
              console.log(err);
            }
          }); 
        }

        $scope.displayThumbnail = function() {

          var url = $scope.config.downloadUrl + '?'
            + 'view=' + $scope.config.thumbView + '&'
            + 'obs=' + $scope.obs.uuid;

          $http.get(url, {responseType: "arraybuffer"})
            .success(function (data, status, headers) {
              $scope.obs.mimeType = headers('Content-Type');
              $scope.obs.contentFamily = headers('Content-Family');  // Custom header
              $scope.obs.fileName = headers('File-Name');  // Custom header
              $scope.obs.fileExt = headers('File-Ext');  // Custom header
              
              switch ($scope.obs.contentFamily) {
                case "IMAGE":
                  $scope.obs.complexData = module.arrayBufferToBase64(data);
                  break;
                case "OTHER":
                  break;
              }
            })
            .error(function (data, status) {
              $scope.loading = false;
              $().toastmessage('showToast', { type: 'error', position: 'top-right', text: emr.message("visitdocumentsui.thumbail.get.error") });
              console.log(err);
            });
        }
        $scope.displayThumbnail();

        $scope.displayContent = function() {

          if ($scope.obs.uuid in module.obsCache) {
            var obs = module.obsCache[$scope.obs.uuid];
            $scope.$emit(obs.displayEventName, obs);
            return;
          }

          $scope.loading = true;

          var url = $scope.config.downloadUrl + '?'
            + 'view=' + $scope.config.originalView + '&'
            + 'obs=' + $scope.obs.uuid;

          $http.get(url, {responseType: "arraybuffer"})
            .success(function (data, status, headers) {
              var obs = {};
              angular.copy($scope.obs, obs);  // deep copy
              obs.mimeType = headers('Content-Type');
              obs.contentFamily = headers('Content-Family');  // Custom header
              obs.fileName = headers('File-Name');  // Custom header
              
              switch (obs.contentFamily) {
                case "IMAGE":
                  obs.complexData = module.arrayBufferToBase64(data);
                  obs.displayEventName = module.eventDisplayImage;
                  break;
                case "OTHER":
                  obs.complexData = data;
                  obs.displayEventName = module.eventDownloadFile;
                  break;
              }
              module.obsCache[$scope.obs.uuid] = obs;
              if (obs.displayEventName) {
                $scope.$emit(obs.displayEventName, obs);
              }
              $scope.loading = false;
            })
            .error(function (data, status) {
              $scope.loading = false;
              $().toastmessage('showToast', { type: 'error', position: 'top-right', text: emr.message("visitdocumentsui.thumbail.get.error") });
              console.log(err);
            });

        }

        // http://stackoverflow.com/a/28541187/321797
        $scope.triggerFileDownload = function(complexObs) {
          var blob = new Blob([complexObs.complexData], { type: complexObs.mimeType });     
          var downloadLink = angular.element('<a></a>');
          downloadLink.attr('href', $window.URL.createObjectURL(blob));
          downloadLink.attr('download', complexObs.fileName);
          downloadLink[0].click();
        }

        $scope.$on(module.eventDownloadFile, function(event, obs) {
          $scope.triggerFileDownload(obs);
        });

      }

    };
  }]);