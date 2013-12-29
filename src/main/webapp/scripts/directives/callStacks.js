'use strict';

pinpointApp.constant('callStacksConfig', {
    agentDividerWarningTime : 500,
    agentDividerClasses : [
        'first',
        'second',
        'third',
        'forth',
        'fifth'
    ]
});

pinpointApp.directive('callStacks', [ 'callStacksConfig', function (cfg) {
    return {
        restrict: 'EA',
        replace: true,
        templateUrl: 'views/callStacks.html',
        scope : {
            namespace : '@' // string value
        },
        link: function postLink(scope, element, attrs) {

            // define private variables
            var sLastAgent, nLastExecTime;

            // define private variables of methods
            var initialize, addAgentDividerClassToTransactionDetail;

            // initialize scope variables
            scope.transactionDetail = null;

            /**
             * initialize
             * @param t transactionDetail
             */
            initialize = function (t) {
                scope.transactionDetail = t;
                scope.key = t.callStackIndex;
                scope.barRatio = 100 / (t.callStack[0][scope.key.end] - t.callStack[0][scope.key.begin]);
                addAgentDividerClassToTransactionDetail();
                scope.$digest();
                var oTreeGridTable = new TreeGridTable({
                    tableId : element.find('table'), // element should be a table of DOM, so it should be replace:true at the top
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
                        if (stack[key.begin] - nLastExecTime > cfg.agentDividerWarningTime) {
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
             * add agent divider class to transactionDetail
             */
            addAgentDividerClassToTransactionDetail = function () {
                if (!scope.transactionDetail || !scope.transactionDetail.callStack || !scope.key) {
                    return;
                }
                var callStack = scope.transactionDetail.callStack,
                    key = scope.key,
                    htLastStack = callStack[0],
                    nClassCount = 0;
                angular.forEach(callStack, function (stack) {
                    if (stack[key.agent] !== '' && htLastStack[key.agent] !== stack[key.agent]) {
                        stack['agentDividerClass'] = cfg.agentDividerClasses[++nClassCount];
                        htLastStack = stack;
                        if (nClassCount === cfg.agentDividerClasses.length) {
                            nClassCount = 0;
                        }
                    } else {
                        stack['agentDividerClass'] = cfg.agentDividerClasses[nClassCount];
                    }
                });
            };

            /**
             * scope event on callStacks.initialize
             */
            scope.$on('callStacks.initialize.' + scope.namespace, function (event, transactionDetail) {
                initialize(transactionDetail);
            });
        }
    };
}]);
