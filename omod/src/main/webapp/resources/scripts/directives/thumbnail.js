angular.module('vdui.widget.thumbnail', ['obsService'])

  .directive('vduiThumbnail', [ 'Obs', function(Obs) {

    return {

      restrict: 'E',
      scope: {
      	obs: '=obs',
				config: '=config'
      },

      templateUrl: 'templates/thumbnail.page',

    	controller : function($scope) {
    		$scope.editMode = false;

    		$scope.toggleEditMode = function(editMode) {
		      $scope.newCaption = $scope.obs.comment;
		      $scope.editMode = editMode;
		      if ($scope.editMode) {
		      	$scope.editModeCss = "vdui_editMode";
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
    	}

    };
  }]);