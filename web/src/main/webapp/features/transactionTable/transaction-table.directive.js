(function() {
	'use strict';
	/**
	 * (en)transactionTableDirective 
	 * @ko transactionTableDirective
	 * @group Directive
	 * @name transactionTableDirective
	 * @class
	 */
	pinpointApp.directive( "transactionTableDirective", [ "$window", "helpContentTemplate", "helpContentService", "AnalyticsService", "CommonUtilService", function ($window, helpContentTemplate, helpContentService, AnalyticsService, CommonUtilService ) {
	    return {
	        restrict: 'EA',
	        replace: true,
	        templateUrl: 'features/transactionTable/transactionTable.html?v=' + G_BUILD_TIME,
	        link: function postLink(scope, element, attrs) {
	
	            // define private variables of methods
	            var clear, appendTransactionList, resetIndexToTransactionList;
				var bIsDetailPageLoading = false;
	
	            // initialize scope variables
	            scope.transactionList = [];
	            scope.currentTransaction = null;
	            scope.transactionReverse = false;
	
	            /**
	             * clear
	             */
	            clear = function () {
	                scope.transactionList = [];
	            };
	
	            /**
	             * append transaction list
	             * @param transactionList
	             */
	            appendTransactionList = function (transactionList) {
	                if (scope.transactionList.length > 0) {
	                    scope.transactionOrderBy = '';
	                    scope.transactionReverse = false;
	                    element.find('table tbody tr:last-child')[0].scrollIntoView(true);
	//                    $(".transaction-table_wrapper").animate({ scrollTop: element.find('table tbody tr:last-child').offset().top }, 500);
	                } else {
		                scope.transactionOrderBy = 'elapsed';
		                scope.transactionReverse = true;
	                }
	                scope.transactionList = scope.transactionList.concat(transactionList);
	                resetIndexToTransactionList();
	                element.find('[data-toggle="tooltip"]').tooltip('destroy').tooltip();
	            };
	
	            /**
	             * reset index to transaction list
	             */
	            resetIndexToTransactionList = function () {
	                var index = 1;
	                angular.forEach(scope.transactionList, function (val, key) {
	                    val['index'] = index++;
	                });
	            };
	
	            /**
	             * scope trace by application
	             * @param transaction
	             */
	            scope.traceByApplication = function (transaction) {
					if ( bIsDetailPageLoading === true ) {
						return;
					}
					AnalyticsService.send(AnalyticsService.CONST.CALLSTACK, AnalyticsService.CONST.CLK_TRANSACTION);
	                scope.currentTransaction = transaction;
	                scope.$emit('transactionTableDirective.applicationSelected', transaction);
	            };
	
	            /**
	             * open transaction view
	             * @param transaction
	             */
	            scope.openTransactionView = function (transaction) {
	                $window.open('#/transactionView/' + transaction.agentId + '/' + transaction.traceId + '/' + transaction.collectorAcceptTime + '/' + transaction.spanId);
	            };
	
	            /**
	             * scope trace by sequence
	             * @param transaction
				 * not used
	             */
	            //scope.traceBySequence = function (transaction) {
	            //    scope.currentTransaction = transaction;
	            //    scope.$emit('transactionTableDirective.sequenceSelected', transaction);
	            //};
	
	            /**
	             * scope transaction order
	             * @param orderKey
	             */
	            scope.transactionOrder = function (orderKey) {
	                if (scope.transactionOrderBy === orderKey) {
	                    scope.transactionReverse = !scope.transactionReverse;
	                } else {
	                    scope.transactionReverse = false;
	                }
	                AnalyticsService.send(AnalyticsService.CONST.CALLSTACK, AnalyticsService.CONST.ST_ + orderKey.charAt(0).toUpperCase() + orderKey.substring(1), scope.transactionReverse ? AnalyticsService.CONST.DESCENDING : AnalyticsService.CONST.ASCENDING );
	                scope.transactionOrderBy = orderKey;
	            };
	            scope.formatDate = function( time ) {
					return CommonUtilService.formatDate( time, "MM/DD HH:mm:ss SSS");
				};

	            /**
	             * scope event on transactionTableDirective.appendTransactionList
	             */
	            scope.$on('transactionTableDirective.appendTransactionList', function (event, transactionList) {
	                appendTransactionList(transactionList);
	            });
				scope.$on('transactionTableDirective.completedDetailPageLoad', function () {
					bIsDetailPageLoading = false;
				});
	
	            /**
	             * scope event on transactionTableDirective.clear
	             */
	            scope.$on('transactionTableDirective.clear', function (event) {
	                clear();
	            });

	            scope.initTooltipster = function() {
		            jQuery('.neloTooltip').tooltipster({
	                	content: function() {
	                		return helpContentTemplate(helpContentService.transactionTable.log);
	                	},
	                	position: "bottom",
	                	trigger: "click",
	                	interactive: true
	                });	
	            };
	        }
	    };
	}]);
})();