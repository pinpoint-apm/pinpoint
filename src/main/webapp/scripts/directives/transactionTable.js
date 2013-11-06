'use strict';

pinpointApp.directive('transactionTable', ['$window', function ($window) {
    return {
        restrict: 'EA',
        replace: true,
        templateUrl: 'views/transactionTable.html',
        link: function postLink(scope, element, attrs) {

            // define private variables of methods
            var clear, appendTransactionList, resetIndexToTransactionList;

            // initialize scope variables
            scope.transactionList = [];
            scope.currentTransaction = null;
            scope.transactionReverse = false;

            /**
             * clear
             */
            clear = function () {
                scope.transactionList = [];
            };

            /**
             * append transaction list
             * @param transactionList
             */
            appendTransactionList = function (transactionList) {
                scope.transactionList = scope.transactionList.concat(transactionList);
                resetIndexToTransactionList();
            };

            /**
             * reset index to transaction list
             */
            resetIndexToTransactionList = function () {
                var index = 1;
                angular.forEach(scope.transactionList, function (val, key) {
                    val['index'] = index++;
                });
            };

            /**
             * scope trace by application
             * @param transaction
             */
            scope.traceByApplication = function (transaction) {
                scope.currentTransaction = transaction;
                scope.$emit('transactionTable.applicationSelected', transaction);
            };

            scope.traceByApplicationInNewWindow = function (transaction) {
                $window.open('#/transactionDetail/' + transaction.traceId + '/' + transaction.collectorAcceptTime);
            };

            /**
             * scope trace by sequence
             * @param transaction
             */
            scope.traceBySequence = function (transaction) {
                scope.currentTransaction = transaction;
                scope.$emit('transactionTable.sequenceSelected', transaction);
            };

            /**
             * scope trace remote addr
             * @param transaction
             */
            scope.traceRemoteAddr = function (transaction) {
                console.log('traceRemoteAddr', transaction);
                alert('not implemented. ip정보 조회 페이지로 연결.');
            };

            /**
             * scope transaction order
             * @param orderKey
             */
            scope.transactionOrder = function (orderKey) {
                if (scope.transactionOrderBy === orderKey) {
                    scope.transactionReverse = !scope.transactionReverse;
                } else {
                    scope.transactionReverse = false;
                }
                scope.transactionOrderBy = orderKey;
            };

            /**
             * scope event on transactionTable.appendTransactionList
             */
            scope.$on('transactionTable.appendTransactionList', function (event, transactionList) {
                appendTransactionList(transactionList);
            });

            /**
             * scope event on transactionTable.clear
             */
            scope.$on('transactionTable.clear', function (event) {
                clear();
            });
        }
    };
}]);
