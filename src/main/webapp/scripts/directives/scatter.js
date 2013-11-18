'use strict';

pinpointApp.constant('scatterConfig', {
    get: {
        scatterData: '/getScatterData.pinpoint',
        lastScatterData: '/getLastScatterData.pinpoint'
    },
    useIntervalForFetching: false,
    nFetchingInterval: 2000,
    nFetchLimit: 5000
});

// FIXME child window에서 접근할 수 있도록 global변수로 일단 빼둠. 나중에 리팩토링할 것.
//var selectdTracesBox = {};

pinpointApp.directive('scatter',
    [ 'scatterConfig', '$rootScope', '$timeout', 'webStorage', 'TransactionDao', function (cfg, $rootScope, $timeout, webStorage, oTransactionDao) {
        return {
            template: '<div class="scatter"></div>',
            restrict: 'EA',
            replace: true,
            link: function (scope, element, attrs) {

                // define private variables
                var oScatterChart, oNavbarVo;

                // define private variables of methods
                var showScatter, makeScatter;

                // initialize
                oScatterChart = null;
                oNavbarVo = null;

                /**
                 * show scatter
                 * @param applicationName
                 * @param from
                 * @param to
                 * @param period
                 * @param filter
                 * @param w
                 * @param h
                 */
                showScatter = function (applicationName, from, to, period, filter, w, h) {
                    if (oScatterChart) {
//							oScatterChart.clear();
                    }

//                    selectdTracesBox = {};
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

                    var bDrawOnceAll = false;

                    var htDataSource = {
                        sUrl: function (nFetchIndex) {
                            return cfg.get.scatterData;
                        },
                        htParam: function (nFetchIndex, htLastFetchParam, htLastFetchedData) {
                            // calculate parameter
                            var htData;
                            if (nFetchIndex === 0) {
                                htData =  {
                                    'application': applicationName,
                                    'from': from,
                                    'to': to,
                                    'limit': cfg.nFetchLimit,
                                    'v': 2
                                };
                            } else {
                                htData = {
                                    'application': applicationName,
                                    // array[0] 이 최근 값, array[len]이 오래된 이다.
                                    'from': from,
                                    'to': htLastFetchedData.resultFrom - 1,
                                    'limit': cfg.nFetchLimit,
                                    'v': 2
                                };
                            }
                            if (filter) {
                                htData.filter = filter;
                            }

                            return htData;
                        },
                        nFetch: function (htLastFetchParam, htLastFetchedData) {
                            // -1 : stop, n = 0 : immediately, n > 0 : interval
                            if (htLastFetchedData.resultFrom - 1 > from) {
                                if (cfg.useIntervalForFetching) {
                                    bDrawOnceAll = true;
                                    return cfg.nFetchingInterval;
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
                        },
                        index: {
                            x: 0,
                            y: 1,
                            transactionId: 2,
                            type: 3
                        },
                        type: {
                            '0' : 'Failed',
                            '1' : 'Success'
                        }
                    };
                    oScatterChart.drawWithDataSource(htDataSource);
                };

                /**
                 * make scatter
                 * @param title
                 * @param start
                 * @param end
                 * @param period
                 * @param filter
                 * @param w
                 * @param h
                 */
                makeScatter = function (title, start, end, period, filter, w, h) {
                    if (!Modernizr.canvas) {
                        alert("Can't draw scatter. Not supported browser.");
                    }

                    var yAxisMAX = 10000;
//                    var date = new Date();

                    var options = {
                        sContainerId: element,
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
                            var transactions = {
                                htXY : htXY,
                                aTraces : []
                            };
                            transactions.aTraces = this.getDataByXY(htXY.nXFrom, htXY.nXTo, htXY.nYFrom, htXY.nYTo);
                            if (transactions.aTraces.length === 0) {
                                return;
                            }

                            var token = 'transactionsFromScatter_' + _.random(100000, 999999);
//                            webStorage.session.add(token, transactions);
//                            window[token] = transactions;
//                            window.open("/selectedScatter.pinpoint", token);

                            oTransactionDao.addData(token, transactions);
                            window.open("#/transactionList", token);
                        }
                    };

                    $timeout(function () {
                        if (oScatterChart !== null) {
                            oScatterChart.destroy();
                        }
                        oScatterChart = new BigScatterChart(options);
                        showScatter(title, start, end, period, filter);
                    }, 100);

                };

                /**
                 * scope event on scatter.initialize
                 */
                scope.$on('scatter.initialize', function (event, navbarVo) {
                    oNavbarVo = navbarVo;
                    makeScatter(oNavbarVo.getApplicationName(), oNavbarVo.getQueryStartTime(), oNavbarVo.getQueryEndTime(), oNavbarVo.getQueryPeriod(), oNavbarVo.getFilter());
                });

                /**
                 * scope event on scatter.initializeWithNode
                 */
                scope.$on('scatter.initializeWithNode', function (event, node) {
                    makeScatter(node.applicationName || node.text, oNavbarVo.getQueryStartTime(), oNavbarVo.getQueryEndTime(), oNavbarVo.getQueryPeriod(), oNavbarVo.getFilter());
                });

            }
        };
    } ]);
