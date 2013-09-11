'use strict';

pinpointApp.constant('agentInfoConfig', {
    agentUrl: 'http://10.25.149.249:9996/agents?callback=JSON_CALLBACK&agentId='
});

pinpointApp
    .directive('agentInfo', [ 'agentInfoConfig', '$routeParams', '$http', '$timeout', function (cfg, $routeParams, $http, $timeout) {
        return {
            restrict: 'EA',
            replace: true,
            templateUrl: 'views/agentInfo.html',
            link: function postLink(scope, element, attrs) {

                scope.$on('agentList.agentChanged', function (event, agent) {
                    scope.agent = agent;
                    console.log('got agentList.agentChanged', agent);
                });

                return;

                var get_agent_stats = function (agent_id, callback) {
                    // FIXME collector Stat URL을 제공할 수 있는 API 필요.
                    // zookeeper에 공통 정보를 기록해두면 될 것 같음.
//                    var url = 'http://10.25.149.249:9996/agents?callback=JSON_CALLBACK&agentId=' + agent_id;
                    var url = cfg.agentUrl + agent_id;
                    var config = { cache: true };
                    $http.jsonp(url, config).success(function (data, status) {
                        callback(data);
                    }).error(function (data, status) {
                        console.log("error", data, status);
                    });
                }

                var refresh = function (interval) {
                    var timestamp = new Date().getTime();
                    var LIMIT_LINE_COUNT = true;
                    var MAX_LINE_COUNT = 100;
                    scope.metrics = [
                        { id: 'total', title: 'Memory.Total', span: 'span12', line: [
                            { id: 'jvm.memory.total.init', key: 'init', values: [] },
                            { id: 'jvm.memory.total.used', key: 'used', values: [] },
                            { id: 'jvm.memory.total.committed', key: 'committed', values: [] },
                            { id: 'jvm.memory.total.max', key: 'max', values: [] },
                            { id: 'jvm.gc.ConcurrentMarkSweep', key: 'CMS time', values: [], bar: true }
                        ] },
                        { id: 'heap', title: 'Memory.Heap', span: 'span6', line: [
                            { id: 'jvm.memory.heap.init', key: 'init', values: [] },
                            { id: 'jvm.memory.heap.used', key: 'used', values: [] },
                            { id: 'jvm.memory.heap.committed', key: 'committed', values: [] },
                            { id: 'jvm.memory.heap.max', key: 'max', values: [] }
                        ] },
                        { id: 'non_heap', title: 'Memory.Non-Heap', span: 'span6', line: [
                            { id: 'jvm.memory.non-heap.init', key: 'init', values: [] },
                            { id: 'jvm.memory.non-heap.used', key: 'used', values: [] },
                            { id: 'jvm.memory.non-heap.committed', key: 'committed', values: [] },
                            { id: 'jvm.memory.non-heap.max', key: 'max', values: [] }
                        ] }
                    ];
                    scope.metrics.forEach(function (each) {
                        for (var i = 0; i < each.line.length; ++i) {
                            for (var j = timestamp - (MAX_LINE_COUNT * 5000); j < timestamp; j += 5000) {
                                each.line[i].values.push({x: j, y: 0});
                            }
                        }
                    });

                    var refresher = $timeout(function do_refresh() {
                        get_agent_stats(scope.agent_id, function (data) {
                            timestamp = new Date().getTime();
                            scope.agent_stats = data;
                            scope.timestamp = d3.time.format('%x %X')(new Date(timestamp));

                            scope.metrics.forEach(function (each) {
                                // update the data
                                for (var i = 0; i < each.line.length; ++i) {
                                    // cut off the first of line graph
                                    if (LIMIT_LINE_COUNT && each.line[i].values.length > MAX_LINE_COUNT) {
                                        each.line[i].values.shift();
                                    }
                                    if (each.line[i].bar) {
                                        // bar chart
                                        var time = data[each.line[i].id + ".time"].value;
                                        var prev_time = each.line[i].prev_time;
                                        if (prev_time && time - prev_time > 0) {
                                            each.line[i].values.push({x: timestamp, y: time - prev_time});
                                        } else {
                                            each.line[i].values.push({x: timestamp, y: 0});
                                        }
                                        each.line[i].prev_time = time;
                                    } else {
                                        // line chart
                                        each.line[i].values.push({x: timestamp, y: data[each.line[i].id].value});
                                    }
                                }
                                // FIXME update the graph... should be a directive.
                                nv.addGraph(function () {
                                    var chart = nv.models.linePlusBarChart();
                                    chart.x(function (d, i) {
                                        return i;
                                    });
                                    chart.xAxis.tickFormat(function (d) {
                                        var dx = each.line[0].values[d] && each.line[0].values[d].x || 0;
                                        return d3.time.format('%X')(new Date(dx));
                                    });
                                    chart.y1Axis.axisLabel('CMS elapsed (ms)').tickFormat(function (d) {
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
                        });
                        refresher = $timeout(do_refresh, interval);
                    });

                    // destroy the timer on close
                    //$scope.$on('$destroy', function(e) {
                    //	$timeout.cancel(refresher);
                    //});
                }

                scope.agent_id = $routeParams.agentId;
                scope.agent_stats = {};
                refresh(5000);
            }
        };
    }]);
