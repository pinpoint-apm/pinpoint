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
                var getSampleRate, showAgentStat, d3MakeGcCharts;

                // initialize
                scope.agentInfoTemplate = 'views/agentInfoReady.html';
                oAlert = new Alerts();
                oProgressBar = new ProgressBar();

                // TODO this is dummy
                scope.info = [
                    { key: 'Application Type', val: 'Tomcat' },
                    { key: 'JVM Version', val: '1.6.0_32' },
                    { key: 'JVM Options', val: '' }
                ];

                /**
                 * scope event of agentInfo.initialize
                 */
                scope.$on('agentInfo.initialize', function (event, navbarVo, agent) {
                    scope.agentInfoTemplate = 'views/agentInfoMain.html';
                    scope.agent = agent;
                    oNavbarVo = navbarVo;
                    scope.memoryGroup = null;

                    scope.info = [
                        { key: 'Agent Id', val: agent.agentId },
                        { key: 'Application Name', val: agent.applicationName },
                        { key: 'Hostname', val: agent.hostname },
                        { key: 'IP', val: agent.ip },
                        { key: 'Service Type', val: agent.serviceType },
                        { key: 'PID', val: agent.pid },
                        { key: 'Agent Version', val: agent.version }
                    ];

                    $timeout(function () {
                        showAgentStat(agent.agentId, oNavbarVo.getQueryStartTime(), oNavbarVo.getQueryEndTime(), oNavbarVo.getPeriod());
                        scope.$apply();
                    });
                });

                d3MakeGcCharts = function (agentStat) {
                    var total = { id: 'total', title: 'Total (Heap + PermGen)', span: 'span12', line: [
                        { id: 'jvmMemoryTotalUsed', key: 'used', values: [] },
                        { id: 'jvmMemoryTotalMax', key: 'max', values: [] },
                        { id: 'gc', key: 'GC', values: [], bar: true }
                    ]};

                    var heap = { id: 'heap', title: 'Heap', span: 'span12', line: [
                        { id: 'jvmMemoryHeapUsed', key: 'used', values: [] },
                        { id: 'jvmMemoryHeapMax', key: 'max', values: [] },
                        { id: 'gc', key: 'GC', values: [], bar: true }
                    ]};

                    var nonheap = { id: 'nonheap', title: 'PermGen', span: 'span12', line: [
                        { id: 'jvmMemoryNonHeapUsed', key: 'used', values: [] },
                        { id: 'jvmMemoryNonHeapMax', key: 'max', values: [] },
                        { id: 'gc', key: 'GC', values: [], bar: true }
                    ]};

                    var result = [ heap, nonheap ];
                    scope.memoryGroup = result;

                    var POINTS_TIMESTAMP = 0;
                    var POINTS_MIN = 1;
                    var POINTS_MAX = 2;
                    var POINTS_AVG = 3;

                    result.forEach(function (each) {
                        each.line.forEach(function (line) {
                            if (line.bar) {
                                // bar chart
                                var pointsTime = agentStat.charts['jvmGcOldTime'].points;
                                var pointsCount = agentStat.charts['jvmGcOldCount'].points;

                                if (pointsTime.length !== pointsCount.length) {
                                    console.log('assertion error', 'time.length != count.length');
                                    return;
                                }

                                for (var i = pointsCount.length - 1; i >= 0; --i) {
                                    var timestamp = pointsTime[i][POINTS_TIMESTAMP];
                                    var currTime = pointsTime[i][POINTS_MAX];
                                    var currCount = pointsCount[i][POINTS_MAX];
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
                                    line.values.push({x: points[j][POINTS_TIMESTAMP], y: points[j][POINTS_MAX]});
                                }
                            }
                        });

                        // draw a chart
                        nv.addGraph(function () {
                            var chart = nv.models.linePlusBarChart();
                            chart.x(function (d, i) {
                                return i;
                            });
                            chart.xAxis.tickFormat(function (d) {
                                var dx = each.line[0].values[d] && each.line[0].values[d].x || 0;
                                return d3.time.format('%X')(new Date(dx));
                            });
                            chart.y1Axis.axisLabel('GC elapsed time (ms)').tickFormat(function (d) {
                                return d;
                            });
                            chart.y2Axis.tickFormat(function (d) {
                                var sizes = [' B', 'KB', 'MB', 'GB', 'TB'];
                                var posttxt = 0;
                                var precision = 2;
                                if (d == 0) return '0';
                                while (d >= 1024) {
                                    posttxt++;
                                    d = d / 1024;
                                }
                                return parseInt(d).toFixed(precision) + " " + sizes[posttxt];
                            });
                            chart.bars.forceY([0]);
                            chart.lines.forceY([0]);
                            chart.margin({top: 30, right: 100, bottom: 50, left: 100})
                            d3.select('#line_' + each.id).datum(each.line).transition().duration(100).call(chart);
                            d3.select("#circle").attr("stroke-width", "1px");
                            nv.utils.windowResize(chart.update);
                            return chart;
                        });
                    });
                };

                /**
                 * show agent stat
                 * @param agentId
                 * @param from
                 * @param to
                 */
                showAgentStat = function (agentId, from, to, period) {
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
                        if (result.type) {
                            scope.info.push({key: 'JVM GC Type', val: result.type});
                            oProgressBar.setLoading(80);
                            d3MakeGcCharts(result);
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
            }
        };
    }]);
