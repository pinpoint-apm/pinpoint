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
                var id, aDynamicKey;

                // define variables of methods
                var setIdAutomatically, setWidthHeight, render, parseTimeSeriesHistogramForAmcharts;

                /**
                 * set id automatically
                 */
                setIdAutomatically = function () {
                    id = 'loadId-' + scope.namespace;
                    element.attr('id', id);
                };

                /**
                 * set width height
                 * @param w
                 * @param h
                 */
                setWidthHeight = function (w, h) {
                    if (w) element.css('width', w);
                    if (h) element.css('height', h);
                };

                /**
                 * render
                 * @param data
                 * @param useChartCursor
                 */
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
                                "title": aDynamicKey[0],
                                "type": "column",
//                                "color": "#000000",
                                "valueField": aDynamicKey[0]
                            }, {
                                "balloonText": "[[title]] : <b>[[value]]</b>",
                                "fillAlphas": 0.8,
//                                "labelText": "[[value]]",
                                "lineAlpha": 0.3,
                                "title": aDynamicKey[1],
                                "type": "column",
                                "color": "#000000",
                                "valueField": aDynamicKey[1]
                            }, {
                                "balloonText": "[[title]] : <b>[[value]]</b>",
                                "fillAlphas": 0.8,
//                                "labelText": "[[value]]",
                                "lineAlpha": 0.3,
                                "title": aDynamicKey[2],
                                "type": "column",
//                                "color": "#000000",
                                "valueField": aDynamicKey[2]
                            }, {
                                "balloonText": "[[title]] : <b>[[value]]</b>",
                                "fillAlphas": 0.8,
//                                "labelText": "[[value]]",
                                "lineAlpha": 0.3,
                                "title": aDynamicKey[3],
                                "type": "column",
//                                "color": "#000000",
                                "valueField": aDynamicKey[3]
                            }, {
                                "balloonText": "[[title]] : <b>[[value]]</b>",
                                "fillAlphas": 0.8,
//                                "labelText": "[[value]]",
                                "lineAlpha": 0.3,
                                "title": aDynamicKey[4],
                                "type": "column",
//                                "color": "#000000",
                                "valueField": aDynamicKey[4]
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

                    aDynamicKey = [];

                    var newData = [];
                    for (var key in data) {
                        aDynamicKey.push(data[key].key);
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

                /**
                 * scope event on loadChart.initAndRenderWithData.namespace
                 */
                scope.$on('loadChart.initAndRenderWithData.' + scope.namespace, function (event, data, w, h, useChartCursor) {
                    setIdAutomatically();
                    setWidthHeight(w, h);
                    render(parseTimeSeriesHistogramForAmcharts(data), useChartCursor);
                });

                /**
                 * scope event on loadChart.updateData.namespace
                 */
                scope.$on('loadChart.updateData.' + scope.namespace, function (event, data) {
                    render(parseTimeSeriesHistogramForAmcharts(data));
                });
            }
        };
    }]);
