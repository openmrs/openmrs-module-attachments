angular.module('vdui.widget.thumbnail', ['complexObsService'])
  .directive('vduiThumbnail', [ 'ComplexObs', function(Obs) {

    return {

      restrict: 'E',
      scope: {
      	obs: '=',
				config: '='
      },
      templateUrl: '/' + module.getPath(OPENMRS_CONTEXT_PATH) + '/templates/thumbnail.page',

    	controller : function($scope, $rootScope) {
    		$scope.editMode = false;

    		$scope.src = "";

    		$scope.toggleEditMode = function(editMode) {
		      $scope.newCaption = $scope.obs.comment;
		      $scope.editMode = editMode;
		      if ($scope.editMode) {
		      	$scope.editModeCss = "vdui_thumbnailEditMode";
		      }
		      else {
		      	$scope.editModeCss = "";
		      }
		    }

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

		    $scope.delete = function() {	// Work in progress, see backend
		      var saved = Obs.purge($scope.obs);
		      
		      saved.$promise.then(function(obs) {
		      	console.log("Obs deleted");
		      }, function(reason) {
		      	console.log("Obs deletion error");
		      });
		    }

		    $scope.show = function() {
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