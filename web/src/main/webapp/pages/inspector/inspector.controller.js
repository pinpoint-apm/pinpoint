(function() {
	'use strict';
	/**
	 * (en)InspectorCtrl 
	 * @ko InspectorCtrl
	 * @group Controller
	 * @name InspectorCtrl
	 * @class
	 */
	pinpointApp.constant( "InspcectorCtrlConfig", {
		ID: "INSPECTOR_CTRL_",
		PAGE_NAME: "inspector",
		SLASH: "/"
	});
	pinpointApp.controller("InspectorCtrl", [ "InspcectorCtrlConfig", "$scope", "$timeout", "$routeParams", "locationService", "CommonUtilService", "UrlVoService", "AnalyticsService",
	    function ( cfg, $scope, $timeout, $routeParams, locationService, CommonUtilService, UrlVoService, AnalyticsService) {
			cfg.ID +=  CommonUtilService.getRandomNum();
			AnalyticsService.send(AnalyticsService.CONST.INSPECTOR_PAGE);

	        $timeout(function () {
				// to next tick
				// UrlVoService.initUrlVo( cfg.PAGE_NAME, $routeParams );
				// UrlVoService.autoCalculateByQueryEndDateTimeAndReadablePeriod();

				// $scope.$broadcast( "down.initialize", cfg.ID, true );
	        });
			$scope.$on( "up.changed.application.url", function ( event, invokerId ) {
				console.log( cfg.ID + " up.changed.application.url", invokerId );
	            changeLocation(function() {
					$scope.$broadcast( "down.changed.application.url", invokerId );
					$scope.$broadcast( "down.changed.agent.url", invokerId, {} );
				});
			});
			$scope.$on( "up.changed.period.url", function ( event, invokerId ) {
				console.log( cfg.ID + " up.changed.period.url", invokerId );
				changeLocation(function() {
					$scope.$broadcast( "down.changed.period.url", true, invokerId );
				});
			});
			$scope.$on( "up.changed.agent.url", function ( event, invokerId, agent, bInvokedByTop ) {
				if ( UrlVoService.getAgentId() === agent.agentId ) { 	// when open page or change period
					$scope.$broadcast( "down.changed.agent.url", invokerId, agent, bInvokedByTop );
				} else {												// when select other agent
					if ( CommonUtilService.isEmpty( agent.agentId ) === false ) {
						UrlVoService.setAgentId( agent.agentId );
					}
					changeLocation(function() {
						$scope.$broadcast( "down.changed.agent.url", invokerId, agent, bInvokedByTop );
					});
				}
			});
	        var changeLocation = function ( callback ) {
				var newPath = getLocation();
				console.log( "changeLocation : ", locationService.path() , newPath );
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

				console.log( "getLocation :", UrlVoService.getAgentId() );
	            if ( UrlVoService.getAgentId() !== "" ) {
	                url += cfg.SLASH + UrlVoService.getAgentId();
	            }
	            return url;
	        };

			$scope.$broadcast("down.initialize", cfg.ID );
	    }
	]);
})();