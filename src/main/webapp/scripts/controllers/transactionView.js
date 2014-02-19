'use strict';

pinpointApp.constant('TransactionViewConfig', {
    applicationUrl: '/transactionInfo.pinpoint'
});

pinpointApp.controller('TransactionViewCtrl', [ 'TransactionViewConfig', '$scope', '$rootScope', '$rootElement', 'Alerts', 'ProgressBar', '$timeout', '$routeParams', 'TransactionDao', 'AgentDao',
    function (cfg, $scope, $rootScope, $rootElement, Alerts, ProgressBar, $timeout, $routeParams, TransactionDao, AgentDao) {

        // define private variables
        var oAlert, oProgressBar, htHeapCache;

        // define private variables of methods
        var parseTransactionDetail, parseCompleteStateToClass, showCallStacks, showServerMap, showHeapChart;

        // initialize
        $rootScope.wrapperClass = 'no-navbar';
        $rootScope.wrapperStyle = {
            'padding-top': '30px'
        };

        oAlert = new Alerts($rootElement);
        oProgressBar = new ProgressBar($rootElement);

        /**
         * initialize
         */
        $timeout(function () {
            if ($routeParams.agentId && $routeParams.traceId && $routeParams.focusTimestamp) {
                oProgressBar.startLoading();
                oProgressBar.setLoading(30);
                TransactionDao.getTransactionDetail($routeParams.traceId, $routeParams.focusTimestamp, function (err, result) {
                    if (err) {
                        oProgressBar.stopLoading();
                        oAlert.showError('There is some error while downloading the data.');
                    }
                    oProgressBar.setLoading(70);
                    parseTransactionDetail(result);
                    showCallStacks();
                    showServerMap();
                    $timeout(function () {
                        oProgressBar.setLoading(100);
                        oProgressBar.stopLoading();

                        $("#main-container").layout({
                            north__minSize: 50,
                            north__size: 210,
//                north__spacing_closed: 20,
//                north__togglerLength_closed: 100,
//                north__togglerAlign_closed: "top",
                            onresize_end: function (edge) {
                                if (edge === 'center') {
                                    $scope.$broadcast('distributedCallFlow.resize.forTransactionDetail');
                                }
                            },
                            center__maskContents: true // IMPORTANT - enable iframe masking
                        });
                    }, 100);
                });
                showHeapChart($routeParams.agentId, $routeParams.focusTimestamp);
            }
        }, 100);

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
                completeStateClass = 'label-warning';
            }
            return completeStateClass;
        };

        /**
         * show call stacks
         */
        showCallStacks = function () {
//            $scope.$broadcast('callStacks.initialize.forTransactionView', $scope.transactionDetail);
            $scope.$broadcast('distributedCallFlow.initialize.forTransactionView', $scope.transactionDetail);
        };

        /**
         * show server map
         */
        showServerMap = function () {
            $scope.$broadcast('serverMap.initializeWithMapData', $scope.transactionDetail);
        };

        /**
         * show heap chart
         */
        showHeapChart = function (agentId, focusTimestamp) {
            focusTimestamp = parseInt(focusTimestamp, 10);
            var query = {
                agentId: agentId,
                from: focusTimestamp - (1000 * 60 * 10), // - 10 mins
                to: focusTimestamp + (1000 * 60 * 10), // + 10 mins
                sampleRate: AgentDao.getSampleRate(20)
            };
            $scope.$broadcast('agentChartGroup.initialize.forTransactionView', query);
        };


        $scope.$on('distributedCallFlow.rowSelected.forTransactionView', function (e, item) {
            console.log('distributedCallFlow.rowSelected.forTransactionView', item);
        });

    }
]);
