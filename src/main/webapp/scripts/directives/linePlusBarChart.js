'use strict';

pinpointApp.directive('linePlusBarChart', function () {
    return {
        template: '<svg></svg>',
        restrict: 'E',
        replace: true,
        scope: {
            namespace: '@'
        },
        link: function postLink(scope, element, attrs) {


            var initialize;

            initialize = function (oLinePlusBarChartVo) {

                if (angular.isDefined(oLinePlusBarChartVo.height)) {
                    attrs.$set('height', oLinePlusBarChartVo.height);
                    console.log('height', oLinePlusBarChartVo.height);
                }

                // draw a chart
                nv.addGraph(function () {
                    var chart = nv.models.linePlusBarChart();
                    chart.x(function (d, i) {
                        return i;
                    });
                    chart.xAxis.tickFormat(function (d) {
                        var dx = oLinePlusBarChartVo.datum[0].values[d] && oLinePlusBarChartVo.datum[0].values[d].x || 0;
                        return d3.time.format('%X')(new Date(dx));
                    });
                    chart.y1Axis.axisLabel(oLinePlusBarChartVo.y1AxisLabel).tickFormat(function (d) {
                        return d;
                    });
                    chart.y2Axis.tickFormat(function (d) {
                        var sizes = [' B', 'KB', 'MB', 'GB', 'TB'],
                            posttxt = 0,
                            precision = 2;
                        if (d === 0) { return '0'; }
                        while (d >= 1024) {
                            posttxt += 1;
                            d = d / 1024;
                        }
                        return parseInt(d, 10).toFixed(precision) + " " + sizes[posttxt];
                    });
                    chart.bars.forceY([0]);
                    chart.lines.forceY([0]);
                    chart.margin(oLinePlusBarChartVo.margin);
                    d3.select(element.get(0)).datum(oLinePlusBarChartVo.datum).transition().duration(100).call(chart);
//                    d3.select("#circle").attr("stroke-width", "1px");
                    nv.utils.windowResize(chart.update);
                    return chart;
                });
            };

            scope.$on('linePlusBarChart.initialize.' + scope.namespace, function (event, oLinePlusBarChartVo) {
                initialize(oLinePlusBarChartVo);
            });
        }
    };
});
