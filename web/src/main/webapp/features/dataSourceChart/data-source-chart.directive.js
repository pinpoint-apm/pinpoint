(function() {
	'use strict';
	pinpointApp.directive( "dsChartDirective", [
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
					var aColorMap = [
						"#850901", "#969755", "#421416", "#c8814b", "#aa8735", "#cd7af4", "#f6546a", "#1c1a1f", "#127999", "#b7ebd9",
						"#f6546a", "#bea87f", "#d1b4b0", "#e0d4ba", "#0795d9", "#43aa83", "#09d05b", "#c26e67", "#ed7575", "#96686a"
					];

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
					// function renderUpdate(data) {
					// 	oChart.dataProvider = data;
					// 	oChart.validateData();
					// }
					function render(oChartData, maxValue, oVisible) {
						var options = {
							"type": "serial",
							"theme": "light",
							"autoMargins": false,
							"marginTop": 10,
							"marginLeft": 70,
							"marginRight": 70,
							"marginBottom": 40,
							"usePrefixes": true,
							"dataProvider": oChartData,
							"switchable": true,
							"valueAxes": [
								{
									"id": "v1",
									"gridAlpha": 0,
									"axisAlpha": 1,
									"position": "left",
									"title": "Connection",
									"maximum" : Math.max( maxValue, 10 ),
									"minimum" : 0
								}
							],
							"graphs": [],
							"chartCursor": {
								"cursorColor": "#C10000",
								"oneBalloonOnly": true,
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
						for( var p in oChartData[0] ) {
							if ( p === "time" ) {
								continue;
							}
							var aSplit = p.split("_");
							options.graphs.push({
								"valueAxis": "v1",
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
						oChart = AmCharts.makeChart(sId, options);
					}
					function showBalloonData( valueField, index ) {
						scope.$emit("dsChartDirective.cursorChanged." + scope.namespace, valueField, index);
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
					function getGraphId( graphId ) {
						for( var i = 0 ; i < oChart.graphs.length ; i++ ) {
							if ( oChart.graphs[i].valueField === graphId ) {
								return oChart.graphs[i].id;
							}
						}
						return "";
					}
					scope.$on("dsChartDirective.initAndRenderWithData." + scope.namespace, function (event, data, oVisible, w, h) {
						// if ( hasId() ) {
						// 	renderUpdate( data );
						// } else {
							element.empty();
							setIdAutomatically();
							setWidthHeight(w, h);
							render(data.data, data.max, oVisible);
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
						showCursorAt(category);
					});
					scope.$on("dsChartDirective.resize." + scope.namespace, function (event) {
						resize();
					});
				}
			};
		}
	]);
})();