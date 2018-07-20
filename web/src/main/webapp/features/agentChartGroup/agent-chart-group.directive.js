(function() {
	'use strict';
	/**
	 * (en)agentChartGroupDirective 
	 * @ko agentChartGroupDirective
	 * @group Directive
	 * @name agentChartGroupDirective
	 * @class
	 */	
	pinpointApp.constant("agentChartGroupConfig", {
	    POINTS_TIMESTAMP: 0,
	    POINTS_MIN: 1,
	    POINTS_MAX: 2,
	    POINTS_AVG: 3
	});
	
	pinpointApp.directive("agentChartGroupDirective", [ "agentChartGroupConfig", "$timeout", "AgentAjaxService", "CPULoadChartDaoService", "MemoryChartDaoService", "AnalyticsService",
	    function (cfg, $timeout, AgentAjaxService, CPULoadChartDaoService, MemoryChartDaoService, AnalyticsService) {
	    return {
	        restrict: "EA",
	        replace: true,
	        templateUrl: "features/agentChartGroup/agentChartGroup.html?v=" + G_BUILD_TIME,
	        scope: {
	            namespace: "@"
	        },
	        link: function postLink( scope, element ) {
	            scope.showChartGroup = false;
	
	            function initialize(query) {

	                scope.showChartGroup = true;
	
	                scope.$digest();

					AgentAjaxService.getJVMChartData(query, function (result) {
						showHeapChart(result);
						showPermGenChart(result);
					});
					AgentAjaxService.getCpuLoadChartData( query, function (result) {
						showCpuLoadChart(result);
					});
	                element.tabs({
	                    activate: function (event, ui) {
	                        var activatedTabText = ui.newTab.text();
	                        if (activatedTabText === "Heap") {
								AnalyticsService.send(AnalyticsService.CONST.MIXEDVIEW, AnalyticsService.CONST.CLK_HEAP);
								scope.$broadcast("agentInspectorChartDirective.resize.transaction-heap" );
	                        } else if (activatedTabText === "PermGen") {
								AnalyticsService.send(AnalyticsService.CONST.MIXEDVIEW, AnalyticsService.CONST.CLK_PERM_GEN);
								scope.$broadcast("agentInspectorChartDirective.resize.transaction-non-heap" );
	                        } else if (activatedTabText === "CpuLoad") {
								AnalyticsService.send(AnalyticsService.CONST.MIXEDVIEW, AnalyticsService.CONST.CLK_CPU_LOAD);
								scope.$broadcast("agentInspectorChartDirective.resize.transaction-cpu-load" );
	                        }
	                    }
	                });
	                element.tabs("paging");
	            }

				function showHeapChart( chartData ) {
					var refinedChartData = MemoryChartDaoService.parseHeapData( chartData );
					scope.$broadcast(
						"agentInspectorChartDirective.initAndRenderWithData.transaction-heap",
						refinedChartData,
						MemoryChartDaoService.getChartOptions( refinedChartData ),
						"100%",
						"100%"
					);
				}
				function showPermGenChart( chartData ) {
					var refinedChartData2 = MemoryChartDaoService.parseNonHeapData( chartData );
					scope.$broadcast(
						"agentInspectorChartDirective.initAndRenderWithData.transaction-non-heap",
						refinedChartData2,
						MemoryChartDaoService.getChartOptions( refinedChartData2 ),
						"100%",
						"100%"
					);
				}
				function showCpuLoadChart( chartData ) {
					var refinedChartData = CPULoadChartDaoService.parseData( chartData );
					scope.$broadcast(
						"agentInspectorChartDirective.initAndRenderWithData.transaction-cpu-load",
						refinedChartData,
						CPULoadChartDaoService.getChartOptions( refinedChartData ),
						"100%",
						"100%"
					);
				}
	            function resize() {
					scope.$broadcast( "agentInspectorChartDirective.resize.transaction-heap" );
					scope.$broadcast( "agentInspectorChartDirective.resize.transaction-non-heap" );
					scope.$broadcast( "agentInspectorChartDirective.resize.transaction-cpu-load" );
	            }
	            scope.$on("agentChartGroupDirective.initialize." + scope.namespace, function (event, query) {
	                initialize(query);
	            });
	            scope.$on("agentChartGroupDirective.showCursorAt." + scope.namespace, function (event, category) {
					scope.$broadcast( "agentInspectorChartDirective.showCursorAt", "", category );
	            });
	            scope.$on("agentChartGroupDirective.resize." + scope.namespace, function () {
	                resize();
	            });
	        }
	    };
	}]);
})();
