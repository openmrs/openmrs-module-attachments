angular.module('vdui.widget.thumbnail', ['complexObsService', 'ngDialog'])

  // .directive('vduiFocusOn', function() {
  //  return function(scope, elem, attr) {
  //     scope.$on(attr.vduiFocusOn, function(e) {
  //       elem[0].focus();
  //     });
  //   };
  // })

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

  .directive('vduiThumbnail', [ 'ComplexObs', 'ngDialog', function(Obs, ngDialog) {
    return {
      restrict: 'E',
      scope: {
      	obs: '=',
        config: '='
      },
      templateUrl: '/' + module.getPath(OPENMRS_CONTEXT_PATH) + '/templates/thumbnail.page',

      controller : function($scope, $rootScope) {

        emr.loadMessages("visitdocumentsui.thumbail.save.success,visitdocumentsui.thumbail.save.error,visitdocumentsui.thumbail.delete.error");

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
              $scope.$broadcast('vdui_event_editMode');
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

        $scope.getEditModeCss = function() {
          return $scope.editModeCss;
        }

        $scope.saveCaption = function() {
          var caption = $scope.obs.comment;
          if (caption == $scope.newCaption) {
            $scope.toggleEditMode(false);
            return;
          }
            
          $scope.obs.comment = $scope.newCaption;
          
          var saved = Obs.save($scope.obs);
          saved.$promise.then(function(obs) {
            $scope.toggleEditMode(false);
            $().toastmessage('showToast', { type: 'success', position: 'top-right', text: emr.message("visitdocumentsui.thumbail.save.success") });
		      }, function(reason) {
            $scope.obs.comment = caption;
            $().toastmessage('showToast', { type: 'error', position: 'top-right', text: emr.message("visitdocumentsui.thumbail.save.error") });
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
          }, function(err) {
            scope.closeThisDialog();
            if (purge === true) { // We should only do this error 500 is the cause: https://github.com/openmrs/openmrs-core/blob/1.11.x/api/src/main/java/org/openmrs/api/impl/ObsServiceImpl.java#L213
              scope.purge(null, scope);
            }
            else {
              $().toastmessage('showToast', { type: 'error', position: 'top-right', text: emr.message("visitdocumentsui.thumbail.delete.error") });  
              console.log(err);
            }
          }); 
        }

        $scope.showOriginal = function() {
          if (!$scope.editMode) {
          	var cfg = {};
          	cfg.url = $scope.config.afterUrl + $scope.obs.uuid;
          	cfg.caption = $scope.obs.comment;
          	$scope.$emit('vdui_event_diplayComplexObs', cfg);
          }
        }
      }

    };
  }]);