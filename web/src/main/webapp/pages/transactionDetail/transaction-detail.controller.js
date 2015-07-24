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
	    applicationUrl: '/transactionInfo.pinpoint'
	});
	
	pinpointApp.controller('TransactionDetailCtrl', ['TransactionDetailConfig', '$scope', '$rootScope', '$routeParams', '$timeout', '$rootElement', 'AlertsService', 'ProgressBarService', 'TransactionDaoService', '$window', '$location', 'helpContentTemplate', 'helpContentService',
	    function (cfg, $scope, $rootScope, $routeParams, $timeout, $rootElement, AlertsService, ProgressBarService, TransactionDaoService, $window, $location, helpContentTemplate, helpContentService) {
			$at($at.TRANSACTION_DETAIL_PAGE);
	        // define private variables
	        var oAlertService, oProgressBarService, bShowCallStacksOnce, bIsFirstTimelineView = true;
	
	        // define private variables of methods
	        var parseTransactionDetail, showCallStacks, parseCompleteStateToClass, initSearchVar;
	
	        // initialize
	        bShowCallStacksOnce = false;
	        $rootScope.wrapperClass = 'no-navbar';
	        $rootScope.wrapperStyle = {
	            'padding-top': '70px'
	        };
	        oAlertService = new AlertsService($rootElement);
	        oProgressBarService = new ProgressBarService($rootElement);
	        $("#customLogPopup").modal("hide");
	
	        /**
	         * initialize
	         */
	        $timeout(function () {
	            if ($routeParams.traceId && $routeParams.focusTimestamp) {
	                oProgressBarService.startLoading();
	                oProgressBarService.setLoading(30);
	                TransactionDaoService.getTransactionDetail($routeParams.traceId, $routeParams.focusTimestamp, function (err, result) {
	                    if (err || result.exception ) {
                            oProgressBarService.stopLoading();
                            if ( err ) {
                            	oAlertService.showError('There is some error while downloading the data.');
                            } else {
                            	oAlertService.showError(result.exception);
                            }
                        }
	                    oProgressBarService.setLoading(70);
	                    parseTransactionDetail(result);
	                    showCallStacks();
	                    $timeout(function () {
	                        oProgressBarService.setLoading(100);
	                        oProgressBarService.stopLoading();
	                    }, 100);
	                });
	            }
	        });
	
	        /**
	         * parse transaction detail
	         * @param result
	         */
	        parseTransactionDetail = function (result) {
	            $scope.transactionDetail = result;
	            $scope.logLinkEnable = result.logLinkEnable || false;
	            $scope.loggingTransactionInfo = result.loggingTransactionInfo || false;
	            $scope.logButtonName = result.logButtonName || "";
	            $scope.logPageUrl = result.logPageUrl || "";
	            $scope.logDisableMessage = result.disableButtonMessage || "";
	            $scope.completeStateClass = parseCompleteStateToClass(result.completeState);
	            $scope.$digest();
	            $rootElement.find('[data-toggle="tooltip"]').tooltip('destroy').tooltip();
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
	            if (bShowCallStacksOnce === false) {
	                bShowCallStacksOnce = true;
	                //$scope.$broadcast('callStacks.initialize.forTransactionDetail', $scope.transactionDetail);
	                $scope.$broadcast('distributedCallFlowDirective.initialize.forTransactionDetail', $scope.transactionDetail);
	            }
	        };
	        initSearchVar = function() {
	        	$scope.searchMinTime = 1000;
	        	$scope.timelineSearchIndex = 0;
	        	$scope.calltreeSearchIndex = 0;
	        	$scope.searchMessage = "";
	        };
	        $scope.calltreeSearchIndex = 0;
	        $scope.timelineSearchIndex = 0;
	        $scope.searchMinTime = 1000; // ms
	        $scope.searchMessage = "";
	        $scope.searchCall = function() {
	        	if ( $("#CallStacks").is(":visible") ) {
	        		$scope.$broadcast('distributedCallFlowDirective.searchCall.forTransactionDetail', parseInt($scope.searchMinTime), parseInt($scope.calltreeSearchIndex) );
	        	} else {
	        		$scope.$broadcast('timelineDirective.searchCall', parseInt($scope.searchMinTime), parseInt($scope.timelineSearchIndex) );
	        	}
	        };
	        $scope.viewLog = function( url ) {
	        	if ( $scope.loggingTransactionInfo == false ) {
	        		$("#customLogPopup").find("div.modal-body").html( $scope.logDisableMessage ).end().modal("show");
	        		return false;
	        	} else {
	        		window.open(url);
	        	}
	        };
	        $scope.$watch( "searchMinTime", function( newVal ) {
	        	$scope.calltreeSearchIndex = 0;
	        	$scope.timelineSearchIndex = 0;
	        });
	
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
	        $scope.openTransactionView = function () {
	            $window.open('/#/transactionView/' + $scope.transactionDetail.agentId + '/' + $scope.transactionDetail.transactionId + '/' + $scope.transactionDetail.callStackStart);
	        };
	        $scope.$on("transactionDetail.selectDistributedCallFlowRow", function( event, rowId ) {
	        	$at($at.CALLSTACK, $at.CLK_DISTRIBUTED_CALL_FLOW);
	        	$("#traceTabs li:nth-child(1) a").trigger("click");
	        	$scope.$broadcast('distributedCallFlowDirective.selectRow.forTransactionDetail', rowId);
	        });
	        $scope.$on("transactionDetail.calltreeSearchCallResult", function(event, message) {
	        	if ( message == "Loop" ) {
	        		$scope.calltreeSearchIndex = 1;
	        	} else {
	        		$scope.searchMessage = message.replace("{time}", $scope.searchMinTime);
	        		if ( message == "" ) {
	            		$scope.calltreeSearchIndex++;
	        		}
	        	}
	        });
	        $scope.$on("transactionDetail.timelineSearchCallResult", function(event, message) {
	        	if ( message == "Loop" ) {
	        		$scope.timelineSearchIndex = 1;
	        	} else {
	        		$scope.searchMessage = message.replace("{time}", $scope.searchMinTime);
	        		if ( message == "" ) {
	            		$scope.timelineSearchIndex++;
	        		}
	        	}
	        });
	        
	        
	        $('#traceTabs li a[data-toggle="tab"]').on('shown.bs.tab', function(e) {
	        	if ( e.target.href.indexOf( "#CallStacks") != -1 ) {
	        		$at($at.CALLSTACK, $at.CLK_DISTRIBUTED_CALL_FLOW);
//	        		$("#traceTabs li:nth-child(5)").show();
	        	}
	        });
	        // events binding
	        $("#traceTabs li a").bind("click", function (e) {
	            e.preventDefault();
	        });
	        $("#traceTabs li:nth-child(2) a").bind("click", function (e) {
	        	$at($at.CALLSTACK, $at.CLK_SERVER_MAP);
	        	initSearchVar();
	            $scope.$broadcast('serverMapDirective.initializeWithMapData', $scope.transactionDetail);
	        });
	        var testCount = 0;
	        $("#traceTabs li:nth-child(3) a").bind("click", function (e) {
	        	$at($at.CALLSTACK, $at.CLK_RPC_TIMELINE);
	        	initSearchVar();
	        	if (bIsFirstTimelineView){
	            	$scope.$broadcast('timelineDirective.initialize', $scope.transactionDetail);
	            	bIsFirstTimelineView = false;
	        	}
	        });
	        
            jQuery('.callTreeTooltip').tooltipster({
            	content: function() {
            		return helpContentTemplate(helpContentService.callTree.column);
            	},
            	position: "bottom",
            	trigger: "click"
            });	
	    }
	]);
})();