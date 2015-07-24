(function() {
	'use strict';
	/**
	 * (en)InspectorCtrl 
	 * @ko InspectorCtrl
	 * @group Controller
	 * @name InspectorCtrl
	 * @class
	 */
	pinpointApp.controller('InspectorCtrl', [ '$scope', '$timeout', '$routeParams', 'locationService', 'NavbarVoService',
	    function ($scope, $timeout, $routeParams, locationService, NavbarVoService) {
			$at($at.INSPECTOR_PAGE);
	        // define private variables
	        var oNavbarVoService, oAgent;
	
	        // define private variables of methods
	        var getFirstPathOfLocation, changeLocation, getLocation, isLocationChanged;
	
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
	            if ($routeParams.agentId) {
	                oNavbarVoService.setAgentId($routeParams.agentId);
	            }
	            oNavbarVoService.autoCalculateByQueryEndDateTimeAndReadablePeriod();
	            $scope.$emit('navbarDirective.initializeWithStaticApplication', oNavbarVoService);
	            $scope.$emit('agentListDirective.initialize', oNavbarVoService);
	        }, 500);
	
	        /**
	         * scope event on navbarDirective.changed
	         */
	        $scope.$on('navbarDirective.changed', function (event, navbarVoService) {
	            oNavbarVoService = navbarVoService;
	            changeLocation();
	        });
	
	        /**
	         * scope event of agentListDirective.agentChanged
	         */
	        $scope.$on('agentListDirective.agentChanged', function (event, agent) {
	            oAgent = agent;
	            oNavbarVoService.setAgentId(agent.agentId);
	
	            if (isLocationChanged()) {
	                changeLocation();
	            }
	            if (oAgent) {
	                $scope.$emit('agentInfoDirective.initialize', oNavbarVoService, oAgent);
	            }
	        });
	
	        /**
	         * get first path of loction
	         * @returns {*|string}
	         */
	        getFirstPathOfLocation = function () {
	            var splitedPath = locationService.path().split('/');
	            return splitedPath[1] || 'inspector';
	        };
	
	        /**
	         * change location
	         */
	        changeLocation = function () {
	            var url = getLocation();
	            if (isLocationChanged()) {
	                if (locationService.path() === '/inspector') {
	
	                } else {
	                	locationService.skipReload().path(url).replace();
	                }
	                $scope.$emit('navbarDirective.initializeWithStaticApplication', oNavbarVoService);
	                $scope.$emit('agentListDirective.initialize', oNavbarVoService);
	            }
	        };
	
	        /**
	         * get location
	         * @returns {string}
	         */
	        getLocation = function () {
	            var url = '/' + getFirstPathOfLocation() + '/' + oNavbarVoService.getApplication() + '/' + oNavbarVoService.getReadablePeriod() + '/' + oNavbarVoService.getQueryEndDateTime();
	            if (oNavbarVoService.getAgentId()) {
	                url += '/' + oNavbarVoService.getAgentId();
	            }
	            return url;
	        };
	
	        /**
	         * is location changed
	         * @returns {boolean}
	         */
	        isLocationChanged = function () {
	            var url = getLocation();
	            if (locationService.path() !== url) {
	                return true;
	            }
	            return false;
	        };
	    }
	]);
})();