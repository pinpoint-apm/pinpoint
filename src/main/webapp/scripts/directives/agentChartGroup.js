'use strict';

pinpointApp.constant('agentChartGroupConfig', {
    POINTS_TIMESTAMP: 0,
    POINTS_MIN: 1,
    POINTS_MAX: 2,
    POINTS_AVG: 3
});

pinpointApp.directive('agentChartGroup', [ 'agentChartGroupConfig', '$timeout', 'AgentDao', function (cfg, $timeout, AgentDao) {
    return {
        restrict: 'EA',
        replace: true,
        templateUrl: 'views/agentChartGroup.html',
        scope: {
            namespace: '@'
        },
        link: function postLink(scope, element, attrs) {

            // define private variables
            var htChartCache, htLastAgentStat;

            // define private variables of methods
            var initialize, showHeapChart, parseAgentStat, showPermGenChart;

            scope.showChartGroup = false;

            initialize = function (query) {
                htChartCache = {
                    'Heap': false,
                    'PermGen': false
                };
                htLastAgentStat = null;
                scope.showChartGroup = true;

                scope.$digest();

                AgentDao.getAgentStat(query, function (err, result) {
                    if (err) {
                        console.log('error', err);
                        return;
                    }
                    if (htChartCache.Heap === false) {
                        showHeapChart(result);
                    }
                    htLastAgentStat = result;
                });
                element.tabs({
                    activate: function (event, ui) {
//                        console.log(ui.newTab.text(), ui.newTab.index());
                        if (ui.newTab.text() === 'PermGen' && htChartCache.PermGen === false) {
                            showPermGenChart(htLastAgentStat);
                        }
                    }
                });
                element.tabs('paging');
            };

            parseAgentStat = function (agentData, agentStat) {
                agentData.line.forEach(function (line) {
                    if (line.bar) {
                        // bar chart
                        var pointsTime = agentStat.charts.jvmGcOldTime.points;
                        var pointsCount = agentStat.charts.jvmGcOldCount.points;

                        if (pointsTime.length !== pointsCount.length) {
                            console.log('assertion error', 'time.length != count.length');
                            return;
                        }

                        for (var i = pointsCount.length - 1; i >= 0; --i) {
                            var timestamp = pointsTime[i][cfg.POINTS_TIMESTAMP];
                            var currTime = pointsTime[i][cfg.POINTS_MAX];
                            var currCount = pointsCount[i][cfg.POINTS_MAX];
                            var prevTime = line.prevTime;
                            var prevCount = line.prevCount;

                            if (!line.prevTime || !line.prevCount) {
                                line.values.push({x: timestamp, y: 0});
                                line.prevTime = currTime;
                                line.prevCount = currCount;
                            } else {
                                if ((currCount - prevCount > 0) && (currTime - prevTime > 0)) {
                                    line.values.push({x: timestamp, y: currTime - prevTime});
                                    line.prevTime = currTime;
                                    line.prevCount = currCount;
                                } else {
                                    line.values.push({x: timestamp, y: 0});
                                }
                            }
                        }
                    } else {
                        // line chart
                        var points = agentStat.charts[line.id].points;
                        for (var j = points.length - 1; j >= 0; --j) {
                            line.values.push({x: points[j][cfg.POINTS_TIMESTAMP], y: points[j][cfg.POINTS_MAX]});
                        }
                    }
                });
                return agentData;
            };

            showHeapChart = function (agentStat) {
                htChartCache['Heap'] = true;
                var heap = { id: 'heap', title: 'Heap', span: 'span12', line: [
                    { id: 'jvmMemoryHeapUsed', key: 'used', values: [] },
                    { id: 'jvmMemoryHeapMax', key: 'max', values: [] },
                    { id: 'gc', key: 'GC', values: [], bar: true }
                ]};

                heap = parseAgentStat(heap, agentStat);

                var oLinePlusBarChartVo = {
                    height: '150px',
                    y1AxisLabel: 'GC elapsed time (ms)',
                    margin: {top: 0, right: 60, bottom: 20, left: 70},
                    datum: heap.line
                };
                scope.$broadcast('linePlusBarChart.initialize.Heap_' + scope.namespace, oLinePlusBarChartVo);
            };

            showPermGenChart = function (agentStat) {
                htChartCache['PermGen'] = true;
                var nonheap = { id: 'nonheap', title: 'PermGen', span: 'span12', line: [
                    { id: 'jvmMemoryNonHeapUsed', key: 'used', values: [] },
                    { id: 'jvmMemoryNonHeapMax', key: 'max', values: [] },
                    { id: 'gc', key: 'GC', values: [], bar: true }
                ]};

                nonheap = parseAgentStat(nonheap, agentStat);

                var oLinePlusBarChartVo = {
                    height: '150px',
                    y1AxisLabel: 'GC elapsed time (ms)',
                    margin: {top: 0, right: 60, bottom: 20, left: 70},
                    datum: nonheap.line
                };
                scope.$broadcast('linePlusBarChart.initialize.PermGen_' + scope.namespace, oLinePlusBarChartVo);
            };

            scope.$on('agentChartGroup.initialize.' + scope.namespace, function (event, query) {
                initialize(query);
            });
        }
    };
}]);
