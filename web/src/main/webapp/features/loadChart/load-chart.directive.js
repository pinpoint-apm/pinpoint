(function() {
	'use strict';
	/**
	 * (en)loadChartDirective 
	 * @ko loadChartDirective
	 * @group Directive
	 * @name loadChartDirective
	 * @class
	 */	
	pinpointApp.constant('loadChartDirectiveConfig', {});
	
	pinpointApp.directive('loadChartDirective', ['loadChartDirectiveConfig', '$timeout', 'AnalyticsService', 'PreferenceService', function (cfg, $timeout, analyticsService, preferenceService ) {
		var responseTypeColor = preferenceService.getResponseTypeColor();
        return {
            template: '<div style="text-align:center"></div>',
            replace: true,
            restrict: 'EA',
            scope: {
                namespace: '@' // string value
            },
            link: function postLink(scope, element, attrs) {

                // define variables
                var id, aDynamicKey, oChart;

                // define variables of methods
                var setIdAutomatically, setWidthHeight, render, parseTimeSeriesHistogramForAmcharts, updateData,
                    renderSimple, renderEmpty;

                /**
                 * set id automatically
                 */
                setIdAutomatically = function () {
                    id = 'loadId-' + scope.namespace;
                    element.attr('id', id);
                };

                /**
                 * set width height
                 * @param w
                 * @param h
                 */
                setWidthHeight = function (w, h) {
                    if (w) element.css('width', w);
                    if (h) element.css('height', h);
                };

                /**
                 * render
                 * @param data
                 * @param useChartCursor
                 */
                render = function (data, useChartCursor) {
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
                        	analyticsService.send(analyticsService.CONST.MAIN, analyticsService.CONST.CLK_LOAD_GRAPH);
                        });
                    });
                };

                /**
                 * render simple
                 * @param data
                 * @param useChartCursor
                 */
                renderSimple = function (data, useChartCursor) {
                    $timeout(function () {
                        var options = {
                            "type": "serial",
                            "pathToImages": "./components/amcharts/images/",
                            "theme": "light",
                            "dataProvider": data,
                            "valueAxes": [{
                                "stackType": "regular",
                                "axisAlpha": 0,
                                "gridAlpha": 0,
                                "labelsEnabled": false
                            }],
                            "categoryField": "time",
                            "categoryAxis": {
                                "startOnAxis": true,
                                "gridPosition": "start",
                                "labelFunction": function (valueText, serialDataItem, categoryAxis) {
                                    return moment(valueText).format("HH:mm");
                                }
                            },
                            "chartScrollbar": {
                                "graph": "AmGraph-1"
                            },
                            "graphs": [{
                                "id": "AmGraph-1",
                                "fillAlphas": 0.2,
                                "fillColors": responseTypeColor[0],
                                "lineAlpha": 0.8,
                                "lineColor": "#787779",
                                "type": "step",
                                "valueField": aDynamicKey[0]
                            }, {
                                "id": "AmGraph-2",
                                "fillAlphas": 0.3,
                                "fillColors": responseTypeColor[1],
                                "lineAlpha": 0.8,
                                "lineColor": "#787779",
                                "type": "step",
                                "valueField": aDynamicKey[1]
                            }, {
                                "id": "AmGraph-3",
                                "fillAlphas": 0.4,
                                "fillColors": responseTypeColor[2],
                                "lineAlpha": 0.8,
                                "lineColor": "#787779",
                                "type": "step",
                                "valueField": aDynamicKey[2]
                            }, {
                                "id": "AmGraph-4",
                                "fillAlphas": 0.6,
                                "fillColors": responseTypeColor[3],
                                "lineAlpha": 0.8,
                                "lineColor": "#787779",
                                "type": "step",
                                "valueField": aDynamicKey[3]
                            }, {
                                "id": "AmGraph-5",
                                "fillAlphas": 0.6,
                                "fillColors": responseTypeColor[4],
                                "lineAlpha": 0.8,
                                "lineColor": "#787779",
                                "type": "step",
                                "valueField": aDynamicKey[4]
                            }]
                        };
                        if (useChartCursor) {
                            options["chartCursor"] = {
                                "avoidBalloonOverlapping": false
                            };
                        }
                        oChart = AmCharts.makeChart(id, options);

                        oChart.addListener('changed', function (e) {
//                            console.log('changed');
                            // broadcast
                        });
                    });
                };
                renderEmpty = function() {
                	element.append("<h4 style='padding-top:25%'>No Data</h4>");
                };

                /**
                 * update data
                 * @param data
                 */
                updateData = function (data) {
//                 	if ( angular.isDefined( oChart ) ) {
// 	                    oChart.clear();
//                 	}
//                     element.empty();
//                     $timeout(function () {
//                     	if( data.length === 0 ) {
//                     		renderEmpty();
//                     	} else {
//                     		render(data, true);
//                     	}
//                     });

					if ( angular.isUndefined( oChart ) ) {
						if ( data.length !== 0 ) {
							render(data, true);
						}
					} else {
						oChart.dataProvider = data;
						$timeout(function () {
							oChart.validateData();
						});
					}
				};

                /**
                 * parse time series histogram for amcharts
                 * @param data
                 * @returns {Array}
                 */
                parseTimeSeriesHistogramForAmcharts = function (data) {
                	if ( angular.isUndefined( data ) ) return [];
                	
                    function getKeyFromNewDataByTime (time) {
                        for (var key in newData) {
                            if (moment(time).format("YYYY-MM-DD HH:mm") === newData[key].time) {
                                return key;
                            }
                        }
                        return -1;
                    }

                    aDynamicKey = [];

                    var newData = [];
					for( var i = 0 ; i < data.length ; i++ ) {
						var oPart = data[i];
                        aDynamicKey.push( oPart.key );
						for( var j = 0 ; j < oPart.values.length ; j++ ) {
							var aInner = oPart.values[j];
                            var a = getKeyFromNewDataByTime( aInner[0]);
                            if (a > -1) {
                                newData[a][ oPart.key ] = aInner[1];
                            } else {
                                var b = {
                                    time: moment( aInner[0]).format('YYYY-MM-DD HH:mm')
                                };
                                b[ oPart.key ] = aInner[1];
                                newData.push(b);
                            }
                        }
                    }
                    return newData;
                };

                /**
                 * scope event on loadChartDirective.initAndRenderWithData.namespace
                 */
                scope.$on('loadChartDirective.initAndRenderWithData.' + scope.namespace, function (event, data, w, h, useChartCursor) {
                    setIdAutomatically();
                    setWidthHeight(w, h);
                    var parsedData = parseTimeSeriesHistogramForAmcharts(data);
                    if ( parsedData.length === 0 ) {
                    	renderEmpty();
                    } else {
                    	render(parsedData, useChartCursor);
                    }
                });

                /**
                 * scope event on loadChartDirective.updateData.namespace
                 */
                scope.$on('loadChartDirective.updateData.' + scope.namespace, function (event, data) {
                    updateData(parseTimeSeriesHistogramForAmcharts(data));
                });

                /**
                 * scope event on loadChartDirective.initAndSimpleRenderWithData.namespace
                 */
                scope.$on('loadChartDirective.initAndSimpleRenderWithData.' + scope.namespace, function (event, data, w, h, useChartCursor) {
                    setIdAutomatically();
                    setWidthHeight(w, h);
                    var parsedData = parseTimeSeriesHistogramForAmcharts(data);
                    if ( parsedData.length === 0 ) {
                    	renderEmpty();
                    } else {
                    	renderSimple(parsedData, useChartCursor);
                    }
                });
            }
        };
    }]);
})();