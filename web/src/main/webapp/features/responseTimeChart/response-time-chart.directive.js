(function() {
	'use strict';
	pinpointApp.constant('responseTimeChartDirectiveConfig', {
	    myColors: ["#2ca02c", "#3c81fa", "#f8c731", "#f69124", "#f53034"]
	});
	
	pinpointApp.directive("responseTimeChartDirective", ["responseTimeChartDirectiveConfig", "$timeout", "AnalyticsService", "PreferenceService", "CommonUtilService",
        function (cfg, $timeout, AnalyticsService, PreferenceService, CommonUtilService ) {
			var responseTypeColor = PreferenceService.getResponseTypeColor();
            return {
                template: "<div style='user-select:none;'></div>",
                replace: true,
                restrict: "EA",
                scope: {
                    namespace: "@" // string value
                },
                link: function postLink(scope, element, attrs) {

                    // define variables
                    var id, oChart;

                    function setIdAutomatically() {
                        id = "responseTimeId-" + scope.namespace;
                        element.attr("id", id);
                    }
                    function setWidthHeight(w, h) {
                        element.css("width", w || "100%");
                        element.css("height", h || "150px");
                    }

                    function render(data, useFilterTransaction, useChartCursor) {
                        $timeout(function () {
                            var options = {
                                "type": "serial",
                                "theme": "none",
                                "dataProvider": data,
                                "startDuration": 0,
                                "valueAxes": [{
									"gridAlpha": 0.1,
									"usePrefixes": true
								}],
                                "graphs": [{
                                        "balloonText": useFilterTransaction ? '[[category]] filtering' : '',
                                        "colorField": "color",
                                        "labelText": "[[value]]",
                                        "fillAlphas": 0.3,
                                        "alphaField": "alpha",
                                        "lineAlpha": 0.8,
                                        "lineColor": "#787779",
                                        "type": "column",
                                        "valueField": "count"
								}],
                                "categoryField": "responseTime",
                                "categoryAxis": {
                                    "gridAlpha": 0
                                }
                            };
                            if (useChartCursor) {
                                options["chartCursor"] = {
                                    "fullWidth": true,
                                    "categoryBalloonAlpha": 0.7,
                                    "cursorColor": "#000000",
                                    "cursorAlpha": 0,
                                    "zoomable": false
                                };
                            }
                            oChart = AmCharts.makeChart(id, options);
                            oChart.addListener('clickGraphItem', function(event) {
                            	if ( event.item.category == "Error" ) {
									AnalyticsService.send(AnalyticsService.CONST.MAIN, AnalyticsService.CONST.CLK_RESPONSE_GRAPH);
                            		scope.$emit('responseTimeChartDirective.showErrorTransactionList', event.item.category );
                            	}
                            	if ( useFilterTransaction ) {
                            		scope.$emit('responseTimeChartDirective.itemClicked.' + scope.namespace, event.item.serialDataItem.dataContext);
                            	}
                            });
                            if (useFilterTransaction) {
                                oChart.addListener('clickGraphItem', clickGraphItemListener);
                                oChart.addListener('rollOverGraphItem', function (e) {
                                    e.event.target.style.cursor = 'pointer';
                                });
                            }
                        });
                    }
                    function renderChart(data, useFilterTransaction, useChartCursor) {
                    	element.empty().append("<canvas>");
						oChart = new Chart(element.find("canvas"), {
							type: "bar",
							data: {
								labels: data.keys,
								datasets: [{
									data: data.values,
									backgroundColor: [
										"rgba(44, 160, 44, 0.2)",
										"rgba(60, 129, 250, 0.3)",
										"rgba(248, 199, 49, 0.4)",
										"rgba(246, 145, 36, 0.6)",
										"rgba(245, 48, 52, 0.6)"
									],
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
											scope.$emit('responseTimeChartDirective.showErrorTransactionList', type);
										}
										if (useFilterTransaction) {
											scope.$emit('responseTimeChartDirective.itemClicked.' + scope.namespace, {
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
                        scope.$emit('responseTimeChartDirective.itemClicked.' + scope.namespace, event.item.serialDataItem.dataContext);
                    }

                    function updateData(data) {
                	    oChart.dataProvider = data;
						oChart.validateData();
                    }
                    function updateChart(data) {
						oChart.data.datasets[0].data = data.values;
						oChart.update();
					}

                    function parseHistogramForAmcharts(data) {
						if ( angular.isUndefined( data ) ) {
                    		data = PreferenceService.getResponseTypeFormat();
						}
                        var newData = [],
                            alpha = [0.2, 0.3, 0.4, 0.6, 0.6],
                            i = 0;
                        for (var key in data) {
                            newData.push({
                                responseTime: key,
                                count: data[key],
                                color: responseTypeColor[i],
                                alpha: alpha[i++]
                            });
                        }
                        return newData;
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

                    scope.$on("responseTimeChartDirective.initAndRenderWithData." + scope.namespace, function (event, data, w, h, useFilterTransaction, useChartCursor) {
                        setIdAutomatically();
                        setWidthHeight(w, h);
                       	// render(parseHistogramForAmcharts(data), useFilterTransaction, useChartCursor);
						renderChart( parseHistogram(data), useFilterTransaction, useChartCursor );
                    });

                    scope.$on("responseTimeChartDirective.updateData." + scope.namespace, function (event, data) {
                        // updateData(parseHistogramForAmcharts(data));
						updateChart( parseHistogram(data) );
                    });

                }
            };
        }
	]);
})();