(function() {
	'use strict';
	pinpointApp.constant("loadChartDirectiveConfig", {});

	pinpointApp.directive("loadChartDirective", ["loadChartDirectiveConfig", "$timeout", "AnalyticsService", "PreferenceService", "CommonUtilService", function (cfg, $timeout, AnalyticsService, PreferenceService, CommonUtilService ) {
		var responseTypeColor = PreferenceService.getResponseTypeColor();
        return {
			template: "<div style='text-align:center;user-select:none;'></div>",
            replace: true,
            restrict: 'EA',
            scope: {
                namespace: '@' // string value
            },
            link: function postLink(scope, element, attrs) {

                // define variables
                var id, aDynamicKey, oChart;

                function setIdAutomatically() {
                    id = 'loadId-' + scope.namespace;
                    element.attr('id', id);
                }

                function setWidthHeight(w, h) {
					element.css('width', w || '100%');
					element.css('height', h || '220px');
                }
                function renderChart(data, useChartCursor) {
                	element.empty().append("<canvas>");
					oChart = new Chart(element.find("canvas"), {
						type: "bar",
						data: {
							labels: data.labels,
							borderWidth: 0,
							datasets: [{
								label: data.keyValues[0].key,
								data: data.keyValues[0].values,
								backgroundColor: "rgba(44, 160, 44, 0.2)",
								borderColor: "rgba(120, 119, 121, 0.8)",
								borderWidth: 0
							},{
								label: data.keyValues[1].key,
								data: data.keyValues[1].values,
								backgroundColor: "rgba(60, 129, 250, 0.2)",
								borderColor: "rgba(120, 119, 121, 0.8)",
								borderWidth: 0
							},{
								label: data.keyValues[2].key,
								data: data.keyValues[2].values,
								backgroundColor: "rgba(248, 199, 49, 0.2)",
								borderColor: "rgba(120, 119, 121, 0.8)",
								borderWidth: 0
							},{
								label: data.keyValues[3].key,
								data: data.keyValues[3].values,
								backgroundColor: "rgba(246, 145, 36, 0.2)",
								borderColor: "rgba(120, 119, 121, 0.8)",
								borderWidth: 0
							},{
								label: data.keyValues[4].key,
								data: data.keyValues[4].values,
								backgroundColor: "rgba(245, 48, 52, 0.2)",
								borderColor: "rgba(120, 119, 121, 0.8)",
								borderWidth: 0
							}]
						},
						options: {
							onClick: function() {
								AnalyticsService.send(AnalyticsService.CONST.MAIN, AnalyticsService.CONST.CLK_LOAD_GRAPH);
							},
							maintainAspectRatio: false,
							tooltips: {
								mode: "label",
								bodySpacing: 6
							},
							scales: {
								yAxes: [{
									gridLines: {
										zeroLineColor: "rgba(0, 0, 0, 1)",
										zeroLineWidth: 0.5
									},
									ticks: {
										beginAtZero: true,
										maxTicksLimit: 5,
										callback: function(label) {
											if ( label >= 1000 ) {
												return "   " + label/1000 + 'k';
											} else {
												if ( label % 1 === 0 ) {
													return getPreSpace(""+label) + label;
												}
											}
										}
									},
									stacked: true
								}],
								xAxes: [{
									gridLines: {
										zeroLineColor: "rgba(0, 0, 0, 1)",
										zeroLineWidth: 0.5
									},
									ticks: {
										autoSkip: true
									},
									categoryPercentage: 1.0,
									barPercentage: 1.0,
									stacked: true,
									display:true
								}]
							},
							animation: {
								duration: 0
							},
							legend: {
								display: true,
								labels: {
									boxWidth: 20,
									padding: 10
								}
							}
						}
					});
				}
				function getPreSpace( str ) {
                	var space = "       "; //7 is max space
					if ( str.length > space.length ) {
						return str;
					} else {
						return space.substr(0, space.length - str.length);
					}
				}
				function renderEmptyChart() {
					element.find("canvas").hide();
					if ( element.find("h4").length === 0 ) {
						element.append("<h4 style='padding-top:25%;text-align:center;'>No Data</h4>");
					} else {
						element.find("h4").show();
					}
				}
				function updateChart(data) {
					if ( angular.isUndefined( oChart ) ) {
						if ( data.length !== 0 ) {
							renderChart(data, true);
						}
					} else {
						if ( data.length === 0 ) {
							renderEmptyChart();
						} else {
							element.find("h4").hide().end().find("canvas").show();
							oChart.data.labels = data.labels;
							oChart.data.datasets[0].data = data.keyValues[0].values;
							oChart.data.datasets[1].data = data.keyValues[1].values;
							oChart.data.datasets[2].data = data.keyValues[2].values;
							oChart.data.datasets[3].data = data.keyValues[3].values;
							oChart.data.datasets[4].data = data.keyValues[4].values;
							oChart.data.datasets[0].label = data.keyValues[0].key;
							oChart.data.datasets[1].label = data.keyValues[1].key;
							oChart.data.datasets[2].label = data.keyValues[2].key;
							oChart.data.datasets[3].label = data.keyValues[3].key;
							oChart.data.datasets[4].label = data.keyValues[4].key;
							oChart.update();
						}
					}
				}
                function parseTimeSeriesHistogram(data) {
					if ( angular.isUndefined(data) )  return [];

					var newData = {
						labels: [],
						keyValues: []
					};
					var bHasData = false;
					for( var i = 0 ; i < data.length; i++ ) {
						var newValues = [];
						for( var j = 0 ; j < data[i].values.length ; j++ ) {
							bHasData = true;
							newValues.push( data[i].values[j][1] );
							if ( i === 0 ) {
								newData.labels.push( CommonUtilService.formatDate(data[i].values[j][0], "MM-DD HH:mm") );
							}
						}
						newData.keyValues.push({
							key : data[i].key,
							values: newValues
						});
					}
					return bHasData ? newData : [];
				}

                scope.$on("loadChartDirective.initAndRenderWithData." + scope.namespace, function (event, data, w, h, useChartCursor) {
                    setIdAutomatically();
                    setWidthHeight(w, h);
                    var parsedData = parseTimeSeriesHistogram(data);
                    if ( parsedData.length === 0 ) {
						renderEmptyChart();
                    } else {
                    	renderChart( parsedData, useChartCursor );
                    }
                });

                scope.$on("loadChartDirective.updateData." + scope.namespace, function (event, data) {
					updateChart(parseTimeSeriesHistogram(data));
                });
            }
        };
    }]);
})();