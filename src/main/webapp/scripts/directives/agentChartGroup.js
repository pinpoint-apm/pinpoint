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

            /**
             * initialize
             * @param query
             */
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


            /**
             * show heap chart
             * @param agentStat
             */
            showHeapChart = function (agentStat) {
                htChartCache.Heap = true;
                var heap = { id: 'heap', title: 'Heap', span: 'span12', line: [
                    { id: 'jvmMemoryHeapUsed', key: 'Used', values: [], isFgc: false },
                    { id: 'jvmMemoryHeapMax', key: 'Max', values: [], isFgc: false },
                    { id: 'fgc', key: 'FGC', values: [], isFgc: true }
                ]};

                scope.$broadcast('jvmMemoryChart.initAndRenderWithData.forHeap_' + scope.namespace, AgentDao.parseChartDataForAmcharts(heap, agentStat), '100%', '100%');
            };

            /**
             * show perm gen chart
             * @param agentStat
             */
            showPermGenChart = function (agentStat) {
                htChartCache.PermGen = true;
                var nonheap = { id: 'nonheap', title: 'PermGen', span: 'span12', line: [
                    { id: 'jvmMemoryNonHeapUsed', key: 'Used', values: [], isFgc: false },
                    { id: 'jvmMemoryNonHeapMax', key: 'Max', values: [], isFgc: false },
                    { id: 'fgc', key: 'FGC', values: [], isFgc: true }
                ]};

                scope.$broadcast('jvmMemoryChart.initAndRenderWithData.forNonHeap_' + scope.namespace, AgentDao.parseChartDataForAmcharts(nonheap, agentStat), '100%', '100%');
            };

            /**
             * scope event on agentChartGroup.initialize.namespace
             */
            scope.$on('agentChartGroup.initialize.' + scope.namespace, function (event, query) {
                initialize(query);
            });
        }
    };
}]);
