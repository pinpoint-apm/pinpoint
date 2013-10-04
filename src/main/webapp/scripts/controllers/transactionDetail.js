'use strict';

pinpointApp
    .controller('TransactionDetailCtrl', ['$scope', '$rootScope', function ($scope, $rootScope) {
        $rootScope.wrapperClass = 'no-navbar';
        $rootScope.wrapperStyle = {
            'padding-top': '70px'
        };
    }]);
