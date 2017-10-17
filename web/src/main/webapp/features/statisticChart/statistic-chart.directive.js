(function() {
	'use strict';
	angular.module("pinpointApp").directive("statisticChartDirective", [ "helpContentService",
		function (helpContentService) {
			return {
				template: '<div></div>',
				replace: true,
				restrict: 'E',
				scope: {
					namespace: '@' // string value
				},
				link: function postLink(scope, element, attrs) {
					var sId = "", oChart;
					var elNoData;
					var noDataCollected = helpContentService.inspector.noDataCollected;

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

					function renderUpdate(chartData) {
						oChart.dataProvider = chartData.data;
						oChart.validateData();
						elNoData[chartData["empty"] ? "show" : "hide"]();
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
								"valueWidth": 50,
								"markerSize": 10,
								"valueAlign": "left"
							},
							"usePrefixes": true,
							"dataProvider": chartData.data,
							"valueAxes": [
								{
									"id": "v1",
									"gridAlpha": 0,
									"axisAlpha": 1,
									"position": "left",
									"title": chartData.yAxisTitle,
									"minimum" : 0,
									"labelFunction": chartData.labelFunc
								}
							],
							"graphs": [
								{
									"valueAxis": "v1",
									"balloonText": "[[title]] : [[value]]" + chartData.appendUnit + "<br><strong>[[description]]</strong>",
									"legendValueText": "[[value]]" + chartData.appendUnit,
									"lineColor": "#66B2FF",
									"fillColor": "#66B2FF",
									"lineThickness": 1.5,
									"title": chartData.category[2],
									"valueField": chartData.field[2], // min
									"descriptionField": chartData.field[4],
									"fillAlphas": 0,
									"connect": false,
									"dashLength" : 2
								},
								{
									"valueAxis": "v1",
									"balloonText": "[[title]] : [[value]]" + chartData.appendUnit,
									"legendValueText": "[[value]]" + chartData.appendUnit,
									"lineColor": "#4C0099",
									"fillColor": "#4C0099",
									"lineThickness": 1.5,
									"title": chartData.category[0],
									"valueField": chartData.field[0], // avg
									"fillAlphas": 0,
									"connect": false
								},
								{
									"valueAxis": "v1",
									"balloonText": "[[title]] : [[value]]" + chartData.appendUnit + "<br><strong>[[description]]</strong>",
									"legendValueText": "[[value]]" + chartData.appendUnit,
									"lineColor": "#0000CC",
									"fillColor": "#0000CC",
									"lineThickness": 1.5,
									"title": chartData.category[1],
									"valueField": chartData.field[1], // max
									"descriptionField": chartData.field[3],
									"fillAlphas": 0,
									"connect": false,
									"dashLength" : 2
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
								listeners: [{
									"event": "changed",
									"method": function (event) {
										scope.$emit("statisticChartDirective.cursorChanged", event, scope.namespace);
									}
								}]
							}
						};
						if ( chartData.empty || chartData.fixMax ) {
							options["valueAxes"][0].maximum = chartData["defaultMax"];
						}
						oChart = AmCharts.makeChart(sId, options);

						addNoDataElement();
						elNoData[chartData["empty"] ? "show" : "hide"]();
					}
					function addNoDataElement() {
						elNoData = element.append('<div class="no-data"><span>' + noDataCollected + '</span></div>').find(".no-data").hide();
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
					scope.$on('statisticChartDirective.showCursorAt', function (event, category, namespace) {
						if ( scope.namespace !== namespace ) {
							showCursorAt(category);
						}
					});
					scope.$on('statisticChartDirective.hide', function () {
						console.log( "hide statistic chart" );
					});
					scope.$on('statisticChartDirective.resize.' + scope.namespace, function (event) {
						resize();
					});
				}
			};
		}
	]);
})();