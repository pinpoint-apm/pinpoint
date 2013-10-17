'use strict';

pinpointApp.constant('linkInfoDetailsConfig', {
    linkStatisticsUrl: '/linkStatistics.pinpoint',
    myColors: ["#008000", "#4B72E3", "#A74EA7", "#BB5004", "#FF0000"]
});

pinpointApp.directive('linkInfoDetails', [ 'linkInfoDetailsConfig', function (config) {
    return {
        restrict: 'EA',
        replace: true,
        templateUrl: 'views/linkInfoDetails.html',
        link: function postLink(scope, element, attrs) {

            // define private variables
            var htQuery;

            // define private variables of methods;
            var reset, showDetailInformation, getLinkStatisticsData, renderStatisticsTimeSeriesHistogram, renderStatisticsSummary, showApplicationStatistics;

            /**
             * reset
             */
            reset = function () {
                scope.linkCategory = null;
                scope.rawdata = null;
                scope.query = null;
                scope.targetinfo = null;
                scope.sourceinfo = null;
                scope.showLinkInfoChart = false;
                scope.showLinkInfoBarChart = false;
                scope.$digest();
            };

            /**
             * show detail information of scope
             * @param applicationName
             */
            scope.showDetailInformation = function (applicationName) {
                showDetailInformation(scope.rawdata[applicationName]);
            };

            /**
             * show detail information
             * @param query
             * @param data
             */
            showDetailInformation = function (data) {
                console.log('showDetailInformation', data);
                if (data.rawdata) {
                    scope.linkCategory = 'UnknownLinkInfoBox';
                } else {
                    scope.linkCategory = 'LinkInfoBox';
                    showApplicationStatistics(
                        htQuery.from,
                        htQuery.to,
                        data.sourceinfo.serviceTypeCode,
                        data.sourceinfo.applicationName,
                        data.targetinfo.serviceTypeCode,
                        data.targetinfo.applicationName
                    );
                }

                scope.rawdata = data.rawdata;
                scope.query = data.query;
                scope.targetinfo = data.targetinfo;
                scope.sourceinfo = data.sourceinfo;
                scope.$digest();
            };

            /**
             * get link statistics data
             * @param query
             * @param callback
             */
            getLinkStatisticsData = function (query, callback) {
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
                        destApplicationName: query.destApplicationName
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
             * @param data
             */
            renderStatisticsSummary = function (data) {
                nv.addGraph(function () {
                    var chart = nv.models.discreteBarChart().x(function (d) {
                        return d.label;
                    }).y(function (d) {
                        return d.value;
                    }).staggerLabels(false).tooltips(false).showValues(true);

                    chart.xAxis.tickFormat(function (d) {
                        if (angular.isNumber(d)) {
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

                    d3.select('.linkInfoDetails .infoBarChart svg')
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
             */
            showApplicationStatistics = function (begin, end, srcServiceType, srcApplicationName, destServiceType, destApplicationName) {
                var params = {
                    "from": begin,
                    "to": end,
                    "srcServiceType": srcServiceType,
                    "srcApplicationName": srcApplicationName,
                    "destServiceType": destServiceType,
                    "destApplicationName": destApplicationName
                };

                getLinkStatisticsData(params, function (query, result) {
                    scope.showLinkInfoChart = true;
                    scope.showLinkInfoBarChart = true;
                    scope.$digest();
                    renderStatisticsTimeSeriesHistogram(result.timeseriesHistogram);
                    renderStatisticsSummary(result.histogramSummary);
                });
            };

            /**
             * scope event on servermap.nodeClicked
             */
            scope.$on('linkInfoDetails.initializeWithNodeData', function (event, e, query, node, data) {
                reset();
            });

            /**
             * scope event on servermap.linkClicked
             */
            scope.$on('linkInfoDetails.initializeWithLinkData', function (event, e, query, link, data) {
                reset();
                htQuery = query;
                showDetailInformation(link);
            });
        }
    };
} ]);
