(function() {
	'use strict';
	/**
	 * (en)ScatterFullScreenModeCtrl 
	 * @ko ScatterFullScreenModeCtrl
	 * @group Controller
	 * @name ScatterFullScreenModeCtrl
	 * @class
	 */
	pinpointApp.controller('ScatterFullScreenModeCtrl', [ '$scope', '$rootScope', '$routeParams', '$timeout', 'NavbarVoService',
	    function ($scope, $rootScope, $routeParams, $timeout, NavbarVoService) {
			$at($at.SCATTER_FULL_SCREEN_PAGE);
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
	            $scope.$emit('scatterDirective.initializeWithNode', {applicationName: oNavbarVoService.getApplicationName()}, 800, 600)
	        }, 500);
	    }
	]);
})();