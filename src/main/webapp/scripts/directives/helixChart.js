'use strict';

pinpointApp.directive('helixChart', [
    function () {
        return {
            restrict: 'EA',
            scope: {
                namespace: '@' // string value
            },
            link: function postLink(scope, element, attrs) {

                // define private variables of methods
                var initializeWithJSON, removeChart;

                /**
                 * initialize with json
                 * @param helixChartVo
                 */
                initializeWithJSON = function (helixChartVo) {
                    removeChart();
                    helix.render(helixChartVo);
                };

                /**
                 * remove chart
                 */
                removeChart = function () {
                    element.empty();
                    scope.$digest();
                };

                scope.$on('helixChart.initialize.' + scope.namespace, function (event, oHelixChartVo) {
                    oHelixChartVo.setTarget(element.get(0));
                    initializeWithJSON(oHelixChartVo.toJSON());
                });

                scope.$on('helixChart.removeChart.' + scope.namespace, function (event) {
                    removeChart();
                });
            }
        };
    }
]);
