(function( $ ) {
	'use strict';
	/**
	 * (en)serverListDirective 
	 * @ko serverListDirective
	 * @group Directive
	 * @name serverListDirective
	 * @class
	 */
	pinpointApp.directive( "serverListDirective", [ "$timeout", "$window", "AnalyticsService", "TooltipService",
		function ( $timeout, $window, analyticsService, tooltipService ) {
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
					scope.bIsNode = true;
					scope.hasScatter = false;
					scope.selectedAgent = "";
					/*

					tooltipService.init( "serverList" );
					*/
					function showLayer() {
						$element.animate({
							"right": 421
						}, 500, function() {});
					}
					function showChart( instanceName, histogram, timeSeriesHistogram ) {
						scope.selectedAgent = instanceName;
						scope.$broadcast('changedCurrentAgent.forServerList', instanceName );
						if ( bInitialized ) {
							scope.$broadcast('responseTimeSummaryChartDirective.updateData.forServerList', histogram);
							scope.$broadcast('loadChartDirective.updateData.forServerList', timeSeriesHistogram);
						} else {
							scope.$broadcast('responseTimeSummaryChartDirective.initAndRenderWithData.forServerList', histogram, '100%', '150px', false, true);
							scope.$broadcast('loadChartDirective.initAndRenderWithData.forServerList', timeSeriesHistogram, '100%', '220px', false, true);
							bInitialized = true;
						}
					}
					function setData(bIsNodeServer, node, oNavbarVoService) {
						scope.bIsNode = bIsNodeServer;
						scope.node = node;
						scope.oNavbarVoService = oNavbarVoService;
						scope.hasScatter = false;
						if ( bIsNodeServer ) {
							scope.bIsNode = true;
							scope.serverList = node.serverList;
							if ( node.isWas ) {
								scope.hasScatter = true;
								if  ( scope.namespace === "forMain" ) {
									scope.$broadcast('scatterDirective.initializeWithNode.forServerList', node);
								} else {
									scope.$broadcast('scatterDirective.showByNode.forServerList', node);
								}
							}
							$timeout(function() {
								var instanceName = $element.find( "._node input[type=radio][checked]" ).val();
								try {
									showChart(instanceName, scope.node.agentHistogram[instanceName], scope.node.agentTimeSeriesHistogram[instanceName]);
								}catch(e) {}
							});
						} else {
							scope.linkList = scope.node.sourceHistogram;
							scope.bIsNode = false;

							$timeout(function () {
								var instanceName = $element.find("._link input[type=radio][checked]").val();
								showChart( instanceName, scope.node.sourceHistogram[instanceName], scope.node.sourceTimeSeriesHistogram[instanceName]);
							});
						}
					}
					scope.hideLayer = function( delay ) {
						delay = delay || 100;
						$element.animate({
							"right": -386
						}, delay, function() {
							bVisible = false;
						});
					};
					scope.hasError = function( instance ) {
						return (instance && instance.Error && instance.Error > 0 ) ? "red": "";
					};
					scope.openInspector = function( $event, instanceName ) {
						$event.preventDefault();
						analyticsService.send( analyticsService.CONST.MAIN, analyticsService.CONST.CLK_OPEN_INSPECTOR );
						$window.open( "#/inspector/" + ( scope.node.applicationName || scope.node.filterApplicationName ) + "@" + ( scope.node.serviceType || "" ) + "/" + scope.oNavbarVoService.getReadablePeriod() + "/" + scope.oNavbarVoService.getQueryEndDateTime() + "/" + instanceName );
					};
					scope.selectServer = function( instanceName ) {
						if ( scope.bIsNode ) {
							showChart( instanceName, scope.node.agentHistogram[instanceName], scope.node.agentTimeSeriesHistogram[instanceName] );
						} else {
							showChart( instanceName, scope.node.sourceHistogram[instanceName], scope.node.sourceTimeSeriesHistogram[instanceName] );
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
					scope.$on('serverListDirective.show', function ( event, bIsNodeServer, node, oNavbarVoService ) {
						if ( bVisible === true ) {
							scope.hideLayer();
							return;
						}
						bVisible = true;
						if ( angular.isUndefined( scope.node ) || scope.node === null || ( scope.node.key !== node.key ) ) {
							setData(bIsNodeServer, node, oNavbarVoService);
						}
						showLayer();
					});
					scope.$on('serverListDirective.setData', function ( event, bIsNodeServer, node, oNavbarVoService ) {
						if ( angular.isUndefined( scope.node ) || scope.node === null || ( scope.node.key !== node.key ) ) {
							setData(bIsNodeServer, node, oNavbarVoService);
						}
					});
                }
            };
	    }
	]);
})( jQuery );