(function() {
	'use strict';
	pinpointApp.controller( "ScatterFullScreenModeCtrl", [ "$scope", "$rootScope", "$window", "$routeParams", "$timeout", "UrlVoService", "AnalyticsService",
	    function ($scope, $rootScope, $window, $routeParams, $timeout, UrlVoService, analyticsService) {

			analyticsService.send(analyticsService.CONST.SCATTER_FULL_SCREEN_PAGE);
			$window.htoScatter = $window.htoScatter || {};
        	$window.$routeParams = $window.$routeParams || $routeParams;

	        $rootScope.wrapperClass = 'no-navbar';
	        $rootScope.wrapperStyle = {
	            'padding-top': '0px'
	        };

	        $timeout(function () {
				UrlVoService.initUrlVo( "scatterFullScreenMode", $routeParams );
				UrlVoService.autoCalculateByQueryEndDateTimeAndReadablePeriod();

				$scope.$broadcast("scatterDirective.initialize.forMain");
				$scope.$broadcast("scatterDirective.initializeWithNode.forMain", {
					key: UrlVoService.getApplicationName() + "^" + UrlVoService.getServiceType(),
					serviceType: UrlVoService.getServiceType(),
					applicationName: UrlVoService.getApplicationName(),
					agentIds : UrlVoService.getAgentList()
				}, 800, 600);
	        }, 500);
	    }
	]);
})();