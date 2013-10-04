'use strict';

pinpointApp.directive('transactionTable', [function () {
    return {
        restrict: 'EA',
        replace: true,
        templateUrl: 'views/transactionTable.html',
        link: function postLink(scope, element, attrs) {
            /**
             * variables definition
             */
            var clear, appendTransactionList;

            /**
             * variables initialization
             */
            scope.transactionList = [];
            scope.currentTransaction = null;

            /**
             * internal methods
             */
            clear = function () {
                scope.transactionList = [];
            };
            appendTransactionList = function (transactionList) {
                scope.transactionList = scope.transactionList.concat(transactionList);
            };

            /**
             * scope methods
             */
            scope.traceByAppliation = function (transaction) {
                scope.currentTransaction = transaction;
                scope.$emit('transactionTable.applicationSelected', transaction);
                console.log('transactionTable.applicationSelected', transaction);
            };
            scope.etraceBySequence = function (transaction) {
                scope.currentTransaction = transaction;
                scope.$emit('transactionTable.sequenceSelected', transaction);
                console.log('transactionTable.sequenceSelected', transaction);
            };
            scope.traceRemoteAddr = function (transaction) {
                console.log('traceRemoteAddr', transaction);
                alert('not implemented. ip정보 조회 페이지로 연결.');
            };

            /**
             * event listeners
             */
            scope.$on('transactionTable.appendTransactionList', function (event, transactionList) {
                appendTransactionList(transactionList);
            });
            scope.$on('transactionTable.clear', function (event) {
                clear();
            });
        }
    };
}]);
