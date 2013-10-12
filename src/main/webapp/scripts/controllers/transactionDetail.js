'use strict';
pinpointApp.constant('TransactionDetailConfig', {
    applicationUrl: '/transactionInfo.pinpoint'
});

pinpointApp
    .controller('TransactionDetailCtrl', ['TransactionDetailConfig', '$scope', '$rootScope', '$routeParams', function (cfg,  $scope, $rootScope, $routeParams) {
        $rootScope.wrapperClass = 'no-navbar';
        $rootScope.wrapperStyle = {
            'padding-top': '70px'
        };

        var getTransactionDetail, parseTransactionDetail, showCallStacks,
            bShowCallStacksOnce;

        bShowCallStacksOnce = false;

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

        parseTransactionDetail = function (result) {
            $scope.transactionDetail = result;
            $scope.$digest();
        };

        showCallStacks = function () {
            if (bShowCallStacksOnce === false) {
                bShowCallStacksOnce = true;
                $scope.$emit('callStacks.initialize', $scope.transactionDetail);
            }
        };

        if ($routeParams.traceId && $routeParams.focusTimestamp) {
            getTransactionDetail($routeParams.traceId, $routeParams.focusTimestamp, function (result) {
                parseTransactionDetail(result);
                showCallStacks();
            });
        }

        $("#traceTabs li:nth-child(2) a").bind("click", function (e) {
            $scope.$emit('servermap.initializeWithMapData', $scope.transactionDetail);
        });
        $("#traceTabs li:nth-child(3) a").bind("click", function (e) {
            $scope.$emit('timeline.initialize', $scope.transactionDetail);
        });


    }]);
