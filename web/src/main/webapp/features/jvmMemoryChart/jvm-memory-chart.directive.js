(function() {
	'use strict';
	/**
	 * (en)jvmMemoryChartDirective 
	 * @ko jvmMemoryChartDirective
	 * @group Directive
	 * @name jvmMemoryChartDirective
	 * @class
	 */	
	angular.module("pinpointApp").directive("jvmMemoryChartDirective", [
        function () {
            return {
                template: '<div></div>',
                replace: true,
                restrict: 'E',
                scope: {
                    namespace: '@' // string value
                },
                link: function postLink(scope, element, attrs) {
                    var sId = "", oChart;

                    function setIdAutomatically() {
                        sId = 'multipleValueAxesId-' + scope.namespace;
                        element.attr('id', sId);
                    }
                    function hasId() {
						return sId === "" ? false : true;
					}

                    function setWidthHeight(w, h) {
                        if (w) element.css('width', w);
                        if (h) element.css('height', h);
                    }

                    function renderUpdate(data) {
						oChart.dataProvider = data;
						oChart.validateData();
					}

                    function render(chartData) {
                        var options = {
                            "type": "serial",
                            "theme": "light",
                            "autoMargins": false,
                            "marginTop": 10,
                            "marginLeft": 70,
                            "marginRight": 70,
                            "marginBottom": 40,
                            "legend": {
                                "useGraphSettings": true,
                                "autoMargins": false,
                                "align" : "right",
                                "position": "top",
                                "valueWidth": 70,
								"markerSize": 10,
								"valueAlign": "left"
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
                            "categoryField": "time",
                            "categoryAxis": {
                                "axisColor": "#DADADA",
                                "startOnAxis": true,
                                "gridPosition": "start",
                                "labelFunction": function (valueText) {
									return valueText.replace(/\s/, "<br>").replace(/-/g, ".").substring(2);
                                }
                            },
							"chartCursor": {
								"categoryBalloonAlpha": 0.7,
								"fullWidth": true,
								"cursorAlpha": 0.1,
								"listeners": [{
									"event": "changed",
									"method": function (event) {
										scope.$emit("jvmMemoryChartDirective.cursorChanged." + scope.namespace, event);
									}
								}]
							}
                        };
						oChart = AmCharts.makeChart(sId, options);
						// var oChartCursor = new AmCharts.ChartCursor({
						// 	"categoryBalloonAlpha": 0.7,
						// 	"fullWidth": true,
						// 	"cursorAlpha": 0.1
						// });
						// oChartCursor.addListener("changed", function (event) {
						// 	scope.$emit("jvmMemoryChartDirective.cursorChanged." + scope.namespace, event);
						// });
						// oChart.addChartCursor( oChartCursor );
                    }

					function showCursorAt(category) {
						if (category) {
							if (angular.isNumber(category)) {
								if ( oChart.dataProvider[category] && oChart.dataProvider[category].time ) {
									try {
										oChart.chartCursor.showCursorAt(oChart.dataProvider[category].time);
									} catch(e) {}
									return;
								}
							}
						}
						oChart.chartCursor.hideCursor();
					}

                    function resize() {
                        if (oChart) {
                            oChart.validateNow();
                            oChart.validateSize();
                        }
                    }
                    scope.$on('jvmMemoryChartDirective.initAndRenderWithData.' + scope.namespace, function (event, data, w, h) {
						if ( hasId() ) {
							renderUpdate( data );
						} else {
							setIdAutomatically();
							setWidthHeight(w, h);
							render(data);
						}
                    });
                    scope.$on('jvmMemoryChartDirective.showCursorAt.' + scope.namespace, function (event, category) {
                        showCursorAt(category);
                    });
                    scope.$on('jvmMemoryChartDirective.resize.' + scope.namespace, function (event) {
                        resize();
                    });
                }
            };
        }
    ]);
})();