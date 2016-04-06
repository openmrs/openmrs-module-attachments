angular.module('PatientImages', [])

.controller( 'PatientImagesPageCtrl', ['$scope', '$window', function($scope, $window) {

	$scope.patientUuid = $window.config.patient.uuid;
	$scope.visitUuid = $window.config.visit.uuid;

}
])

;