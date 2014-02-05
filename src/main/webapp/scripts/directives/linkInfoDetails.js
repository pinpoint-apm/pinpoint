'use strict';

pinpointApp.constant('linkInfoDetailsConfig', {
    linkStatisticsUrl: '/linkStatistics.pinpoint',
    myColors: ["#008000", "#4B72E3", "#A74EA7", "#BB5004", "#FF0000"]
});

pinpointApp.directive('linkInfoDetails', [ 'linkInfoDetailsConfig', 'HelixChartVo', '$filter', 'ServerMapFilterVo',  'filteredMapUtil',
    function (config, HelixChartVo, $filter, ServerMapFilterVo, filteredMapUtil) {
        return {
            restrict: 'EA',
            replace: true,
            templateUrl: 'views/linkInfoDetails.html',
            link: function postLink(scope, element, attrs) {

                // define private variables
                var htQuery;

                // define private variables of methods;
                var reset, showDetailInformation, getLinkStatisticsData, renderStatisticsTimeSeriesHistogram,
                    renderStatisticsSummary, showApplicationStatistics, parseHistogramForD3;

                /**
                 * reset
                 */
                reset = function () {
                    scope.showLinkInfoDetails = false;
                    scope.linkCategory = null;
//                scope.rawdata = null;
//                scope.query = null;
                    scope.targetinfo = null;
                    scope.sourceinfo = null;
                    scope.showLinkInfoChart = false;
                    scope.showLinkInfoBarChart = false;
                    if (!scope.$$phase) {
                        scope.$digest();
                    }
                };

                /**
                 * show detail information of scope
                 * @param applicationName
                 */
                scope.showDetailInformation = function (applicationName) {
                    var link = scope.rawdata[applicationName];
                    showDetailInformation(link);
                    scope.$emit('linkInfoDetail.showDetailInformationClicked', htQuery, link);
                };

                /**
                 * show detail information
                 * @param query
                 * @param data
                 */
                showDetailInformation = function (data) {
                    if (data.rawdata) {
                        scope.linkCategory = 'UnknownLinkInfoBox';
                        for (var key in data.targetinfo) {
                            renderStatisticsSummary('.linkInfoDetails .summaryCharts_' + data.targetinfo[key].sequence +
                                ' svg', parseHistogramForD3(data.rawdata[data.targetinfo[key].applicationName].histogram));
                        }
                    } else {
                        scope.linkCategory = 'LinkInfoBox';
                        showApplicationStatistics(
                            htQuery.from,
                            htQuery.to,
                            data.sourceinfo.serviceTypeCode,
                            data.sourceinfo.applicationName,
                            data.targetinfo.serviceTypeCode,
                            data.targetinfo.applicationName,
                            data.histogram
                        );
                    }

                    scope.rawdata = data.rawdata;
//                scope.query = data.query;
                    scope.targetinfo = data.targetinfo;
                    scope.sourceinfo = data.sourceinfo;
                    scope.showLinkInfoDetails = true;
                    if (!scope.$$phase) {
                        scope.$digest();
                    }
                };

                /**
                 * get link statistics data
                 * @param query
                 * @param version
                 * @param callback
                 */
                getLinkStatisticsData = function (query, version, callback) {
                    jQuery.ajax({
                        type: 'GET',
                        url: config.linkStatisticsUrl,
                        cache: false,
                        dataType: 'json',
                        data: {
                            from: query.from,
                            to: query.to,
                            srcServiceType: query.srcServiceType,
                            srcApplicationName: query.srcApplicationName,
                            destServiceType: query.destServiceType,
                            destApplicationName: query.destApplicationName,
                            v: version
                        },
                        success: function (result) {
                            callback(query, result);
                        },
                        error: function (xhr, status, error) {
                            console.log("ERROR", status, error);
                        }
                    });
                };

                /**
                 * render statistics timseries histogram
                 * @param data
                 */
                renderStatisticsTimeSeriesHistogram = function (data) {
                    nv.addGraph(function () {
                        angular.element('.linkInfoDetails .infoChart svg').empty();
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
//                        console.log('element: ' + e.value, data);
//                        console.dir(e.point);
                        });

                        d3.select('.linkInfoDetails .infoChart svg')
                            .datum(data)
                            .transition()
                            .duration(0)
                            .call(chart);

                        nv.utils.windowResize(chart.update);

                        return chart;
                    });
                };


                /**
                 * render statics summary
                 * @param querySelector
                 * @param data
                 * @param clickEventName
                 */
                renderStatisticsSummary = function (querySelector, data, clickEventName) {
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
//                                var filterDataSet = {
//                                    label: e.point.label,
//                                    value: e.value,
//                                    values: e.series.values,
//                                    srcServiceType: scope.sourceinfo.serviceType,
//                                    srcApplicationName: scope.sourceinfo.applicationName,
//                                    destServiceType: scope.targetinfo.serviceType,
//                                    destApplicationName: scope.targetinfo.applicationName
//                                };
                                var label = e.point.label,
                                    values = e.series.values;
                                var oServerMapFilterVo = new ServerMapFilterVo();
                                oServerMapFilterVo
                                    .setFromApplication(scope.sourceinfo.applicationName)
                                    .setFromServiceType(scope.sourceinfo.serviceType)
                                    .setToApplication(scope.targetinfo.applicationName)
                                    .setToServiceType(scope.targetinfo.serviceType);
                                if (label === 'error') {
                                    oServerMapFilterVo.setIncludeException(true);
                                } else if (label.indexOf('+') > 0) {
                                    oServerMapFilterVo
                                        .setResponseFrom(parseInt(label, 10))
                                        .setResponseTo('max');
                                } else {
                                    oServerMapFilterVo
                                        .setResponseFrom(filteredMapUtil.getStartValueForFilterByLabel(label, values))
                                        .setResponseTo(parseInt(label, 10));
                                }
                                scope.$emit('linkInfoDetails.' + clickEventName + '.barClicked', oServerMapFilterVo);
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
                 * show application statistics
                 * @param begin
                 * @param end
                 * @param srcServiceType
                 * @param srcApplicationName
                 * @param destServiceType
                 * @param destApplicationName
                 * @param histogram
                 */
                showApplicationStatistics = function (begin, end, srcServiceType, srcApplicationName, destServiceType,
                                                      destApplicationName, histogram) {
                    var params = {
                        "from": begin,
                        "to": end,
                        "srcServiceType": srcServiceType,
                        "srcApplicationName": srcApplicationName,
                        "destServiceType": destServiceType,
                        "destApplicationName": destApplicationName
                    };

                    scope.showLinkInfoChart = true;
                    scope.showLinkInfoBarChart = true;
                    renderStatisticsSummary('.linkInfoDetails .infoBarChart svg', parseHistogramForD3(histogram), 'ResponseSummary');

                    getLinkStatisticsData(params, 1, function (query, result) {
                        renderStatisticsTimeSeriesHistogram(result.timeseriesHistogram);
                    });

                    getLinkStatisticsData(params, 2, function (query, result) {
                        var oHelixChartVo = new HelixChartVo();
                        oHelixChartVo
                            .setType('stacked_line')
                            .setGroup('loadForLinkInfoDetails')
                            .setWidth(380)
                            .setHeight(200)
                            .setPadding([30, 90, 35, 50])
                            .setMargin([0, 0, 20, 0])
//                        .setXCount(30)
//                        .setXInterval(5)
//                        .setXTick('minutes')
//                        .setXTickInterval(1)
//                        .setXTickFormat('%H:%M')
                            .setYTicks(5)
                            .setLegend('1.0s,3.0s,5.0s,Slow,Failed')
                            .setQueryValue('1.0s,3.0s,5.0s,Slow,Failed')
//                        .setQueryInterval('5s')
                            .setQueryFrom(begin)
                            .setQueryTo(end)
                            .generateEverythingForChart()
                            .setData(result.timeseriesHistogram);
//                        .parseDataTimestampToDateInstance();
                        scope.$broadcast('helixChart.initialize.loadForLinkInfoDetails', oHelixChartVo);
                    });
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
                 * scope event on linkInfoDetails.reset
                 */
                scope.$on('linkInfoDetails.reset', function (event) {
                    reset();
                });

                /**
                 * scope event on linkInfoDetails.linkClicked
                 */
                scope.$on('linkInfoDetails.initialize', function (event, e, query, link) {
                    reset();
                    htQuery = query;
                    showDetailInformation(link);
                });
            }
        };
    } ]);
