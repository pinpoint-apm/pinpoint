'use strict';

pinpointApp.directive('timeline', function () {
    return {
        restrict: 'EA',
        replace: true,
        templateUrl: 'views/timeline.html',
        link: function postLink(scope, element, attrs) {

            // define private variables of methods
            var initialize;

            /**
             * initialize
             * @param transactionDetail
             */
            initialize = function (transactionDetail) {
                scope.timeline = transactionDetail;
                scope.key = transactionDetail.callStackIndex;
                scope.barRatio = 1000 / (transactionDetail.callStack[0][scope.key.end] - transactionDetail.callStack[0][scope.key.begin]);
                scope.$digest();
                $('.timeline').tooltip();
            };

            /**
             * scope event on timeline.initialize
             */
            scope.$on('timeline.initialize', function (event, transactionDetail) {
                initialize(transactionDetail);
            });
        }
    };
});
