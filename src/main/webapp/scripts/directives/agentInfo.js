'use strict';

pinpointApp.constant('agentInfoConfig', {
    agentStatUrl: '/getAgentStat.pinpoint'
});

pinpointApp.directive('agentInfo', [ 'agentInfoConfig', '$routeParams', '$http', '$timeout', function (cfg, $routeParams, $http, $timeout) {
    return {
        restrict: 'EA',
        replace: true,
        templateUrl: 'views/agentInfo.html',
        link: function postLink(scope, element, attrs) {

            // define private variables
            var oNavbarDao;

            // define private variables of methods
            var getSampleRate, getAgentStat, showAgentStat, d3MakeGcCharts;

            // initialize
            scope.agentInfoTemplate = 'views/agentInfoReady.html';

            // TODO this is dummy
            scope.info = [
                { key: 'Application Type', val: 'Tomcat' },
                { key: 'JVM Version', val: '1.6.0_32' },
                { key: 'JVM Options', val: '' }
            ];

            /**
             * scope event of agentInfo.initialize
             */
            scope.$on('agentInfo.initialize', function (event, navbarDao, agent) {
                scope.agentInfoTemplate = 'views/agentInfoMain.html';
                scope.agent = agent;
                oNavbarDao = navbarDao;

                scope.info = [
                    { key: 'Agent Id', val: agent.agentId },
                    { key: 'Application Name', val: agent.applicationName },
                    { key: 'Hostname', val: agent.hostname },
                    { key: 'IP', val: agent.ip },
                    { key: 'Service Type', val: agent.serviceType },
                    { key: 'PID', val: agent.pid },
                    { key: 'Agent Version', val: agent.version }
                ];

                showAgentStat(agent.agentId, oNavbarDao.getQueryStartTime(), oNavbarDao.getQueryEndTime(), oNavbarDao.getPeriod());
                scope.$apply();
            });

            /**
             * calculate a sampling rate based on the given period
             * @param period in minutes
             */
            getSampleRate = function (period) {
                var MAX_POINTS = 100;
                var points = (period * 60) / 5;
                var rate = Math.floor(points / MAX_POINTS);
                return points <= MAX_POINTS ? 1 : rate;
            };

            /**
             jvmMemoryPoolsCodeCacheUsage
             jvmMemoryPoolsPSEdenSpaceUsage
             jvmMemoryPoolsPSOldGenUsage
             jvmMemoryPoolsPSPermGenUsage
             jvmMemoryPoolsPSSurvivorSpaceUsage
             */
            d3MakeGcCharts = function (agentStat, cb) {
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
                            var key;
                            if ('serial' === agentStat.type) {
                                key = 'jvmGcMarkSweepCompact';
                            } else if ('parallel' === agentStat.type) {
                                key = 'jvmGcPSMarkSweep';
                            } else if ('cms' === agentStat.type) {
                                key = 'jvmGcCms';
                            } else if ('g1' === agentStat.type) {
                                key = 'jvmGcG1OldGeneration';
                            }
                            if (key) {
                                var pointsTime = agentStat.charts[key + 'Time'].points;
                                var pointsCount = agentStat.charts[key + 'Count'].points;

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
             * get agent stat
             * @param query
             * @param cb
             */
            getAgentStat = function (query, cb) {
                jQuery.ajax({
                    type: 'GET',
                    url: cfg.agentStatUrl,
                    cache: false,
                    dataType: 'json',
                    data: query,
                    success: function (result) {
                        cb(result);
                    },
                    error: function (xhr, status, error) {
                        console.log("ERROR", status, error);
                    }
                });
            };

            /**
             * show agent stat
             * @param agentId
             * @param from
             * @param to
             */
            showAgentStat = function (agentId, from, to, period) {
                var query = {
                    agentId: agentId,
                    from: from,
                    to: to,
                    sampleRate: getSampleRate(period)
                };

                getAgentStat(query, function (result) {
                    scope.agentStat = result;
                    if (result.type) {
                        scope.info.push({key: 'JVM GC Type', val: result.type});
                    }
                    d3MakeGcCharts(result, function () {
                    });
                    scope.$digest();
                });
            };
        }
    };
}]);
