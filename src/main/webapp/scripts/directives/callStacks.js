'use strict';

pinpointApp.directive('callStacks', [ function () {
    return {
        restrict: 'EA',
        replace: true,
        templateUrl: 'views/callStacks.html',
        link: function postLink(scope, element, attrs) {

            var initialize;

            scope.transactionDetail = null;

            initialize = function (transactionDetail) {
                scope.transactionDetail = transactionDetail;
                scope.key = transactionDetail.callStackIndex;
                scope.$digest();
                var oTreeGridTable = new TreeGridTable({
                    tableId : element,
                    height : "auto"
                });
            };

            scope.$on('callStacks.initialize', function (event, transactionDetail) {
                initialize(transactionDetail);
            });
        }
    };
}]);
