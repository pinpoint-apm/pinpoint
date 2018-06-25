(function() {
	'use strict';
	angular.module("pinpointApp").directive("statisticChartDirective", [ "helpContentService",
		function (helpContentService) {
			return {
				template: "<div style='width:100%;height:270px'><div class='chart'></div><div class='loading'><i class='xi-spinner-3 xi-spin xi-3x'></i></div><div class='no-data'><span></span></div></div>",
				replace: true,
				restrict: "E",
				scope: {
					namespace: "@" // string value
				},
				link: function postLink(scope, element, attrs) {
					var sId = "", oChart;
					var currentChartData;
					var elChart;
					var elNoData;
					var elLoading;
					var noDataCollected = helpContentService.inspector.noDataCollected;
					elChart = element.find("div.chart");
					elLoading = element.find("div.loading");
					elNoData = element.find("div.no-data").hide();
					elNoData.find("span").html( noDataCollected );

					function setIdAutomatically() {
						sId = "multipleValueAxesId-" + scope.namespace;
						elChart.attr("id", sId);
					}

					function hasId() {
						return sId === "" ? false : true;
					}
					function setWidthHeight(w, h) {
						if (w) elChart.css("width", w);
						if (h) elChart.css("height", h);
					}

					function renderUpdate() {
						oChart.dataProvider = currentChartData.data;
						if ( currentChartData.empty || currentChartData.fixMax ) {
							setYMax( oChart );
						} else {
							removeYMax( oChart );
						}
						oChart.validateData();
						elNoData[currentChartData["empty"] ? "show" : "hide"]();
						elLoading.hide();
					}

					function render() {
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
								"valueAlign": "left",
								"valueFunction": function(graphDataItem, valueText) {
									if ( parseInt( valueText ) === -1 ) {
										return "";
									}
									return valueText;
								}
							},
							"usePrefixes": true,
							"dataProvider": currentChartData.data,
							"valueAxes": [
								{
									"id": "v1",
									"gridAlpha": 0,
									"axisAlpha": 1,
									"position": "left",
									"title": currentChartData.yAxisTitle,
									"minimum" : 0,
									"labelFunction": currentChartData.labelFunc
								}
							],
							"graphs": [
								{
									"valueAxis": "v1",
									"balloonText": "[[title]] : [[value]]" + currentChartData.appendUnit + "<br><strong>[[description]]</strong>",
									"legendValueText": "[[value]]" + currentChartData.appendUnit,
									"lineColor": "#66B2FF",
									"fillColor": "#66B2FF",
									"lineThickness": 1.5,
									"title": currentChartData.category[2],
									"valueField": currentChartData.field[2], // min
									"descriptionField": currentChartData.field[4],
									"fillAlphas": 0,
									"connect": false,
									"dashLength" : 2
								},
								{
									"valueAxis": "v1",
									"balloonText": "[[title]] : [[value]]" + currentChartData.appendUnit,
									"legendValueText": "[[value]]" + currentChartData.appendUnit,
									"lineColor": "#4C0099",
									"fillColor": "#4C0099",
									"lineThickness": 1.5,
									"title": currentChartData.category[0],
									"valueField": currentChartData.field[0], // avg
									"fillAlphas": 0,
									"connect": false
								},
								{
									"valueAxis": "v1",
									"balloonText": "[[title]] : [[value]]" + currentChartData.appendUnit + "<br><strong>[[description]]</strong>",
									"legendValueText": "[[value]]" + currentChartData.appendUnit,
									"lineColor": "#0000CC",
									"fillColor": "#0000CC",
									"lineThickness": 1.5,
									"title": currentChartData.category[1],
									"valueField": currentChartData.field[1], // max
									"descriptionField": currentChartData.field[3],
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
								"listeners": [{
									"event": "changed",
									"method": function (event) {
										scope.$emit("statisticChartDirective.cursorChanged", event, scope.namespace);
									}
								}]
							}
						};
						if ( currentChartData.empty || currentChartData.fixMax ) {
							setYMax( options );
						}
						oChart = AmCharts.makeChart(sId, options);
						elNoData[currentChartData["empty"] ? "show" : "hide"]();
						elLoading.hide();
					}
					function setYMax( oTarget ) {
						for( var i = 0 ; i < oTarget["valueAxes"].length ; i++ ) {
							oTarget["valueAxes"][i].maximum = currentChartData["defaultMax"];
						}
					}
					function removeYMax( oTarget ) {
						for( var i = 0 ; i < oTarget["valueAxes"].length ; i++ ) {
							delete oTarget["valueAxes"][i].maximum;
						}
					}
					function showCursorAt(category) {
						if (category && angular.isNumber(category)) {
							if ( category >= oChart.startIndex && category <= oChart.endIndex ) {
								if (oChart.dataProvider[category] && oChart.dataProvider[category].time) {
									try {
										oChart.chartCursor.showCursorAt(oChart.dataProvider[category].time);
									} catch (e) {}
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
					scope.$on("statisticChartDirective.initAndRenderWithData." + scope.namespace, function (event, chartData, w, h) {
						elLoading.show();
						currentChartData = chartData;
						if ( hasId() ) {
							renderUpdate();
						} else {
							setIdAutomatically();
							setWidthHeight(w, h);
							render();
						}
					});
					scope.$on("statisticChartDirective.showCursorAt", function (event, category, namespace) {
						if ( currentChartData && currentChartData.empty === false && scope.namespace !== namespace ) {
							showCursorAt(category);
						}
					});
					scope.$on("statisticChartDirective.resize." + scope.namespace, function (event) {
						resize();
					});
				}
			};
		}
	]);
})();