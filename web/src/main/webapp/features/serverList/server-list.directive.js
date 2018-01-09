(function( $ ) {
	'use strict';
	/**
	 * (en)serverListDirective 
	 * @ko serverListDirective
	 * @group Directive
	 * @name serverListDirective
	 * @class
	 */
	pinpointApp.directive( "serverListDirective", [ "$timeout", "$window", "AnalyticsService",
		function ( $timeout, $window, analyticsService ) {
            return {
                restrict: "EA",
				replace: true,
				templateUrl: "features/serverList/serverList.html?v=" + G_BUILD_TIME,
				scope: {
					namespace: "@"
				},
                link: function(scope, element) {
					var bVisible = false;
					var bInitialized = false;
					var $element = $(element);
					var oChartYMax;
					scope.hasScatter = false;
					scope.selectedAgent = "";
					function showLayer() {
						$element.animate({
							"right": 421
						}, 500, function() {});
					}
					function showChart( instanceName, histogram, timeSeriesHistogram ) {
						scope.selectedAgent = instanceName;
						scope.$broadcast('changedCurrentAgent.forServerList', instanceName );
						if ( bInitialized ) {
							scope.$broadcast('responseTimeSummaryChartDirective.updateData.forServerList', histogram, oChartYMax["responseSummaryChart"]);
							scope.$broadcast('loadChartDirective.updateData.forServerList', timeSeriesHistogram, oChartYMax["loadChart"]);
						} else {
							scope.$broadcast('responseTimeSummaryChartDirective.initAndRenderWithData.forServerList', histogram, oChartYMax["responseSummaryChart"], '100%', '150px', false, true);
							scope.$broadcast('loadChartDirective.initAndRenderWithData.forServerList', timeSeriesHistogram, oChartYMax["loadChart"], '100%', '220px', false, true);
							bInitialized = true;
						}
					}
					function setData(bIsNodeServer, node, serverHistogramData, oNavbarVoService) {
						scope.node = node;
						scope.serverHistogramData = serverHistogramData;
						scope.oNavbarVoService = oNavbarVoService;
						scope.hasScatter = false;
						if ( serverHistogramData ) {
							scope.serverList = serverHistogramData.serverList;
							if (node.isWas) {
								scope.hasScatter = true;
								if (scope.namespace === "forMain") {
									scope.$broadcast('scatterDirective.initializeWithNode.forServerList', node);
								} else {
									scope.$broadcast('scatterDirective.showByNode.forServerList', node);
								}
							}
							$timeout(function () {
								var instanceName = $element.find("._node input[type=radio][checked]").val();
								try {
									showChart(instanceName, serverHistogramData.agentHistogram[instanceName], serverHistogramData.agentTimeSeriesHistogram[instanceName]);
								} catch (e) {}
							});
						} else {
							// 일단 이전 버젼용
							scope.serverList = node.serverList;
							if (node.isWas) {
								scope.hasScatter = true;
								if (scope.namespace === "forMain") {
									scope.$broadcast('scatterDirective.initializeWithNode.forServerList', node);
								} else {
									scope.$broadcast('scatterDirective.showByNode.forServerList', node);
								}
							}
							$timeout(function () {
								var instanceName = $element.find("._node input[type=radio][checked]").val();
								try {
									showChart(instanceName, scope.node.agentHistogram[instanceName], scope.node.agentTimeSeriesHistogram[instanceName]);
								} catch (e) {}
							});
						}
					}
					scope.isWasNode = function() {
						return scope.node && scope.node.isWas;
					};
					scope.hideLayer = function( delay ) {
						delay = delay || 100;
						$element.animate({
							"right": -386
						}, delay, function() {
							bVisible = false;
						});
					};
					scope.hasError = function( instanceName ) {
						var instance = scope.serverHistogramData.agentHistogram[instanceName];
						return (instance && instance.Error && instance.Error > 0 ) ? "red": "";
					};
					scope.openInspector = function( $event, instanceName ) {
						$event.preventDefault();
						analyticsService.send( analyticsService.CONST.MAIN, analyticsService.CONST.CLK_OPEN_INSPECTOR );
						$window.open( "#/inspector/" + ( scope.node.applicationName || scope.node.filterApplicationName ) + "@" + ( scope.node.serviceType || "" ) + "/" + scope.oNavbarVoService.getReadablePeriod() + "/" + scope.oNavbarVoService.getQueryEndDateTime() + "/" + instanceName );
					};
					scope.selectServer = function( instanceName ) {
						if ( scope.serverHistogramData ) {
							showChart( instanceName, scope.serverHistogramData.agentHistogram[instanceName], scope.serverHistogramData.agentTimeSeriesHistogram[instanceName] );
						} else {
							showChart( instanceName, scope.node.agentHistogram[instanceName], scope.node.agentTimeSeriesHistogram[instanceName] );
						}
					};
					scope.$on('serverListDirective.initialize', function ( event, oNavbarVoService ) {
						scope.node = null;
						scope.oNavbarVoService = null;
						scope.selectedAgent = "";
						scope.hasScatter = false;
						scope.$broadcast('scatterDirective.initialize.forServerList', oNavbarVoService);
						scope.hideLayer( 0 );
					});
					scope.$on('serverListDirective.show', function ( event, bIsNodeServer, node, serverHistogramData, chartMax, oNavbarVoService ) {
						if ( bVisible === true ) {
							scope.hideLayer();
							return;
						}
						bVisible = true;
						oChartYMax = chartMax;
						if ( angular.isUndefined( scope.node ) || scope.node === null || ( scope.node.key !== node.key ) ) {
							setData(bIsNodeServer, node, serverHistogramData, oNavbarVoService);
						}
						showLayer();
					});
					scope.$on('serverListDirective.setData', function ( event, bIsNodeServer, node, serverHistogramData, oNavbarVoService ) {
						if ( angular.isUndefined( scope.node ) || scope.node === null || ( scope.node.key !== node.key ) ) {
							setData(bIsNodeServer, node, serverHistogramData, oNavbarVoService);
						}
					});
                }
            };
	    }
	]);
})( jQuery );