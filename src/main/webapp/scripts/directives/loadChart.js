'use strict';

pinpointApp.constant('loadChartConfig', {
    myColors: ["#2ca02c", "#3c81fa", "#f8c731", "#f69124", "#f53034"]
});

pinpointApp
    .directive('loadChart', ['loadChartConfig', '$timeout', function (cfg, $timeout) {
        return {
            template: '<div></div>',
            replace: true,
            restrict: 'EA',
            scope: {
                namespace: '@' // string value
            },
            link: function postLink(scope, element, attrs) {

                // define variables
                var id;

                // define variables of methods
                var setIdAutomatically, setWidthHeight, render, parseTimeSeriesHistogramForAmcharts;

                setIdAutomatically = function () {
                    id = 'loadId-' + scope.namespace;
                    element.attr('id', id);
                };

                setWidthHeight = function (w, h) {
                    if (w) element.css('width', w);
                    if (h) element.css('height', h);
                };

                render = function (data, useChartCursor) {
                    $timeout(function () {
                        var options = {
                            "type": "serial",
                            "theme": "light",
                            "legend": {
                                "autoMargins": false,
                                "align" : "right",
                                "borderAlpha": 0,
                                "equalWidths": true,
                                "horizontalGap": 0,
                                "verticalGap": 0,
                                "markerSize": 10,
                                "useGraphSettings": false,
                                "valueWidth": 0,
                                "spacing": 0,
                                "markerType" : "circle", // square, circle, diamond, triangleUp, triangleDown, triangleLeft, triangleDown, bubble, line, none.
                                "position": "top"
                            },
                            "colors" : cfg.myColors,
                            "dataProvider": data,
                            "valueAxes": [{
                                "stackType": "regular",
                                "axisAlpha": 0.1,
                                "gridAlpha": 0.1
                            }],
                            "graphs": [{
                                "balloonText": "[[title]] : <b>[[value]]</b>",
                                "balloonColor": "red",
                                "fillAlphas": 0.8,
//                                "labelText": "[[value]]",
                                "lineAlpha": 0.3,
                                "title": "1s",
                                "type": "column",
//                                "color": "#000000",
                                "valueField": "1s"
                            }, {
                                "balloonText": "[[title]] : <b>[[value]]</b>",
                                "fillAlphas": 0.8,
//                                "labelText": "[[value]]",
                                "lineAlpha": 0.3,
                                "title": "3s",
                                "type": "column",
                                "color": "#000000",
                                "valueField": "3s"
                            }, {
                                "balloonText": "[[title]] : <b>[[value]]</b>",
                                "fillAlphas": 0.8,
//                                "labelText": "[[value]]",
                                "lineAlpha": 0.3,
                                "title": "5s",
                                "type": "column",
//                                "color": "#000000",
                                "valueField": "5s"
                            }, {
                                "balloonText": "[[title]] : <b>[[value]]</b>",
                                "fillAlphas": 0.8,
//                                "labelText": "[[value]]",
                                "lineAlpha": 0.3,
                                "title": "Slow",
                                "type": "column",
//                                "color": "#000000",
                                "valueField": "Slow"
                            }, {
                                "balloonText": "[[title]] : <b>[[value]]</b>",
                                "fillAlphas": 0.8,
//                                "labelText": "[[value]]",
                                "lineAlpha": 0.3,
                                "title": "Error",
                                "type": "column",
//                                "color": "#000000",
                                "valueField": "Error"
                            }],
                            "categoryField": "time",
                            "categoryAxis": {
                                "parseDates": true,
                                "equalSpacing": true,
                                "dashLength": 1,
                                "minorGridEnabled": true,
                                "minPeriod": "NN"
                            }
                        };
                        if (useChartCursor) {
                            options["chartCursor"] = {
                                "cursorPosition": "mouse",
                                    "categoryBalloonAlpha": 0.7,
                                    "categoryBalloonDateFormat": "H:NN"
                            };
                        }
                        AmCharts.makeChart(id, options);
                    });
                };

                /**
                 * parse time series histogram for amcharts
                 * @param data
                 * @returns {Array}
                 */
                parseTimeSeriesHistogramForAmcharts = function (data) {
                    function getKeyFromNewDataByTime (time) {
                        for (var key in newData) {
                            if (new Date(time).toString() === newData[key].time.toString()) {
                                return key;
                            }
                        }
                        return -1;
                    }

                    var newData = [];
                    for (var key in data) {
                        for (var innerKey in data[key].values) {
                            var a = getKeyFromNewDataByTime(data[key].values[innerKey][0]);
                            if (a > -1) {
                                newData[a][data[key].key] = data[key].values[innerKey][1];
                            } else {
                                var b = {
                                    time: new Date(data[key].values[innerKey][0])
                                };
                                b[data[key].key] = data[key].values[innerKey][1];
                                newData.push(b);
                            }
                        }
                    }
                    return newData;
                };

                scope.$on('loadChart.initAndRenderWithData.' + scope.namespace, function (event, data, w, h, useChartCursor) {
                    console.log('responseTimeChart.initAndRenderWithData.' + scope.namespace);
                    setIdAutomatically();
                    setWidthHeight(w, h);
                    render(parseTimeSeriesHistogramForAmcharts(data), useChartCursor);
                });

                scope.$on('loadChart.updateData.' + scope.namespace, function (event, data) {
                    render(parseTimeSeriesHistogramForAmcharts(data));
                });
            }
        };
    }]);
