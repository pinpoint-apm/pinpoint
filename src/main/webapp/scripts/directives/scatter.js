'use strict';

pinpointApp.constant('scatterConfig', {
    get: {
        scatterData: '/getScatterData.pinpoint',
        lastScatterData: '/getLastScatterData.pinpoint'
    }
});

pinpointApp.directive('scatter',
    [ 'scatterConfig', '$rootScope', '$timeout', function (scatterConfig, $rootScope, $timeout) {
        return {
            template: '<div id="scatterchart"></div>',
            restrict: 'EA',
            replace: true,
            link: function (scope, element, attrs) {

                var oScatterChart = null;
                var selectdTracesBox = {};

                var showScatter = function (applicationName, from, to, period, filter, w, h) {
                    if (oScatterChart) {
//							oScatterChart.clear();
                    }

                    selectdTracesBox = {};
//						var fullscreenButton = $("#scatterChartContainer I.icon-fullscreen");
//						fullscreenButton.data("applicationName", applicationName);
//						fullscreenButton.data("from", from);
//						fullscreenButton.data("to", to);
//						fullscreenButton.data("period", period);
//						fullscreenButton.data("usePeriod", usePeriod);
//						fullscreenButton.data("filter", filter);

//						var downloadButton = $("#scatterChartContainer A");

//						var imageFileName = applicationName +
//								"_" +
//								new Date(from).toString("yyyyMMdd_HHmm") +
//								"~" +
//								new Date(to).toString("yyyyMMdd_HHmm") +
//								"_response_scatter.png";
//
//						downloadButton.attr("download", imageFileName);
//						downloadButton.unbind("click");
//						downloadButton.bind("click", function() {
//							var sImageUrl = oScatterChart.getChartAsPNG();
//							$(this).attr('href', sImageUrl);
//						});

//						$("#scatterChartContainer SPAN").unbind("click");
//						$("#scatterChartContainer SPAN").bind("click", function() {
//							showRequests(applicationName, from, to, period, usePeriod, filter);
//						});

//						$("#scatterChartContainer").show();

                    var bDrawOnceAll = false,
                        nInterval = 2000,
                        fetchLimit = 2000;

                    var htDataSource = {
                        sUrl: function (nFetchIndex) {
//								if (!usePeriod) {
//									return scatterConfig.get.scatterData;
//								}

//								if (nFetchIndex === 0) {
//									return scatterConfig.get.lastScatterData;
//								} else {
                            return scatterConfig.get.scatterData;
//								}
                        },
                        htParam: function (nFetchIndex, htLastFetchParam, htLastFetchedData) {
                            // calculate parameter
                            var htData;
//								console.log("htParam", nFetchIndex, htLastFetchParam, htLastFetchedData);

//								if (nFetchIndex === 0 && !usePeriod) {
                            if (nFetchIndex === 0) {
                                return {
                                    'application': applicationName,
                                    'from': from,
                                    'to': to,
                                    'limit': fetchLimit,
                                    'filter': filter
                                };
                            }

                            // period만큼 먼저 조회해본다.
                            if (nFetchIndex === 0 /*|| typeof(htLastFetchParam) === 'undefined' || typeof(htLastFetchedData) === 'undefined'*/) {
                                htData = {
                                    'application': applicationName,
                                    'period': period,
                                    'limit': fetchLimit,
                                    'filter': filter
                                };
                            } else {
                                if (bDrawOnceAll || htLastFetchedData.scatter.length == 0) {
                                    htData = {
                                        'application': applicationName,
                                        'from': htLastFetchParam.to + 1,
                                        'to': htLastFetchParam.to + 2000,
                                        'limit': fetchLimit,
                                        'filter': filter
                                    };
                                } else {
                                    htData = {
                                        'application': applicationName,
                                        // array[0] 이 최근 값, array[len]이 오래된 이다.
                                        'from': from,
                                        'to': htLastFetchedData.scatter[htLastFetchedData.scatter.length - 1].x - 1,
                                        'limit': fetchLimit,
                                        'filter': filter
                                    };
                                }
                            }

                            return htData;
                        },
                        nFetch: function (htLastFetchParam, htLastFetchedData) {
                            // -1 : stop, n = 0 : immediately, n > 0 : interval
                            var useInterval = false;

//								console.log("nFetch", htLastFetchedData);

                            if (useInterval && htLastFetchedData.scatter.length === 0) {
//									console.log("2A");
                                bDrawOnceAll = true;
                                return nInterval;
                            }

                            if (htLastFetchedData.scatter.length !== 0) {
                                // array[0] 이 최근 값, array[len]이 오래된 이다.
                                if (htLastFetchedData.scatter[0].x > from) {
                                    // TO THE NEXT
                                    return 0;
                                } else {
                                    // STOP
                                    return -1;
                                }
                            }

                            if (htLastFetchedData.scatter[htLastFetchedData.scatter.length - 1] &&
                                htLastFetchedData.scatter[htLastFetchedData.scatter.length - 1].x < date.getTime()) {
                                if (useInterval) {
                                    bDrawOnceAll = true;
                                    return nInterval;
                                }
                                // TO THE NEXT
                                return 0;
                            }

                            // STOP
                            return -1;
                        },
                        htOption: {
                            dataType: 'jsonp',
                            jsonp: '_callback'
                        }
                    };
                    oScatterChart.drawWithDataSource(htDataSource);
                };
                var makeScatter = function (title, start, end, targetId, period, w, h) {
                    if (!Modernizr.canvas) {
                        alert("Can't draw scatter. Not supported browser.");
                    }

                    var yAxisMAX = 10000;
//                    var date = new Date();

                    var options = {
                        sContainerId: targetId,
                        nWidth: w ? w : 400,
                        nHeight: h ? h : 250,
                        // nXMin: date.getTime() - 86400000, nXMax: date.getTime(),
                        nXMin: start, nXMax: end,
                        nYMin: 0, nYMax: yAxisMAX,
                        nZMin: 0, nZMax: 5,
                        nBubbleSize: 3,
                        sXLabel: '',
                        sYLabel: '(ms)',
                        sTitle: title,
                        htTypeAndColor: {
                            // type name : color
                            'Success': '#2ca02c',
                            // 'Warning' : '#f5d025',
                            'Failed': '#d62728'
                        },
                        fOnSelect: function (htPosition, htXY) {
                            var traces = this.getDataByXY(htXY.nXFrom, htXY.nXTo, htXY.nYFrom, htXY.nYTo);

                            if (traces.length === 0) {
                                return;
                            }

                            if (traces.length === 1) {
//									openTrace(traces[0].traceId, traces[0].x);
                                return;
                            }

                            var token = Math.random() * 10000 + 1;
                            selectdTracesBox[token] = traces;

                            var popupwindow = window.open("/selectedScatter.pinpoint", token);
                        }
                    };

                    $timeout(function () {
                        if (oScatterChart !== null) {
                            oScatterChart.destroy();
                        }
                        oScatterChart = new BigScatterChart(options);
                        showScatter(scope.navbar.applicationName, start, end, period);
                    }, 100);

                };


                scope.$on('navbar.applicationChanged', function (event, navbar) {
                    scope.navbar = navbar;
                    makeScatter(navbar.applicationName, navbar.queryStartTime, navbar.queryEndTime, 'scatterchart', navbar.queryPeriod);
                });
                scope.$on('servermap.passingTransactionResponseToScatterChart', function (event, node) {
                    scope.node = node;
                    makeScatter(node.applicationName || node.text, scope.navbar.queryStartTime, scope.navbar.queryEndTime, 'scatterchart', scope.navbar.queryPeriod);
                });

            }
        };
    } ]);
