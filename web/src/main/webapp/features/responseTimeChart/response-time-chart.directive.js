(function() {
	"use strict";
	angular.module( "pinpointApp" ).directive( "responseTimeChartDirective", [
		function () {
			return {
				template: "<div></div>",
				replace: true,
				restrict: "E",
				scope: {
					namespace: "@" // string value
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
									"title": "Response Time",
									"minimum" : 0
								}
							],
							"graphs": [
								{
									"balloonText": "[[description]] : [[value]]",
									"legendValueText": "([[description]]) [[value]]",
									"lineColor": "rgb(44, 160, 44)",
									"fillColor": "rgb(44, 160, 44)",
									"title": "AVG",
									"descriptionField": "title",
									"valueField": "avg",
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
										scope.$emit("responseTimeChartDirective.cursorChanged." + scope.namespace, event);
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
						// 	scope.$emit("responseTimeChartDirective.cursorChanged." + scope.namespace, event);
						// });
						// oChart.addChartCursor( oChartCursor );
					}
					function showCursorAt(category) {
						if (category) {
							if (angular.isNumber(category)) {
								if ( oChart.dataProvider[category] && oChart.dataProvider[category].time ) {
									try {
										oChart.chartCursor.showCursorAt(oChart.dataProvider[category].time);
									}catch(e) {}
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
					scope.$on( "responseTimeChartDirective.initAndRenderWithData." + scope.namespace, function (event, data, w, h) {
						if ( hasId() ) {
							renderUpdate( data );
						} else {
							setIdAutomatically();
							setWidthHeight(w, h);
							render(data);
						}
					});

					scope.$on( "responseTimeChartDirective.showCursorAt." + scope.namespace, function (event, category) {
						showCursorAt(category);
					});
					scope.$on( "responseTimeChartDirective.resize." + scope.namespace, function (event) {
						resize();
					});
				}
			};
		}
	]);
})();