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

                function render(data, useChartCursor) {
                	element.empty();
                    $timeout(function () {
                        var options = {
                            "type": "serial",
                            "theme": "light",
                            "legend": {
                                "autoMargins": false,
                                "align" : "right",
                                "borderAlpha": 0,
                                "equalWidths": true,
                                "horizontalGap": 0,
                                "verticalGap": 0,
                                "markerSize": 10,
                                "useGraphSettings": false,
                                "valueWidth": 0,
                                "spacing": 0,
                                "markerType" : "circle", // square, circle, diamond, triangleUp, triangleDown, triangleLeft, triangleDown, bubble, line, none.
                                "position": "top"
                            },
//                            "colors" : responseTypeColor,
                            "dataProvider": data,
                            "valueAxes": [{
                                "stackType": "regular",
                                "axisAlpha": 1,
                                "usePrefixes": true,
                                "gridAlpha": 0.1
                            }],
                            "categoryField": "time",
                            "categoryAxis": {
//                                "parseDates": true,
//                                "equalSpacing": true,
                                "startOnAxis": true,
                                "gridPosition": "start",
//                                "dashLength": 1,
//                                "minorGridEnabled": true,
//                                "minPeriod": "mm",
//                                "categoryFunction": function (category, dataItem, categoryAxis) {
//                                    return category;
//                                },
                                "labelFunction": function (valueText, serialDataItem, categoryAxis) {
                                	//return valueText.substring( valueText.indexOf( " " ) + 1 );
                                	var dashIndex = valueText.indexOf("-");
                                	var spaceIndex = valueText.indexOf(" ");
                                	return valueText.substring( dashIndex + 1, spaceIndex ) + "\n" + valueText.substring( spaceIndex + 1 );
                                }
                            },
                            "balloon": {
                                "fillAlpha": 1,
                                "borderThickness": 1
                            },
                            "graphs": [{
                                "balloonText": "[[title]] : <b>[[value]]</b>",
//                                "balloonColor": "red",
                                "fillAlphas": 0.2,
                                "fillColors": responseTypeColor[0],
//                                "labelText": "[[value]]",
                                "lineAlpha": 0.8,
                                "lineColor": "#787779",
                                "title": aDynamicKey[0],
                                "type": "step",
                                "legendColor": responseTypeColor[0],
                                "valueField": aDynamicKey[0]
                            }, {
                                "balloonText": "[[title]] : <b>[[value]]</b>",
                                "fillAlphas": 0.3,
                                "fillColors": responseTypeColor[1],
//                                "labelText": "[[value]]",
                                "lineAlpha": 0.8,
                                "lineColor": "#787779",
                                "title": aDynamicKey[1],
                                "type": "step",
                                "legendColor": responseTypeColor[1],
                                "valueField": aDynamicKey[1]
                            }, {
                                "balloonText": "[[title]] : <b>[[value]]</b>",
                                "fillAlphas": 0.4,
                                "fillColors": responseTypeColor[2],
//                                "labelText": "[[value]]",
                                "lineAlpha": 0.8,
                                "lineColor": "#787779",
                                "title": aDynamicKey[2],
                                "type": "step",
                                "legendColor": responseTypeColor[2],
                                "valueField": aDynamicKey[2]
                            }, {
                                "balloonText": "[[title]] : <b>[[value]]</b>",
                                "fillAlphas": 0.6,
                                "fillColors": responseTypeColor[3],
//                                "labelText": "[[value]]",
                                "lineAlpha": 0.8,
                                "lineColor": "#787779",
                                "title": aDynamicKey[3],
                                "type": "step",
                                "legendColor": responseTypeColor[3],
                                "valueField": aDynamicKey[3]
                            }, {
                                "balloonText": "[[title]] : <b>[[value]]</b>",
                                "fillAlphas": 0.6,
                                "fillColors": responseTypeColor[4],
//                                "labelText": "[[value]]",
                                "lineAlpha": 0.8,
                                "lineColor": "#787779",
                                "title": aDynamicKey[4],
                                "type": "step",
                                "legendColor": responseTypeColor[4],
                                "valueField": aDynamicKey[4]
                            }]
                        };
                        if (useChartCursor) {
                            options["chartCursor"] = {
                                "cursorPosition": "mouse",
                                "categoryBalloonAlpha": 0.7,
                                "categoryBalloonDateFormat": "H:NN"
                            };
                        }
                        oChart = AmCharts.makeChart(id, options);
                        oChart.addListener("clickGraph", function(e) {
                        	AnalyticsService.send(AnalyticsService.CONST.MAIN, AnalyticsService.CONST.CLK_LOAD_GRAPH);
                        });
                    });
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
                function updateData(data) {
					if ( angular.isUndefined( oChart ) ) {
						if ( data.length !== 0 ) {
							render(data, true);
						}
					} else {
						oChart.dataProvider = data;
						oChart.validateData();
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