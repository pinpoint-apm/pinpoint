(function() {
	'use strict';
	/**
	 * (en)TransactionListCtrl 
	 * @ko TransactionListCtrl
	 * @group Controller
	 * @name TransactionListCtrl
	 * @class
	 */
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
	
	pinpointApp.controller('TransactionListCtrl', ['TransactionListConfig', '$scope', '$routeParams', '$rootScope', '$timeout', '$window', '$http', 'webStorage', 'TimeSliderVoService', 'TransactionDaoService',
	    function (cfg, $scope, $routeParams, $rootScope, $timeout, $window, $http, webStorage, TimeSliderVoService, oTransactionDaoService) {
			$at($at.TRANSACTION_LIST_PAGE);
	        // define private variables
	        var nFetchCount, nLastFetchedIndex, htTransactionInfo, htTransactionData, oTimeSliderVoService;
	
	        // define private variables of methods
	        var fetchStart, fetchNext, fetchAll, emitTransactionListToTable, getQuery, getTransactionList, changeTransactionDetail,
	            validateParentWindow, parseWindowName, hasScatterByApplicationName, getDataByTransactionInfo;
	
	        /**
	         * initialization
	         */
	        $timeout(function () {
	
	            // initialize private variables;
	            nFetchCount = 1;
	            nLastFetchedIndex = 0;
	            $scope.transactionDetailUrl = 'index.html#/transactionDetail';
	            $scope.sidebarLoading = true;
	
	            if(!validateParentWindow()) {
	                alert('Scatter data of parent window had been changed.\r\nso can\'t scan the data any more.');
	                $window.location.replace( $window.location.href.replace( "transactionList", "main" ) );
	            }
	
	            htTransactionInfo = parseWindowName($window.name);
	
	            if(!hasScatterByApplicationName(htTransactionInfo.applicationName)) {
	                alert('There is no ' + htTransactionInfo.applicationName + ' scatter data in parent window.');
	                $window.location.replace( $window.location.href.replace( "transactionList", "main" ) );
	            }
	
	            htTransactionData = getDataByTransactionInfo(htTransactionInfo);
	            oTimeSliderVoService = new TimeSliderVoService();
	            oTimeSliderVoService.setTotal(htTransactionData.length);
	
	            fetchStart();
	            $timeout(function () {
	                $("#main-container").layout({
	                    north__minSize: 20,
	                    north__size: (window.innerHeight - 40) / 2,
	//                north__spacing_closed: 20,
	//                north__togglerLength_closed: 100,
	//                north__togglerAlign_closed: "top",
	                    center__maskContents: true // IMPORTANT - enable iframe masking
	                });
	            }, 100);
	
	        }, 100);
	
	        /**
	         * validate parent window
	         * @returns {*}
	         */
	        validateParentWindow = function () {
	        	if ( $window.opener == null ) return false;
	            var $parentParams = $window.opener.$routeParams;
	            return angular.isDefined($routeParams) &&
	                angular.isDefined($parentParams) &&
	                angular.equals($routeParams.application, $parentParams.application) &&
	                angular.equals($routeParams.readablePeriod, $parentParams.readablePeriod) &&
	                angular.equals($routeParams.queryEndDateTime, $parentParams.queryEndDateTime);
	        };
	
	        /**
	         * parse window name
	         * @param windowName
	         * @returns {{applicationName: *, nXFrom: *, nXTo: *, nYFrom: *, nYTo: *}}
	         */
	        parseWindowName = function (windowName) {
	            var t = windowName.split('|');
	            return {
	                applicationName: t[0],
	                nXFrom: t[1],
	                nXTo: t[2],
	                nYFrom: t[3],
	                nYTo: t[4]
	            };
	        };
	
	        /**
	         * has scatter by application name
	         * @param applicationName
	         * @returns {*}
	         */
	        hasScatterByApplicationName = function (applicationName) {
	            return angular.isDefined($window.opener.htoScatter[applicationName]);
	        };
	
	        /**
	         * get data by transaction list info
	         * @param t
	         * @returns {*}
	         */
	        getDataByTransactionInfo = function (t) {
	            var oScatter = $window.opener.htoScatter[t.applicationName];
	            return oScatter.getDataByXY(t.nXFrom, t.nXTo, t.nYFrom, t.nYTo);
	        };
	
	        /**
	         * emit transaction list to table
	         * @param data
	         */
	        emitTransactionListToTable = function (data) {
	            $scope.$emit('transactionTableDirective.appendTransactionList', data.metadata);
	        };
	
	        /**
	         * get query
	         * @returns {Array | Boolean}
	         */
	        getQuery = function () {
	            if (!htTransactionData) {
	                $window.alert("Query failed - Query parameter cache deleted.\n\nPossibly due to scatter chart being refreshed.")
	                return false;
	            }
	            var query = [];
	            for (var i = nLastFetchedIndex, j = 0; i < cfg.MAX_FETCH_BLOCK_SIZE * nFetchCount && i < htTransactionData.length; i++, j++) {
	                if (i > 0) {
	                    query.push("&");
	                }
	                query = query.concat(["I", j, "=", htTransactionData[i][cfg.transactionIndex.transactionId]]);
	                query = query.concat(["&T", j, "=", htTransactionData[i][cfg.transactionIndex.x]]);
	                query = query.concat(["&R", j, "=", htTransactionData[i][cfg.transactionIndex.y]]);
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
	                    $scope.$emit('timeSliderDirective.disableMore');
	                    $scope.$emit('timeSliderDirective.changeMoreToDone');
	                    return false;
	                } else if (data.metadata.length < cfg.MAX_FETCH_BLOCK_SIZE || oTimeSliderVoService.getTotal() === data.metadata.length + oTimeSliderVoService.getCount()) {
	                    $scope.$emit('timeSliderDirective.disableMore');
	                    $scope.$emit('timeSliderDirective.changeMoreToDone');
	                    oTimeSliderVoService.setInnerFrom(htTransactionInfo.nXFrom);
	                } else {
	                    $scope.$emit('timeSliderDirective.enableMore');
	                    oTimeSliderVoService.setInnerFrom(_.last(data.metadata).collectorAcceptTime);
	                }
	                emitTransactionListToTable(data);
	
	                oTimeSliderVoService.addCount(data.metadata.length);
	                $scope.$emit('timeSliderDirective.setInnerFromTo', oTimeSliderVoService);
	                $scope.sidebarLoading = false;
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
	                    $scope.$emit('timeSliderDirective.disableMore');
	                    $scope.$emit('timeSliderDirective.changeMoreToDone');
	                    return false;
	                } else if (data.metadata.length < cfg.MAX_FETCH_BLOCK_SIZE || oTimeSliderVoService.getTotal() === data.metadata.length) {
	                    $scope.$emit('timeSliderDirective.disableMore');
	                    $scope.$emit('timeSliderDirective.changeMoreToDone');
	                    oTimeSliderVoService.setInnerFrom(htTransactionInfo.nXFrom);
	                } else {
	                    $scope.$emit('timeSliderDirective.enableMore');
	                    oTimeSliderVoService.setInnerFrom(_.last(data.metadata).collectorAcceptTime);
	                }
	                emitTransactionListToTable(data);
	
	                oTimeSliderVoService.setFrom(htTransactionInfo.nXFrom);
	                oTimeSliderVoService.setTo(htTransactionInfo.nXTo);
	                oTimeSliderVoService.setInnerTo(htTransactionInfo.nXTo);
	                oTimeSliderVoService.setCount(data.metadata.length);
	
	                $scope.$emit('timeSliderDirective.initialize', oTimeSliderVoService);
	                $scope.sidebarLoading = false;
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
	            var transactionDetailUrl = 'index.html#/transactionDetail'; // the filename should be existing, if not it's doesn't work on ie and firefox
	            if (transaction.traceId && transaction.collectorAcceptTime) {
	                transactionDetailUrl += '/' + $window.encodeURIComponent(transaction.traceId) + '/' + transaction.collectorAcceptTime;
	                $scope.transactionDetailUrl = transactionDetailUrl;
	            }
	
	        };
	
	        /**
	         * scope event on transactionTable.applicationSelected
	         */
	        $scope.$on('transactionTableDirective.applicationSelected', function (event, transaction) {
	            changeTransactionDetail(transaction);
	        });
	
	        /**
	         * scope event on timeSliderDirective.moreClicked
	         */
	        $scope.$on('timeSliderDirective.moreClicked', function (event) {
	            $scope.sidebarLoading = true;
	            $scope.$emit('timeSliderDirective.disableMore');
	            $timeout(function () {
	                fetchNext();
	            }, 1000);
	
	        });
	    }
	]);
})();