(function() {
	'use strict';
	/**
	 * (en)InspectorCtrl 
	 * @ko InspectorCtrl
	 * @group Controller
	 * @name InspectorCtrl
	 * @class
	 */
	pinpointApp.constant( "InspectorCtrlConfig", {
		ID: "INSPECTOR_CTRL_",
		PAGE_NAME: "inspector",
		SLASH: "/"
	});
	pinpointApp.controller("InspectorCtrl", [ "InspectorCtrlConfig", "$scope", "$routeParams", "$timeout", "SystemConfigurationService", "locationService", "CommonUtilService", "UrlVoService", "AnalyticsService",
	    function ( cfg, $scope, $routeParams, $timeout, SystemConfigService, locationService, CommonUtilService, UrlVoService, AnalyticsService) {
			$scope.selectedAgent = false;
			$scope.timeSliderOption = null;
			$scope.showStatistic = SystemConfigService.get("showApplicationStat");
			cfg.ID +=  CommonUtilService.getRandomNum();
			AnalyticsService.send(AnalyticsService.CONST.INSPECTOR_PAGE);

			UrlVoService.initUrlVo( "inspector", $routeParams );
			$scope.$on( "up.changed.application", function ( event, invokerId, newAppName ) {
				UrlVoService.setApplication( newAppName );
				UrlVoService.setAgentId( "" );
				$timeout(function() {
					changeLocation(function () {
						$scope.timeSliderOption = null;
						$scope.$broadcast("down.changed.application", invokerId);
					});
				});
			});
			$scope.$on( "up.changed.period", function ( event, invokerId ) {
				changeLocation(function() {
					$scope.timeSliderOption = null;
					$scope.$broadcast( "down.changed.period", true, invokerId );
				});
			});
			$scope.$on( "up.changed.agent", function ( event, invokerId, agent, bInvokedByTop ) {
				$scope.selectedAgent = true;
				$timeout(function() {
					if (agent && ( agent.agentId === UrlVoService.getAgentId() )) { 	// when open page or change period
						$scope.$broadcast("down.changed.agent", invokerId, agent, bInvokedByTop);
					} else {												// when select other agent
						if (CommonUtilService.isEmpty(agent.agentId) === false) {
							UrlVoService.setAgentId(agent.agentId);
						}
						changeLocation(function () {
							$scope.$broadcast("down.changed.agent", invokerId, agent, bInvokedByTop, $scope.timeSliderOption);
						});
					}
				});
			});
			$scope.$on( "up.select.application", function ( event, invokerId ) {
				$scope.selectedAgent = false;
				UrlVoService.setAgentId("");
				$timeout(function() {
					changeLocation(function () {
						$scope.$broadcast("down.select.application", invokerId, $scope.timeSliderOption);
					});
				});
			});
			$scope.$on( "up.changed.timeSliderOption", function( event, sliderTimeSeries, sliderSelectionTimeSeries, sliderSelectedTime ) {
				$scope.timeSliderOption = {
					timeSeries: sliderTimeSeries,
					selectionTimeSeries: sliderSelectionTimeSeries,
					selectedTime: sliderSelectedTime
				};
			});
	        var changeLocation = function ( callback ) {
				var newPath = getLocation();
	            if ( locationService.path() !== newPath ) {
                	locationService.skipReload().path( newPath ).replace();
				}
				callback();
	        };
	        var getLocation = function () {
				var url = [
					"",
					cfg.PAGE_NAME,
					UrlVoService.getApplication(),
					UrlVoService.getReadablePeriod(),
					UrlVoService.getQueryEndDateTime()
				].join( cfg.SLASH );

	            if ( UrlVoService.getAgentId() !== "" ) {
	                url += cfg.SLASH + UrlVoService.getAgentId();
	            }
	            return url;
	        };
	    }
	]);
})();