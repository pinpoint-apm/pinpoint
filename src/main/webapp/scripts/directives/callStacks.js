'use strict';

pinpointApp.directive('callStacks', [ function () {
    return {
        restrict: 'EA',
        replace: true,
        templateUrl: 'views/callStacks.html',
        link: function postLink(scope, element, attrs) {

            // define private variables
            var sLastAgent, nLastExecTime;

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
             * get tr class
             * @param stack
             * @param key
             * @returns {string}
             */
            scope.getTrClass = function (stack, key, index) {
                var trClass = '';
                if (index === 0) {
                    sLastAgent = false;
                }
                if (angular.isUndefined(key)) {
                    return trClass;
                }
                if (angular.isDefined(stack[key.isFocused]) && stack[key.isFocused] === true) {
                    trClass += 'info';
                } else if (angular.isDefined(stack[key.hasException]) && stack[key.hasException] === true) {
                    trClass += 'error';
                }
                if (angular.isDefined(stack[key.agent]) && stack[key.agent]) {
                    if (sLastAgent && sLastAgent !== stack[key.agent]) {
                        if (stack[key.begin] - nLastExecTime > 500) {
                            trClass += ' agent-divider-warn';
                        } else {
                            trClass += ' agent-divider-normal';
                        }

                    }
                    sLastAgent = stack[key.agent];
                    nLastExecTime = stack[key.begin];
                }
                return trClass;
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
