'use strict';

pinpointApp.constant('TransactionListConfig', {
    applicationUrl: '/transactionmetadata.pinpoint',
    MAX_FETCH_BLOCK_SIZE: 10
});

pinpointApp.controller('TransactionListCtrl', ['TransactionListConfig', '$scope', '$rootScope', '$timeout', 'webStorage', 'timeSliderDao', function (cfg, $scope, $rootScope, $timeout, webStorage, oTimeSliderDao) {

    // variables definition
    var fetchCount, lastFetchedIndex, token, traces,
        fetchStart, fetchNext, fetchAll, emitTransactionListToTable, getQuery, getTransactionList;

    // initialize private variables;
    fetchCount = 1;
    lastFetchedIndex = 0;
    token = parent.window.name;
    traces = webStorage.session.get(token);
    oTimeSliderDao.setTotal(traces.length);

    /**
     * emit transaction list to table
     * @param data
     */
    emitTransactionListToTable = function (data) {
        $scope.$emit('transactionTable.appendTransactionList', data.metadata);
    };

    /**
     * get query
     * @returns {Array}
     */
    getQuery = function () {
        if (!traces) {
            alert("Query parameter 캐시가 삭제되었기 때문에 데이터를 조회할 수 없습니다.\n\n이러한 현상은 scatter chart를 새로 조회했을 때 발생할 수 있습니다.");
            return;
        }
        var query = [];
        for (var i = lastFetchedIndex, j = 0; i < cfg.MAX_FETCH_BLOCK_SIZE * fetchCount && i < traces.length; i++, j++) {
            if (i > 0) { query.push("&"); }
            query = query.concat(["I", j, "=", traces[i].traceId]);
            query = query.concat(["&T", j, "=", traces[i].x]);
            query = query.concat(["&R", j, "=", traces[i].y]);
            lastFetchedIndex++;
        }
        fetchCount++;
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
            } else if (data.metadata.length < cfg.MAX_FETCH_BLOCK_SIZE) {
                $scope.$emit('timeSlider.disableMore');
            }
            emitTransactionListToTable(data);

            oTimeSliderDao.setInnerFrom(_.last(data.metadata).startTime);
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
            } else if (data.metadata.length < cfg.MAX_FETCH_BLOCK_SIZE) {
                $scope.$emit('timeSlider.disableMore');
            }
            emitTransactionListToTable(data);

            if (oTimeSliderDao.getFrom() === null) {
                oTimeSliderDao.setFrom(_.last(traces).x);
            }
            if (oTimeSliderDao.getTo() === null) {
                var to = _.first(traces).x;
                oTimeSliderDao.setTo(to);
                oTimeSliderDao.setInnerTo(to);
            }
            oTimeSliderDao.setInnerFrom(_.last(data.metadata).startTime);
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
        $.post(cfg.applicationUrl, query.join(""),function (data) {
            cb(data);
        }).fail(function () {
            alert("Failed to fetching the request informations.");
        });
    };

    /**
     * initialization
     */
    $(document).ready(function () {
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

    // scope events on\
    $scope.$on('transactionTable.applicationSelected', function (event, transaction) {
        angular.element('#transactionDetail').attr('src', "#/transactionDetail/" + transaction.traceId + "/" + transaction.collectorAcceptTime);
//        angular.element('#transactionDetail').attr('src', "/transactionInfo.pinpoint?traceId=" + transaction.traceId + "&focusTimestamp=" + transaction.collectorAcceptTime);
    });
    $scope.$on('transactionTable.sequenceSelected', function (event, transaction) {

    });
    $scope.$on('timeSlider.moreClicked', function (event) {
        fetchNext();
    });
}]);
