(function() {
	'use strict';
	pinpointApp.constant( "agentListDirectiveConfig", {
		ID: "AGENT_LIST_DRTV_",
		AGENT_STATE_INFO: {
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
		}
	});
	pinpointApp.directive( "agentListDirective", [ "agentListDirectiveConfig", "CommonUtilService", "UrlVoService", "AgentAjaxService", "PreferenceService", "TooltipService", "AnalyticsService", function ( cfg, CommonUtilService, UrlVoService, AgentAjaxService, PreferenceService, TooltipService, AnalyticsService ) {
	    return {
	        restrict: 'EA',
	        replace: true,
	        templateUrl: 'features/agentList/agentList.html?v=' + G_BUILD_TIME,
	        link: function postLink( scope ) {
				cfg.ID += CommonUtilService.getRandomNum();

				init();
				function init() {
					TooltipService.init( "agentList" );
					scope.agentGroup = [];
					scope.currentAgent = {};
					showAgentGroup( false );
				}
				function showAgentGroup( bApplicationChange ) {
					AgentAjaxService.getAgentList( {
						application: UrlVoService.getApplicationName(),
						from: UrlVoService.getQueryStartTime(),
						to: UrlVoService.getQueryEndTime()
					}, function( result ) {
						if ( result.errorCode || result.status ) {

						} else {
							scope.agentGroup = result;
							if ( bApplicationChange ) {
								scope.$emit( "up.changed.agent", cfg.ID, { agentId: "" }, true );
							} else {
								changeAgent(findAgentByAgentId(UrlVoService.getAgentId()), true);
							}
						}
					});
	            }
	            function findAgentByAgentId( agentId ) {
	                for ( var key in scope.agentGroup ) {
	                    for ( var innerKey in scope.agentGroup[ key ] ) {
	                        if ( scope.agentGroup[ key ][ innerKey ].agentId === agentId ) {
	                            return scope.agentGroup[ key ][ innerKey ];
	                        }
	                    }
	                }
	                return false;
	            }
				function changeAgent( agent, bInvokedByTop ) {
					AnalyticsService.send( AnalyticsService.CONST.INSPECTOR, AnalyticsService.CONST.CLK_CHANGE_AGENT_INSPECTOR );
					scope.currentAgent = agent;
					scope.$emit( "up.changed.agent", cfg.ID, agent, bInvokedByTop );
				}
				scope.selectAgent = function ( agent ) {
					if ( scope.currentAgent === agent ) {
						return;
					}
					changeAgent( agent, false );
	            };
				scope.getState = function( stateCode ) {
					return cfg.AGENT_STATE_INFO.sign[ stateCode + "" ];
				};
				scope.getStateColor = function( stateCode ) {
					return cfg.AGENT_STATE_INFO.color[ stateCode + "" ];
				};
				// scope.$on( "down.initialize", function( event, invokerId ) {
					// if ( cfg.ID === invokerId ) return;
					// scope.agentGroup = [];
					// scope.currentAgent = {};
					// showAgentGroup( false );
	            // });
				scope.$on( "down.changed.application", function( event, invokerId ) {
					if ( cfg.ID === invokerId ) return;
					showAgentGroup( true );
				});
				scope.$on( "down.changed.period", function( event, invokerId ) {
					if ( cfg.ID === invokerId ) return;
					showAgentGroup( false );
				});
			}
	    };
	}]);
})();