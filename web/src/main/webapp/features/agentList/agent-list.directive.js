(function() {
	'use strict';
	/**
	 * (en)agentListDirective 
	 * @ko agentListDirective
	 * @group Directive
	 * @name agentListDirective
	 * @class
	 */	
	pinpointApp.constant('agentListConfig', {
	});
	
	pinpointApp.directive('agentListDirective', [ 'agentListConfig', '$rootScope', 'AgentAjaxService', 'TooltipService', "AnalyticsService", function (cfg, $rootScope, agentAjaxService, tooltipService, analyticsService ) {
	    return {
	        restrict: 'EA',
	        replace: true,
	        templateUrl: 'features/agentList/agentList.html?v=' + G_BUILD_TIME,
	        link: function postLink(scope, element, attrs) {
				tooltipService.init( "agentList" );

				var oAgentState = {
					"sign": {
						"100": "ok-sign",
						"200": "minus-sign",
						"201": "minus-sign",
						"300": "remove-sign",
						"-1": "question-sign"
					},
					"color": {
						"100": "#40E340",
						"200": "#F00",
						"201": "#F00",
						"300": "#AAA",
						"-1": "#AAA"
					}
				};
	            var showAgentGroup = function (applicationName, serviceType, from, to, selectedAgentId) {
					agentAjaxService.getAgentList( {
						application: applicationName,
						from: from,
						to: to
					}, function( result ) {
						if ( result.errorCode || result.status ) {

						} else {
							scope.agentGroup = result;
							scope.select(findAgentByAgentId(selectedAgentId));
						}
					});
	            };
	            var findAgentByAgentId = function (agentId) {
	                for (var key in scope.agentGroup) {
	                    for (var innerKey in scope.agentGroup[key]) {
	                        if (scope.agentGroup[key][innerKey].agentId === agentId) {
	                            return scope.agentGroup[key][innerKey];
	                        }
	                    }
	                }
	                return false;
	            };
	
	            scope.select = function (agent) {
					analyticsService.send( analyticsService.CONST.INSPECTOR, analyticsService.CONST.CLK_CHANGE_AGENT_INSPECTOR );
	                scope.currentAgent = agent;
	                scope.$emit('agentListDirective.agentChanged', agent);
	            };
				scope.getState = function( stateCode ) {
					return oAgentState.sign[stateCode+""];
				};
				scope.getStateColor = function( stateCode ) {
					return oAgentState.color[stateCode+""];
				};
	            scope.$on('agentListDirective.initialize', function (event, navbarVoService) {
	                showAgentGroup(navbarVoService.getApplicationName(), navbarVoService.getServiceTypeName(), navbarVoService.getQueryStartTime(), navbarVoService.getQueryEndTime(), navbarVoService.getAgentId());
	            });
	        }
	    };
	}]);
})();