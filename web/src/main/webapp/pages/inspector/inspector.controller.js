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
	pinpointApp.controller("InspectorCtrl", [ "InspectorCtrlConfig", "$scope", "$routeParams", "locationService", "CommonUtilService", "UrlVoService", "AnalyticsService",
	    function ( cfg, $scope, $routeParams, locationService, CommonUtilService, UrlVoService, AnalyticsService) {
			cfg.ID +=  CommonUtilService.getRandomNum();
			AnalyticsService.send(AnalyticsService.CONST.INSPECTOR_PAGE);

			$scope.$on( "up.changed.application", function ( event, invokerId, newAppName ) {
				UrlVoService.setApplication( newAppName );
				UrlVoService.setAgentId( "" );
	            changeLocation(function() {
					$scope.$broadcast( "down.changed.application", invokerId );
					$scope.$broadcast( "down.changed.agent.", invokerId, {} );
				});
			});
			$scope.$on( "up.changed.period", function ( event, invokerId ) {
				changeLocation(function() {
					$scope.$broadcast( "down.changed.period", true, invokerId );
				});
			});
			$scope.$on( "up.changed.agent", function ( event, invokerId, agent, bInvokedByTop ) {
				if ( UrlVoService.getAgentId() === agent.agentId ) { 	// when open page or change period
					$scope.$broadcast( "down.changed.agent", invokerId, agent, bInvokedByTop );
				} else {												// when select other agent
					if ( CommonUtilService.isEmpty( agent.agentId ) === false ) {
						UrlVoService.setAgentId( agent.agentId );
					}
					changeLocation(function() {
						$scope.$broadcast( "down.changed.agent", invokerId, agent, bInvokedByTop );
					});
				}
			});
	        var changeLocation = function ( callback ) {
				var newPath = getLocation();
	            if ( locationService.path() !== newPath ) {
                	locationService.skipReload().path( newPath ).replace();
					callback();
	            }
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

			$scope.$broadcast("down.initialize", cfg.ID );
	    }
	]);
})();