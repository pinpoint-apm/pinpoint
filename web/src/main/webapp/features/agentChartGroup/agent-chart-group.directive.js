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
	
	pinpointApp.directive("agentChartGroupDirective", [ "agentChartGroupConfig", "$timeout", "AgentAjaxService", "AgentDaoService", "AnalyticsService",
	    function (cfg, $timeout, AgentAjaxService, AgentDaoService, AnalyticsService) {
	    return {
	        restrict: "EA",
	        replace: true,
	        templateUrl: "features/agentChartGroup/agentChartGroup.html?v=" + G_BUILD_TIME,
	        scope: {
	            namespace: "@"
	        },
	        link: function postLink( scope, element ) {
	            var htChartCache = {
					"Heap": false,
					"PermGen": false,
					"CpuLoad": false
				};
	            var htLastJvmChartData, htLastCpuLoadChartData;
	            scope.showChartGroup = false;
	
	            function initialize(query) {

	                htLastJvmChartData = null;
	                htLastCpuLoadChartData = null;
	                scope.showChartGroup = true;
	
	                scope.$digest();

					AgentAjaxService.getJVMChartData(query, function (result) {
						showHeapChart(result);
						htLastJvmChartData = result;
					});
					AgentAjaxService.getCpuLoadChartData( query, function (result) {
						showCpuLoadChart(result);
						htLastCpuLoadChartData = result;
					});

	                element.tabs({
	                    activate: function (event, ui) {
	                        var activatedTabText = ui.newTab.text();
	                        if (activatedTabText === "Heap") {
								AnalyticsService.send(AnalyticsService.CONST.MIXEDVIEW, AnalyticsService.CONST.CLK_HEAP);
	                            if (htChartCache.Heap === false) {
	                                showHeapChart(htLastJvmChartData);
	                            } else {
	                                scope.$broadcast("jvmMemoryChartDirective.resize.forHeap_" + scope.namespace);
	                            }
	                            return;
	                        } else if (activatedTabText === "PermGen") {
								AnalyticsService.send(AnalyticsService.CONST.MIXEDVIEW, AnalyticsService.CONST.CLK_PERM_GEN);
	                            if (htChartCache.PermGen === false) {
	                                showPermGenChart(htLastJvmChartData);
	                            } else {
	                                scope.$broadcast("jvmMemoryChartDirective.resize.forNonHeap_" + scope.namespace);
	                            }
	                            return;
	                        } else if (activatedTabText === "CpuLoad") {
								AnalyticsService.send(AnalyticsService.CONST.MIXEDVIEW, AnalyticsService.CONST.CLK_CPU_LOAD);
	                            if (htChartCache.CpuLoad === false) {
	                                showCpuLoadChart(htLastCpuLoadChartData);
	                            } else {
	                                scope.$broadcast("cpuLoadChartDirective.resize.forCpuLoad_" + scope.namespace);
	                            }
	                            return;
	                        }
	                    }
	                });
	                element.tabs('paging');
	            };

				function showHeapChart( chartData ) {
				    htChartCache.Heap = true;

					var heap = { id: 'heap', title: 'Heap', span: 'span12', line: [
						{ id: 'JVM_MEMORY_HEAP_USED', key: 'Used', values: [], isFgc: false },
						{ id: 'JVM_MEMORY_HEAP_MAX', key: 'Max', values: [], isFgc: false },
						{ id: 'fgc', key: 'FGC', values: [], isFgc: true }
					]};
					scope.$broadcast( "jvmMemoryChartDirective.initAndRenderWithData.forHeap_" + scope.namespace, AgentDaoService.parseMemoryChartDataForAmcharts(heap, chartData), '100%', '100%');
				}
				function showPermGenChart( chartData ) {
					htChartCache.PermGen = true;
					var nonheap = { id: 'nonheap', title: 'PermGen', span: 'span12', line: [
						{ id: 'JVM_MEMORY_NON_HEAP_USED', key: 'Used', values: [], isFgc: false },
						{ id: 'JVM_MEMORY_NON_HEAP_MAX', key: 'Max', values: [], isFgc: false },
						{ id: 'fgc', key: 'FGC', values: [], isFgc: true }
					]};
					scope.$broadcast( "jvmMemoryChartDirective.initAndRenderWithData.forNonHeap_" + scope.namespace, AgentDaoService.parseMemoryChartDataForAmcharts(nonheap, chartData), '100%', '100%');
				}
				function showCpuLoadChart( chartData ) {
					htChartCache.CpuLoad = true;
					var cpuLoad = {
						id: 'cpuLoad', title: 'JVM/System Cpu Usage',
						span: 'span12', isAvailable: false
					};
					scope.$broadcast("cpuLoadChartDirective.initAndRenderWithData.forCpuLoad_" + scope.namespace, AgentDaoService.parseCpuLoadChartDataForAmcharts(cpuLoad, chartData), "100%", "100%");
				}
	            function showCursorAt(category) {
	                if (htChartCache.Heap) {
	                    scope.$broadcast('jvmMemoryChartDirective.showCursorAt.forHeap_' + scope.namespace, category);
	                }
	                if (htChartCache.PermGen) {
	                    scope.$broadcast('jvmMemoryChartDirective.showCursorAt.forNonHeap_' + scope.namespace, category);
	                }
	                if (htChartCache.CpuLoad) {
	                    scope.$broadcast('cpuLoadChartDirective.showCursorAt.forCpuLoad_' + scope.namespace, category);
	                }
	            };

	            function resize() {
	                if (htChartCache.Heap) {
	                    scope.$broadcast('jvmMemoryChartDirective.resize.forHeap_' + scope.namespace);
	                }
	                if (htChartCache.PermGen) {
	                    scope.$broadcast('jvmMemoryChartDirective.resize.forNonHeap_' + scope.namespace);
	                }
	                if (htChartCache.CpuLoad) {
	                    scope.$broadcast('cpuLoadChartDirective.resize.forCpuLoad_' + scope.namespace);
	                }
	            };
	            scope.$on("agentChartGroupDirective.initialize." + scope.namespace, function (event, query) {
	                initialize(query);
	            });
	            scope.$on("agentChartGroupDirective.showCursorAt." + scope.namespace, function (event, category) {
	                showCursorAt(category);
	            });
	            scope.$on("agentChartGroupDirective.resize." + scope.namespace, function () {
	                resize();
	            });
	        }
	    };
	}]);
})();
