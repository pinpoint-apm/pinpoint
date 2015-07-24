(function() {
	'use strict';
	/**
	 * (en)jvmMemoryChartDirective 
	 * @ko jvmMemoryChartDirective
	 * @group Directive
	 * @name jvmMemoryChartDirective
	 * @class
	 */	
	angular.module('pinpointApp').directive('jvmMemoryChartDirective', ['$timeout',
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
                                "autoMargins": false,
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
                                    "position": "right",
                                    "title": "Full GC (ms)",
                                    "minimum": 0
                                },
                                {
                                    "id": "v2",
                                    "gridAlpha": 0,
                                    "axisAlpha": 1,
                                    "position": "left",
                                    "title": "Memory (bytes)",
                                    "minimum": 0
                                }
                            ],
                            "graphs": [
                                {
                                    "valueAxis": "v2",
                                    "balloonText": "[[value]]B",
                                    "legendValueText": "[[value]]B",
                                    "lineColor": "rgb(174, 199, 232)",
                                    "title": "Max",
                                    "valueField": "Max",
                                    "fillAlphas": 0,
                                    "connect": false
                                },
                                {
                                    "valueAxis": "v2",
                                    "balloonText": "[[value]]B",
                                    "legendValueText": "[[value]]B",
                                    "lineColor": "rgb(31, 119, 180)",
                                    "fillColor": "rgb(31, 119, 180)",
                                    "title": "Used",
                                    "valueField": "Used",
                                    "fillAlphas": 0.4,
                                    "connect": false
                                },
                                {
                                    "valueAxis": "v1",
                                    "balloonFunction": function(item, graph) {
                                        var data = item.serialDataItem.dataContext;
                                        var balloonText = data.FGCTime + "ms";
                                        var fgcCount = data.FGCCount;
                                        if (fgcCount > 1) {
                                            balloonText += " (" + fgcCount + ")";
                                        }
                                        return balloonText;
                                    },
                                    "legendValueText": "[[value]]ms",
                                    "lineColor": "#FF6600",
                                    "title": "FGC",
                                    "valueField": "FGCTime",
                                    "type": "column",
                                    "fillAlphas": 0.3,
                                    "connect": false
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
                                	return valueText.substring( valueText.indexOf( " " ) + 1 );
                                }
                            }
                        };
                        $timeout(function () {
                            oChart = AmCharts.makeChart(sId, options);
                            oChart.chartCursor.addListener('changed', function (event) {
                                scope.$emit('jvmMemoryChartDirective.cursorChanged.' + scope.namespace, event);
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
                     * scope event on jvmMemoryChartDirective.initAndRenderWithData.namespace
                     */
                    scope.$on('jvmMemoryChartDirective.initAndRenderWithData.' + scope.namespace, function (event, data, w, h) {
                        setIdAutomatically();
                        setWidthHeight(w, h);
                        render(data);
                    });

                    /**
                     * scope event on jvmMemoryChartDirective.showCursorAt.namespace
                     */
                    scope.$on('jvmMemoryChartDirective.showCursorAt.' + scope.namespace, function (event, category) {
                        showCursorAt(category)
                    });

                    /**
                     * scope event on jvmMemoryChartDirective.resize.namespace
                     */
                    scope.$on('jvmMemoryChartDirective.resize.' + scope.namespace, function (event) {
                        resize();
                    });
                }
            };
        }
    ]);
})();