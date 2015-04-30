'use strict';

pinpointApp.directive('timeline', function () {
    return {
        restrict: 'EA',
        replace: true,
        templateUrl: 'views/timeline.html',
        link: function postLink(scope, element, attrs) {

            // define private variables of methods
            var initialize;

            /**
             * initialize
             * @param transactionDetail
             */
            initialize = function (transactionDetail) {
                scope.timeline = transactionDetail;
                scope.key = transactionDetail.callStackIndex;
                scope.barRatio = 1000 / (transactionDetail.callStack[0][scope.key.end] - transactionDetail.callStack[0][scope.key.begin]);
                scope.$digest();
                $('.timeline').tooltip();
            };

            /**
             * scope event on timeline.initialize
             */
            scope.$on('timeline.initialize', function (event, transactionDetail) {
                initialize(transactionDetail);
            });
            scope.moveCallFlow = function( item ) {
            	$at($at.CALLSTACK, $at.CLK_CALL);
            	scope.$emit( "transactionDetail.selectDistributedCallFlowRow", item[6] );
            };
            scope.mouseEnterCallFlow = function( $event ) {
            	$( $event.currentTarget ).parent().css({
            		"background-color": "#F5CF05",
            		"box-shadow":"6px 6px 2px -2px rgba(0,0,0,0.75)",
            		"font-weight":"bold"
            	});
            };
            scope.mouseLeaveCallFlow = function( $event ) {
            	$( $event.currentTarget ).parent().css({
            		"background-color": "#84B464",
            		"box-shadow":"none",
            		"font-weight":"normal"
            	});
            };
        }
    };
});
