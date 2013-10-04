'use strict';

pinpointApp.directive('callStacks', [ function () {
    return {
        restrict: 'EA',
        replace: true,
        templateUrl: 'views/callStacks.html',
        link: function postLink(scope, element, attrs) {

            var initialize;

            initialize = function () {
                var oTreeGridTable = new TreeGridTable({
                    tableId : "callStacks",
                    height : "auto"
                });
            };

            scope.$on('callStacks.initialize', function (event, transactionDetail) {
                scope.transactionDetail = transactionDetail;
                scope.key = transactionDetail.callStackIndex;
                scope.$digest();
                initialize();
                console.log('transactionDetail', transactionDetail);
            });
        }
    };
}]);
