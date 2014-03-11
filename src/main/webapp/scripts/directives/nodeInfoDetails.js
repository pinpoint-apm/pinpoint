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

                    if (!node.rawdata && /*node.category !== "USER" &&*/ node.category !== "UNKNOWN_GROUP") {
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
                                ' svg', parseHistogramForD3(node.rawdata[node.textArr[key].applicationName].histogram));
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

//                var getApplicationStatisticsData = function (query, callback) {
//                    jQuery.ajax({
//                        type : 'GET',
//                        url : config.applicationStatisticsUrl,
//                        cache : false,
//                        dataType: 'json',
//                        data : {
//                            from : query.from,
//                            to : query.to,
//                            applicationName : query.applicationName,
//                            serviceType : query.serviceType
//                        },
//                        success : function (result) {
//                            callback(query, result);
//                        },
//                        error : function (xhr, status, error) {
//                            console.log("ERROR", status, error);
//                        }
//                    });
//                };
                /**
                 * render application statistics
                 * @param data
                 */
//                renderApplicationStatistics = function (data) {
//
//                    nv.addGraph(function () {
//                        angular.element('.nodeInfoDetails .histogram svg').empty();
//                        var chart = nv.models.discreteBarChart().x(function (d) {
//                            return d.label;
//                        }).y(function (d) {
//                            return d.value;
//                        }).staggerLabels(false).tooltips(false).showValues(true);
//
//                        chart.xAxis.tickFormat(function (d) {
//                        	// FIXME d로 넘어오는 값의 타입이 string이고 angular.isNumber는 "1000"에 대해 false를 반환함.
//                        	// if (angular.isNumber(d)) {
//                        	if (/^\d+$/.test(d)) {
//                            	if (d >= 1000) {
//                            		return $filter('number')(d / 1000) + "s";
//                            	} else {
//                            		return $filter('number')(d) + "ms";
//                            	}
//                            } else if (d.charAt(d.length - 1) == '+') {
//                            	var v = d.substr(0, d.length - 1);
//                            	if (v >= 1000) {
//                            		return $filter('number')(v / 1000) + "s+";
//                            	} else {
//                            		return $filter('number')(v) + "ms+";
//                            	}
//                            } else {
//                            	return d;
//                            }
//                        });
//
//                        chart.yAxis.tickFormat(function (d, i) {
//                    		if (d >= 1000) {
//                    			return $filter('number')(Math.floor(d / 1000)) + "k";
//                    		} else {
//                    			return $filter('number')(d);
//                    		}
//                        });
//
//                        chart.valueFormat(function (d) {
//                        	return $filter('number')(d);
//                        });
//
//                        chart.color(config.myColors);
//
//                        d3.select('.nodeInfoDetails .histogram svg')
//                            .datum(data)
//                            .transition()
//                            .duration(0)
//                            .call(chart);
//
//                        nv.utils.windowResize(chart.update);
//
//                        return chart;
//                    });
//                };

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
                        angular.element().empty(querySelector);
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

//                var showApplicationStatisticsSummary = function (begin, end, applicationName, serviceType) {
//                    var params = {
//                        "from" : begin,
//                        "to" : end,
//                        "applicationName" : applicationName,
//                        "serviceType" : serviceType
//                    };
//                    getApplicationStatisticsData(params, function (query, result) {
//                        console.log('result', result);
//                        renderApplicationStatistics(result.histogramSummary);
//                    });
//                };


                // histogram 데이터 서버에서 만들지 않고, link정보에서 수집한다.
//                var extractHistogramFromData = function (data) {
//                    var histogram = [];
//                    if (data && data.serverList /*&& angular.isArray(data.serverList) && data.serverList.length > 0*/) {
//                        angular.forEach(data.serverList, function (serverInfo, serverName) {
//                            var i = 0;
//                            angular.forEach(serverInfo.instanceList, function (innerVal, innerKey) {
//                            	if (innerVal.histogram == null) {
//                            		return;
//                            	}
//                            	angular.forEach(innerVal.histogram, function(v, k) {
//                            		if (histogram[i]) {
//                            			histogram[i].value += Number(v, 10);
//                            		} else {
//                            			histogram[i] = {
//                            					'label' : k,
//                            					'value' : Number(v, 10)
//                            			};
//                            		}
//                            		i++;
//                            	});
//                            	i = 0;
//                            });
//                        });
//                    }
//                    var histogramData = [{
//                        'key' : "Response Time Histogram",
//                        'values': histogram
//                    }];
//                    return histogramData;
//                };

                /**
                 * recalculate histogram
                 * @param key
                 * @param linkDataArray
                 * @returns {Array}
                 */
//                recalculateHistogram = function (key, linkDataArray) {
//                    // application histogram data 서버에서 만들지 않고 클라이언트에서 만든다.
//                    // var histogramData = extractHistogramFromData(node);
//                    var histogram = [];
//                    angular.forEach(linkDataArray, function (value, index) {
//                        var i = 0;
//                        if (value.to === key) {
//                            angular.forEach(value.histogram, function (v, k) {
//                                if (histogram[i]) {
//                                    histogram[i].value += Number(v, 10);
//                                } else {
//                                    histogram[i] = {
//                                        'label': k,
//                                        'value': Number(v, 10)
//                                    };
//                                }
//                                i += 1;
//                            });
//                        }
//                    });
//                    return histogram;
//                };

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
