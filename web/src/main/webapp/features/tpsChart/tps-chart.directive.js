(function() {
    'use strict';
    /**
     * (en)tpsChartDirective 
     * @ko tpsChartDirective
     * @group Directive
     * @name tpsChartDirective
     * @class
     */ 
    angular.module('pinpointApp').directive('tpsChartDirective', ['$timeout',
        function ($timeout) {
            return {
                template: '<div></div>',
                replace: true,
                restrict: 'E',
                scope: {
                    namespace: '@' // string value
                },
                link: function postLink(scope, element, attrs) {
    
                    // define variables
                    var sId, oChart;
    
                    // define variables of methods
                    var setIdAutomatically, setWidthHeight, render, showCursorAt, resize;
    
                    /**
                     * set id automatically
                     */
                    setIdAutomatically = function () {
                        sId = 'multipleValueAxesId-' + scope.namespace;
                        element.attr('id', sId);
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
                     * @param chartData
                     */
                    render = function (chartData) {
                        var options = {
                            "type": "serial",
                            "theme": "light",
                            "autoMargins": false,
                            "marginTop": 10,
                            "marginLeft": 70,
                            "marginRight": 70,
                            "marginBottom": 30,
                            "legend": {
                                "useGraphSettings": true,
                                "autoMargins": true,
                                "align" : "right",
                                "position": "top",
                                "valueWidth": 70
                            },
                            "usePrefixes": true,
                            "dataProvider": chartData,
                            "valueAxes": [
                                {
                                	"stackType": "regular",
                                    "gridAlpha": 0,
                                    "axisAlpha": 1,
                                    "position": "left",
                                    "title": "TPS",
                                    "minimum" : 0
                                }
                            ],
                            "graphs": [
                                {
                                    "balloonText": "Sampled Continuation : [[value]]",
                                    "legendValueText": "[[value]]",
                                    "lineColor": "rgb(214, 141, 8)",
                                    "fillColor": "rgb(214, 141, 8)",
                                    "title": "S.C",
                                    "valueField": "sampledContinuationTps",
                                    "fillAlphas": 0.4,
                                    "connect": true
                                },{
                                    "balloonText": "Sampled New : [[value]]",
                                    "legendValueText": "[[value]]",
                                    "lineColor": "rgb(252, 178, 65)",
                                    "fillColor": "rgb(252, 178, 65)",
                                    "title": "S.N",
                                    "valueField": "sampledNewTps",
                                    "fillAlphas": 0.4,
                                    "connect": true
                                },{
                                    "balloonText": "Unsampled Continuation : [[value]]",
                                    "legendValueText": "[[value]]",
                                    "lineColor": "rgb(90, 103, 166)",
                                    "fillColor": "rgb(90, 103, 166)",
                                    "title": "U.C",
                                    "valueField": "unsampledContinuationTps",
                                    "fillAlphas": 0.4,
                                    "connect": true
                                },{
                                    "balloonText": "Unsampled New : [[value]]",
                                    "legendValueText": "[[value]]",
                                    "lineColor": "rgb(160, 153, 255)",
                                    "fillColor": "rgb(160, 153, 255)",
                                    "title": "U.N",
                                    "valueField": "unsampledNewTps",
                                    "fillAlphas": 0.4,
                                    "connect": true
                                },{
                                    "balloonText": "Total : [[value]]",
                                    "legendValueText": "[[value]]",
                                    "lineColor": "rgba(31, 119, 180, 0)",
                                    "fillColor": "rgba(31, 119, 180, 0)",
                                    "valueField": "totalTps",
                                    "fillAlphas": 0.4,
                                    "connect": true
                                }
                            ],
                            "chartCursor": {
                                "categoryBalloonAlpha": 0.7,
                                "fullWidth": true,
                                "cursorAlpha": 0.1
                            },
                            "categoryField": "time",
                            "categoryAxis": {
                                "axisColor": "#DADADA",
                                "startOnAxis": true,
                                "gridPosition": "start",
                                "labelFunction": function (valueText, serialDataItem, categoryAxis) {
                                    return moment(valueText).format("HH:mm:ss");
                                }
                            }
                        };
                        $timeout(function () {
                            oChart = AmCharts.makeChart(sId, options);
                            oChart.chartCursor.addListener('changed', function (event) {
                                scope.$emit('tpsChartDirective.cursorChanged.' + scope.namespace, event);
                            });
                        });
                    };

                    /**
                     * show cursor at
                     * @param category
                     */
                    showCursorAt = function (category) {
                        if (category) {
                            if (angular.isNumber(category)) {
                                category = oChart.dataProvider[category].time;
                            }
                            oChart.chartCursor.showCursorAt(category);
                        } else {
                            oChart.chartCursor.hideCursor();
                        }
                    };

                    /**
                     * resize
                     */
                    resize = function () {
                        if (oChart) {
                            oChart.validateNow();
                            oChart.validateSize();
                        }
                    };

                    /**
                     * scope event on tpsChartDirective.initAndRenderWithData.namespace
                     */
                    scope.$on('tpsChartDirective.initAndRenderWithData.' + scope.namespace, function (event, data, w, h) {
                        setIdAutomatically();
                        setWidthHeight(w, h);
                        render(data);
                    });

                    /**
                     * scope event on tpsChartDirective.showCursorAt.namespace
                     */
                    scope.$on('tpsChartDirective.showCursorAt.' + scope.namespace, function (event, category) {
                        showCursorAt(category);
                    });

                    /**
                     * scope event on tpsChartDirective.resize.namespace
                     */
                    scope.$on('tpsChartDirective.resize.' + scope.namespace, function (event) {
                        resize();
                    });
                }
            };
        }
    ]);
})();