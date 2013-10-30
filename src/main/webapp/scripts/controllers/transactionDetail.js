'use strict';
pinpointApp.constant('TransactionDetailConfig', {
    applicationUrl: '/transactionInfo.pinpoint'
});

pinpointApp.controller('TransactionDetailCtrl', ['TransactionDetailConfig', '$scope', '$rootScope', '$routeParams', '$timeout', '$rootElement', 'Alerts', 'ProgressBar', function (cfg, $scope, $rootScope, $routeParams, $timeout, $rootElement, Alerts, ProgressBar) {

    // defien private variables
    var oAlert, oProgressBar, bShowCallStacksOnce;

    // define private variables of methods
    var getTransactionDetail, parseTransactionDetail, showCallStacks, parseCompleteStateToClass;

    // initialize
    bShowCallStacksOnce = false;
    $rootScope.wrapperClass = 'no-navbar';
    $rootScope.wrapperStyle = {
        'padding-top': '70px'
    };
    oAlert = new Alerts($rootElement);
    oProgressBar = new ProgressBar($rootElement);

    /**
     * initialize
     */
    $timeout(function () {
        if ($routeParams.traceId && $routeParams.focusTimestamp) {
            oProgressBar.startLoading();
            getTransactionDetail($routeParams.traceId, $routeParams.focusTimestamp, function (result) {
                parseTransactionDetail(result);
                showCallStacks();
                $timeout(function () {
                    oProgressBar.setLoading(100);
                    oProgressBar.stopLoading();
                }, 100);
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
        oProgressBar.setLoading(30);
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
                oProgressBar.setLoading(70);
                cb(result);
            },
            error: function (xhr, status, error) {
                oProgressBar.stopLoading();
                oAlert.showError('There is some error while downloading the data.');
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
        $scope.completeStateClass = parseCompleteStateToClass(result.completeState);
        $scope.$digest();
    };

    /**
     * parse complete state to class
     * @param completeState
     * @returns {string}
     */
    parseCompleteStateToClass = function (completeState) {
        var completeStateClass = 'label-important';
        if (completeState === 'Complete') {
            completeStateClass = 'label-success';
        } else if (completeState === 'Progress') {
            completeStateClass = 'lable-warning';
        }
        return completeStateClass;
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
        $scope.$emit('serverMap.initializeWithMapData', $scope.transactionDetail);
    });
    $("#traceTabs li:nth-child(3) a").bind("click", function (e) {
        $scope.$emit('timeline.initialize', $scope.transactionDetail);
    });

}]);
