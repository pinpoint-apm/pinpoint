(function( $ ) {
	'use strict';
	/**
	 * (en)timelineDirective 
	 * @ko timelineDirective
	 * @group Directive
	 * @name timelineDirective
	 * @class
	 */
	pinpointApp.directive('timelineDirective', ['AnalyticsService', function (analyticsService) {
	    return {
	        restrict: 'EA',
	        replace: true,
	        templateUrl: 'features/timeline/timeline.html?v=' + G_BUILD_TIME,
	        link: function postLink(scope, element, attrs) {

	        	// define private variables of methods
	            var initialize, filterCallStacks, searchTime, searchStartIndex, searchSuccess;
	            var colorSet = [
	                "#66CCFF", "#FFCCFF", "#66CC00", "#FFCC33", "#669999", "#FF9999", "#6666FF", "#FF6633", "#66FFCC", "#006666",
	                "#FFFF00", "#66CCCC", "#FFCCCC", "#6699FF", "#FF99FF", "#669900", "#FF9933", "#66FFFF", "#996600", "#66FF00" 
	            ], colorSetIndex = [];
	            
	            var icscroller = new InfiniteCircularScroll({
	            	scroller: $(element),
	            	wrapper: $(element).find("div"),
	            	elementHeight: 21,
	            	template: [ 
		       			'<div class="timeline-bar">',
			                '<div class="clickable-bar">',
			                	'<div class="timeline-bar-frame">',
			                		'<span>',
			                			'<span class="before" style="position:absolute;text-align:right;white-space:nowrap;left:-70px;display:none;"><span class="glyphicon glyphicon-resize-horizontal" aria-hidden="true"></span> <span class="startTime"></span>ms</span>',
			         					'<span class="nameType"></span>', 
			         					'<span class="after" style="white-space:nowrap;display:none;">( <span class="glyphicon glyphicon-resize-horizontal" aria-hidden="true"></span> <span class="startTime"></span>ms )</span>',
			                		'</span>',
			                	'</div>',
			                '</div>',
			            '</div>'
				    ].join("")
	            });
	            icscroller.renderFunc(function( $element, index, elementData ) {
                	var marginLeft = getMarginLeft(elementData);
                	$element[ index == this._selectedRow ? "addClass" : "removeClass" ]("timeline-bar-selected")
                	.find("div.clickable-bar").css({
	            		width			: getWidth(elementData) + "px",
	            		backgroundColor	: getColorByString(elementData[scope.key.applicationName]),
	            		marginLeft		: marginLeft + "px"
	            	}).find("span.nameType").html( elementData[scope.key.applicationName] + "/" + elementData[scope.key.apiType] + "(" + (elementData[scope.key.end] - elementData[scope.key.begin]) + "ms)" );
	            	if ( marginLeft >= 68 ) {
	            		$element.find("span.before").show().end().find("span.after").hide().end().find("span.before .startTime").html( getStartTime(elementData) );
	            	} else {
	            		$element.find("span.before").hide().end().find("span.after").show().end().find("span.after .startTime").html( getStartTime(elementData) );
	            	}
	            });
	            
	            filterCallStacks = function() {
	            	var newCallStacks = [];
	    	        angular.forEach(scope.timeline.callStack, function (val) {
	    	            if (val[scope.key.isMethod] && !val[scope.key.excludeFromTimeline] && val[scope.key.service] !== '') {
	    	                newCallStacks.push(val);
	    	            }
	    	        });
	    	        return newCallStacks;
	            };
	            /**
	             * initialize
	             * @param transactionDetail
	             */
	            initialize = function (transactionDetail) {
	                scope.timeline = transactionDetail;
	                scope.key = transactionDetail.callStackIndex;
	                scope.barRatio = 1000 / (transactionDetail.callStack[0][scope.key.end] - transactionDetail.callStack[0][scope.key.begin]);
	                scope.newCallStacks = filterCallStacks();
	                icscroller.source( scope.newCallStacks )
	                	.viewAreaHeight( $(element).parentsUntil("div.wrapper").height() - 70 ) // 70 is header area height
	                	.selectedRow(-1, angular.noop)
	                	.reset(); 
	                scope.maxHeight = icscroller.contentsAreaHeight();
	                searchStartIndex = 0;
	                
	                scope.$digest();
	            };

	            var getColorByString = function(str) {
	            	var index = colorSetIndex.indexOf( str );
	            	if ( index == -1 ) {
	            		colorSetIndex.push( str );
	            		index = colorSetIndex.length - 1;
	            	}
	            	return colorSet[ index >= colorSet.length ? 0 : index ];
	            };
	            var getMarginLeft = function( stack ) {
	            	return ((stack[scope.key.begin] - scope.timeline.callStackStart) * scope.barRatio) + 0.9;
	            };
	            var getWidth = function( stack ) {
	            	  return ((stack[scope.key.end] - stack[scope.key.begin]) * scope.barRatio) + 0.9;
	            };
	            var getStartTime = function( stack ) {
	            	return (stack[scope.key.begin] - scope.timeline.callStackStart);
	            };
	            
	            $(element).on("click", ".clickable-bar", function() {
	            	analyticsService.send(analyticsService.CONST.CALLSTACK, analyticsService.CONST.CLK_CALL);
	            	scope.$emit( "transactionDetail.selectDistributedCallFlowRow", scope.newCallStacks[parseInt($(this).parent().attr("data-index"))][6] );
                });
	            $(element).on("mouseenter", ".timeline-bar-frame", function(event) {
	            	$(this).parent().css({
	            		//"background-color": "#F5CF05",
	            		"box-shadow":"6px 6px 2px -2px rgba(0,0,0,0.75)",
	            		"font-weight":"bold"
	            	});
	            });
	            $(element).on("mouseleave", ".timeline-bar-frame", function(event) {
	            	$(this).parent().css({
	            		//"background-color": "#84B464",
	            		"box-shadow":"none",
	            		"font-weight":"normal"
	            	});
	            });

	            /**
	             * scope event on timelineDirective.initialize
	             */
	            scope.$on('timelineDirective.initialize', function (event, transactionDetail) {
	                initialize(transactionDetail);
	            });
	            scope.$on('timelineDirective.resize', function (event) {
	                icscroller.resize($(element).parentsUntil("div.wrapper").height() - 70);
	            });
	            scope.$on("timelineDirective.searchCall", function( event, time, index ) {
	            	var resultIndex = searchTime( searchStartIndex, -1, time );
	            	if ( resultIndex == -1 ) {
	            		if ( searchStartIndex === 0 ) {
	            			scope.$emit("transactionDetail.searchActionResult", "No call took longer than " + time + "ms." );
	            		} else {
		            		resultIndex = searchTime( 0, searchStartIndex, time );
	                		if ( resultIndex == -1 ) {
	                			scope.$emit("transactionDetail.searchActionResult", "No call took longer than " + time + "ms." );
	                		} else {
	                			searchSuccess(resultIndex, "Loop");
	                		}
	            		}
                	} else {
                		searchSuccess(resultIndex, "");
                	}
	            });
	            searchTime = function( from, to, time ) {
	            	return icscroller.searchRow(from, to, function( elementData ) {
	            		if ( elementData[scope.key.end] - elementData[scope.key.begin] >= time ) {
	            			return true;
	            		}
	            		return false;
	            	});
	            };
	            searchSuccess = function(resultIndex, message) {
	            	icscroller.selectedRow( resultIndex, function(newIndex) {
            			this.$wrapper.find("div[data-index=" + this._selectedRow + "]").removeClass("timeline-bar-selected");
            			this.$wrapper.find("div[data-index=" + newIndex + "]").addClass("timeline-bar-selected");
            		}).moveByRow( resultIndex );
            		searchStartIndex = resultIndex + 1;
            		scope.$emit("transactionDetail.searchActionResult", message );
	            };
	        }
	    };
	}]);
})(jQuery);