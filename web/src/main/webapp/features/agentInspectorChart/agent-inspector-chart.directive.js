(function() {
	'use strict';
	angular.module("pinpointApp").directive("agentInspectorChartDirective", [ "helpContentService",
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

					function setIdAutomatically() {
						sId = "multipleValueAxesId-" + scope.namespace;
						element.find("div").attr("id", sId);
					}
					function hasId() {
						return sId === "" ? false : true;
					}
					function setWidthHeight(w, h) {
						if (w) element.find("#"+ sId).css("width", w);
						if (h) element.find("#"+ sId).css("height", h);
					}

					function renderUpdate() {
						oChart.dataProvider = currentChartData.data;
						setYMax( oChart );
						oChart.validateData();
						elNoData[currentChartData["empty"] ? "show" : "hide"]();
					}
					function setYMax( oTarget ) {
						for( var i = 0 ; i < oTarget["valueAxes"].length ; i++ ) {
							oTarget["valueAxes"][i].maximum = currentChartData["defaultMax"];
						}
					}

					function render(chartOptions) {
						chartOptions["chartCursor"]["listeners"] = [{
							"event": "changed",
							"method": function (event) {
								scope.$emit( "agentInspectorChartDirective.cursorChanged", scope.namespace, event );
							}
						}];
						if ( currentChartData.empty ) {
							setYMax( chartOptions );
						}
						oChart = AmCharts.makeChart(sId, chartOptions);
						addNoDataElement();
						elNoData[currentChartData["empty"] ? "show" : "hide"]();
					}
					function addNoDataElement() {
						elNoData = element.append('<div class="no-data"><span>' + noDataCollected + '</span></div>').find(".no-data").hide();
					}
					function showCursorAt(category) {
						if (category && angular.isNumber(category)) {
							if ( oChart.dataProvider[category] && oChart.dataProvider[category].time ) {
								try {
									oChart.chartCursor.showCursorAt(oChart.dataProvider[category].time);
									return;
								} catch(e) {}
							}
						} else {
							try {
								oChart.chartCursor.showCursorAt(category);
								return;
							} catch(e) {}
						}
						oChart.chartCursor.hideCursor();
					}
					function resize() {
						if (oChart) {
							oChart.validateNow();
							oChart.validateSize();
						}
					}
					scope.$on("agentInspectorChartDirective.initAndRenderWithData." + scope.namespace, function (event, oChartData, oChartOptions, w, h) {
						currentChartData = oChartData;
						if ( hasId() ) {
							renderUpdate();
						} else {
							setIdAutomatically();
							setWidthHeight( w, h );
							render( oChartOptions );
						}
					});

					scope.$on("agentInspectorChartDirective.showCursorAt", function (event, sourceNamespace, category ) {
						if ( currentChartData && currentChartData.empty === false && scope.namespace !== sourceNamespace) {
							showCursorAt(category);
						}
					});

					scope.$on("agentInspectorChartDirective.resize." + scope.namespace, function() {
						resize();
					});
				}
			};
		}
	]);
})();