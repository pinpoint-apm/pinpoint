(function() {
	"use strict";

	pinpointApp.directive("nodeInfoDetailsDirective", [ "$timeout", "SystemConfigurationService", "PreferenceService", "UrlVoService", "AgentHistogramDaoService", "TooltipService",
        function ( $timeout, SystemConfigService, PreferenceService, UrlVoService, AgentHistogramDaoService, TooltipService) {
            return {
				replace: true,
                restrict: "EA",
                templateUrl: "features/nodeInfoDetails/nodeInfoDetails.html?v=" + G_BUILD_TIME,
				scope: {
					namespace: "@"
				},
                link: function postLink(scope, element) {
					scope.isWas = false;
					scope.isGroup = false;
					scope.isAuthorized = true;
					var bRequesting = false;
					var nextFrom = -1;
					var nextTo = -1;
					var DEFAULT_PERIOD = 1000 * 60 * 5;
					var REAL_TIME_INTERVAL = 2000;
					var refTriggerTimeout;
					var htLastTarget;
                    var agentHistogramData = null;
                    var oNavBarVoService = null;

                    function renderResponseSummary(namespace, histogram, w, h) {
                        scope.$broadcast("responseTimeSummaryChartDirective.initAndRenderWithData." + namespace, histogram, w, h, false, true);
                    }
					function renderLoad(namespace, timeSeriesHistogram, w, h, useChartCursor) {
                        scope.$broadcast("loadChartDirective.initAndRenderWithData." + namespace, timeSeriesHistogram, w, h, useChartCursor);
					}
					function updateResponseSummary(namespace, histogram) {
						scope.$broadcast( "responseTimeSummaryChartDirective.updateData." + namespace, histogram );
					}
					function updateLoad(namespace, timeSeriesHistogram) {
						scope.$broadcast("loadChartDirective.updateData." + namespace, timeSeriesHistogram);
					}

					function identifyTarget(target) {
						scope.isGroup = ( target["unknownNodeGroup"] || target["unknownLinkGroup"] );
						scope.isWas = angular.isUndefined( target["isWas"] ) ? false : target["isWas"];
						if ( target["from"] && target["to"] ) {
							scope.isNode = false;
						} else {
							scope.isNode = true;
							scope.isAuthorized = target["isAuthorized"] === false ? false : true;
						}
						htLastTarget = target;
					}
					function loadAgentHistogram( callback, from, to ) {
						if ( agentHistogramData ) {
							callback( agentHistogramData );
						} else {
							AgentHistogramDaoService.loadAgentHistogram(
								htLastTarget, oNavBarVoService, function(result, data) {
									if ( result === "success" ) {
										agentHistogramData = data;
										callback( data );
									}
								},
								from, to
							);
						}
					}
					function loadRealTimeAgentHistogram(agentName, from, to, callback ) {
						if ( bRequesting === false ) {
							bRequesting = true;
							agentHistogramData = null;
							var reqTime = Date.now();
							loadAgentHistogram(function (histogramData) {
								var responseSummaryData = null;
								var loadData = null;
								if ( agentName === PreferenceService.getAgentAllStr() ) {
									responseSummaryData = histogramData.histogram;
									loadData = histogramData.timeSeriesHistogram;
								} else {
									responseSummaryData = histogramData["agentHistogram"][agentName];
									loadData = histogramData["agentTimeSeriesHistogram"][agentName];
								}
								updateResponseSummary("forMain", responseSummaryData);
								updateLoad("forMain", loadData);
								setNextFromTo( histogramData["currentServerTime"] );
								bRequesting = false;
								callback(Date.now() - reqTime);
							}, from, to);
						}
					}
					function setNextFromTo( currentServerTime ) {
						nextTo = currentServerTime;
						nextFrom = nextTo - DEFAULT_PERIOD;
					}
					function startInnerRealTimeTrigger(delay) {
						refTriggerTimeout = setTimeout(function() {
							loadRealTimeAgentHistogram( PreferenceService.getAgentAllStr(), nextFrom, nextTo, function( reqTime ) {
								startInnerRealTimeTrigger(Math.max(0, REAL_TIME_INTERVAL - reqTime) );
							});
						}, delay);
					}
					function releaseTimeout() {
						if ( refTriggerTimeout ) {
							clearTimeout( refTriggerTimeout );
						}
					}
					scope.isNotAuthorized = function() {
						return scope.isAuthorized === false;
					};
					scope.getAuthGuideUrl = function() {
						return SystemConfigService.get("securityGuideUrl");
					};
                    scope.$on("nodeInfoDetailsDirective.initialize", function (event, target, oNavBarVoServiceArgu, bSendChartRequest) {
						identifyTarget(target);
						oNavBarVoService = oNavBarVoServiceArgu;
						agentHistogramData = null;
						bRequesting = false;
						releaseTimeout();
						if ( scope.isGroup ) {
							scope.showNodeInfoDetails = false;
						} else {
							if ( UrlVoService.isRealtime() && scope.isWas === false ) {
								startInnerRealTimeTrigger(0);
							} else {
								// if (scope.isNode) {
								// 	renderResponseSummary("forMain", target["histogram"], "100%", "150px");
								// 	renderLoad("forMain", target["timeSeriesHistogram"], "100%", "220px", true);
								// } else {
									renderResponseSummary("forMain", target["histogram"], "100%", "150px");
									renderLoad("forMain", target["timeSeriesHistogram"], "100%", "220px", true);
								// }

							}
							scope.showNodeInfoDetails = true;
						}
						if (!(scope.$$phase == "$apply" || scope.$$phase == "$digest") ) {
							if (!(scope.$root.$$phase == "$apply" || scope.$root.$$phase == "$digest") ) {
								scope.$digest();
							}
						}
                    });
                    scope.$on("nodeInfoDetailsDirective.hide", function () {
                    	scope.showNodeInfoDetails = false;
                    });
                    scope.$on("changedCurrentAgent." + scope.namespace, function(event, agentName) {
						if ( oNavBarVoService.isRealtime() === false ) {
							loadAgentHistogram(function (histogramData) {
								var responseSummaryData = null;
								var loadData = null;
								if ( agentName === PreferenceService.getAgentAllStr() ) {
									responseSummaryData = htLastTarget.histogram;
									loadData = htLastTarget.timeSeriesHistogram;
								} else {
									responseSummaryData = histogramData["agentHistogram"][agentName];
									loadData = histogramData["agentTimeSeriesHistogram"][agentName];
								}
								renderResponseSummary("forMain", responseSummaryData, "100%", "150px");
								renderLoad("forMain", loadData, "100%", "220px", true);
							});
						}
					});
					scope.$on("nodeInfoDetailsDirective.loadRealTimeChartData", function (event, applicationName, agentName, from, to ) {
						// if ( lastApplicationName !== applicationName ) {
						// 	return;
						// }
						releaseTimeout();
						loadRealTimeAgentHistogram(agentName, from, to, function() {});
					});

					scope.$on("responseTimeSummaryChartDirective.itemClicked." + scope.namespace, function (event, data) {
						// var label = data["responseTime"];
						// var values = data["count"];
						// var oServerMapFilterVoService = new ServerMapFilterVoService();
						// oServerMapFilterVoService
						// 	.setMainApplication(htLastLink.filterApplicationName)
						// 	.setMainServiceTypeCode(htLastLink.filterApplicationServiceTypeCode);
						//
						// if (htLastLink.sourceInfo.serviceType === "USER") {
						// 	oServerMapFilterVoService
						// 		.setFromApplication("USER")
						// 		.setFromServiceType("USER");
						// } else {
						// 	oServerMapFilterVoService
						// 		.setFromApplication(htLastLink.sourceInfo.applicationName)
						// 		.setFromServiceType(htLastLink.sourceInfo.serviceType);
						// }
						//
						// oServerMapFilterVoService
						// 	.setToApplication(htLastLink.targetInfo.applicationName)
						// 	.setToServiceType(htLastLink.targetInfo.serviceType);
						//
						// if (label.toLowerCase() === "error") {
						// 	oServerMapFilterVoService.setIncludeException(true);
						// } else if (label.toLowerCase() === "slow") {
						// 	oServerMapFilterVoService
						// 		.setResponseFrom(filteredMapUtilService.getStartValueForFilterByLabel(label, values) * 1000)
						// 		.setIncludeException(false)
						// 		.setResponseTo("max");
						// } else {
						// 	oServerMapFilterVoService
						// 		.setResponseFrom(filteredMapUtilService.getStartValueForFilterByLabel(label, values) * 1000)
						// 		.setIncludeException(false)
						// 		.setResponseTo(parseInt(label, 10) * 1000);
						// }
						//
						// var oServerMapHintVoService = new ServerMapHintVoService();
						// if (htLastLink.sourceInfo.isWas && htLastLink.targetInfo.isWas) {
						// 	oServerMapHintVoService.setHint(htLastLink.targetInfo.applicationName, htLastLink.filterTargetRpcList);
						// }
						// scope.$emit("linkInfoDetailsDirective.ResponseSummary.barClicked", oServerMapFilterVoService, oServerMapHintVoService);
					});
					TooltipService.init( "responseSummaryChart" );
					TooltipService.init( "loadChart" );
                }
            };
	    }
	]);
})();