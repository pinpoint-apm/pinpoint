'use strict';

pinpointApp.directive('filterInformation', [ '$filter', '$base64',
    function ($filter, $base64) {
        return {
            restrict: 'EA',
            replace: true,
            templateUrl: 'views/filterInformation.html',
            scope : {
                namespace : '@' // string value
            },
            link: function postLink(scope, element, attrs) {

                // define private variables;

                // define private variables of methods;
                var initialize, reset;

                /**
                 * initialize
                 * @param oServerMapFilterVo
                 */
                initialize = function (oServerMapFilterVo) {
                    reset();
                    if (oServerMapFilterVo.getRequestUrlPattern()) {
                        scope.urlPattern = $base64.decode(oServerMapFilterVo.getRequestUrlPattern());
                    }
                    scope.includeException = oServerMapFilterVo.getIncludeException() ? 'Failed Only' : 'Success + Failed';

                    if (angular.isNumber(oServerMapFilterVo.getResponseFrom()) &&
                        oServerMapFilterVo.getResponseTo()) {
                        var responseTime = [];
                        responseTime.push($filter('number')(oServerMapFilterVo.getResponseFrom()));
                        responseTime.push('ms');
                        responseTime.push('~');
                        if (oServerMapFilterVo.getResponseTo() === 'max') {
                            responseTime.push('30,000+');
                        } else {
                            responseTime.push($filter('number')(oServerMapFilterVo.getResponseTo()));
                        }
                        responseTime.push('ms');

                        scope.responseTime = responseTime.join(' ');
                    }

                };

                /**
                 * reset
                 */
                reset = function () {
                    scope.urlPattern = 'none';
                    scope.responseTime = 'none';
                    scope.includeException = 'none';
                };

                /**
                 * scope event on filterInformation.initialize.namespace
                 */
                scope.$on('filterInformation.initialize.' + scope.namespace, function (e, oServerMapFilterVo) {
                    initialize(oServerMapFilterVo);
                });
            }
        };
    }
]);
