(function() {
	'use strict';
	angular.module("pinpointApp").directive("statisticChartDirective", [
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
					function getUnit(val) {
						return val ? "%" : "";
					}

					function setWidthHeight(w, h) {
						if (w) element.css('width', w);
						if (h) element.css('height', h);
					}

					function renderUpdate(data) {
						oChart.dataProvider = data.data;
						oChart.validateData();
					}

					function render(chartData) {
						var unit = getUnit(chartData.maximum);
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
								"valueWidth": 70
							},
							"usePrefixes": true,
							"dataProvider": chartData.data,
							"valueAxes": [
								{
									"id": "v1",
									"gridAlpha": 0,
									"axisAlpha": 1,
									"position": "left",
									"title": chartData.chartTitle,
									"minimum" : 0
								}
							],
							"graphs": [
								{
									"valueAxis": "v1",
									"balloonText": "[[title]] : [[value]]" + unit + "<br><strong>[[description]]</strong>",
									"legendValueText": "[[value]]" + unit,
									"lineColor": "#66B2FF",
									"fillColor": "#66B2FF",
									"lineThickness": 3,
									"title": chartData.title[2],
									"valueField": chartData.field[2], // min
									"descriptionField": chartData.field[4],
									"fillAlphas": 0,
									"connect": false
								},
								{
									"valueAxis": "v1",
									"balloonText": "[[title]] : [[value]]" + unit,
									"legendValueText": "[[value]]" + unit,
									"lineColor": "#4C0099",
									"fillColor": "#4C0099",
									"lineThickness": 6,
									"title": chartData.title[0],
									"valueField": chartData.field[0], // avg
									"fillAlphas": 0,
									"connect": false
								},
								{
									"valueAxis": "v1",
									"balloonText": "[[title]] : [[value]]" + unit + "<br><strong>[[description]]</strong>",
									"legendValueText": "[[value]]" + unit,
									"lineColor": "#0000CC",
									"fillColor": "#0000CC",
									"lineThickness": 3,
									"title": chartData.title[1],
									"valueField": chartData.field[1], // max
									"descriptionField": chartData.field[3],
									"fillAlphas": 0,
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
							}
						};
						if ( chartData.maximum ) {
							options["valueAxes"][0].maximum = 100;
						}
						oChart = AmCharts.makeChart(sId, options);
						var oChartCursor = new AmCharts.ChartCursor({
							"categoryBalloonAlpha": 0.7,
							"fullWidth": true,
							"cursorAlpha": 0.1
						});
						oChartCursor.addListener('changed', function (event) {
							scope.$emit('statisticChartDirective.cursorChanged.' + scope.namespace, event);
						});
						oChart.addChartCursor( oChartCursor );
					}

					function showCursorAt(category) {
						if (category) {
							if (angular.isNumber(category)) {
								category = oChart.dataProvider[category].time;
							}
							oChart.chartCursor.showCursorAt(category);
						} else {
							oChart.chartCursor.hideCursor();
						}
					}

					function resize() {
						if (oChart) {
							oChart.validateNow();
							oChart.validateSize();
						}
					}
					scope.$on("statisticChartDirective.initAndRenderWithData." + scope.namespace, function (event, data, w, h) {
						if ( hasId() ) {
							renderUpdate( data );
						} else {
							setIdAutomatically();
							setWidthHeight(w, h);
							render(data);
						}
					});

					// scope.$on('statisticChartDirective.showCursorAt.' + scope.namespace, function (event, category) {
					// 	showCursorAt(category);
					// });

					scope.$on('statisticChartDirective.resize.' + scope.namespace, function (event) {
						resize();
					});
				}
			};
		}
	]);
})();