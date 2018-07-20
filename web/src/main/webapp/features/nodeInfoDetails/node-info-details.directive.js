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
					SystemConfigService.getConfig().then(function(config) {
						scope.securyGuideUrl = config["securityGuideUrl"];
					});
					var currentAgentName = PreferenceService.getAgentAllStr();
					var oChartYMax = {};
					var bRequesting = false;
					var nextFrom = -1;
					var nextTo = -1;
					var DEFAULT_PERIOD = 1000 * 60 * 5;
					var REAL_TIME_INTERVAL = 2000;
					var refTriggerTimeout;
					var htLastTarget;
                    var agentHistogramData = null;
                    var oNavBarVoService = null;

                    function renderResponseSummary(namespace, histogram, yMax, w, h) {
                        scope.$broadcast("responseTimeSummaryChartDirective.initAndRenderWithData." + namespace, histogram, yMax, w, h, false, true);
                    }
					function renderLoad(namespace, timeSeriesHistogram, yMax, w, h, useChartCursor) {
                        scope.$broadcast("loadChartDirective.initAndRenderWithData." + namespace, timeSeriesHistogram, yMax, w, h, useChartCursor);
					}
					function updateResponseSummary(namespace, histogram, yMax) {
						scope.$broadcast( "responseTimeSummaryChartDirective.updateData." + namespace, histogram, yMax );
					}
					function updateLoad(namespace, timeSeriesHistogram, yMax) {
						scope.$broadcast("loadChartDirective.updateData." + namespace, timeSeriesHistogram, yMax);
					}

					function identifyTarget(target) {
						scope.isGroup = ( target["unknownNodeGroup"] || target["unknownLinkGroup"] ) ? true : false;
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
								updateResponseSummary(scope.namespace, responseSummaryData, oChartYMax["responseSummaryChart"]);
								updateLoad(scope.namespace, loadData, oChartYMax["loadChart"]);
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
								renderResponseSummary(scope.namespace, target["histogram"], null, "100%", "150px");
								renderLoad(scope.namespace, target["timeSeriesHistogram"], null, "100%", "220px", true);
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
                    scope.$on("changedCurrentAgent." + scope.namespace, function(event, agentName, chartYMax) {
						currentAgentName = agentName;
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
								renderResponseSummary(scope.namespace, responseSummaryData, chartYMax["responseSummaryChart"], "100%", "150px");
								renderLoad(scope.namespace, loadData, chartYMax["loadChart"], "100%", "220px", true);
							});
						}
					});
					scope.$on("nodeInfoDetailsDirective.loadRealTimeChartData", function (event, applicationName, agentName, from, to ) {
						releaseTimeout();
						loadRealTimeAgentHistogram(agentName, from, to, function() {});
					});
					scope.$on("loadChartDirective.saveMax." + scope.namespace, function (event, max ) {
						if ( currentAgentName === PreferenceService.getAgentAllStr() ) {
							oChartYMax["loadChart"] = max;
						}
					});
					scope.$on("responseTimeSummaryChartDirective.saveMax." + scope.namespace, function (event, max ) {
						if ( currentAgentName === PreferenceService.getAgentAllStr() ) {
							oChartYMax["responseSummaryChart"] = max;
						}
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