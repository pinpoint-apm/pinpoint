'use strict';

pinpointApp.constant('nodeInfoDetailsConfig', {
    applicationStatisticsUrl: '/applicationStatistics.pinpoint',
    myColors : ["#008000", "#4B72E3", "#A74EA7", "#BB5004", "#FF0000"]
});

pinpointApp
    .directive('nodeInfoDetails', [ 'nodeInfoDetailsConfig', function (config) {
        return {
            restrict: 'EA',
            replace: true,
            templateUrl: 'views/nodeInfoDetails.html',
            link: function postLink(scope, element, attrs) {

                var reset = function () {
                    scope.nodeName = null;
                    scope.nodeCategory = null;
                    scope.unknownGroup = null;
                    scope.hosts = null;
                    scope.showHosts = false;
                    scope.agents = null;
                    scope.showAgents = false;
                    scope.showNodeInfoBarChart = false;
                    scope.$digest();
                };

                var showDetailInformation = function (query, data) {
                    scope.nodeName = data.text;
                    scope.nodeCategory = data.category;
                    scope.unknownGroup = data.textArr;
                    scope.hosts = data.hosts;
                    scope.showHosts = (scope.hosts.length > 0) ? true : false;
                    scope.agents = data.agents;
                    scope.showAgents = (scope.agents.length > 0) ? true : false;
                    scope.$digest();
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

                var renderApplicationStatistics = function (data) {
                    scope.showNodeInfoBarChart = true;
                    scope.$digest();
                    nv.dev = false;
                    nv.addGraph(function () {
                        var chart = nv.models.discreteBarChart().x(function (d) {
                            return d.label;
                        }).y(function(d) {
                                return d.value;
                            }).staggerLabels(false).tooltips(false).showValues(true);

                        chart.xAxis.tickFormat(function (d) {
                            if(angular.isNumber(d)) {
                                return (d >= 1000) ? d / 1000 + "s" : d + "ms";
                            }
                            return d;
                        });

                        chart.yAxis.tickFormat(function (d) {
                            return d;
                        });

                        chart.valueFormat(function (d) {
                            return d;
                        });

                        chart.color(config.myColors);

                        d3.select('.nodeInfoDetails .infoBarChart svg')
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

                var extractHistogramFromData = function (data) {
                    var histogram = [];
                    if (data && data.hosts && angular.isArray(data.hosts) && data.hosts.length > 0) {
                        var hosts = data.hosts;
                        angular.forEach(hosts, function (val, key) {
                            var i = 0;
                            angular.forEach(val.histogram, function (innerVal, innerKey) {
                                if (histogram[i]) {
                                    histogram[i].value += Number(innerVal, 10);
                                } else {
                                    histogram[i] = {
                                        'label' : innerKey,
                                        'value' : Number(innerVal, 10)
                                    };
                                }
                                i += 1;
                            });
                        });
                    }
                    var histogramData = [{
                        'key' : "Response Time Histogram",
                        'values': histogram
                    }];
                    return histogramData;
                };

                scope.$on('servermap.nodeClicked', function (event, e, query, data, containerId) {
                    reset();
                    showDetailInformation(query, data);
                    if (!data.rawdata && data.category !== "USER" && data.category !== "UNKNOWN_GROUP") {
//                        showApplicationStatisticsSummary(query.from, query.to, data.text, data.serviceTypeCode);
                        var histogramData = extractHistogramFromData(data);
                        renderApplicationStatistics(histogramData);
                    }
                });
                scope.$on('servermap.linkClicked', function (event, e, query, data, containerId) {
                    reset();
                });


            }
        };
    }]);
