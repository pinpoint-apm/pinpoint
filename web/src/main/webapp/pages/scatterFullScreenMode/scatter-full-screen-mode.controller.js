(function() {
	'use strict';
	/**
	 * (en)ScatterFullScreenModeCtrl 
	 * @ko ScatterFullScreenModeCtrl
	 * @group Controller
	 * @name ScatterFullScreenModeCtrl
	 * @class
	 */
	// pinpointApp.controller( "ScatterFullScreenModeCtrl", [ "$scope", "$rootScope", "$window", "$routeParams", "$timeout", "NavbarVoService", "UrlVoService", "AnalyticsService",
	//     function ($scope, $rootScope, $window, $routeParams, $timeout, NavbarVoService, UrlVoService, analyticsService) {
	pinpointApp.controller( "ScatterFullScreenModeCtrl", [ "$scope", "$rootScope", "$window", "$routeParams", "$timeout", "UrlVoService", "AnalyticsService",
	    function ($scope, $rootScope, $window, $routeParams, $timeout, UrlVoService, analyticsService) {

			analyticsService.send(analyticsService.CONST.SCATTER_FULL_SCREEN_PAGE);
			$window.htoScatter = $window.htoScatter || {};
        	$window.$routeParams = $window.$routeParams || $routeParams;
        	// define private variables
	        // var oNavbarVoService;
	
	        // initialize
	        $rootScope.wrapperClass = 'no-navbar';
	        $rootScope.wrapperStyle = {
	            'padding-top': '0px'
	        };

	        /**
	         * initialize
	         */
	        $timeout(function () {
				// oNavbarVoService = new NavbarVoService();
				// if ($routeParams.application) {
	             //    oNavbarVoService.setApplication($routeParams.application);
				// }
				// if ($routeParams.readablePeriod) {
	             //    oNavbarVoService.setReadablePeriod($routeParams.readablePeriod);
				// }
				// if ($routeParams.queryEndDateTime) {
	             //    oNavbarVoService.setQueryEndDateTime($routeParams.queryEndDateTime);
				// }
				// oNavbarVoService.autoCalculateByQueryEndDateTimeAndReadablePeriod();

				UrlVoService.initUrlVo( "scatterFullScreenMode", $routeParams );
				UrlVoService.autoCalculateByQueryEndDateTimeAndReadablePeriod();

				// $scope.$broadcast('scatterDirective.initialize.forMain', oNavbarVoService);
				$scope.$broadcast("scatterDirective.initialize.forMain");
				$scope.$broadcast("scatterDirective.initializeWithNode.forMain", {
					// key: oNavbarVoService.getApplicationName() + "^" + oNavbarVoService.getServiceTypeName(),
					// serviceType: oNavbarVoService.getServiceTypeName(),
					// applicationName: oNavbarVoService.getApplicationName(),
					// agentList : $routeParams.agentList.split(",")
					key: UrlVoService.getApplicationName() + "^" + UrlVoService.getServiceType(),
					serviceType: UrlVoService.getServiceType(),
					applicationName: UrlVoService.getApplicationName(),
					agentList : UrlVoService.getAgentList()
				}, 800, 600);
	        }, 500);
	    }
	]);
})();