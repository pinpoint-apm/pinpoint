'use strict';

pinpointApp.constant('responseTimeChartConfig', {
    myColors: ["#2ca02c", "#3c81fa", "#f8c731", "#f69124", "#f53034"]
});

pinpointApp
    .directive('responseTimeChart', ['responseTimeChartConfig', '$timeout',
        function (cfg, $timeout) {
            return {
                template: '<div></div>',
                replace: true,
                restrict: 'EA',
                scope: {
                    namespace: '@' // string value
                },
                link: function postLink(scope, element, attrs) {

                    // define variables
                    var id, oChart;

                    // define variables of methods
                    var setIdAutomatically, setWidthHeight, render, clickGraphItemListener, updateData,
                        parseHistogramForAmcharts;

                    setIdAutomatically = function () {
                        id = 'responseTimeId-' + scope.namespace;
                        element.attr('id', id);
                    };

                    setWidthHeight = function (w, h) {
                        element.css('width', w || '100%');
                        element.css('height', h || '150px');
                    };

                    render = function (data, useFilterTransaction, useChartCursor) {
                        $timeout(function () {
                            var options = {
                                "type": "serial",
                                "theme": "none",
                                "dataProvider": data,
                                "startDuration": 1,
                                "valueAxes": [
                                    {
                                        "gridAlpha": 0.1
                                    }
                                ],
                                "graphs": [
                                    {
                                        "balloonText": useFilterTransaction ? 'Filter Transaction with [[category]]' : '',
                                        "colorField": "color",
                                        "labelText": "[[value]]",
                                        "fillAlphas": 0.8,
                                        "lineAlpha": 0.2,
                                        "type": "column",
                                        "valueField": "count"
                                    }
                                ],
                                "categoryField": "responseTime"
                            };
                            if (useChartCursor) {
                                options["chartCursor"] = {
                                    "fullWidth": true,
                                        "categoryBalloonAlpha": 0.7,
                                        "cursorColor": "#000",
                                        "cursorAlpha": 0.3
                                };
                            }
                            oChart = AmCharts.makeChart(id, options);

                            if (useFilterTransaction) {
                                oChart.addListener('clickGraphItem', clickGraphItemListener);
                                oChart.addListener('rollOverGraphItem', function (e) {
                                    e.event.target.style.cursor = 'pointer';
                                });
                            }
                        });
                    };

                    clickGraphItemListener = function (event) {
                        scope.$emit('responseTimeChart.itemClicked.' + scope.namespace, event.item.serialDataItem.dataContext);
                    };

                    updateData = function (data) {
                        oChart.setDataProvider = data;
                        oChart.validateNow();
                        oChart.validateData();
                    };

                    parseHistogramForAmcharts = function (data) {
                        var newData = [],
                            i = 0;
                        for (var key in data) {
                            newData.push({
                                responseTime: key,
                                count: data[key],
                                color: cfg.myColors[i++]
                            });
                        }
                        return newData;
                    };

                    scope.$on('responseTimeChart.initAndRenderWithData.' + scope.namespace, function (event, data, w, h, useFilterTransaction, useChartCursor) {
                        console.log('responseTimeChart.initAndRenderWithData.' + scope.namespace);
                        setIdAutomatically();
                        setWidthHeight(w, h);
                        render(parseHistogramForAmcharts(data), useFilterTransaction, useChartCursor);
                    });

                    scope.$on('responseTimeChart.updateData.' + scope.namespace, function (event, data) {
                        updateData(parseHistogramForAmcharts(data));
                    });

                }
            };
        }]);
