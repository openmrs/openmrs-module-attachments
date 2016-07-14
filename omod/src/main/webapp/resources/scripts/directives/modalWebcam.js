angular.module('vdui.widget.modalWebcam', [])

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

.directive('vduiModalWebcam', function() {
  return {
    restrict: 'E',
    scope: {
      visible: '=',
      disabled: '=?'
    },
    templateUrl: '/' + module.getPath(OPENMRS_CONTEXT_PATH) + '/templates/modalWebcam.page',
    controller: function($scope, $rootScope, $window) {
      $scope.imgWidth = 640;
      $scope.imgHeight = 480;
      
      $scope.setWebcam = function() {
        Webcam.set({
          width: $scope.imgWidth,
          height: $scope.imgHeight,
          dest_width: $scope.imgWidth,
          dest_height: $scope.imgHeight,
          force_flash: $window.chrome && $window.location.protocol != "https:"
        });
        Webcam.attach('#vdui_webcam-id');
      }

      $scope.$watch("visible", function() {
        if ($scope.disabled === true)
          return;

        if ($scope.visible) {
          $scope.setWebcam();
        }
        else {
          Webcam.reset();
        }
      });

      $scope.close = function(){
        $scope.visible = false;
        $scope.dataUri = null;
      }

      $scope.snap = function() {
        Webcam.snap( function(dataUri) {
          $scope.dataUri = dataUri;
        });
      }

      $scope.repeat = function() {
        $scope.dataUri = null;
      }

      $scope.finalise = function() {
        var file = module.dataUritoBlob($scope.dataUri);
        file.name = "wc_pic_" + moment(new Date()).format("YYYYMMDD_HHmmss") + ".png";
        $rootScope.$emit(module.webcamCaptureForUpload, file);
        $scope.close();
      }
    }
  };
});