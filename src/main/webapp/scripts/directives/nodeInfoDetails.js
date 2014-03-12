'use strict';

pinpointApp.constant('nodeInfoDetailsConfig', {
    applicationStatisticsUrl: '/applicationStatistics.pinpoint',
    myColors: ["#2ca02c", "#3c81fa", "#f8c731", "#f69124", "#f53034"]
});

pinpointApp
    .directive('nodeInfoDetails', [ 'nodeInfoDetailsConfig', '$filter', function (config, $filter) {
        return {
            restrict: 'EA',
            replace: true,
            templateUrl: 'views/nodeInfoDetails.html',
            link: function postLink(scope, element, attrs) {

                // define private variables
                var htServermapData, htLastNode;

                // define private variables of methods
                var reset, showDetailInformation, parseHistogramForNvd3,
                    renderHistogram, parseHistogramForD3, renderTimeSeriesHistogram;

                /**
                 * reset
                 */
                reset = function () {
                    scope.showNodeInfoDetails = false;
                    scope.unknownGroup = null;
                    scope.hosts = null;
                    scope.showServers = false;
                    scope.agents = null;
                    scope.showAgents = false;
                    scope.showResponseSummary = false;
                    scope.showLoad = false;
                    scope.agentHistogram = false;
                    if (!scope.$$phase) {
                        scope.$digest();
                    }
                };

                /**
                 * show detail information
                 * @param query
                 * @param node
                 */
                showDetailInformation = function (query, node) {
                    scope.showNodeInfoDetails = true;

                    scope.unknownGroup = node.textArr;
                    scope.serverList = node.serverList;
                    scope.showServers = _.isEmpty(scope.serverList) ? false : true;
                    scope.isWas = node.isWas;
                    scope.agentHistogram = node.agentHistogram;

                    if (!node.targetRawData && /*node.category !== "USER" &&*/ node.category !== "UNKNOWN_GROUP") {
                        renderHistogram('.nodeInfoDetails .histogram svg', [
                            {
                                'key': "Response Time Histogram",
                                'values' : parseHistogramForNvd3(node.histogram)
                            }
                        ]);
                        scope.showResponseSummary = true;
                        if (node.isWas) {
                            renderTimeSeriesHistogram('.nodeInfoDetails .timeSeriesHistogram svg', node.timeSeriesHistogram);
                            scope.showLoad = true;

                            for (var key in node.agentHistogram) {
                                var className = $filter('applicationNameToClassName')(key);
                                renderHistogram('.nodeInfoDetails .agentHistogram_' + className +
                                    ' svg', parseHistogramForD3(node.agentHistogram[key]));
                            }
                            for (var key in node.agentTimeSeriesHistogram) {
                                var className = $filter('applicationNameToClassName')(key);
                                renderTimeSeriesHistogram('.nodeInfoDetails .agentTimeSeriesHistogram_' + className +
                                    ' svg', node.agentTimeSeriesHistogram[key]);
                            }
                        }
                    } else if (node.category === 'UNKNOWN_GROUP'){
                        for (var key in node.textArr) {
                            var className = $filter('applicationNameToClassName')(key);
                            renderHistogram('.nodeInfoDetails .summaryCharts_' + className +
                                ' svg', parseHistogramForD3(node.targetRawData[node.textArr[key].applicationName].histogram));
                        }
                    }
                    // scope.agents = data.agents;
                    // scope.showAgents = (scope.agents.length > 0) ? true : false;
                    if (!scope.$$phase) {
                        scope.$digest();
                    }
                };

                /**
                 * parse histogram for d3.js
                 * @param histogram
                 */
                parseHistogramForD3 = function (histogram) {
                    var histogramSummary = [
                        {
                            "key": "Responsetime Histogram",
                            "values": []
                        }
                    ];
                    for (var key in histogram) {
                        histogramSummary[0].values.push({
                            "label": key,
                            "value": histogram[key]
                        });
                    }
                    return histogramSummary;
                };

                /**
                 * render statics summary
                 * @param querySelector
                 * @param data
                 * @param clickEventName
                 */
                renderHistogram = function (querySelector, data, clickEventName) {
                    if (!scope.$$phase) {
                        scope.$digest();
                    }
                    nv.addGraph(function () {
                        angular.element(querySelector).empty();
                        var chart = nv.models.discreteBarChart().x(function (d) {
                            return d.label;
                        }).y(function (d) {
                                return d.value;
                            }).staggerLabels(false).tooltips(false).showValues(true);

                        chart.xAxis.tickFormat(function (d) {
                            // FIXME d로 넘어오는 값의 타입이 string이고 angular.isNumber는 "1000"에 대해 false를 반환함.
                            // if (angular.isNumber(d)) {
                            if (/^\d+$/.test(d)) {
                                if (d >= 1000) {
                                    return $filter('number')(d / 1000) + "s";
                                } else {
                                    return $filter('number')(d) + "ms";
                                }
                            } else if (d.charAt(d.length - 1) == '+') {
                                var v = d.substr(0, d.length - 1);
                                if (v >= 1000) {
                                    return $filter('number')(v / 1000) + "s+";
                                } else {
                                    return $filter('number')(v) + "ms+";
                                }
                            } else {
                                return d;
                            }
                        });

                        chart.yAxis.tickFormat(function (d, i) {
                            if (d >= 1000) {
                                return $filter('number')(Math.floor(d / 1000)) + "k";
                            } else {
                                return $filter('number')(d);
                            }
                        });

                        chart.valueFormat(function (d) {
                            return $filter('number')(d);
                        });

                        chart.color(config.myColors);

                        chart.discretebar.dispatch.on('elementClick', function (e) {
                            if (clickEventName) {
                            }
                        });

                        d3.select(querySelector)
                            .datum(data)
                            .transition()
                            .duration(0)
                            .call(chart);

                        nv.utils.windowResize(chart.update);

                        return chart;
                    });
                };


                /**
                 * render time series histogram
                 * @param querySelector
                 * @param data
                 * @param clickEventName
                 */
                renderTimeSeriesHistogram = function (querySelector, data, clickEventName) {
                    if (!scope.$$phase) {
                        scope.$digest();
                    }
                    nv.addGraph(function () {
                        angular.element(querySelector).empty();
                        var chart = nv.models.multiBarChart().x(function (d) {
                            return d[0];
                        }).y(function (d) {
                                return d[1];
                            }).clipEdge(true).showControls(false);

                        chart.stacked(true);

                        chart.xAxis.tickFormat(function (d) {
                            return d3.time.format('%H:%M')(new Date(d));
                        });

                        chart.yAxis.tickFormat(function (d) {
                            return d;
                        });

                        chart.color(config.myColors);

                        chart.multibar.dispatch.on('elementClick', function (e) {
                            if (clickEventName) {
                            }
                        });

                        d3.select(querySelector)
                            .datum(data)
                            .transition()
                            .duration(0)
                            .call(chart);

                        nv.utils.windowResize(chart.update);

                        return chart;
                    });
                };

                /**
                 * parse histogram for nvd3
                 * @param histogram
                 * @returns {Array}
                 */
                parseHistogramForNvd3 = function (histogram) {
                    var parsedHistogram = [];
                    angular.forEach(histogram, function (val, key) {
                        parsedHistogram.push({
                            label: key,
                            value: val
                        })
                    });
                    return parsedHistogram;
                };

                /**
                 * scope event on nodeInfoDetails.initialize
                 */
                scope.$on('nodeInfoDetails.initialize', function (event, e, query, node, mapData, navbarVo) {
                    reset();
                    htLastNode = node;
                    scope.oNavbarVo = navbarVo;
                    htServermapData = mapData;
                    showDetailInformation(query, node);
                });

                /**
                 * scope event on nodeInfoDetails.reset
                 */
                scope.$on('nodeInfoDetails.reset', function (event) {
                    reset();
                });


            }
        };
    }]);
