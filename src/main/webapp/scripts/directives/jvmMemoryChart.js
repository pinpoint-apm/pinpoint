'use strict';

angular.module('pinpointApp')
    .directive('jvmMemoryChart', ['$timeout',
        function ($timeout) {
            return {
                template: '<div></div>',
                replace: true,
                restrict: 'E',
                scope: {
                    namespace: '@' // string value
                },
                link: function postLink(scope, element, attrs) {

                    // define variables
                    var sId, oChart;

                    // define variables of methods
                    var setIdAutomatically, setWidthHeight, render, showCursorAt;

                    /**
                     * set id automatically
                     */
                    setIdAutomatically = function () {
                        sId = 'multipleValueAxesId-' + scope.namespace;
                        element.attr('id', sId);
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

                    // generate some random data, quite different range
                    function generateChartData() {
                        var chartData = [];
                        var firstDate = new Date();
                        firstDate.setDate(firstDate.getDate() - 100);

                        for (var i = 0; i < 100; i++) {
                            // we create date objects here. In your data, you can have date strings
                            // and then set format of your dates using chart.dataDateFormat property,
                            // however when possible, use date objects, as this will speed up chart rendering.
                            var newDate = new Date(firstDate);
                            newDate.setDate(newDate.getDate() + i);

                            var Max = Math.round(Math.random() * 40) + 100;
                            var Used = Math.round(Math.random() * 80) + 500;
                            var GC = Math.round(Math.random() * 6000);

                            chartData.push({
                                time: newDate,
                                Max: Max,
                                Used: Used,
                                GC: GC
                            });
                        }
                        return chartData;
                    }

                    render = function (chartData) {
                        chartData = chartData || generateChartData();

                        var options = {
                            "type": "serial",
                            "theme": "light",
                            "legend": {
                                "useGraphSettings": true,
                                "autoMargins": false,
                                "align" : "right",
                                "position": "top",
                                "valueWidth": 60
                            },
                            "usePrefixes": true,
                            "dataProvider": chartData,
                            "valueAxes": [
                                {
                                    "id": "v1",
                                    "gridAlpha": 0,
                                    "axisAlpha": 1,
                                    "position": "left"
                                },
                                {
                                    "id": "v2",
                                    "gridAlpha": 0,
                                    "axisAlpha": 1,
                                    "position": "right"
                                }
                            ],
                            "graphs": [
                                {
                                    "valueAxis": "v2",
                                    "balloonText": "[[value]]B",
                                    "lineColor": "rgb(174, 199, 232)",
                                    "title": "Max",
                                    "valueField": "Max",
                                    "fillAlphas": 0
                                },
                                {
                                    "valueAxis": "v2",
                                    "balloonText": "[[value]]B",
                                    "lineColor": "rgb(31, 119, 180)",
                                    "fillColor": "rgb(31, 119, 180)",
                                    "title": "Used",
                                    "valueField": "Used",
                                    "fillAlphas": 0.4
                                },
                                {
                                    "valueAxis": "v1",
                                    "balloonText": "[[value]]ms",
                                    "lineColor": "#FF6600",
                                    "title": "GC",
                                    "valueField": "GC",
                                    "type": "column",
                                    "fillAlphas": 0.3
                                }
                            ],
                            "chartCursor": {
                                "categoryBalloonAlpha": 0.7,
                                "fullWidth": true,
                                "cursorAlpha": 0.1
                            },
                            "categoryField": "time",
                            "categoryAxis": {
                                "axisColor": "#DADADA",
                                "startOnAxis": true,
                                "gridPosition": "start",
                                "labelFunction": function (valueText, serialDataItem, categoryAxis) {
                                    return new Date(valueText).toString('hh:mm');
                                }
                            }
                        };
                        $timeout(function () {
                            oChart = AmCharts.makeChart(sId, options);
                            oChart.chartCursor.addListener('changed', function (event) {
                                scope.$emit('jvmMemoryChart.cursorChanged.' + scope.namespace, event)
                            });
                            oChart.chartCursor.addListener('moved', function (type, x, y, zooming, chart) {
//                                console.log('moved', type, x, y, zooming, chart);
                            });
                            oChart.chartCursor.addListener('selected', function (type, start, end) {
//                                console.log('selected', type, start, end);
                            });
                            oChart.chartCursor.addListener('zoomed', function (type, start, end) {
//                                console.log('zoomed', type, start, end);
                            });
                        });
                    };

                    showCursorAt = function (category) {
                        if (category) {
                            if (angular.isNumber(category)) {
                                category = oChart.dataProvider[category].time;
                            }
                            oChart.chartCursor.showCursorAt(category);
                        } else {
                            oChart.chartCursor.hideCursor();
                        }
                    };

                    /**
                     * scope event on loadChart.initAndRenderWithData.namespace
                     */
                    scope.$on('jvmMemoryChart.initAndRenderWithData.' + scope.namespace, function (event, data, w, h) {
                        setIdAutomatically();
                        setWidthHeight(w, h);
                        //render(parseTimeSeriesHistogramForAmcharts(data), useChartCursor);
                        render(data);
                    });

                    /**
                     * scope event on loadChart.updateData.namespace
                     */
                    scope.$on('jvmMemoryChart.showCursorAt.' + scope.namespace, function (event, category) {
                        showCursorAt(category)
                    });
                }
            };
        }
    ]);
