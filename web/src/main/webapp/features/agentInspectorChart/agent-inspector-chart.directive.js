(function() {
	'use strict';
	angular.module("pinpointApp").directive("agentInspectorChartDirective", [ "helpContentService",
		function ( helpContentService ) {
			return {
				template: "<div style='width:100%;height:270px;'><div class='chart'></div><div class='loading'><i class='xi-spinner-3 xi-spin xi-3x'></i></div><div class='no-data'><span></span></div></div>",
				replace: true,
				restrict: "E",
				scope: {
					namespace: "@"
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
						if ( currentChartData.empty || currentChartData.forceMax ) {
							setYMax( oChart );
						} else {
							removeYMax( oChart );
						}
						oChart.validateData();
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

					function render(chartOptions) {
						chartOptions["chartCursor"]["listeners"] = [{
							"event": "changed",
							"method": function (event) {
								scope.$emit( "agentInspectorChartDirective.cursorChanged", scope.namespace, event );
							}
						}];
						if ( currentChartData.empty || currentChartData.forceMax ) {
							setYMax( chartOptions );
						}
						oChart = AmCharts.makeChart(sId, chartOptions);
						elNoData[currentChartData["empty"] ? "show" : "hide"]();
						elLoading.hide();
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
						} else {
							if ( angular.isString(category) ) {
								try {
									oChart.chartCursor.showCursorAt(category);
								} catch(e) {}
								return;
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
					scope.$on("agentInspectorChartDirective.initAndRenderWithData." + scope.namespace, function (event, oChartData, oChartOptions, w, h) {
						elLoading.show();
						currentChartData = oChartData;
						if ( hasId() ) {
							renderUpdate();
						} else {
							setIdAutomatically();
							setWidthHeight( w, h );
							render( oChartOptions );
						}
					});

					scope.$on("agentInspectorChartDirective.showLoading." + scope.namespace, function() {
						elLoading.show();
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