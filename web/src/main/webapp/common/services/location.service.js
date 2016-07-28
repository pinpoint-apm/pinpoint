(function() {
	'use strict';
	/**
	 * (en)locationService 
	 * @ko locationService
	 * @group Service
	 * @name locationService
	 * @class
	 */
	pinpointApp.factory("locationService", [ "$location", "$route", "$rootScope", function ($location, $route, $rootScope) {
	  	$location.skipReload = function () {
	    	var lastRoute = $route.current;
	    	var un = $rootScope.$on('$locationChangeSuccess', function () {
	      		$route.current = lastRoute;
	      		un();
	    	});
	    	return $location;
	  	};
	  	return $location;
	}]);
})();