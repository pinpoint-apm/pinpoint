'use strict';

pinpointApp.controller('MainCtrl', [ '$scope', '$routeParams', function($scope, $routeParams) {
	
	$scope.main = "";
	$scope.sidebar = "";

	$scope.$on("dividerOpen", function(event) {
		$scope.main = "";
		$scope.sidebar = "";
		$(window).trigger('resize');
	});

	$scope.$on("dividerClosed", function(event) {
		$scope.main = "fullsize";
		$scope.sidebar = "smallsize";
		$(window).trigger('resize');
	});

} ]);
