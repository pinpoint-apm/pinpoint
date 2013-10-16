'use strict';

pinpointApp.directive('callStacks', [ function () {
    return {
        restrict: 'EA',
        replace: true,
        templateUrl: 'views/callStacks.html',
        link: function postLink(scope, element, attrs) {

            // define private variables of methods
            var initialize;

            // initialize scope variables
            scope.transactionDetail = null;

            /**
             * initialize
             * @param transactionDetail
             */
            initialize = function (transactionDetail) {
                scope.transactionDetail = transactionDetail;
                scope.key = transactionDetail.callStackIndex;
                scope.barRatio = 100 / (transactionDetail.callStack[0][scope.key.end] - transactionDetail.callStack[0][scope.key.begin]);
                scope.$digest();
                var oTreeGridTable = new TreeGridTable({
                    tableId : element,
                    height : "auto"
                });
            };

            /**
             * scope event on callStacks.initialize
             */
            scope.$on('callStacks.initialize', function (event, transactionDetail) {
                initialize(transactionDetail);
            });
        }
    };
}]);
