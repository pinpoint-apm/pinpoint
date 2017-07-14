(function() {
	"use strict";
	pinpointApp.constant("responseTimeSummaryChartDirectiveConfig", {});
	
	pinpointApp.directive("responseTimeSummaryChartDirective", ["responseTimeSummaryChartDirectiveConfig", "$timeout", "AnalyticsService", "PreferenceService", "CommonUtilService",
        function (cfg, $timeout, AnalyticsService, PreferenceService, CommonUtilService ) {
			var responseTypeColor = PreferenceService.getResponseTypeColor();
			var chartBackgroundAlpha = [ "0.2", "0.3", "0.4", "0.6", "0.6" ];
			var chartBackgroundColor = responseTypeColor.map(function(color, index){
				var r = parseInt( color.substring(1, 3), 16 );
				var g = parseInt( color.substring(3, 5), 16 );
				var b = parseInt( color.substring(5), 16 );
				return "rgba(" + r + ", " + g + ", " + b + ", " + chartBackgroundAlpha[index] + ")";
			});
            return {
                template: "<div style='user-select:none;'></div>",
                replace: true,
                restrict: "EA",
                scope: {
                    namespace: "@" // string value
                },
                link: function postLink( scope, element ) {
                    var id, oChart;

                    function setIdAutomatically() {
                        id = "responseTimeId-" + scope.namespace;
                        element.attr("id", id);
                    }
                    function setWidthHeight(w, h) {
                        element.css("width", w || "100%");
                        element.css("height", h || "150px");
                    }
                 function renderChart(data, useFilterTransaction, useChartCursor) {
                    	element.empty().append("<canvas>");
						oChart = new Chart(element.find("canvas"), {
							type: "bar",
							data: {
								labels: data.keys,
								datasets: [{
									data: data.values,
									backgroundColor: chartBackgroundColor,
									borderColor: [
										"rgba(120, 119, 121, 0.8)",
										"rgba(120, 119, 121, 0.8)",
										"rgba(120, 119, 121, 0.8)",
										"rgba(120, 119, 121, 0.8)",
										"rgba(120, 119, 121, 0.8)"
									],
									borderWidth: 0.5
								}]
							},
							options: {
								onClick: function( event, aTarget ) {
									if ( aTarget.length > 0 ) {
										var type = aTarget[0]._view.label;
										if (type == "Error") {
											AnalyticsService.send(AnalyticsService.CONST.MAIN, AnalyticsService.CONST.CLK_RESPONSE_GRAPH);
											scope.$emit('responseTimeSummaryChartDirective.showErrorTransactionList', type);
										}
										if (useFilterTransaction) {
											scope.$emit('responseTimeSummaryChartDirective.itemClicked.' + scope.namespace, {
												"responseTime": type,
												"count": aTarget[0]._chart.config.data.datasets[0].data[aTarget[0]._index]
											});
										}
									}
									event.preventDefault();
								},
								maintainAspectRatio: false,
								legend: {
									display: false
								},
								title: {
									text: "",
									display: true,
									fontSize: 8,
									padding: 6
								},
								scales: {
									yAxes: [{
										gridLines: {
											color: "rgba(0, 0, 0, 0.1)",
											zeroLineColor: "rgba(0, 0, 0, 1)",
											zeroLineWidth: 0.5
										},
										ticks: {
											beginAtZero:true,
											maxTicksLimit: 3,
											callback: function(label) {
												if ( label >= 1000 ) {
													return "    " + label/1000 + 'k';
												} else {
													return "    " + label;
												}
											}
										}
									}],
									xAxes: [{
										gridLines: {
											color: "rgba(255, 255, 255, 0)",
											zeroLineColor: "rgba(0, 0, 0, 1)",
											zeroLineWidth: 0.5
										}
									}]
								},
								animation: {
									duration: 0,
									onComplete: function() {
										var ctx = this.chart.ctx;
										ctx.font = Chart.helpers.fontString(Chart.defaults.global.defaultFontSize, 'normal', Chart.defaults.global.defaultFontFamily);
										ctx.fillStyle = this.chart.config.options.defaultFontColor;
										ctx.textAlign = 'center';
										ctx.textBaseline = 'bottom';
										this.data.datasets.forEach(function (dataset) {
											for (var i = 0; i < dataset.data.length; i++) {
												var model = dataset._meta[Object.keys(dataset._meta)[0]].data[i]._model;
												ctx.fillText( CommonUtilService.addComma(dataset.data[i]), model.x, model.y - 5);
											}
										});
									}
								},
								hover: {
									animationDuration: 0
								},
								tooltips: {
									enabled: false
								}
							}
						});
					}

                    function clickGraphItemListener(event) {
                        scope.$emit("responseTimeSummaryChartDirective.itemClicked." + scope.namespace, event.item.serialDataItem.dataContext);
                    }
                    function updateData(data) {
                	    oChart.dataProvider = data;
						oChart.validateData();
                    }
                    function updateChart(data) {
                    	oChart.data.labels = data.keys;
						oChart.data.datasets[0].data = data.values;
						oChart.update();
					}
                    function parseHistogram(data) {
                    	if ( typeof data === "undefined" || data === null ) {
                    		return {
                    			keys: ["1s", "3s", "5s", "Slow", "Error"],
								values: [0, 0, 0, 0, 0]
							};
						}
						var oRet = {
							keys: [],
							values: []
						};
						for( var p in data ) {
							oRet.keys.push( p );
							oRet.values.push( data[p] );
						}
						return oRet;
					}

                    scope.$on("responseTimeSummaryChartDirective.initAndRenderWithData." + scope.namespace, function (event, data, w, h, useFilterTransaction, useChartCursor) {
                        setIdAutomatically();
                        setWidthHeight(w, h);
						renderChart( parseHistogram(data), useFilterTransaction, useChartCursor );
                    });

                    scope.$on("responseTimeSummaryChartDirective.updateData." + scope.namespace, function (event, data) {
						updateChart( parseHistogram(data) );
                    });

                }
            };
        }
	]);
})();