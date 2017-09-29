(function() {
	'use strict';
	/**
	 * (en)TransactionDetailCtrl 
	 * @ko TransactionDetailCtrl
	 * @group Controller
	 * @name TransactionDetailCtrl
	 * @class
	 */
	pinpointApp.constant('TransactionDetailConfig', {
	    applicationUrl: 'transactionInfo.pinpoint',
		CALL_STACK: "#CallStacks",
		SERVER_MAP: "#ServerMap",
		TIMELINE: "#Timeline"
	});
	
	pinpointApp.controller('TransactionDetailCtrl', ['TransactionDetailConfig', '$scope', '$rootScope', '$routeParams', '$timeout', '$rootElement', 'AlertsService', 'ProgressBarService', 'TransactionDaoService', '$window', '$location', 'AnalyticsService', 'TooltipService',
	    function (cfg, $scope, $rootScope, $routeParams, $timeout, $rootElement, AlertsService, ProgressBarService, TransactionDaoService, $window, $location, analyticsService, tooltipService) {
			analyticsService.send(analyticsService.CONST.TRANSACTION_DETAIL_PAGE);

			$rootScope.wrapperClass = 'no-navbar';
			$rootScope.wrapperStyle = {
				'padding-top': '70px'
			};

			var searchIndex = 0;
			var bChangedColumn = false;
	        var bIsFirstTimelineView = true;
	        var bShowCallStacksOnce = false;
	        var oAlertService = new AlertsService($rootElement);
	        var oProgressBarService = new ProgressBarService($rootElement);

	        var currentTab = cfg.CALL_STACK;
	        var $elSearchForm = $("#traceTabs ._searchForm");
			var $elSearchInput = $elSearchForm.find("input");
			$("#customLogPopup").modal("hide");
	
	        $timeout(function () {
	            if ($routeParams.traceId && $routeParams.focusTimestamp) {
	                oProgressBarService.startLoading();
	                oProgressBarService.setLoading(30);
	                TransactionDaoService.getTransactionDetail($routeParams.agentId, $routeParams.spanId, $routeParams.traceId, $routeParams.focusTimestamp, function (err, result) {
	                    if (err || result.exception ) {
                            oProgressBarService.stopLoading();
                            if ( err ) {
                            	oAlertService.showError('There is some error while downloading the data.');
                            } else {
                            	oAlertService.showError(result.exception);
                            }
                        } else {
    	                    oProgressBarService.setLoading(70);
    	                    parseTransactionDetail(result);
    	                    showCallStacks();
    	                    $timeout(function () {
    	                        oProgressBarService.setLoading(100);
    	                        oProgressBarService.stopLoading();
    	                    }, 100);
                        }
	                });
	            }
	        });
	
	        function parseTransactionDetail(result) {
	            $scope.transactionDetail = result;
	            $scope.logLinkEnable = result.logLinkEnable || false;
	            $scope.loggingTransactionInfo = result.loggingTransactionInfo || false;
	            $scope.logButtonName = result.logButtonName || "";
	            $scope.logPageUrl = result.logPageUrl || "";
	            $scope.logDisableMessage = result.disableButtonMessage || "";
	            $scope.completeStateClass = parseCompleteStateToClass(result.completeState);
	            $scope.$digest();
	            $rootElement.find('[data-toggle="tooltip"]').tooltip('destroy').tooltip();
	        }
	
	        function parseCompleteStateToClass(completeState) {
	            var completeStateClass = 'label-important';
	            if (completeState === 'Complete') {
	                completeStateClass = 'label-success';
	            } else if (completeState === 'Progress') {
	                completeStateClass = 'label-warning';
	            }
	            return completeStateClass;
	        }

	        function showCallStacks() {
	            if (bShowCallStacksOnce === false) {
	                bShowCallStacksOnce = true;
	                $scope.$broadcast('distributedCallFlowDirective.initialize.forTransactionDetail', $scope.transactionDetail);
	            }
	        }
	        function isCallStacks() {
				return currentTab === cfg.CALL_STACK;
			}
			function isServerMap() {
				return currentTab === cfg.SERVER_MAP;
			}
	        function initSearchVar() {
	        	bChangedColumn = true;
				searchIndex = 0;
				$elSearchInput.val("");
				$scope.searchColumn = "self";
				$scope.searchPlaceholder = "1000(ms)";
				$elSearchForm.find("input").val("").attr("placeholder", "1000(ms)");
				if ( isServerMap() ) {
					$elSearchForm.hide();
				} else {
					$elSearchForm.show();
					$("#traceTabs option:first").attr("selected", "selected");
					if ( isCallStacks() ) {
						$("#traceTabs option:last").removeAttr("disabled");
					} else {
						$("#traceTabs option:last").attr("disabled", "disabled");
					}
					$("#traceTabs option:first").removeAttr("selected");
				}
	        }
			$scope.viewLog = function( url ) {
	        	if ( $scope.loggingTransactionInfo === false ) {
	        		$("#customLogPopup").find("h4").html("Notice").end().find("div.modal-body").html( $scope.logDisableMessage ).end().modal("show");
	        		return false;
	        	} else {
	        		window.open(url);
	        	}
	        };
	
	        $scope.openInNewWindow = function () {
	            $window.open($location.absUrl());
	        };
	
	        window.onresize = function (e) {
	            $scope.$broadcast('distributedCallFlowDirective.resize.forTransactionDetail');
	            $scope.$broadcast('timelineDirective.resize');
	        };
	
	        /**
	         * open transaction view
	         * @param transaction
	         */
	        $scope.openTransactionView = function ($event) {
	        	$event.preventDefault();
	        	console.log('#/transactionView/' + $scope.transactionDetail.agentId + '/' + $scope.transactionDetail.transactionId + '/' + $scope.transactionDetail.callStackStart + '/' + $routeParams.spanId);
	            $window.open('#/transactionView/' + $scope.transactionDetail.agentId + '/' + $scope.transactionDetail.transactionId + '/' + $scope.transactionDetail.callStackStart + '/' + $routeParams.spanId);
	        };
	        $scope.$on("transactionDetail.selectDistributedCallFlowRow", function( event, rowId ) {
				analyticsService.send(analyticsService.CONST.CALLSTACK, analyticsService.CONST.CLK_DISTRIBUTED_CALL_FLOW);
				$("#traceTabs li:nth-child(1) a").trigger("click");
				currentTab = cfg.CALL_STACK;
				initSearchVar();
	        	$scope.$broadcast('distributedCallFlowDirective.selectRow.forTransactionDetail', rowId);
	        });
			$scope.$on("transactionDetail.searchActionResult", function(event, message) {
				if ( message === "Loop" ) {
					searchIndex = 1;
				} else {
					$scope.searchMessage = message;
					if ( message === "" ) {
						searchIndex++;
					}
				}
			});

			$elSearchInput.val("");
	        $scope.searchColumn = "self";
	        $scope.searchPlaceholder = "1000(ms)";
	        $scope.selectSearchColumn = function() {
				bChangedColumn = true;
	        	if ( $scope.searchColumn === "self" ) {
					$scope.searchPlaceholder = "1000(ms)";
				} else {
					$scope.searchPlaceholder = "input your text";
				}
			};
	        $scope.searchByClick = function() {
	        	var trimVal = $.trim( $elSearchInput.val() );
	        	if ( trimVal === "" ) return;

	        	if ( bChangedColumn ) {
	        		searchIndex = 0;
	        		bChangedColumn = false;
				}
				if ( $scope.searchColumn === "self" ) {
					var num = parseInt( trimVal );
					if ( isCallStacks() ) {
						$scope.$broadcast('distributedCallFlowDirective.searchCall.forTransactionDetail', num, searchIndex );
					} else {
						$scope.$broadcast('timelineDirective.searchCall', num, searchIndex );
					}
 				} else {
					$scope.$broadcast('distributedCallFlowDirective.searchArgument.forTransactionDetail', trimVal, searchIndex  );
				}
			};
	        $scope.searchByKeydown = function( $event ) {
				if ( $event.keyCode === 13 ) {
					$scope.searchByClick();
				} else {
					searchIndex = 0;
				}
			};

	        
	        // $('#traceTabs li a[data-toggle="tab"]').on('shown.bs.tab', function(e) {
	        // 	if ( e.target.href.indexOf( "#CallStacks") != -1 ) {
	        // 		analyticsService.send(analyticsService.CONST.CALLSTACK, analyticsService.CONST.CLK_DISTRIBUTED_CALL_FLOW);
	        // 	}
	        // });
	        // events binding
	        $("#traceTabs li:nth-child(1) a").bind("click", function (e) {
				analyticsService.send(analyticsService.CONST.CALLSTACK, analyticsService.CONST.CLK_DISTRIBUTED_CALL_FLOW);
	        	currentTab = cfg.CALL_STACK;
	        	initSearchVar();
	            e.preventDefault();
	        });
	        $("#traceTabs li:nth-child(2) a").bind("click", function (e) {
				analyticsService.send(analyticsService.CONST.CALLSTACK, analyticsService.CONST.CLK_SERVER_MAP);
				currentTab = cfg.SERVER_MAP;
	        	initSearchVar();
	            $scope.$broadcast('serverMapDirective.initializeWithMapData', false, $scope.transactionDetail);
				e.preventDefault();
			});
	        $("#traceTabs li:nth-child(3) a").bind("click", function (e) {
				analyticsService.send(analyticsService.CONST.CALLSTACK, analyticsService.CONST.CLK_RPC_TIMELINE);
				currentTab = cfg.TIMELINE;
	        	initSearchVar();
	        	if (bIsFirstTimelineView){
	            	$scope.$broadcast('timelineDirective.initialize', $scope.transactionDetail);
	            	bIsFirstTimelineView = false;
	        	}
				e.preventDefault();
			});
			tooltipService.init( "callTree" );

			// invoke top frame angular method
			try {
				top.angular.element( top.document.getElementById("main-container") ).scope().completedDetailPageLoad();
			} catch(e) {}
	    }
	]);
})();