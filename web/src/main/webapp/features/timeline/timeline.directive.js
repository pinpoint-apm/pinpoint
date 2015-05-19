(function() {
	'use strict';
	
	pinpointApp.directive('timelineDirective', function () {
	    return {
	        restrict: 'EA',
	        replace: true,
	        templateUrl: 'features/timeline/timeline.html',
	        link: function postLink(scope, element, attrs) {
	
	            // define private variables of methods
	            var colorSet = [
	                "#66CCFF", "#FFCCFF", "#66CC00", "#FFCC33", "#669999", "#FF9999", "#6666FF", "#FF6633", "#66FFCC", "#006666",
	                "#FFFF00", "#66CCCC", "#FFCCCC", "#6699FF", "#FF99FF", "#669900", "#FF9933", "#66FFFF", "#996600", "#66FF00" 
	            ], colorSetIndex = [];
	            
	
	        	// define private variables of methods
	            var initialize, getColorByString;
	
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
	
	            scope.getColorByString = function(str) {
	            	var index = colorSetIndex.indexOf( str );
	            	if ( index == -1 ) {
	            		colorSetIndex.push( str );
	            		index = colorSetIndex.length - 1;
	            	}
	            	return colorSet[ index >= colorSet.length ? 0 : index ];
	            };
	            /**
	             * scope event on timelineDirective.initialize
	             */
	            scope.$on('timelineDirective.initialize', function (event, transactionDetail) {
	                initialize(transactionDetail);
	            });
	            scope.moveCallFlow = function( item ) {
	            	$at($at.CALLSTACK, $at.CLK_CALL);
	            	scope.$emit( "transactionDetail.selectDistributedCallFlowRow", item[6] );
	            };
	            scope.mouseEnterCallFlow = function( $event ) {
	            	$( $event.currentTarget ).parent().css({
	            		//"background-color": "#F5CF05",
	            		"box-shadow":"6px 6px 2px -2px rgba(0,0,0,0.75)",
	            		"font-weight":"bold"
	            	});
	            };
	            scope.mouseLeaveCallFlow = function( $event ) {
	            	$( $event.currentTarget ).parent().css({
	            		//"background-color": "#84B464",
	            		"box-shadow":"none",
	            		"font-weight":"normal"
	            	});
	            };
	        }
	    };
	});
})();