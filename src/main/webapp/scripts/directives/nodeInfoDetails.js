'use strict';

pinpointApp.constant('nodeInfoDetailsConfig', {
    applicationStatisticsUrl: '/applicationStatistics.pinpoint',
    myColors: ["#2ca02c", "#3c81fa", "#f8c731", "#f69124", "#f53034"],
    maxTimeToShowLoadAsDefaultForUnknown: 60 * 60 * 12 // 12h
});

pinpointApp
    .directive('nodeInfoDetails', [ 'nodeInfoDetailsConfig', '$filter', '$timeout', 'isVisible',
        function (cfg, $filter, $timeout, isVisible) {
            return {
                restrict: 'EA',
                replace: true,
                templateUrl: 'views/nodeInfoDetails.html',
                link: function postLink(scope, element) {

                    // define private variables
                    var htServermapData, htLastNode, htUnknownResponseSummary, htUnknownLoad, htTargetRawData, htQuery;

                    // define private variables of methods
                    var reset, showDetailInformation, parseHistogramForNvd3, renderResponseSummary,
                        parseHistogramForD3, renderLoad, renderAllChartWhichIsVisible, hide, show;

                    /**
                     * reset
                     */
                    reset = function () {
                        htUnknownResponseSummary = {};
                        htUnknownLoad = {};
                        htTargetRawData = false;
                        htQuery = false;
                        scope.showNodeInfoDetails = false;
                        scope.node = false;
                        scope.unknownGroup = null;
                        scope.hosts = null;
                        scope.showServers = false;
                        scope.agents = null;
                        scope.showAgents = false;
                        scope.showNodeResponseSummaryForUnknown = false;
                        scope.showNodeResponseSummary = false;
                        scope.showNodeLoad = false;
                        scope.agentHistogram = false;
                        scope.nodeOrderBy = 'count';
                        scope.nodeOrderByNameClass = '';
                        scope.nodeOrderByCountClass = 'glyphicon-sort-by-order-alt';
                        scope.nodeOrderByDesc = true;
                        if (!scope.$$phase) {
                            scope.$digest();
                        }
                    };

                    /**
                     * show detail information
                     * @param node
                     */
                    showDetailInformation = function (node) {
                        scope.showNodeInfoDetails = true;
                        scope.node = node;
                        scope.unknownGroup = node.textArr;
                        scope.serverList = node.serverList;
                        scope.showServers = _.isEmpty(scope.serverList) ? false : true;
                        scope.agentHistogram = node.agentHistogram;

                        if (!node.targetRawData && node.category !== "UNKNOWN_GROUP") {
                            renderResponseSummary('.nodeInfoDetails .histogram svg', [
                                {
                                    'key': "Response Time Histogram",
                                    'values' : parseHistogramForNvd3(node.histogram)
                                }
                            ]);
                            scope.showNodeResponseSummary = true;
                            scope.showNodeLoad = true;
                            renderLoad('.nodeInfoDetails .timeSeriesHistogram svg', node.timeSeriesHistogram);

                            for (var key in node.agentHistogram) {
                                var className = $filter('applicationNameToClassName')(key);
                                renderResponseSummary('.nodeInfoDetails .agentHistogram_' + className +
                                    ' svg', parseHistogramForD3(node.agentHistogram[key]));
                            }
                            for (var key in node.agentTimeSeriesHistogram) {
                                var className = $filter('applicationNameToClassName')(key);
                                renderLoad('.nodeInfoDetails .agentTimeSeriesHistogram_' + className +
                                    ' svg', node.agentTimeSeriesHistogram[key]);
                            }
                        } else if (node.category === 'UNKNOWN_GROUP'){
                            htTargetRawData = node.targetRawData;
                            scope.showNodeResponseSummaryForUnknown = (scope.oNavbarVo.getPeriod() <= cfg.maxTimeToShowLoadAsDefaultForUnknown) ? false : true;

                            renderAllChartWhichIsVisible(node);

                            $timeout(function () {
                                element.find('[data-toggle="tooltip"]').tooltip('destroy').tooltip();
                            });
                        }
                        if (!scope.$$phase) {
                            scope.$digest();
                        }
                    };

                    /**
                     * render all chart which is visible
                     * @param node
                     */
                    renderAllChartWhichIsVisible = function (node) {
                        $timeout(function () {
                            for (var key in node.textArr) {
                                var applicationName = node.textArr[key].applicationName,
                                    className = $filter('applicationNameToClassName')(applicationName);
                                if (angular.isDefined(htUnknownResponseSummary[applicationName])) continue;
                                if (angular.isDefined(htUnknownLoad[applicationName])) continue;

                                if (scope.showNodeResponseSummaryForUnknown) {
                                    var elQuery = '.nodeInfoDetails .summaryCharts_' + className + ' .response-summary svg',
                                        el = angular.element(elQuery);
                                    var visible = isVisible(el.get(0));
                                    if (!visible) continue;
                                    htUnknownResponseSummary[applicationName] = true;
                                    renderResponseSummary(elQuery, parseHistogramForD3(node.targetRawData[applicationName].histogram));
                                } else {
                                    var elQuery = '.nodeInfoDetails .summaryCharts_' + className + ' .load svg',
                                        el = angular.element(elQuery);
                                    var visible = isVisible(el.get(0));
                                    if (!visible) continue;
                                    htUnknownLoad[applicationName] = true;
                                    renderLoad(elQuery, node.targetRawData[applicationName].timeSeriesHistogram);
                                }
                            }
                        });
                    };

                    /**
                     * parse histogram for d3.js
                     * @param histogram
                     */
                    parseHistogramForD3 = function (histogram) {
                        var histogramSummary = [
                            {
                                "key": "Response Time Histogram",
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
                     * hide
                     */
                    hide = function () {
                        element.hide();
                    };

                    /**
                     * show
                     */
                    show = function () {
                        element.show();
                    };

                    /**
                     * show node detail information of scope
                     * @param applicationName
                     */
                    scope.showNodeDetailInformation = function (applicationName) {
                        htLastNode = htTargetRawData[applicationName];
                        showDetailInformation(htLastNode);
                        scope.$emit('nodeInfoDetail.showDetailInformationClicked', htQuery, htLastNode);
                    };

                    /**
                     * render statics summary
                     * @param querySelector
                     * @param data
                     * @param clickEventName
                     */
                    renderResponseSummary = function (querySelector, data, clickEventName) {
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
                                return $filter('humanReadableNumberFormat')(d, 0);
                            });

                            chart.valueFormat(function (d) {
                                return $filter('number')(d);
    //                            return $filter('humanReadableNumberFormat')(d, 1, true);
                            });

                            chart.color(cfg.myColors);

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
                     * scope render node response summary
                     * @param applicationName
                     */
                    scope.renderNodeResponseSummary = function (applicationName) {
                        if (angular.isUndefined(htUnknownResponseSummary[applicationName])) {
                            htUnknownResponseSummary[applicationName] = true;
                            var className = $filter('applicationNameToClassName')(applicationName);

                            renderResponseSummary('.nodeInfoDetails .summaryCharts_' + className +
                            ' .response-summary svg', parseHistogramForD3(htLastNode.targetRawData[applicationName].histogram));
                        }
                    };

                    /**
                     * scope render node load
                     * @param applicationName
                     */
                    scope.renderNodeLoad = function (applicationName) {
                        if (angular.isUndefined(htUnknownLoad[applicationName])) {
                            htUnknownLoad[applicationName] = true;
                            var className = $filter('applicationNameToClassName')(applicationName);

                            renderLoad('.nodeInfoDetails .summaryCharts_' + className +
                                ' .load svg', htLastNode.targetRawData[applicationName].timeSeriesHistogram);
                        }
                    };

                    /**
                     * render time series histogram
                     * @param querySelector
                     * @param data
                     * @param clickEventName
                     */
                    renderLoad = function (querySelector, data, clickEventName) {
                        if (!scope.$$phase) {
                            scope.$digest();
                        }
                        nv.addGraph(function () {
                            angular.element(querySelector).empty();
                            var chart = nv.models.multiBarChart().x(function (d) {
                                return d[0];
                            }).y(function (d) {
                                    return d[1];
                                }).clipEdge(true).showControls(false).delay(0);

                            chart.stacked(true);

                            chart.xAxis.tickFormat(function (d) {
                                return d3.time.format('%H:%M')(new Date(d));
                            });

                            chart.yAxis.tickFormat(function (d) {
    //                            return $filter('humanReadableNumberFormat')(d, 0);
                                return $filter('number')(d);
                            });

                            chart.color(cfg.myColors);

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
                     * scope node search change
                     */
                    scope.nodeSearchChange = function () {
                        renderAllChartWhichIsVisible(htLastNode);
                    };

                    /**
                     * scope node order by name
                     */
                    scope.nodeOrderByName = function () {
                        if (scope.nodeOrderBy === 'applicationName') {
                            scope.nodeOrderByDesc = !scope.nodeOrderByDesc;
                            if (scope.nodeOrderByNameClass === 'glyphicon-sort-by-alphabet-alt') {
                                scope.nodeOrderByNameClass = 'glyphicon-sort-by-alphabet';
                            } else {
                                scope.nodeOrderByNameClass = 'glyphicon-sort-by-alphabet-alt';
                            }
                        } else {
                            scope.nodeOrderByNameClass = 'glyphicon-sort-by-alphabet-alt';
                            scope.nodeOrderByCountClass = '';
                            scope.nodeOrderByDesc = true;
                            scope.nodeOrderBy = 'applicationName';
                        }
                        renderAllChartWhichIsVisible(htLastNode);
                    };

                    /**
                     * scope node order by count
                     */
                    scope.nodeOrderByCount = function () {
                        if (scope.nodeOrderBy === 'count') {
                            scope.nodeOrderByDesc = !scope.nodeOrderByDesc;
                            if (scope.nodeOrderByCountClass === 'glyphicon-sort-by-order-alt') {
                                scope.nodeOrderByCountClass = 'glyphicon-sort-by-order';
                            } else {
                                scope.nodeOrderByCountClass = 'glyphicon-sort-by-order-alt';
                            }
                        } else {
                            scope.nodeOrderByCountClass = 'glyphicon-sort-by-order-alt';
                            scope.nodeOrderByNameClass = '';
                            scope.nodeOrderByDesc = true;
                            scope.nodeOrderBy = 'count';
                        }
                        renderAllChartWhichIsVisible(htLastNode);
                    };

                    /**
                     * show unknown node by
                     * @param nodeSearch
                     * @param node
                     * @returns {boolean}
                     */
                    scope.showUnknownNodeBy = function (nodeSearch, node) {
                        if (nodeSearch) {
                            if (node.applicationName.indexOf(nodeSearch) > -1 ||
                                node.count.toString().indexOf(nodeSearch) > -1) {
                                return true;
                            } else {
                                return false;
                            }
                        } else {
                            return true;
                        }
                    };

                    /**
                     * scope event on nodeInfoDetails.initialize
                     */
                    scope.$on('nodeInfoDetails.initialize', function (event, e, query, node, mapData, navbarVo) {
                        show();
                        if (angular.equals(htLastNode, node)) return;
                        reset();
                        htQuery = query;
                        htLastNode = node;
                        scope.oNavbarVo = navbarVo;
                        htServermapData = mapData;
                        showDetailInformation(node);
                    });

                    /**
                     * scope event on nodeInfoDetails.hide
                     */
                    scope.$on('nodeInfoDetails.hide', function (event) {
                        hide();
                    });

                    /**
                     * scope event on nodeInfoDetails.lazyRendering
                     */
                    scope.$on('nodeInfoDetails.lazyRendering', function (event, e) {
                        renderAllChartWhichIsVisible(htLastNode);
                    });

                }
            };
    }
]);
