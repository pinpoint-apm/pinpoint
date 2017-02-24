(function() {
	'use strict';
	pinpointApp.directive( "dsEachChartDirective", [
		function () {
			return {
				template: "<div></div>",
				replace: true,
				restrict: "E",
				scope: {
					namespace: "@"
				},
				link: function postLink(scope, element, attrs) {
					var sId = "", oChart;

					function setIdAutomatically() {
						sId = 'multipleValueAxesId-each' + scope.namespace;
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
						oChart.valueAxes[0].maximum = data.max;
						oChart.validateNow();
						oChart.dataProvider = data.list;
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
								"valueWidth": 70
							},
							"usePrefixes": true,
							"dataProvider": chartData.list,
							"valueAxes": [
								{
									"id": "v1",
									"gridAlpha": 0,
									"axisAlpha": 1,
									"position": "left",
									"title": "Connection",
									"maximum" : chartData.max,
									"minimum" : 0
								}
							],
							"graphs": [
								{
									"valueAxis": "v1",
									"balloonText": "[[title]] : [[value]]",
									"legendValueText": "[[value]]",
									"lineColor": "rgb(174, 199, 232)",
									"fillColor": "rgb(174, 199, 232)",
									"title": "Active(avg)",
									"valueField": "activeAvg",
									"fillAlphas": 0.4,
									"connect": false
								},
								{
									"valueAxis": "v1",
									"balloonText": "[[title]] : [[value]]",

									"legendValueText": "[[value]]",
									"lineColor": "rgb(31, 119, 180)",
									"fillColor": "rgb(31, 119, 180)",
									"title": "Active(max)",
									"valueField": "activeMax",
									"fillAlphas": 0.4,
									"connect": false
								},
								{
									"valueAxis": "v1",
									"balloonText": "[[title]] : [[value]]",
									"legendValueText": "[[value]]",
									"lineColor": "#FF6600",
									"title": "Max",
									"valueField": "max",
									"fillAlphas": 0,
									"connect": false
								}
							],
							"chartCursor": {
								"categoryBalloonAlpha": 0.7,
								"fullWidth": true,
								"cursorAlpha": 0.1,
								"listeners": [{
									"event": "changed",
									"method": function(event) {
										scope.$emit('dsEachChartDirective.cursorChanged.' + scope.namespace, event);
									}
								}]
							},
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
						oChart = AmCharts.makeChart(sId, options);
					}

					function showCursorAt(category) {
						if (category) {
							if (angular.isNumber(category)) {
								if ( oChart.dataProvider[category] && oChart.dataProvider[category].time ) {
									oChart.chartCursor.showCursorAt(oChart.dataProvider[category].time);
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
					scope.$on("dsEachChartDirective.initAndRenderWithData." + scope.namespace, function (event, data, w, h) {
						if ( hasId() ) {
							renderUpdate( data );
						} else {
							setIdAutomatically();
							setWidthHeight(w, h);
							render(data);
						}
					});
					scope.$on("dsEachChartDirective.showCursorAt." + scope.namespace, function (event, category) {
						showCursorAt(category);
					});
					scope.$on("dsEachChartDirective.resize." + scope.namespace, function (event) {
						resize();
					});
				}
			};
		}
	]);
})();