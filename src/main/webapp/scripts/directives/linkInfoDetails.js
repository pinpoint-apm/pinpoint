'use strict';

pinpointApp.constant('linkInfoDetailsConfig', {
    linkStatisticsUrl: '/linkStatistics.pinpoint',
    myColors: ["#2ca02c", "#3c81fa", "#f8c731", "#f69124", "#f53034"]
});

pinpointApp.directive('linkInfoDetails', [ 'linkInfoDetailsConfig', 'HelixChartVo', '$filter', 'ServerMapFilterVo',  'filteredMapUtil',
    function (config, HelixChartVo, $filter, ServerMapFilterVo, filteredMapUtil) {
        return {
            restrict: 'EA',
            replace: true,
            templateUrl: 'views/linkInfoDetails.html',
            link: function postLink(scope, element, attrs) {

                // define private variables
                var htQuery, htTargetRawData, htLastLink;

                // define private variables of methods;
                var reset, showDetailInformation, getTimeSeriesHistogramData, renderTimeSeriesHistogram,
                    renderResponseSummary, showTimeSeriesHistogram, parseHistogramForD3;

                /**
                 * reset
                 */
                reset = function () {
                    htQuery = false;
                    htLastLink = false;
                    htTargetRawData = false;
                    scope.linkCategory = null;
                    scope.targetinfo = null;
                    scope.sourceinfo = null;
                    scope.showLinkInfoDetails = false;
                    scope.showLinkResponseSummary = false;
                    scope.ShowLinkLoad = false;
                    if (!scope.$$phase) {
                        scope.$digest();
                    }
                };

                /**
                 * show detail information of scope
                 * @param applicationName
                 */
                scope.showDetailInformation = function (applicationName) {
                    var link = htTargetRawData[applicationName];
                    showDetailInformation(link);
                    scope.$emit('linkInfoDetail.showDetailInformationClicked', htQuery, link);
                };

                /**
                 * show detail information
                 * @param query
                 * @param data
                 */
                showDetailInformation = function (link) {
                    if (link.targetRawData) {
                        htTargetRawData = link.targetRawData;
                        scope.linkCategory = 'UnknownLinkInfoBox';
                        for (var key in link.targetInfo) {
                            var className = $filter('applicationNameToClassName')(link.targetInfo[key].applicationName)
                            renderResponseSummary('.linkInfoDetails .summaryCharts_' + className +
                                ' svg', parseHistogramForD3(link.targetRawData[link.targetInfo[key].applicationName].histogram));
                        }
                        scope.sourceInfo = link.sourceInfo;
                        scope.targetInfo = link.targetInfo;
                    } else {
                        scope.linkCategory = 'LinkInfoBox';
                        showTimeSeriesHistogram(
                            htQuery.from,
                            htQuery.to,
                            link.sourceInfo.serviceTypeCode,
                            link.sourceInfo.applicationName,
                            link.targetInfo.serviceTypeCode,
                            link.targetInfo.applicationName,
                            link.histogram
                        );
                    }

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
                getTimeSeriesHistogramData = function (query, version, callback) {
                    jQuery.ajax({
                        type: 'GET',
                        url: config.linkStatisticsUrl,
                        cache: false,
                        dataType: 'json',
                        data: {
                            from: query.from,
                            to: query.to,
                            sourceServiceType: query.sourceServiceType,
                            sourceApplicationName: query.sourceApplicationName,
                            targetServiceType: query.targetServiceType,
                            targetApplicationName: query.targetApplicationName,
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
                 * render statistics time series histogram
                 * @param data
                 */
                renderTimeSeriesHistogram = function (data) {
                    nv.addGraph(function () {
                        angular.element('.linkInfoDetails .infoChart svg').empty();
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
                renderResponseSummary = function (querySelector, data, clickEventName) {
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
                        	if (d >= 1000000) {
                                return $filter('number')(Math.floor(d / 1000000)) + "M";
                    		} else if (d >= 1000) {
                                return $filter('number')(Math.floor(d / 1000)) + "K";
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
                                var label = e.point.label,
                                    values = e.series.values;
                                var oServerMapFilterVo = new ServerMapFilterVo();
                                oServerMapFilterVo
                                    .setMainApplication(htLastLink.filterApplicationName)
                                    .setMainServiceTypeCode(htLastLink.filterApplicationServiceTypeCode)
                                    .setFromApplication(htLastLink.sourceInfo.applicationName)
                                    .setFromServiceType(htLastLink.sourceInfo.serviceType)
                                    .setToApplication(htLastLink.targetInfo.applicationName)
                                    .setToServiceType(htLastLink.targetInfo.serviceType);

                                if (label === 'error') {
                                    oServerMapFilterVo.setIncludeException(true);
                                } else if (label.indexOf('+') > 0) {
                                    oServerMapFilterVo
                                        .setResponseFrom(parseInt(label, 10))
                                        .setResponseTo('max');
                                } else {
                                    oServerMapFilterVo
                                        .setResponseFrom(filteredMapUtil.getStartValueForFilterByLabel(label, values) * 1000)
                                        .setResponseTo(parseInt(label, 10) * 1000);
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
                 * @param sourceServiceType
                 * @param sourceApplicationName
                 * @param targetServiceType
                 * @param targetApplicationName
                 * @param histogram
                 */
                showTimeSeriesHistogram = function (begin, end, sourceServiceType, sourceApplicationName, targetServiceType,
                                                      targetApplicationName, histogram) {
                    var params = {
                        "from": begin,
                        "to": end,
                        "sourceServiceType": sourceServiceType,
                        "sourceApplicationName": sourceApplicationName,
                        "targetServiceType": targetServiceType,
                        "targetApplicationName": targetApplicationName
                    };

                    scope.ShowLinkLoad = true;
                    scope.showLinkResponseSummary = true;
                    renderResponseSummary('.linkInfoDetails .infoBarChart svg', parseHistogramForD3(histogram), 'ResponseSummary');

                    getTimeSeriesHistogramData(params, 1, function (query, result) {
                        renderTimeSeriesHistogram(result.timeSeriesHistogram);
                    });

//                    getTimeSeriesHistogramData(params, 2, function (query, result) {
//                        var oHelixChartVo = new HelixChartVo();
//                        oHelixChartVo
//                            .setType('stacked_line')
//                            .setGroup('loadForLinkInfoDetails')
//                            .setWidth(380)
//                            .setHeight(200)
//                            .setPadding([30, 90, 35, 50])
//                            .setMargin([0, 0, 20, 0])
////                        .setXCount(30)
////                        .setXInterval(5)
////                        .setXTick('minutes')
////                        .setXTickInterval(1)
////                        .setXTickFormat('%H:%M')
//                            .setYTicks(5)
//                            .setLegend('1.0s,3.0s,5.0s,Slow,Failed')
//                            .setQueryValue('1.0s,3.0s,5.0s,Slow,Failed')
////                        .setQueryInterval('5s')
//                            .setQueryFrom(begin)
//                            .setQueryTo(end)
//                            .generateEverythingForChart()
//                            .setData(result.timeseriesHistogram);
////                        .parseDataTimestampToDateInstance();
//                        scope.$broadcast('helixChart.initialize.loadForLinkInfoDetails', oHelixChartVo);
//                    });
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
                 * passing transaction map from link info details
                 * @param toApplicationName
                 * @param toServiceType
                 */
                scope.passingTransactionMapFromLinkInfoDetails = function (toApplicationName, toServiceType) {
                    var oServerMapFilterVo = new ServerMapFilterVo();
                    oServerMapFilterVo
                        .setMainApplication(htLastLink.filterApplicationName)
                        .setMainServiceTypeCode(htLastLink.filterApplicationServiceTypeCode)
                        .setFromApplication(htLastLink.sourceInfo.applicationName)
                        .setFromServiceType(htLastLink.sourceInfo.serviceType)
                        .setToApplication(toApplicationName)
                        .setToServiceType(toServiceType);
                    scope.$broadcast('linkInfoDetails.openFilteredMap', oServerMapFilterVo);
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
                    htLastLink = link;
                    showDetailInformation(link);
                });
            }
        };
    } ]);
