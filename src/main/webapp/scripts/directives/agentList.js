'use strict';

angular.module('pinpointApp')
    .directive('agentList', function () {
        return {
            restrict: 'EA',
            replace: true,
            templateUrl: 'views/agentList.html',
            link: function postLink(scope, element, attrs) {

            }
        };
    });
