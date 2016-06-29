angular.module('vdui.page.main', ['obsService', 'session', 'vdui.widget.fileUpload', 'vdui.widget.gallery', 'vdui.widget.thumbnail']);

angular.module('vdui.page.main').controller('FileUploadCtrl', ['$scope', '$window',
  function ($scope, $window) {
    $scope.config = $window.config;
  }]);

angular.module('vdui.page.main').controller('GalleryCtrl', ['$scope', '$window',
  function($scope, $window) {
    $scope.galleryConfig = {};
    $scope.galleryConfig.canEdit = true;  // This should be obtained from privileges
    $scope.obsQuery = {
      patient: $window.config.patient.uuid,
      conceptList: $window.config.conceptComplexUuidList.toString()  // http://stackoverflow.com/a/202247/321797
    };
  }]);