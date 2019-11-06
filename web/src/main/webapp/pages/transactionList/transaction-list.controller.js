(function( $ ) {
	'use strict';
	/**
	 * (en)TransactionListCtrl
	 * @ko TransactionListCtrl
	 * @group Controller
	 * @name TransactionListCtrl
	 * @class
	 */
	pinpointApp.constant("TransactionListConfig", {
		applicationUrl: "transactionmetadata.pinpoint",
		MIN_TRANSACTION_LIST_HEIGHT: 75,
		MAX_FETCH_BLOCK_SIZE: 100,
		TRANSACTION_LIST_HANDLE_POSITION: "transactionList.resizer"
	});

	pinpointApp.controller("TransactionListCtrl", ["TransactionListConfig", "$scope", "$location", "locationService", "$routeParams", "$rootScope", "$timeout", "$window", "$http", "webStorage", "TimeSliderVoService", "TransactionDaoService", "AnalyticsService", "helpContentService",
		function (cfg, $scope, $location, locationService, $routeParams, $rootScope, $timeout, $window, $http, webStorage, TimeSliderVoService, oTransactionDaoService, analyticsService, helpContentService) {
			analyticsService.send(analyticsService.CONST.TRANSACTION_LIST_PAGE);
			// define private variables
			var nFetchCount, nLastFetchedIndex, nRequestCount, htTransactionInfo, htTransactionData, oTimeSliderVoService;
			var aParamTransactionInfo, beforeTransactionDetailUrl = "";

			/**
			 * initialization
			 */
			$timeout(function () {

				// initialize private variables;
				//nFetchCount = 1;
				nLastFetchedIndex = 0;
				$scope.transactionDetailUrl = 'index.html?vs=' + Date.now() + '#/transactionDetail';
				$scope.sidebarLoading = true;

				var bHasParent = hasParent();
				var bHasValidParam = hasValidParam();
				var bHasTransactionInfo = !angular.isUndefined( $routeParams.transactionInfo );
				if ( bHasTransactionInfo ) {
					var i2 = $routeParams.transactionInfo.lastIndexOf("-");
					var i1 = $routeParams.transactionInfo.lastIndexOf("-", i2 -1);
					aParamTransactionInfo = [ $routeParams.transactionInfo.substring(0, i1), $routeParams.transactionInfo.substring(i1+1, i2), $routeParams.transactionInfo.substring(i2+1) ];
				}
				if ( bHasParent && bHasValidParam ) {
					htTransactionInfo = getTransactionInfoFromWindow($window.name);

					if(!hasScatterByApplicationName(htTransactionInfo.applicationName)) {
						alertAndMove(helpContentService.transactionList.openError.noData.replace(/\{\{application\}\}/, htTransactionInfo.applicationName ) );
					} else {
						htTransactionData = getDataByTransactionInfo(htTransactionInfo);
						initAndLoad( bHasTransactionInfo );
					}
				} else {
					if ( bHasTransactionInfo === false ) {
						alertAndMove(helpContentService.transactionList.openError.noParent);
					} else {
						htTransactionInfo = getTransactionInfoFromURL();
						htTransactionData = [[ aParamTransactionInfo[0], aParamTransactionInfo[1], aParamTransactionInfo[2] ]];
						initAndLoad( bHasTransactionInfo );
					}
				}

				$timeout(function () {
					var resizerY = webStorage.get( cfg.TRANSACTION_LIST_HANDLE_POSITION ) === null ? (window.innerHeight - 40) / 2 : parseInt( webStorage.get( cfg.TRANSACTION_LIST_HANDLE_POSITION ) );
					resizerY = Math.max( cfg.MIN_TRANSACTION_LIST_HEIGHT, resizerY );
					if( $("#main-container").length !== 0 ) {
						$("#main-container").layout({
							north__minSize: 30,
							north__size: resizerY,
							//                north__spacing_closed: 20,
							//                north__togglerLength_closed: 100,
							//                north__togglerAlign_closed: "top",
							center__maskContents: true, // IMPORTANT - enable iframe masking
							onresize: function () {
								if (arguments[0] === "north") {
									webStorage.add(cfg.TRANSACTION_LIST_HANDLE_POSITION, arguments[2].innerHeight);
								}
							}
						});
					}
				}, 100);

			}, 100);
			function alertAndMove( msg ) {
				alert( msg );
				locationService.path( "/main/" + $routeParams.application + "/" + $routeParams.readablePeriod + "/" + $routeParams.queryEndDateTime ).replace();
			}
			function initAndLoad(bHasTransactionInfo) {
				oTimeSliderVoService = new TimeSliderVoService();
				oTimeSliderVoService.setTotal(htTransactionData.length);

				fetchStart(bHasTransactionInfo);
			}
			function hasParent() {
				return angular.isDefined( $window.opener );
			}
			function hasValidParam() {
				if ( angular.isUndefined( $window.opener ) || $window.opener === null ) return false;
				var $parentParams = $window.opener.$routeParams;
				if ( angular.isDefined($routeParams) && angular.isDefined($parentParams) ) {
					if ( $parentParams.readablePeriod === "realtime" ) {
						if ( angular.equals($routeParams.application, $parentParams.application ) ) {
							return true;
						}
					} else {
						if ( angular.equals($routeParams.application, $parentParams.application) &&
							angular.equals($routeParams.readablePeriod, $parentParams.readablePeriod) &&
							angular.equals($routeParams.queryEndDateTime, $parentParams.queryEndDateTime) ) {
							return true;
						}
					}
				}
				return false;
			}
			function getTransactionInfoFromWindow(windowName) {
				var t = windowName.split('|');
				if (t.length === 6 ) {
					return {
						applicationName: t[0],
						type: t[1],
						min: t[2],
						max: t[3],
						agent: t[4],
						include: t[5]
					};
				} else {
					return {
						applicationName: t[0],
						nXFrom: t[1],
						nXTo: t[2],
						nYFrom: t[3],
						nYTo: t[4],
						agent: t[5],
						include: t[6]
					};
				}
			}
			function getTransactionInfoFromURL() {
				return {
					applicationName: $routeParams.application.split("@")[0],
					nXFrom: parseInt(aParamTransactionInfo[1]) - 1000,
					nXTo: parseInt(aParamTransactionInfo[1]) + 1000,
					nYFrom: 0,
					nYTo: 0
				};
			}
			function hasScatterByApplicationName(applicationName) {
				return angular.isDefined($window.opener.htoScatter[applicationName]);
			}
			function getDataByTransactionInfo(t) {
				var oScatter = $window.opener.htoScatter[t.applicationName];
				if ( t.type ) {
					return oScatter.getDataByRange( t.type, t.min, t.max, t.agent, t.include );
				} else {
					return oScatter.getDataByXY( t.nXFrom, t.nXTo, t.nYFrom, t.nYTo, t.agent, t.include );
				}

			}
			function emitTransactionListToTable(data) {
				$scope.$emit('transactionTableDirective.appendTransactionList', data.metadata);
			}
			function getQuery() {
				if (!htTransactionData) {
					$window.alert("Query failed - Query parameter cache deleted.\n\nPossibly due to scatter chart being refreshed.");
					return false;
				}
				nRequestCount = 0;
				var query = [];
				var maxLen = nLastFetchedIndex + cfg.MAX_FETCH_BLOCK_SIZE;
				for (var i = nLastFetchedIndex, j = 0; i < maxLen && i < htTransactionData.length; i++, j++) {
					if (i > 0) {
						query.push("&");
					}
					query = query.concat(
						["I", j, "=", htTransactionData[i][0]],
						["&T", j, "=", htTransactionData[i][1]],
						["&R", j, "=", htTransactionData[i][2]]
					);
					nRequestCount++;
					nLastFetchedIndex++;
				}
				return query;
			}
			function fetchNext() {
				getTransactionList(getQuery(), function (data) {
					if (data.metadata.length === 0 ) {
						$scope.$emit('timeSliderDirective.disableMore');
						$scope.$emit('timeSliderDirective.changeMoreToDone');
						return false;
					} else if (data.metadata.length < cfg.MAX_FETCH_BLOCK_SIZE || data.metadata.length + oTimeSliderVoService.getCount() >= oTimeSliderVoService.getTotal() ) {
						$scope.$emit('timeSliderDirective.disableMore');
						$scope.$emit('timeSliderDirective.changeMoreToDone');
						oTimeSliderVoService.setInnerFrom(htTransactionInfo.nXFrom);
					} else {
						$scope.$emit('timeSliderDirective.enableMore');
						oTimeSliderVoService.setInnerFrom(_.last(data.metadata).collectorAcceptTime);
					}
					emitTransactionListToTable(data);
					updateTotal(data.metadata.length);
					oTimeSliderVoService.addCount(data.metadata.length);
					$scope.$emit('timeSliderDirective.setInnerFromTo', oTimeSliderVoService);
					$scope.sidebarLoading = false;
				});
			}
			var fetchStartLoadTryCount = 0;
			var fetchStartLoadTryMaxCount = 3;
			function updateTotal( responseCount ) {
				if ( responseCount > nRequestCount ) {
					oTimeSliderVoService.setTotal( oTimeSliderVoService.getTotal() + responseCount - nRequestCount );
				}
			}
			function fetchStart( bHasTransactionInfo ) {
				var query = getQuery();
				getTransactionList(query, function (data) {
					if (data.metadata.length === 0) {
						$scope.$emit('timeSliderDirective.disableMore');
						$scope.$emit('timeSliderDirective.changeMoreToDone');
						if ( fetchStartLoadTryCount < fetchStartLoadTryMaxCount ) {
							fetchStartLoadTryCount++;
							$timeout(function() {
								fetchStart(bHasTransactionInfo);
							}, 3000);
						} else {
							$window.alert("There is no data.");
							$window.close();
						}
						return false;
					} else if (data.metadata.length < cfg.MAX_FETCH_BLOCK_SIZE || data.metadata.length >= oTimeSliderVoService.getTotal() ) {
						$scope.$emit('timeSliderDirective.disableMore');
						$scope.$emit('timeSliderDirective.changeMoreToDone');
						oTimeSliderVoService.setInnerFrom(htTransactionInfo.nXFrom);
					} else {
						$scope.$emit('timeSliderDirective.enableMore');
						oTimeSliderVoService.setInnerFrom(_.last(data.metadata).collectorAcceptTime);
					}
					emitTransactionListToTable(data);

					updateTotal(data.metadata.length);
					oTimeSliderVoService.setFrom(htTransactionInfo.nXFrom);
					oTimeSliderVoService.setTo(htTransactionInfo.nXTo);
					oTimeSliderVoService.setInnerTo(htTransactionInfo.nXTo);
					oTimeSliderVoService.setCount(data.metadata.length);

					$scope.$emit('timeSliderDirective.initialize', oTimeSliderVoService);
					$scope.sidebarLoading = false;

					if (bHasTransactionInfo) {
						changeTransactionDetail({
							agentId: data.metadata[0].agentId,
							spanId: data.metadata[0].spanId,
							traceId: aParamTransactionInfo[0],
							collectorAcceptTime: aParamTransactionInfo[1],
							elapsed: aParamTransactionInfo[2]
						});
					}
				});
			}
			function getTransactionList(query, cb) {
				$http.post(cfg.applicationUrl, query.join(""), {
					headers: {'Content-Type': 'application/x-www-form-urlencoded'}
				}).success(function (data, status) {
					if (angular.isFunction(cb)) {
						cb(data);
					}
				}).error(function (data, status) {
					$window.alert("Failed to fetching the request information.");
				});
			}
			function changeTransactionDetail(transaction) {
				var transactionDetailUrl = 'index.html?vs=' + Date.now() + '#/transactionDetail';
				if (transaction.traceId && transaction.collectorAcceptTime) {
					transactionDetailUrl += '/' + $window.encodeURIComponent(transaction.traceId) + '/' + transaction.collectorAcceptTime + '/' + transaction.agentId + '/' + transaction.spanId;
				}
				if ( beforeTransactionDetailUrl == transactionDetailUrl ) {
					$scope.$emit( "transactionTableDirective.completedDetailPageLoad" );
				} else {
					beforeTransactionDetailUrl = transactionDetailUrl;
					$location.path( "/transactionList/" + $routeParams.application + "/" + $routeParams.readablePeriod + "/" + $routeParams.queryEndDateTime + "/" + transaction.traceId + "-" + transaction.collectorAcceptTime + "-" + transaction.elapsed, false );
					$timeout(function () {
						$scope.transactionDetailUrl = transactionDetailUrl;
					});
				}
			}
			$scope.$on('transactionTableDirective.applicationSelected', function (event, transaction) {
				changeTransactionDetail(transaction);
			});
			$scope.$on('timeSliderDirective.moreClicked', function (event) {
				$scope.sidebarLoading = true;
				$scope.$emit('timeSliderDirective.disableMore');
				$timeout(function () {
					fetchNext();
				}, 1000);
			});
			$scope.completedDetailPageLoad = function() {
				$scope.$emit( "transactionTableDirective.completedDetailPageLoad" );
			};
		}
	]);
})( jQuery );
