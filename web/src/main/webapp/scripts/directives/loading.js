'use strict';

pinpointApp
    .directive('loading', ['$timeout', '$templateCache', '$compile', function ($timeout, $templateCache, $compile) {
        return {
            restrict: 'A',
            scope: {
                showLoading: '=loading',
                loadingMessage: '@'
            },
            link: function postLink(scope, element, attrs) {
                if (!scope.loadingMessage) {
                    scope.loadingMessage = 'Please Wait...';
                }
                var loadingHtml = $templateCache.get('loading.html');
                $timeout(function () {
                    if (element.css('position') === 'static') {
                        element.css('position', 'relative');
                    }
                    element.append($compile(loadingHtml)(scope));
                });
            }
        };
    }]);
