'use strict';

pinpointApp.constant('TransactionListConfig', {
    applicationUrl: '/transactionmetadata.pinpoint',
    MAX_FETCH_BLOCK_SIZE: 100,
    transactionIndex: {
        x: 0,
        y: 1,
        transactionId: 2,
        type: 3
    }
});

pinpointApp.controller('TransactionListCtrl', ['TransactionListConfig', '$scope', '$rootScope', '$timeout', '$window', '$http', 'webStorage', 'TimeSliderVo', 'encodeURIComponentFilter', 'WebSql',
    function (cfg, $scope, $rootScope, $timeout, $window, $http, webStorage, TimeSliderVo, encodeURIComponentFilter, oWebSql) {

        // define private variables
        var nFetchCount, nLastFetchedIndex, htTransactions, oTimeSliderVo;

        // define private variables of methods
        var fetchStart, fetchNext, fetchAll, emitTransactionListToTable, getQuery, getTransactionList, changeTransactionDetail;

        /**
         * initialization
         */
        $timeout(function () {

            // initialize private variables;
            nFetchCount = 1;
            nLastFetchedIndex = 0;
            $scope.transactionDetailUrl = '#/transactionDetail';

//            htTransactions = webStorage.session.get($window.name);
//        htTransactions = opener[$window.name];
            oWebSql.select('SELECT data FROM transactionList WHERE name = ?',  [$window.name], function (results) {
                htTransactions = JSON.parse(results.rows.item(0).data);
                console.log('htTransactions', htTransactions);
                oTimeSliderVo = new TimeSliderVo();
                oTimeSliderVo.setTotal(htTransactions.aTraces.length);

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



        });

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
                query = query.concat(["I", j, "=", htTransactions.aTraces[i][cfg.transactionIndex.transactionId]]);
                query = query.concat(["&T", j, "=", htTransactions.aTraces[i][cfg.transactionIndex.x]]);
                query = query.concat(["&R", j, "=", htTransactions.aTraces[i][cfg.transactionIndex.y]]);
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
                    $scope.$emit('timeSlider.changeMoreToDone');
                    return false;
                } else if (data.metadata.length < cfg.MAX_FETCH_BLOCK_SIZE || oTimeSliderVo.getTotal() === data.metadata.length + oTimeSliderVo.getCount()) {
                    $scope.$emit('timeSlider.disableMore');
                    $scope.$emit('timeSlider.changeMoreToDone');
                    oTimeSliderVo.setInnerFrom(htTransactions.htXY.nXFrom);
                } else {
                    $scope.$emit('timeSlider.enableMore');
                    oTimeSliderVo.setInnerFrom(_.last(data.metadata).collectorAcceptTime);
                }
                emitTransactionListToTable(data);

                oTimeSliderVo.addCount(data.metadata.length);
                $scope.$emit('timeSlider.setInnerFromTo', oTimeSliderVo);
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
                    $scope.$emit('timeSlider.changeMoreToDone');
                    return false;
                } else if (data.metadata.length < cfg.MAX_FETCH_BLOCK_SIZE || oTimeSliderVo.getTotal() === data.metadata.length) {
                    $scope.$emit('timeSlider.disableMore');
                    $scope.$emit('timeSlider.changeMoreToDone');
                    oTimeSliderVo.setInnerFrom(htTransactions.htXY.nXFrom);
                } else {
                    $scope.$emit('timeSlider.enableMore');
                    oTimeSliderVo.setInnerFrom(_.last(data.metadata).collectorAcceptTime);
                }
                emitTransactionListToTable(data);

                oTimeSliderVo.setFrom(htTransactions.htXY.nXFrom);
                oTimeSliderVo.setTo(htTransactions.htXY.nXTo);
                oTimeSliderVo.setInnerTo(htTransactions.htXY.nXTo);
                oTimeSliderVo.setCount(data.metadata.length);

                $scope.$emit('timeSlider.initialize', oTimeSliderVo);
            });
        };

        /**
         * get transaction list
         * @param query
         * @param cb
         */
        getTransactionList = function (query, cb) {
            $http
                .post(cfg.applicationUrl, query.join(""), {
                    headers: {'Content-Type': 'application/x-www-form-urlencoded'}
                })
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
         * change transaction detail
         * @param transaction
         */
        changeTransactionDetail = function (transaction) {
            $scope.transactionDetailUrl = '#/transactionDetail';
            if (transaction.traceId && transaction.collectorAcceptTime) {
                $scope.transactionDetailUrl += '/' + encodeURIComponentFilter(transaction.traceId) + '/' + transaction.collectorAcceptTime;
            }
        };

        /**
         * scope event on transactionTable.applicationSelected
         */
        $scope.$on('transactionTable.applicationSelected', function (event, transaction) {
            changeTransactionDetail(transaction);
        });

        /**
         * scope event on timeSlider.moreClicked
         */
        $scope.$on('timeSlider.moreClicked', function (event) {
            $scope.$emit('timeSlider.disableMore');
            $timeout(function () {
                fetchNext();
            }, 1000);

        });
    }]);
