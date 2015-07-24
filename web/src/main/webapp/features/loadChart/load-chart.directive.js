(function() {
	'use strict';
	/**
	 * (en)loadChartDirective 
	 * @ko loadChartDirective
	 * @group Directive
	 * @name loadChartDirective
	 * @class
	 */	
	pinpointApp.constant('loadChartDirectiveConfig', {
	    myColors: ["#2ca02c", "#3c81fa", "#f8c731", "#f69124", "#f53034"]
	//    myColors: ["#c9e7a5", "#bbcdf0", "#fce0b5", "#f69124", "#f53034"]
	});
	
	pinpointApp.directive('loadChartDirective', ['loadChartDirectiveConfig', '$timeout', function (cfg, $timeout) {
        return {
            template: '<div></div>',
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
                    renderSimple;

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
//                            "colors" : cfg.myColors,
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
                                "fillColors": cfg.myColors[0],
//                                "labelText": "[[value]]",
                                "lineAlpha": 0.8,
                                "lineColor": "#787779",
                                "title": aDynamicKey[0],
                                "type": "step",
                                "legendColor": cfg.myColors[0],
                                "valueField": aDynamicKey[0]
                            }, {
                                "balloonText": "[[title]] : <b>[[value]]</b>",
                                "fillAlphas": 0.3,
                                "fillColors": cfg.myColors[1],
//                                "labelText": "[[value]]",
                                "lineAlpha": 0.8,
                                "lineColor": "#787779",
                                "title": aDynamicKey[1],
                                "type": "step",
                                "legendColor": cfg.myColors[1],
                                "valueField": aDynamicKey[1]
                            }, {
                                "balloonText": "[[title]] : <b>[[value]]</b>",
                                "fillAlphas": 0.4,
                                "fillColors": cfg.myColors[2],
//                                "labelText": "[[value]]",
                                "lineAlpha": 0.8,
                                "lineColor": "#787779",
                                "title": aDynamicKey[2],
                                "type": "step",
                                "legendColor": cfg.myColors[2],
                                "valueField": aDynamicKey[2]
                            }, {
                                "balloonText": "[[title]] : <b>[[value]]</b>",
                                "fillAlphas": 0.6,
                                "fillColors": cfg.myColors[3],
//                                "labelText": "[[value]]",
                                "lineAlpha": 0.8,
                                "lineColor": "#787779",
                                "title": aDynamicKey[3],
                                "type": "step",
                                "legendColor": cfg.myColors[3],
                                "valueField": aDynamicKey[3]
                            }, {
                                "balloonText": "[[title]] : <b>[[value]]</b>",
                                "fillAlphas": 0.6,
                                "fillColors": cfg.myColors[4],
//                                "labelText": "[[value]]",
                                "lineAlpha": 0.8,
                                "lineColor": "#787779",
                                "title": aDynamicKey[4],
                                "type": "step",
                                "legendColor": cfg.myColors[4],
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
                        	$at($at.MAIN, $at.CLK_LOAD_GRAPH);
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
                                "fillColors": cfg.myColors[0],
                                "lineAlpha": 0.8,
                                "lineColor": "#787779",
                                "type": "step",
                                "valueField": aDynamicKey[0]
                            }, {
                                "id": "AmGraph-2",
                                "fillAlphas": 0.3,
                                "fillColors": cfg.myColors[1],
                                "lineAlpha": 0.8,
                                "lineColor": "#787779",
                                "type": "step",
                                "valueField": aDynamicKey[1]
                            }, {
                                "id": "AmGraph-3",
                                "fillAlphas": 0.4,
                                "fillColors": cfg.myColors[2],
                                "lineAlpha": 0.8,
                                "lineColor": "#787779",
                                "type": "step",
                                "valueField": aDynamicKey[2]
                            }, {
                                "id": "AmGraph-4",
                                "fillAlphas": 0.6,
                                "fillColors": cfg.myColors[3],
                                "lineAlpha": 0.8,
                                "lineColor": "#787779",
                                "type": "step",
                                "valueField": aDynamicKey[3]
                            }, {
                                "id": "AmGraph-5",
                                "fillAlphas": 0.6,
                                "fillColors": cfg.myColors[4],
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

                /**
                 * update data
                 * @param data
                 */
                updateData = function (data) {
//                    oChart.dataProvider = data;
                    oChart.clear();
                    element.empty();
                    $timeout(function () {
//                        oChart.validateData();
                        render(data, true);
                    });
                };

                /**
                 * parse time series histogram for amcharts
                 * @param data
                 * @returns {Array}
                 */
                parseTimeSeriesHistogramForAmcharts = function (data) {
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
                    for (var key in data) {
                        aDynamicKey.push(data[key].key);
                        for (var innerKey in data[key].values) {
                            var a = getKeyFromNewDataByTime(data[key].values[innerKey][0]);
                            if (a > -1) {
                                newData[a][data[key].key] = data[key].values[innerKey][1];
                            } else {
                                var b = {
                                    time: moment(data[key].values[innerKey][0]).format('YYYY-MM-DD HH:mm')
                                };
                                b[data[key].key] = data[key].values[innerKey][1];
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
                    render(parseTimeSeriesHistogramForAmcharts(data), useChartCursor);
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
                    renderSimple(parseTimeSeriesHistogramForAmcharts(data), useChartCursor);
                });
            }
        };
    }]);
})();