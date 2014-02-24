'use strict';

pinpointApp.directive('sidebarTitle', [ '$timeout',
    function ($timeout) {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'views/sidebarTitle.html',
            scope: {
                namespace: '@'
            },
            link: function poLink(scope, element, attrs) {

                // define private variables of methods
                var initialize, empty;

                /**
                 * bootrap
                 */
                $timeout(function () {
                    empty();
                });

                /**
                 * initialize
                 * @param oSidebarTitleVo
                 */
                initialize = function (oSidebarTitleVo) {
                    scope.stImage = oSidebarTitleVo.getImage();
                    scope.stImageShow = oSidebarTitleVo.getImage() ? true : false;
                    scope.stTitle = oSidebarTitleVo.getTitle();
                    scope.stImage2 = oSidebarTitleVo.getImage2();
                    scope.stImage2Show = oSidebarTitleVo.getImage2() ? true : false;
                    scope.stTitle2 = oSidebarTitleVo.getTitle2();
                    if (!scope.$$phase) {
                        scope.$digest();
                    }
                    element.find('[data-toggle="tooltip"]').tooltip('destroy').tooltip();
                };

                /**
                 * empty
                 */
                empty = function () {
                    scope.stImage = false;
                    scope.stImageShow = false;
                    scope.stTitle = false;
                    scope.stImage2 = false;
                    scope.stTitle2 = false;
                    scope.stImage2Show = false;
                };

                /**
                 * scope on sidebarTitle.initialize.namespace
                 */
                scope.$on('sidebarTitle.initialize.' + scope.namespace, function (event, oSidebarTitleVo) {
                    initialize(oSidebarTitleVo);
                });

                /**
                 * scope on sidebarTitle.empty.namespace
                 */
                scope.$on('sidebarTitle.empty.' + scope.namespace, function (event) {
                    empty();
                });
            }
        };
    }]
);
