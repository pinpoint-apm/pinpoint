'use strict';

pinpointApp.constant('TransactionListConfig', {
    applicationUrl: '/transactionmetadata.pinpoint',
    MAX_FETCH_BLOCK_SIZE: 10
});

pinpointApp.controller('TransactionListCtrl', ['TransactionListConfig', '$scope', '$rootScope', '$timeout', '$window', '$http', 'webStorage', 'TimeSliderDao',
    function (cfg, $scope, $rootScope, $timeout, $window, $http, webStorage, TimeSliderDao) {

        // define private variables
        var nFetchCount, nLastFetchedIndex, htTransactions, oTimeSliderDao;

        // define private variables of methods
        var fetchStart, fetchNext, fetchAll, emitTransactionListToTable, getQuery, getTransactionList;

        // initialize private variables;
        nFetchCount = 1;
        nLastFetchedIndex = 0;
        htTransactions = webStorage.session.get($window.name);
        oTimeSliderDao = new TimeSliderDao();
        oTimeSliderDao.setTotal(htTransactions.aTraces.length);

        /**
         * emit transaction list to table
         * @param data
         */
        emitTransactionListToTable = function (data) {
            $scope.$emit('transactionTable.appendTransactionList', data.metadata);
        };

        /**
         * get query
         * @returns {Array | Boolean}
         */
        getQuery = function () {
            if (!htTransactions.aTraces) {
                $window.alert("Query parameter 캐시가 삭제되었기 때문에 데이터를 조회할 수 없습니다.\n\n이러한 현상은 scatter chart를 새로 조회했을 때 발생할 수 있습니다.");
                return false;
            }
            var query = [];
            for (var i = nLastFetchedIndex, j = 0; i < cfg.MAX_FETCH_BLOCK_SIZE * nFetchCount && i < htTransactions.aTraces.length; i++, j++) {
                if (i > 0) {
                    query.push("&");
                }
                query = query.concat(["I", j, "=", htTransactions.aTraces[i].traceId]);
                query = query.concat(["&T", j, "=", htTransactions.aTraces[i].x]);
                query = query.concat(["&R", j, "=", htTransactions.aTraces[i].y]);
                nLastFetchedIndex++;
            }
            nFetchCount++;
            return query;
        };

        /**
         * fetch next
         */
        fetchNext = function () {
            getTransactionList(getQuery(), function (data) {
                if (data.metadata.length === 0) {
                    $scope.$emit('timeSlider.disableMore');
                    return false;
                } else if (data.metadata.length < cfg.MAX_FETCH_BLOCK_SIZE || oTimeSliderDao.getTotal() === data.metadata.length + oTimeSliderDao.getCount()) {
                    $scope.$emit('timeSlider.disableMore');
                    oTimeSliderDao.setInnerFrom(htTransactions.htXY.nXFrom);
                } else {
                    oTimeSliderDao.setInnerFrom(_.last(data.metadata).startTime);
                }
                emitTransactionListToTable(data);

                oTimeSliderDao.addCount(data.metadata.length);
                $scope.$emit('timeSlider.setInnerFromTo', oTimeSliderDao);
            });
        };

        /**
         * fetch all
         */
        fetchAll = function () {
            cfg.MAX_FETCH_BLOCK_SIZE = 100000000;
            fetchNext();
        };

        /**
         * fetch start
         */
        fetchStart = function () {
            getTransactionList(getQuery(), function (data) {
                if (data.metadata.length === 0) {
                    $scope.$emit('timeSlider.disableMore');
                    return false;
                } else if (data.metadata.length < cfg.MAX_FETCH_BLOCK_SIZE || oTimeSliderDao.getTotal() === data.metadata.length) {
                    $scope.$emit('timeSlider.disableMore');
                    oTimeSliderDao.setInnerFrom(htTransactions.htXY.nXFrom);
                } else {
                    oTimeSliderDao.setInnerFrom(_.last(data.metadata).startTime);
                }
                emitTransactionListToTable(data);

                oTimeSliderDao.setFrom(htTransactions.htXY.nXFrom);
                oTimeSliderDao.setTo(htTransactions.htXY.nXTo);
                oTimeSliderDao.setInnerTo(htTransactions.htXY.nXTo);
                oTimeSliderDao.setCount(data.metadata.length);

                $scope.$emit('timeSlider.initialize', oTimeSliderDao);
            });
        };

        /**
         * get transaction list
         * @param query
         * @param cb
         */
        getTransactionList = function (query, cb) {
//        $.post(cfg.applicationUrl, query.join(""),function (data) {
//            cb(data);
//        }).fail(function () {
//            $window.alert("Failed to fetching the request information.");
//        });
            $http
                .post(cfg.applicationUrl + '?' + query.join(""))
                .success(function (data, status) {
                    if (angular.isFunction(cb)) {
                        cb(data);
                    }
                })
                .error(function (data, status) {
                    $window.alert("Failed to fetching the request information.");
                });
        };

        /**
         * initialization
         */
        $timeout(function () {
            fetchStart();
            $timeout(function () {
                $("#main-container").layout({
                    north__minSize: 20,
                    north__size: 200,
//                north__spacing_closed: 20,
//                north__togglerLength_closed: 100,
//                north__togglerAlign_closed: "top",
                    center__maskContents: true // IMPORTANT - enable iframe masking
                });
            }, 500);
        });

        /**
         * scope event on transactionTable.applicationSelected
         */
        $scope.$on('transactionTable.applicationSelected', function (event, transaction) {
            angular.element('#transactionDetail').attr('src', "#/transactionDetail/" + transaction.traceId + "/" + transaction.collectorAcceptTime);
        });

        /**
         * scope event on timeSlider.moreClicked
         */
        $scope.$on('timeSlider.moreClicked', function (event) {
            fetchNext();
        });
    }]);
