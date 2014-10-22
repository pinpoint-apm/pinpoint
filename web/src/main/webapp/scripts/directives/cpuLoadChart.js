'use strict';

angular.module('pinpointApp')
    .directive('cpuLoadChart', ['$timeout',
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
                    var setIdAutomatically, setWidthHeight, render, showCursorAt, resize;

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

                    /**
                     * render
                     * @param chartData
                     */
                    render = function (chartData) {
                        var options = {
                            "type": "serial",
                            "theme": "light",
                            "legend": {
                                "useGraphSettings": true,
                                "autoMargins": true,
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
                                    "position": "left",
                                    "title": "Cpu Load (%)",
                                    "maximum" : 100,
                                    "minimum" : 0
                                }
                            ],
                            "graphs": [
                                {
                                    "valueAxis": "v1",
                                    "balloonText": "[[jvmCpuLoadValueText]]",
                                    "legendValueText": "[[jvmCpuLoadValueText]]",
                                    "lineColor": "rgb(31, 119, 180)",
                                    "fillColor": "rgb(31, 119, 180)",
                                    "title": "JVM",
                                    "valueField": "jvmCpuLoadValue",
                                    "fillAlphas": 0.4
                                },
                                {
                                    "valueAxis": "v1",
                                    "balloonText": "[[systemCpuLoadValueText]]",
                                    "legendValueText": "[[systemCpuLoadValueText]]",
                                    "lineColor": "rgb(174, 199, 232)",
                                    "fillColor": "rgb(174, 199, 232)",
                                    "title": "System",
                                    "valueField": "systemCpuLoadValue",
                                    "fillAlphas": 0.4
                                },
                                {
                                    "valueAxis": "v1",
                                    "showBalloon": false,
                                    "lineColor": "#FF6600",
                                    "title": "Max",
                                    "valueField": "maxCpuLoad",
                                    "fillAlphas": 0,
                                    "visibleInLegend": false
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
                                scope.$emit('jvmMemoryChart.cursorChanged.' + scope.namespace, event);
                                scope.$emit('cpuLoadChart.cursorChanged.' + scope.namespace, event);
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

                    /**
                     * show cursor at
                     * @param category
                     */
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
                     * resize
                     */
                    resize = function () {
                        if (oChart) {
                            oChart.validateNow();
                            oChart.validateSize();
                        }
                    };

                    /**
                     * scope event on cpuLoadChart.initAndRenderWithData.namespace
                     */
                    scope.$on('cpuLoadChart.initAndRenderWithData.' + scope.namespace, function (event, data, w, h) {
                        setIdAutomatically();
                        setWidthHeight(w, h);
                        //render(parseTimeSeriesHistogramForAmcharts(data), useChartCursor);
                        render(data);
                    });

                    /**
                     * scope event on cpuLoadChart.showCursorAt.namespace
                     */
                    scope.$on('cpuLoadChart.showCursorAt.' + scope.namespace, function (event, category) {
                        showCursorAt(category)
                    });

                    /**
                     * scope event on cpuLoadChart.resize.namespace
                     */
                    scope.$on('cpuLoadChart.resize.' + scope.namespace, function (event) {
                        resize();
                    });
                }
            };
        }
    ]);
