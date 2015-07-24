(function() {
	'use strict';
	/**
	 * (en)cpuLoadChartDirective 
	 * @ko cpuLoadChartDirective
	 * @group Directive
	 * @name cpuLoadChartDirective
	 * @class
	 */	
	angular.module('pinpointApp').directive('cpuLoadChartDirective', ['$timeout',
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
                            "autoMargins": false,
                            "marginTop": 10,
                            "marginLeft": 70,
                            "marginRight": 70,
                            "marginBottom": 30,
                            "legend": {
                                "useGraphSettings": true,
                                "autoMargins": true,
                                "align" : "right",
                                "position": "top",
                                "valueWidth": 70
                            },
                            "usePrefixes": true,
                            "dataProvider": chartData,
                            "valueAxes": [
                                {
                                    "id": "v1",
                                    "gridAlpha": 0,
                                    "axisAlpha": 1,
                                    "position": "left",
                                    "title": "Cpu Usage (%)",
                                    "maximum" : 100,
                                    "minimum" : 0
                                },
                            ],
                            "graphs": [
                                {
                                    "valueAxis": "v1",
                                    "balloonText": "[[value]]%",
                                    "legendValueText": "[[value]]%",
                                    "lineColor": "rgb(31, 119, 180)",
                                    "fillColor": "rgb(31, 119, 180)",
                                    "title": "JVM",
                                    "valueField": "jvmCpuLoad",
                                    "fillAlphas": 0.4,
                                    "connect": false
                                },
                                {
                                    "valueAxis": "v1",
                                    "balloonText": "[[value]]%",
                                    "legendValueText": "[[value]]%",
                                    "lineColor": "rgb(174, 199, 232)",
                                    "fillColor": "rgb(174, 199, 232)",
                                    "title": "System",
                                    "valueField": "systemCpuLoad",
                                    "fillAlphas": 0.4,
                                    "connect": false
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
                                	return moment(valueText).format("HH:mm:ss");
                                }
                            }
                        };
                        $timeout(function () {
                            oChart = AmCharts.makeChart(sId, options);
                            oChart.chartCursor.addListener('changed', function (event) {
                                scope.$emit('cpuLoadChartDirective.cursorChanged.' + scope.namespace, event);
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
                     * scope event on cpuLoadChartDirective.initAndRenderWithData.namespace
                     */
                    scope.$on('cpuLoadChartDirective.initAndRenderWithData.' + scope.namespace, function (event, data, w, h) {
                        setIdAutomatically();
                        setWidthHeight(w, h);
                        render(data);
                    });

                    /**
                     * scope event on cpuLoadChartDirective.showCursorAt.namespace
                     */
                    scope.$on('cpuLoadChartDirective.showCursorAt.' + scope.namespace, function (event, category) {
                        showCursorAt(category)
                    });

                    /**
                     * scope event on cpuLoadChartDirective.resize.namespace
                     */
                    scope.$on('cpuLoadChartDirective.resize.' + scope.namespace, function (event) {
                        resize();
                    });
                }
            };
        }
    ]);
})();