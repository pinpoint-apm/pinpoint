(function() {
	'use strict';
	/**
	 * (en)transactionTableDirective 
	 * @ko transactionTableDirective
	 * @group Directive
	 * @name transactionTableDirective
	 * @class
	 */
	pinpointApp.directive('transactionTableDirective', ['$window', 'helpContentTemplate', 'helpContentService', function ($window, helpContentTemplate, helpContentService) {
	    return {
	        restrict: 'EA',
	        replace: true,
	        templateUrl: 'features/transactionTable/transactionTable.html',
	        link: function postLink(scope, element, attrs) {
	
	            // define private variables of methods
	            var clear, appendTransactionList, resetIndexToTransactionList;
	
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
	            	$at($at.CALLSTACK, $at.CLK_TRANSACTION);
	                scope.currentTransaction = transaction;
	                scope.$emit('transactionTableDirective.applicationSelected', transaction);
	            };
	
	            /**
	             * open transaction view
	             * @param transaction
	             */
	            scope.openTransactionView = function (transaction) {
	                $window.open('#/transactionView/' + transaction.agentId + '/' + transaction.traceId + '/' + transaction.collectorAcceptTime);
	            };
	
	            /**
	             * scope trace by sequence
	             * @param transaction
	             */
	            scope.traceBySequence = function (transaction) {
	                scope.currentTransaction = transaction;
	                scope.$emit('transactionTableDirective.sequenceSelected', transaction);
	            };
	
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
	                $at($at.CALLSTACK, $at.ST_ + orderKey.charAt(0).toUpperCase() + orderKey.substring(1), scope.transactionReverse ? $at.DESCENDING : $at.ASCENDING );
	                scope.transactionOrderBy = orderKey;
	            };
	
	            /**
	             * scope event on transactionTableDirective.appendTransactionList
	             */
	            scope.$on('transactionTableDirective.appendTransactionList', function (event, transactionList) {
	                appendTransactionList(transactionList);
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