angular.module('vdui.page.main').controller('FileUploadCtrl', ['$scope', '$window',
  function ($scope, $window) {
    $scope.config = $window.vdui.config;
  }]);

angular.module('vdui.page.main').controller('GalleryCtrl', ['$scope', '$window',
  function($scope, $window) {
    $scope.obsQuery = {
      patient: $window.vdui.config.patient.uuid,
      conceptList: $window.vdui.config.conceptComplexUuidList.toString()  // http://stackoverflow.com/a/202247/321797
    };
  }]);