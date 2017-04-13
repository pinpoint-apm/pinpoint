(function() {
	'use strict';
	/**
	 * (en)TransactionViewCtrl 
	 * @ko TransactionViewCtrl
	 * @group Controller
	 * @name TransactionViewCtrl
	 * @class
	 */
	pinpointApp.constant( "TransactionViewConfig", {
	    applicationUrl: "transactionInfo.pinpoint"
	});
	
	pinpointApp.controller( "TransactionViewCtrl", [ "TransactionViewConfig", "$scope", "$rootScope", "$rootElement", "CommonUtilService", "AlertsService", "ProgressBarService", "$timeout", "$routeParams", "NavbarVoService", "TransactionDaoService", "AgentDaoService", "AnalyticsService", "PreferenceService",
	    function( cfg, $scope, $rootScope, $rootElement, CommonUtilService, AlertsService, ProgressBarService, $timeout, $routeParams, NavbarVoService, TransactionDaoService, AgentDaoService, analyticsService, preferenceService ) {
			analyticsService.send(analyticsService.CONST.TRANSACTION_VIEW_PAGE);
	        // define private variables
	        var oAlertService, oProgressBarService;
	
	        // define private variables of methods
	        var parseTransactionDetail, parseCompleteStateToClass, showCallStacks, showServerMap, showHeapChart,
	            showChartCursorAt;
	
	        // bootstrap
	        $rootScope.wrapperClass = 'no-navbar';
	        $rootScope.wrapperStyle = {
	            'padding-top': '30px'
	        };
	        oAlertService = new AlertsService($rootElement);
	        oProgressBarService = new ProgressBarService($rootElement);
	        /**
	         * initialize
	         */
	        $timeout(function () {
	            if ($routeParams.agentId && $routeParams.traceId && $routeParams.focusTimestamp) {
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
                        }
	                    oProgressBarService.setLoading(70);
	                    parseTransactionDetail(result);
	                    showCallStacks();
	                    showServerMap();
	                    $timeout(function () {
	                        oProgressBarService.setLoading(100);
	                        oProgressBarService.stopLoading();
	
	                        $("#main-container").layout({
	                            north__minSize: 50,
	                            north__size: 210,
	//                north__spacing_closed: 20,
	//                north__togglerLength_closed: 100,
	//                north__togglerAlign_closed: "top",
	                            onload_end: function () {
	                                $scope.$broadcast('distributedCallFlowDirective.resize.forTransactionView');
	                            },
	                            onresize_end: function (edge) {
	                                if (edge === 'center') {
	                                    $scope.$broadcast('distributedCallFlowDirective.resize.forTransactionView');
	                                    $scope.$broadcast('agentChartGroupDirective.resize.forTransactionView');
	                                    $scope.$broadcast('serverMapDirective.zoomToFit');
	                                }
	                            },
	                            center__maskContents: true // IMPORTANT - enable iframe masking
	                        });
	                    }, 100);
	                });
	                showHeapChart($routeParams.agentId, $routeParams.focusTimestamp);
	            }
	        }, 500);
	
	        /**
	         * parse transaction detail
	         * @param result
	         */
	        parseTransactionDetail = function (result) {
	            $scope.transactionDetail = result;
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
	//            $scope.$broadcast('callStacks.initialize.forTransactionView', $scope.transactionDetail);
	            $scope.$broadcast('distributedCallFlowDirective.initialize.forTransactionView', $scope.transactionDetail);
	        };
	
	        /**
	         * show server map
	         */
	        showServerMap = function () {
				var oNavbarVoService = new NavbarVoService();
				oNavbarVoService.setReadablePeriod(preferenceService.getPeriodTime()[0]);
				oNavbarVoService.setQueryEndDateTime( CommonUtilService.formatDate( $routeParams.focusTimestamp ) );
	            $scope.$broadcast('serverMapDirective.initializeWithMapData', true, $scope.transactionDetail, oNavbarVoService);
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
	                sampleRate: AgentDaoService.getSampleRate(20)
	            };
	            $scope.$broadcast('agentChartGroupDirective.initialize.forTransactionView', query);
	        };
	
	        /**
	         * show chart cursor at
	         * @param category
	         */
	        showChartCursorAt = function (category) {
	            $scope.$broadcast('agentChartGroupDirective.showCursorAt.forTransactionView', category);
	        };
	
	
	        /**
	         * scope event on distributedCallFlowDirective.rowSelected.forTransactionView
	         */
	        $scope.$on('distributedCallFlowDirective.rowSelected.forTransactionView', function (e, item) {
	            var category;
	            if (item.execTime) {
	                var coeff = 1000 * 5;   // round to nearest multiple of 5 seconds
	                var execTime = new Date(item.execTime);
	                category = moment(Math.floor(execTime.getTime() / coeff) * coeff).format('YYYY-MM-DD HH:mm:ss');
	            } else {
	                category = false;
	            }
	            showChartCursorAt(category);
	
	        });
	
	    }
	]);
})();