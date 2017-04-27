(function() {
	"use strict";
	pinpointApp.filter( "startFrom", function () {
		return function( node, start ) {
			if ( !node || !node.length ) { return; }
			return node.slice( start );
		};
	});

	pinpointApp.constant("nodeInfoDetailsDirectiveConfig", {
	    maxTimeToShowLoadAsDefaultForUnknown: 60 * 60 * 12 // 12h
	});
	
	pinpointApp.directive("nodeInfoDetailsDirective", [ "nodeInfoDetailsDirectiveConfig", "$filter", "$timeout", "isVisibleService", "SystemConfigurationService", "PreferenceService", "TooltipService", "CommonAjaxService",
        function (cfg, $filter, $timeout, isVisibleService, SystemConfigService, PreferenceService, TooltipService, CommonAjaxService ) {
            return {
				scope: {},
				replace: true,
                restrict: "EA",
                templateUrl: "features/nodeInfoDetails/nodeInfoDetails.html?v=" + G_BUILD_TIME,
                link: function postLink(scope, element) {

                    var htServerMapData;
                    var htLastNode;
                    var bRequesting = false;

                    function reset() {
						scope.currentAgent = PreferenceService.getAgentAllStr();
                        scope.showNodeInfoDetails = false;
                        scope.node = false;
                        scope.showAgents = false;
                        scope.showNodeResponseSummary = false;
                        scope.showNodeLoad = false;
                        scope.agentHistogram = false;

                        if (!(scope.$$phase == "$apply" || scope.$$phase == "$digest") ) {
                        	if (!(scope.$root.$$phase == "$apply" || scope.$root.$$phase == "$digest") ) {
                        		scope.$digest();
                        	}
                        }
                    }
                    function showDetailInformation(node) {
                        scope.showNodeInfoDetails = true;
                        scope.node = node;
                        scope.agentHistogram = node.agentHistogram;
						scope.showNodeResponseSummary = true;
						scope.showNodeLoad = true;

						renderResponseSummary("forNode", node.applicationName, node.histogram, "100%", "150px");
						renderLoad("forNode", node.applicationName, node.timeSeriesHistogram, "100%", "220px", true);

                        if (!(scope.$$phase == "$apply" || scope.$$phase == "$digest") ) {
                        	if (!(scope.$root.$$phase == "$apply" || scope.$root.$$phase == "$digest") ) {
                        		scope.$digest();
                        	}
                        }
                    }
                    function renderResponseSummary(namespace, toApplicationName, histogram, w, h) {
                        var className = $filter("applicationNameToClassName")(toApplicationName);
						namespace = namespace || "forNode_" + className;
                        scope.$broadcast("responseTimeSummaryChartDirective.initAndRenderWithData." + namespace, histogram, w, h, false, true);
                    }
					function updateResponseSummary(namespace, toApplicationName, histogram, w, h) {
						var className = $filter("applicationNameToClassName")(toApplicationName);
						namespace = namespace || "forNode_" + className;
						scope.$broadcast( "responseTimeSummaryChartDirective.updateData." + namespace, histogram );
					}
                    function renderLoad(namespace, toApplicationName, timeSeriesHistogram, w, h, useChartCursor) {
                        var className = $filter("applicationNameToClassName")(toApplicationName);
						namespace = namespace || "forNode_" + className;
                        scope.$broadcast("loadChartDirective.initAndRenderWithData." + namespace, timeSeriesHistogram, w, h, useChartCursor);
                    }
					function updateLoad(namespace, toApplicationName, timeSeriesHistogram, w, h, useChartCursor) {
						var className = $filter("applicationNameToClassName")(toApplicationName);
						namespace = namespace || "forNode_" + className;
						scope.$broadcast("loadChartDirective.updateData." + namespace, timeSeriesHistogram);
					}
                    function hide() {
                        element.hide();
                    }
                    function show() {
                        element.show();
                    }
					function mergeSummaryData( oData ) {
						var oSummarySum = PreferenceService.getResponseTypeFormat();
						$.each( oData, function (agentName, oValue ) {
							$.each(oValue, function (innerKey, value) {
								oSummarySum[innerKey] += value;
							});
						});
						return oSummarySum;
					}
					function mergeLoadData( oData ) {
						var aLoadSum = [];
						$.each( oData, function (agentName, aData) {
							for (var i = 0; i < aData.length; i++) {
								var aSet = aData[i];
								if (aLoadSum.length < i + 1) {
									aLoadSum[i] = {
										"key": aSet.key,
										"values": []
									};
								}
								for (var j = 0; j < aSet.values.length; j++) {
									if (aLoadSum[i].values.length < j + 1) {
										aLoadSum[i].values[j] = [
											aSet.values[j][0], 0
										];
									}
									aLoadSum[i].values[j][1] += aSet.values[j][1];
								}
							}
						});
						return aLoadSum;
					}
					scope.isNotAuthorized = function() {
						return scope.isAuthorized === false;
					};
					scope.getAuthGuideUrl = function() {
						return SystemConfigService.get("securityGuideUrl");
					};
                    scope.$on("nodeInfoDetailsDirective.initialize", function (event, node, mapData, navbarVoService, reloadOnly, searchQuery) {
                    	if ( node["unknownNodeGroup"] && node["unknownNodeGroup"].length > 0 ) {
                    		scope.showNodeInfoDetails = false;
						} else {
							show();
							reset();
							htLastNode = node;
							scope.isAuthorized = node.isAuthorized === false ? false : true;
							scope.oNavbarVoService = navbarVoService;
							htServerMapData = mapData;

							showDetailInformation(node);
						}
                    });
                    scope.$on("nodeInfoDetailsDirective.hide", function () {
                        hide();
                    });
                    scope.$on("responseTimeSummaryChartDirective.itemClicked.forNode", function (event, data) {
//                        console.log("on responseTimeSummaryChartDirective.itemClicked.forNode", data);
                    });
					scope.$on("responseTimeSummaryChartDirective.loadRealtime", function (event, applicationName, agentName, from, to ) {
						if ( angular.isUndefined( scope.node ) || ( scope.node.applicationName !== applicationName ) ) {
							return;
						}
						if ( bRequesting === false ) {
							bRequesting = true;
							CommonAjaxService.getResponseTimeHistogramData( {
								"applicationName": scope.node.applicationName,
								"serviceTypeName": scope.node.category,
								"from": from,
								"to": to
							}, function (oResult) {
								if (agentName === PreferenceService.getAgentAllStr()) {
									updateResponseSummary("forNode", scope.node.applicationName, mergeSummaryData( oResult.summary ) );
									updateLoad("forNode", scope.node.applicationName, mergeLoadData( oResult.timeSeries ) );
								} else {
									updateResponseSummary("forNode", scope.node.applicationName, oResult.summary[agentName]);
									updateLoad("forNode", scope.node.applicationName, oResult.timeSeries[agentName]);
								}
								bRequesting = false;
							}, function() {
								bRequesting = false;
							});
						}
					});
					scope.$on("changedCurrentAgent.forMain", function( event, agentName ) {
						var responseSummaryData = null;
						var loadData = null;
						if ( agentName === PreferenceService.getAgentAllStr() ) {
							responseSummaryData = scope.node.histogram;
							loadData = scope.node.timeSeriesHistogram;
						} else {
							responseSummaryData = scope.node.agentHistogram[agentName];
							loadData = scope.node.agentTimeSeriesHistogram[agentName];
						}
						renderResponseSummary("forNode", scope.node.applicationName, responseSummaryData, "100%", "150px");
						renderLoad("forNode", scope.node.applicationName, loadData, "100%", "220px", true);
					});

					TooltipService.init( "responseSummaryChart" );
					TooltipService.init( "loadChart" );
                }
            };
	    }
	]);
})();