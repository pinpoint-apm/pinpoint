'use strict';

pinpointApp
    .directive('navbar2', function () {
        return {
            restrict: 'EA',
            replace: true,
            templateUrl: 'views/navbar2.html',
            link: function postLink(scope, element, attrs) {

                scope.$on('navbar2.initializeWithApplicationData', function (event, applicationData) {
                    console.log('on navbar2.initializeWithApplicationData', applicationData);
                });
            }
        };
    });
