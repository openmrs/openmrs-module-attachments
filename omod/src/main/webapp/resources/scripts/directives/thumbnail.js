angular.module('vdui.widget.thumbnail', ['complexObsService', 'ngDialog'])

  .directive('vduiThumbnail', [ 'ComplexObs', 'ngDialog', function(Obs, ngDialog) {

    return {

      restrict: 'E',
      scope: {
      	obs: '=',
				config: '='
      },
      templateUrl: '/' + module.getPath(OPENMRS_CONTEXT_PATH) + '/templates/thumbnail.page',

    	controller : function($scope, $rootScope) {
        $scope.toggleVisible = function(visible) {
          $scope.active = visible;
        }

        $scope.toggleEditMode = function(editMode) {
          $scope.newCaption = $scope.obs.comment;
          $scope.editMode = editMode;
          if ($scope.editMode) {
            $scope.editModeCss = "vdui_thumbnail-edit-mode";
          }
          else {
            $scope.editModeCss = "";
          }
        }

        $scope.toggleEditMode(false);
    		$scope.toggleVisible(true);
    		$scope.src = "";

		    $scope.saveCaption = function() {
		    	var caption = $scope.obs.comment;
		      $scope.obs.comment = $scope.newCaption;
		      var saved = Obs.save($scope.obs);
		      
		      saved.$promise.then(function(obs) {
		        $scope.toggleEditMode(false);
		      }, function(reason) {
		      	$scope.obs.comment = caption;
		      });
		    }

        $scope.delete = function() {

          // https://github.com/likeastore/ngDialog/blob/master/README.md
          ngDialog.open({
            template: 'vdui_thumbnail-confirm-dialog',
            scope: $scope,
            controller: ['$scope', function($scope) {
              $scope.showSpinner = false;
              $scope.confirm = function() {
                $scope.showSpinner = true;
                Obs.delete({
                  uuid: $scope.obs.uuid,
                  purge: true
                })
                .$promise.then(function(res) {
                  $scope.toggleVisible(false);
                  $scope.closeThisDialog();
                });                
              }
            }]
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