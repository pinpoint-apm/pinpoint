'use strict';

pinpointApp.constant('TransactionListConfig', {
    applicationUrl: '/transactionmetadata.pinpoint',
    MAX_FETCH_BLOCK_SIZE: 10
});

pinpointApp.controller('TransactionListCtrl', ['TransactionListConfig', '$scope', '$rootScope', '$timeout', '$document', function (cfg, $scope, $rootScope, $timeout, $document) {

    /**
     * variables definition
     */
    var fetchCount = 1,
        lastFetchedIndex = 0;
    var fetchStart, fetchNext, fetchAll, emitTransactionListToTable;

    /**
     * internal methods
     */
    emitTransactionListToTable = function (data) {
        $scope.$emit('transactionTable.appendTransactionList', data.metadata);
    };
    fetchNext = function () {
        fetchStart();
    };
    fetchAll = function () {
        cfg.MAX_FETCH_BLOCK_SIZE = 100000000;
        fetchStart();
    };
    fetchStart = function () {
        var traces = parent.opener.selectdTracesBox[parent.window.name];
        if (!traces) {
            alert("Query parameter 캐시가 삭제되었기 때문에 데이터를 조회할 수 없습니다.\n\n이러한 현상은 scatter chart를 새로 조회했을 때 발생할 수 있습니다.");
//            $("#loader").hide();
            return;
        }

        var query = [];
        var temp = {};
        for (var i = lastFetchedIndex, j = 0; i < cfg.MAX_FETCH_BLOCK_SIZE * fetchCount && i < traces.length; i++, j++) {
            if (i > 0) {
                query.push("&");
            }
            console.log(i, j, traces.length);
            query.push("I");
            query.push(j);
            query.push("=");
            query.push(traces[i].traceId);

            query.push("&T");
            query.push(j);
            query.push("=");
            query.push(traces[i].x)

            query.push("&R");
            query.push(j);
            query.push("=");
            query.push(traces[i].y)

            lastFetchedIndex++;
        }

        fetchCount++;

        if (i == traces.length) {
//            $("#fetchButtons").hide();
        }

//        $("#readProgress .bar").text("fetched (" + i + " / " + traces.length + ")");
//        $("#readProgress .bar").css("width", i / traces.length * 100 + "%")

        var startTime = new Date().getTime();

        $.post(cfg.applicationUrl, query.join(""), function(d) {
            var fetchedTime = new Date().getTime();
            console.log("List fetch time. " + (fetchedTime - startTime) + "ms");
            emitTransactionListToTable(d);
            var renderTime = new Date().getTime();
            console.log("List render time. " + (renderTime - fetchedTime) + "ms");
//            $("#loader").hide();
        }).fail(function() {
            alert("Failed to fetching the request informations.");
        });
    }

    /**
     * initialization
     */
    $(document).ready(function () {
        if(!parent.opener) {
            return;
        }

        fetchStart();

//        $("#fetchMore").bind('click', fetchNext);
//        $("#fetchAll").bind('click', fetchAll);
    });
    $timeout(function () {
        var myLayout;

        $(document).ready(function () {
            myLayout = $("#main-container").layout({
                north__minSize: 20,
                north__size: 200,
//                north__spacing_closed: 20,
//                north__togglerLength_closed: 100,
//                north__togglerAlign_closed: "top",
                center__maskContents: true // IMPORTANT - enable iframe masking
            });
            //loadIframePage('west');
        });
    }, 500);


    $scope.$on('transactionTable.applicationSelected', function (event, transaction) {
        angular.element('#transactionDetail').attr('src', "#/transactionDetail/" + transaction.traceId + "/" + transaction.collectorAcceptTime);
//        angular.element('#transactionDetail').attr('src', "/transactionInfo.pinpoint?traceId=" + transaction.traceId + "&focusTimestamp=" + transaction.collectorAcceptTime);
    });
    $scope.$on('transactionTable.sequenceSelected', function (event, transaction) {

    });
}]);
