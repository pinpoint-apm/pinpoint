'use strict';
pinpointApp.constant('TransactionDetailConfig', {
    applicationUrl: '/transactionInfo.pinpoint'
});

pinpointApp.controller('TransactionDetailCtrl', ['TransactionDetailConfig', '$scope', '$rootScope', '$routeParams', '$timeout', function (cfg, $scope, $rootScope, $routeParams, $timeout) {

    // define private variables of methods
    var getTransactionDetail, parseTransactionDetail, showCallStacks, bShowCallStacksOnce;

    // initialize
    bShowCallStacksOnce = false;
    $rootScope.wrapperClass = 'no-navbar';
    $rootScope.wrapperStyle = {
        'padding-top': '70px'
    };

    /**
     * initialize
     */
    $timeout(function () {
        if ($routeParams.traceId && $routeParams.focusTimestamp) {
            getTransactionDetail($routeParams.traceId, $routeParams.focusTimestamp, function (result) {
                parseTransactionDetail(result);
                showCallStacks();
            });
        }
    });

    /**
     * get transaction detail
     * @param traceId
     * @param focusTimestamp
     * @param cb
     */
    getTransactionDetail = function (traceId, focusTimestamp, cb) {
        jQuery.ajax({
            type: 'GET',
            url: cfg.applicationUrl,
            cache: false,
            dataType: 'json',
            data: {
                jsonResult: true,
                traceId: traceId,
                focusTimestamp: focusTimestamp
            },
            success: function (result) {
                cb(result);
            },
            error: function (xhr, status, error) {
                console.log("ERROR", status, error);
            }
        });
    };

    /**
     * parse transaction detail
     * @param result
     */
    parseTransactionDetail = function (result) {
        $scope.transactionDetail = result;
        $scope.$digest();
    };

    /**
     * show call stacks
     */
    showCallStacks = function () {
        if (bShowCallStacksOnce === false) {
            bShowCallStacksOnce = true;
            $scope.$emit('callStacks.initialize', $scope.transactionDetail);
        }
    };

    // events binding
    $("#traceTabs li:nth-child(2) a").bind("click", function (e) {
        $scope.$emit('servermap.initializeWithMapData', $scope.transactionDetail);
    });
    $("#traceTabs li:nth-child(3) a").bind("click", function (e) {
        $scope.$emit('timeline.initialize', $scope.transactionDetail);
    });

}]);
