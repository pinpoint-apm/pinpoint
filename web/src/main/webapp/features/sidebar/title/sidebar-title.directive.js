(function() {
	"use strict";
	/**
	 * (en)sidebarTitleDirective 
	 * @ko sidebarTitleDirective
	 * @group Directive
	 * @name sidebarTitleDirective
	 * @class
	 */
	pinpointApp.directive("sidebarTitleDirective", [ "$timeout", "$rootScope", "$window", "AgentHistogramDaoService", "UrlVoService", "PreferenceService", "AnalyticsService",
	    function ( $timeout, $rootScope, $window, AgentHistogramDaoService, UrlVoService, PreferenceService, AnalyticsService ) {
	        return {
	            restrict: "E",
	            replace: true,
	            templateUrl: "features/sidebar/title/sidebarTitle.html?v=" + G_BUILD_TIME,
	            scope: {
	                namespace: "@"
	            },
	            link: function poLink(scope) {
					var htLastTarget = null;
					var oNavbarVoService = null;
					var agentHistogramData = null;
					var oChartYMax = {};
					scope.agentList = [];
					scope.hasServerList = false;
					scope.serverCount = 0;
					scope.errorServerCount = 0;
					scope.isNode = true;
					scope.isGroup = false;

	                $timeout(function () {
	                    empty();
	                });
					function initializeAgentList( node ) {
						scope.currentAgent = PreferenceService.getAgentAllStr();
						if ( angular.isUndefined( node ) || node.isAuthorized === false ) {
							scope.agentList = [];
							return;
						}
						scope.agentList = node["agentIds"] ? node["agentIds"].sort() : [];
					}
					function getIconUrl( serviceType ) {
						return PreferenceService.getIconPath() + ( /.*\_GROUP/.test( serviceType ) ? serviceType.replace(/(.*)(\_GROUP)/, "$1") : serviceType ) + ".png";
					}
					function getNodeName( applicationName ) {
						return applicationName.replace( "_", " " );
					}
	
	                function initialize( target, navbarVoService ) {
						oNavbarVoService = navbarVoService;
						agentHistogramData = null;
						htLastTarget = target;

						if ( angular.isUndefined( target.unknownLinkGroup ) && angular.isUndefined( target.unknownNodeGroup ) ) {
							scope.isGroup = false;
						} else {
							scope.isGroup = true;
						}
						if ( target["from"] && target["to"] ) {
							scope.isNode = false;
							scope.isWas = false;
							scope.fromNodeIcon = getIconUrl(target.sourceInfo.serviceType);
							scope.fromNodeName = getNodeName(target.sourceInfo.applicationName);
							if ( scope.isGroup ) {
								scope.toNodeIcon = getIconUrl(target.toNode.serviceType);
								scope.toNodeName = getNodeName(target.toNode.serviceType);
							} else {
								scope.toNodeIcon = getIconUrl(target.targetInfo.serviceType);
								scope.toNodeName = getNodeName(target.targetInfo.applicationName);
							}
						} else {
							scope.isNode = true;
							scope.isWas = angular.isDefined( target.isWas ) ? target.isWas : false;
							scope.fromNodeIcon = getIconUrl(target.serviceType);
							if ( scope.isGroup ) {
								scope.fromNodeName = getNodeName(target.serviceType);
							} else {
								scope.fromNodeName = getNodeName(target.applicationName);
							}
						}
						checkServerData();
	                }
					function checkServerData() {
						scope.serverCount = 0;
						scope.errorServerCount = 0;
						if ( scope.isNode ) {
							if (angular.isUndefined(htLastTarget.isAuthorized) || htLastTarget.isAuthorized === false) {
								scope.hasServerList = false;
							} else {
								scope.hasServerList = ( htLastTarget["agentIds"] && htLastTarget["agentIds"].length > 0 ) ? true : false;
								scope.serverCount = htLastTarget["instanceCount"];
								scope.errorServerCount = htLastTarget["instanceErrorCount"];
							}
						} else {
							scope.hasServerList = false;
						}
					}
	                function empty() {
						scope.currentAgent = PreferenceService.getAgentAllStr();
						scope.hasServerList = false;
						scope.fromNodeIcon = "";
						scope.fromNodeName = "";
						scope.toNodeIcon = "";
						scope.toNodeName = "";
						scope.isWas = false;
						scope.isGroup = false;
						scope.agentList = [];
	                }
					function loadAgentHistogram( callback ) {
						if ( agentHistogramData ) {
							callback( agentHistogramData );
						} else {
							AgentHistogramDaoService.loadAgentHistogram(
								htLastTarget, oNavbarVoService, function(result, data) {
									if ( result === "success" ) {
										agentHistogramData = data;
										callback( data );
									}
								}
							);
						}
					}
					scope.isWasNode = function() {
						return scope.isNode && scope.isWas;
					};
					scope.changeAgent = function() {
						AnalyticsService.send( AnalyticsService.CONST.INSPECTOR, AnalyticsService.CONST.CLK_CHANGE_AGENT_MAIN );
						$rootScope.$broadcast("changedCurrentAgent." + scope.namespace, scope.currentAgent, oChartYMax);
					};
					scope.showServerList = function() {
						AnalyticsService.send( AnalyticsService.CONST.MAIN, AnalyticsService.CONST.CLK_SHOW_SERVER_LIST);
						loadAgentHistogram(function( histogramData ) {
							$rootScope.$broadcast("serverListDirective.show", scope.isNode, htLastTarget, histogramData, oChartYMax, oNavbarVoService);
						});
					};
					scope.openApplicationInspector = function() {
						$window.open( "#/inspector/" + ( htLastTarget.applicationName || htLastTarget.filterApplicationName ) + "@" + htLastTarget.serviceType + "/" + oNavbarVoService.getReadablePeriod() + "/" + oNavbarVoService.getQueryEndDateTime() );
					};
	                scope.$on("sidebarTitleDirective.initialize." + scope.namespace, function (event, target, navbarVoService) {
						initialize(target, navbarVoService);
						initializeAgentList(target);
	                });
	                scope.$on("sidebarTitleDirective.empty." + scope.namespace, function (event) {
	                    empty();
	                });
					scope.$on("loadChartDirective.saveMax." + scope.namespace, function (event, max ) {
						if ( scope.currentAgent === PreferenceService.getAgentAllStr() ) {
							oChartYMax["loadChart"] = max;
						}
					});
					scope.$on("responseTimeSummaryChartDirective.saveMax." + scope.namespace, function (event, max ) {
						if ( scope.currentAgent === PreferenceService.getAgentAllStr() ) {
							oChartYMax["responseSummaryChart"] = max;
						}
					});

				}
	        };
	    }]
	);
})();