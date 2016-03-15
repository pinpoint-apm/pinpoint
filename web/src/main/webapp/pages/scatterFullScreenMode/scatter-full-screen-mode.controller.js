(function() {
	'use strict';
	/**
	 * (en)ScatterFullScreenModeCtrl 
	 * @ko ScatterFullScreenModeCtrl
	 * @group Controller
	 * @name ScatterFullScreenModeCtrl
	 * @class
	 */
	pinpointApp.controller('ScatterFullScreenModeCtrl', [ '$scope', '$rootScope', '$window', '$routeParams', '$timeout', 'NavbarVoService', 'AnalyticsService',
	    function ($scope, $rootScope, $window, $routeParams, $timeout, NavbarVoService, analyticsService) {
			analyticsService.send(analyticsService.CONST.SCATTER_FULL_SCREEN_PAGE);
			$window.htoScatter = $window.htoScatter || {};
        	$window.$routeParams = $window.$routeParams || $routeParams;
        	// define private variables
	        var oNavbarVoService;
	
	        // initialize
	        $rootScope.wrapperClass = 'no-navbar';
	        $rootScope.wrapperStyle = {
	            'padding-top': '0px'
	        };

	        /**
	         * initialize
	         */
	        $timeout(function () {
	            oNavbarVoService = new NavbarVoService();
	            if ($routeParams.application) {
	                oNavbarVoService.setApplication($routeParams.application);
	            }
	            if ($routeParams.readablePeriod) {
	                oNavbarVoService.setReadablePeriod($routeParams.readablePeriod);
	            }
	            if ($routeParams.queryEndDateTime) {
	                oNavbarVoService.setQueryEndDateTime($routeParams.queryEndDateTime);
	            }
				oNavbarVoService.autoCalculateByQueryEndDateTimeAndReadablePeriod();
				$scope.$emit('scatterDirective.initialize', oNavbarVoService);
				$scope.$emit('scatterDirective.initializeWithNode', {
					key: oNavbarVoService.getApplicationName() + "^" + oNavbarVoService.getServiceTypeName(),
					serviceType: oNavbarVoService.getServiceTypeName(),
					applicationName: oNavbarVoService.getApplicationName(),
					agentList : $routeParams.agentList.split(",")
				}, 800, 600);
	        }, 500);
	    }
	]);
})();