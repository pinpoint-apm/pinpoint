(function() {
    'use strict';
    /**
     * (en)tpsChartDirective 
     * @ko tpsChartDirective
     * @group Directive
     * @name tpsChartDirective
     * @class
     */ 
    angular.module("pinpointApp").directive("tpsChartDirective", [
        function () {
            return {
                template: '<div></div>',
                replace: true,
                restrict: 'E',
                scope: {
                    namespace: '@' // string value
                },
                link: function postLink(scope, element, attrs) {
    
                    // define variables
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
                                "autoMargins": true,
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
                                	"stackType": "regular",
                                    "gridAlpha": 0,
                                    "axisAlpha": 1,
                                    "position": "left",
                                    "title": "TPS",
                                    "minimum" : 0
                                }
                            ],
                            "graphs": [
                                {
                                    "balloonText": "Sampled Continuation : [[value]]",
                                    "legendValueText": "[[value]]",
                                    "lineColor": "rgb(214, 141, 8)",
                                    "fillColor": "rgb(214, 141, 8)",
                                    "title": "S.C",
                                    "valueField": "sampledContinuationTps",
                                    "fillAlphas": 0.4,
                                    "connect": false
                                },{
                                    "balloonText": "Sampled New : [[value]]",
                                    "legendValueText": "[[value]]",
                                    "lineColor": "rgb(252, 178, 65)",
                                    "fillColor": "rgb(252, 178, 65)",
                                    "title": "S.N",
                                    "valueField": "sampledNewTps",
                                    "fillAlphas": 0.4,
                                    "connect": false
                                },{
                                    "balloonText": "Unsampled Continuation : [[value]]",
                                    "legendValueText": "[[value]]",
                                    "lineColor": "rgb(90, 103, 166)",
                                    "fillColor": "rgb(90, 103, 166)",
                                    "title": "U.C",
                                    "valueField": "unsampledContinuationTps",
                                    "fillAlphas": 0.4,
                                    "connect": false
                                },{
                                    "balloonText": "Unsampled New : [[value]]",
                                    "legendValueText": "[[value]]",
                                    "lineColor": "rgb(160, 153, 255)",
                                    "fillColor": "rgb(160, 153, 255)",
                                    "title": "U.N",
                                    "valueField": "unsampledNewTps",
                                    "fillAlphas": 0.4,
                                    "connect": false
                                },{
                                    "balloonText": "Total : [[value]]",
                                    "legendValueText": "[[value]]",
                                    "lineColor": "rgba(31, 119, 180, 0)",
                                    "fillColor": "rgba(31, 119, 180, 0)",
									"title": "Total",
                                    "valueField": "totalTps",
                                    "fillAlphas": 0.4,
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
										scope.$emit("tpsChartDirective.cursorChanged." + scope.namespace, event);
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
						// oChartCursor.addListener('changed', function (event) {
						// 	scope.$emit('tpsChartDirective.cursorChanged.' + scope.namespace, event);
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

                    scope.$on('tpsChartDirective.initAndRenderWithData.' + scope.namespace, function (event, data, w, h) {
						if ( hasId() ) {
							renderUpdate( data );
						} else {
							setIdAutomatically();
							setWidthHeight(w, h);
							render(data);
						}
                    });

                    scope.$on('tpsChartDirective.showCursorAt.' + scope.namespace, function (event, category) {
                        showCursorAt(category);
                    });

                    scope.$on('tpsChartDirective.resize.' + scope.namespace, function (event) {
                        resize();
                    });
                }
            };
        }
    ]);
})();