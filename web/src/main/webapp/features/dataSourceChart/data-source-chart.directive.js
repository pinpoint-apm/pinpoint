(function() {
	'use strict';
	pinpointApp.directive( "dsChartDirective", [ "helpContentService",
		function ( helpContentService ) {
			return {
				template: "<div style='width:100%;height:270px;'><div></div></div>",
				replace: true,
				restrict: "E",
				scope: {
					namespace: "@"
				},
				link: function postLink(scope, element, attrs) {
					var sId = "", oChart;
					var currentChartData;
					var elNoData;
					var noDataCollected = helpContentService.inspector.noDataCollected;
					var aColorMap = [
						"#850901", "#969755", "#421416", "#c8814b", "#aa8735", "#cd7af4", "#f6546a", "#1c1a1f", "#127999", "#b7ebd9",
						"#f6546a", "#bea87f", "#d1b4b0", "#e0d4ba", "#0795d9", "#43aa83", "#09d05b", "#c26e67", "#ed7575", "#96686a"
					];

					function setIdAutomatically() {
						sId = "multipleValueAxesId-" + scope.namespace;
						element.find("div").attr("id", sId);
					}
					// function hasId() {
					// 	return sId === "" ? false : true;
					// }
					function setWidthHeight(w, h) {
						if (w) element.find("#"+ sId).css("width", w);
						if (h) element.find("#"+ sId).css("height", h);
					}
					// function renderUpdate(data) {
					// 	oChart.dataProvider = data;
					// 	oChart.validateData();
					// }
					function render(oVisible) {
						var options = {
							"type": "serial",
							"theme": "light",
							"autoMargins": false,
							"marginTop": 10,
							"marginLeft": 70,
							"marginRight": 70,
							"marginBottom": 40,
							"usePrefixes": true,
							"dataProvider": currentChartData.data,
							"switchable": true,
							"valueAxes": [
								{
									"gridAlpha": 0,
									"axisAlpha": 1,
									"position": "left",
									"title": "Connection(count)",
									"maximum" : 10,
									"minimum" : 0,
									"labelFunction": function(value) {
										return parseInt(value);
									}
								}
							],
							"graphs": [],
							"chartCursor": {
								"oneBalloonOnly": true,
								"categoryBalloonAlpha": 0.7,
								"fullWidth": true,
								"cursorAlpha": 0.1,
								"listeners": [{
									"event": "changed",
									"method": function(event) {
										if ( event.mostCloseGraph && event.index ) {
											showBalloonData(event.mostCloseGraph.valueField, event.index);
										} else {
											showBalloonData("");
										}
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
						var index = 0;
						for( var p in currentChartData.data[0] ) {
							if ( p === "time" ) {
								continue;
							}
							var aSplit = p.split("_");
							options.graphs.push({
								"balloonFunction": function() {
									return "";
								},
								"lineColor": getNextColor(index++),
								"title": aSplit[1],
								"valueField": p,
								"fillAlphas": 0,
								"connect": false,
								"hidden": !oVisible[p]
							});
						}
						if ( index === 0 ) {
							options.graphs.push({
								"balloonFunction": function() {
									return "";
								},
								"lineColor": getNextColor(index),
								"title": "0",
								"valueField": p,
								"fillAlphas": 0,
								"connect": false,
								"hidden": false
							});
						}
						if ( currentChartData.empty || currentChartData.forceMax ) {
							options.valueAxes[0].maximum = currentChartData["defaultMax"];
						}
						oChart = AmCharts.makeChart(sId, options);
						addNoDataElement();
						elNoData[currentChartData["empty"] ? "show" : "hide"]();
					}
					function showBalloonData( valueField, index ) {
						scope.$emit("dsChartDirective.cursorChanged." + scope.namespace, valueField, index);
					}
					function addNoDataElement() {
						elNoData = element.append('<div class="no-data"><span>' + noDataCollected + '</span></div>').find(".no-data").hide();
					}

					function getNextColor( i ) {
						if ( i < aColorMap.length ) {
							return aColorMap[i];
						} else {
							return "#" + getRandomInt() + getRandomInt() + getRandomInt();
						}
					}

					function getRandomInt() {
						var v = Math.floor(Math.random() * 255).toString(16);
						if( v.length == 1 ) {
							return "0" + v;
						} else {
							return v;
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
					function getGraphId( graphId ) {
						for( var i = 0 ; i < oChart.graphs.length ; i++ ) {
							if ( oChart.graphs[i].valueField === graphId ) {
								return oChart.graphs[i].id;
							}
						}
						return "";
					}
					scope.$on("dsChartDirective.initAndRenderWithData." + scope.namespace, function (event, oChartData, oVisible, w, h) {
						currentChartData = oChartData;
						// if ( hasId() ) {
						// 	renderUpdate( data );
						// } else {
							element.html("<div></div>");
							setIdAutomatically();
							setWidthHeight(w, h);
							render( oVisible );
						// }
					});
					scope.$on("dsChartDirective.toggleGraph." + scope.namespace, function(event, valueField, bVisible) {
						var graphId = getGraphId( valueField );
						if ( graphId !== "" ) {
							oChart[bVisible ? "showGraph" : "hideGraph"](oChart.getGraphById(graphId));
						}
					});
					scope.$on("dsChartDirective.toggleGraphAll." + scope.namespace, function() {
						for( var i = 0 ; i < oChart.graphs.length ; i++ ) {
							oChart.showGraph( oChart.graphs[i] );
						}
					});
					scope.$on("dsChartDirective.showCursorAt." + scope.namespace, function (event, category) {
						if ( currentChartData && currentChartData.empty === false ) {
							showCursorAt(category);
						}
					});
					scope.$on("dsChartDirective.resize." + scope.namespace, function (event) {
						resize();
					});
				}
			};
		}
	]);
})();