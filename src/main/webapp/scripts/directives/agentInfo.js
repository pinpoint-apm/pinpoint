'use strict';

pinpointApp.constant('agentInfoConfig', {
    agentStatUrl: '/getAgentStat.pinpoint'
});

pinpointApp.directive('agentInfo', [ 'agentInfoConfig', '$timeout', 'Alerts', 'ProgressBar', 'AgentDao',
    function (cfg, $timeout, Alerts, ProgressBar, AgentDao) {
        return {
            restrict: 'EA',
            replace: true,
            templateUrl: 'views/agentInfo.html',
            link: function postLink(scope, element, attrs) {

                // define private variables
                var oNavbarVo, oAlert, oProgressBar;

                // define private variables of methods
                var getAgentStat, showCharts, parseChartDataForAmcharts;

                // initialize
                scope.agentInfoTemplate = 'views/agentInfoReady.html';
                oAlert = new Alerts();
                oProgressBar = new ProgressBar();

                /**
                 * scope event of agentInfo.initialize
                 */
                scope.$on('agentInfo.initialize', function (event, navbarVo, agent) {
                    scope.agentInfoTemplate = 'views/agentInfoMain.html';
                    scope.agent = agent;
                    oNavbarVo = navbarVo;
                    scope.memoryGroup = null;

                    scope.info = {
                        'agentId': agent.agentId,
                        'applicationName': agent.applicationName,
                        'hostname': agent.hostname,
                        'ip': agent.ip,
                        'serviceType': agent.serviceType,
                        'pid': agent.pid,
                        'agentVersion': agent.version,
                        'jvmGcType': ''
                    };

                    $timeout(function () {
                        getAgentStat(agent.agentId, oNavbarVo.getQueryStartTime(), oNavbarVo.getQueryEndTime(), oNavbarVo.getPeriod());
                        scope.$apply();
                    });
                });

                /**
                 * show charts
                 * @param agentStat
                 */
                showCharts = function (agentStat) {
                    var total = { id: 'total', title: 'Total (Heap + PermGen)', span: 'span12', line: [
                        { id: 'jvmMemoryTotalUsed', key: 'Used', values: [], isFgc: false },
                        { id: 'jvmMemoryTotalMax', key: 'Max', values: [], isFgc: false },
                        { id: 'fgc', key: 'FGC', values: [], bar: true, isFgc: true }
                    ]};

                    var heap = { id: 'heap', title: 'Heap', span: 'span12', line: [
                        { id: 'jvmMemoryHeapUsed', key: 'Used', values: [], isFgc: false },
                        { id: 'jvmMemoryHeapMax', key: 'Max', values: [], isFgc: false },
                        { id: 'fgc', key: 'FGC', values: [], bar: true, isFgc: true }
                    ]};

                    var nonheap = { id: 'nonheap', title: 'PermGen', span: 'span12', line: [
                        { id: 'jvmMemoryNonHeapUsed', key: 'Used', values: [], isFgc: false },
                        { id: 'jvmMemoryNonHeapMax', key: 'Max', values: [], isFgc: false },
                        { id: 'fgc', key: 'FGC', values: [], bar: true, isFgc: true }
                    ]};

                    scope.memoryGroup = [ heap, nonheap ];

                    scope.$broadcast('jvmMemoryChart.initAndRenderWithData.forHeap', parseChartDataForAmcharts(heap, agentStat), '100%', '300px');
                    scope.$broadcast('jvmMemoryChart.initAndRenderWithData.forNonHeap', parseChartDataForAmcharts(nonheap, agentStat), '100%', '300px');
                };

                /**
                 * parse chart data for amcharts
                 * @param info
                 * @param agentStat
                 * @returns {Array}
                 */
                parseChartDataForAmcharts = function (info, agentStat) {
                    var newData = [],
                        POINTS_TIMESTAMP = 0, POINTS_MIN = 1, POINTS_MAX = 2, POINTS_AVG = 3,
                        pointsTime = agentStat.charts['jvmGcOldTime'].points,
                        pointsCount = agentStat.charts['jvmGcOldCount'].points;

                    if (pointsTime.length !== pointsCount.length) {
                        throw new Error('assertion error', 'time.length != count.length');
                        return;
                    }

                    var currTime, currCount, prevTime, prevCount; // for gc

                    for (var i = pointsCount.length - 1; i >= 0; --i) {
                        var thisData = {
                            time: new Date(pointsTime[i][POINTS_TIMESTAMP]).toString('yyyy-MM-dd HH:mm'),
                            Used: 0,
                            Max: 0,
                            GC: 0
                        };
                        for (var k in info.line) {
                            if (info.line[k].isFgc) {
                                var GC = 0;
                                currTime = pointsTime[i][POINTS_MAX];
                                currCount = pointsCount[i][POINTS_MAX];
                                if (!prevTime || !prevCount) {
                                    prevTime = currTime;
                                    prevCount = currCount;
                                } else {
                                    if ((currCount - prevCount > 0) && (currTime - prevTime > 0)) {
                                        GC = currTime - prevTime;
                                        prevTime = currTime;
                                        prevCount = currCount;
                                    }
                                }
                                thisData[info.line[k].key] = GC;
                            } else {
                                thisData[info.line[k].key] = agentStat.charts[info.line[k].id].points[i][POINTS_MAX]
                            }

                        }

                        newData.push(thisData);
                    }

                    return newData;
                };

                /**
                 * get agent stat
                 * @param agentId
                 * @param from
                 * @param to
                 */
                getAgentStat = function (agentId, from, to, period) {
                    oProgressBar.startLoading();
                    var query = {
                        agentId: agentId,
                        from: from,
                        to: to,
                        sampleRate: AgentDao.getSampleRate(period)
                    };
                    oProgressBar.setLoading(40);
                    AgentDao.getAgentStat(query, function (err, result) {
                        if (err) {
                            oProgressBar.stopLoading();
                            oAlert.showError('There is some error.');
                            return;
                        }
                        scope.agentStat = result;
                        if (angular.isDefined(result.type) && result.type) {
                            scope.info['jvmGcType'] =  result.type;
                            oProgressBar.setLoading(80);
                            showCharts(result);
                            $timeout(function () {
                                oProgressBar.setLoading(100);
                                oProgressBar.stopLoading();
                            }, 700);
                        } else {
                            oProgressBar.stopLoading();
                        }

                        scope.$digest();
                    });
                };

                /**
                 * scope event on jvmMemoryChart.cursorChanged.forHeap
                 */
                scope.$on('jvmMemoryChart.cursorChanged.forHeap', function (e, event) {
                    scope.$broadcast('jvmMemoryChart.showCursorAt.forNonHeap', event.index);
                });

                /**
                 * scope event on jvmMemoryChart.cursorChanged.forNonHeap
                 */
                scope.$on('jvmMemoryChart.cursorChanged.forNonHeap', function (e, event) {
                    scope.$broadcast('jvmMemoryChart.showCursorAt.forHeap', event.index);
                });
            }
        };
    }]);
