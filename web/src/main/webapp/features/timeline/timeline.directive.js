(function( $ ) {
	'use strict';
	
	function InfiniteCircularScroller(options) {
		this.options = options || {};
		this._init();
		this._initVar();
		this._initEvent();
	};
	InfiniteCircularScroller.prototype._init = function() {
		this.options.initPlusCount = 8;
		this.$wrapper = this.options.wrapper;
    	this._threshold = this.options.elementHeight * (this.options.initPlusCount / 4);
    	this._previousTop = 0;
	};
	InfiniteCircularScroller.prototype._initVar = function() {
		this._elementArray = [];
    	this._elementArrayStartIndex = 0;
    	this._elementArrayEndIndex = 0;
    	this._elementArraySize = 0;
    	this._previousTop = 0;
	};
	InfiniteCircularScroller.prototype._initEvent = function() {
		var self = this;
		this.options.scroller.on("scroll", function(event) {
        	self._scrollEventHandler(event, $(this));
        });
	};
	InfiniteCircularScroller.prototype._calculateElementStartCount = function() {
		var temp = parseInt( this._viewAreaHeight / this.options.elementHeight ) + this.options.initPlusCount;
        this._elementArraySize = temp > this._source.length ? this._source.length : temp;
        this._elementArrayEndIndex = this._elementArraySize - 1; 
	};
	InfiniteCircularScroller.prototype._initElementArray = function()  {
		this.$wrapper.empty();
		for( var i = 0 ; i < this._elementArraySize ; i++ ) {
        	var $element = $(this.options.template);
        	this._renderElement($element, i);
        	this._elementArray.push($element);
        	this.$wrapper.append($element);
        }
	};
	InfiniteCircularScroller.prototype._renderElement = function($element, index)  {
		this._renderFunc.call( this, $element, index, this._source[index] );
    	$element.attr("data-index", index).css("top", (index * this.options.elementHeight) + "px" );
	};
	InfiniteCircularScroller.prototype._getDataIndex = function(index) {
		return parseInt(this._elementArray[index].attr("data-index"));
	};
	InfiniteCircularScroller.prototype._getTopByIndex = function(index) {
		return parseInt(this._elementArray[index].css("top"));
	};
	InfiniteCircularScroller.prototype._getNextArrayIndex = function(index) {
		return index + 1 >= this._elementArraySize ? 0 : index + 1;
	};
	InfiniteCircularScroller.prototype._getPreviousArrayIndex = function(index) {
		return ( index - 1 < 0 ? this._elementArraySize : index ) - 1;
	};
	InfiniteCircularScroller.prototype._scrollEventHandler = function(event, $targetElement) {
		var top = $targetElement.scrollTop();
		if ( top == this._previousTop ) return;
		if ( top < this._threshold ) return;
		
		//if ( top - this._previousTop > this._viewAreaHeight ) 스크롤 이벤트 입력값이 화면 보다 크게 오는 경우
		var bIsDown = top - this._previousTop > 0;
		if ( bIsDown ) { 	// 아래로 스크롤 - top 값은 커진다. 
			this._scrollDown(top, this._getTopByIndex(this._elementArrayStartIndex));
		} else {			// 위로 스크롤 - top 값은 작아진다.
			this._scrollUp(top, this._getTopByIndex(this._elementArrayEndIndex));
		}		
		this._previousTop = top;
	};
	InfiniteCircularScroller.prototype._scrollDown = function( top, firstElementTop ) {
		var exceededDistance = top - firstElementTop - this._threshold;
		if ( exceededDistance >= 0 ) {
			var sourceLastIndex = this._getDataIndex(this._elementArrayEndIndex);
			var nextIndex = this._elementArrayStartIndex;
			var nextIndexStep = parseInt(exceededDistance / this.options.elementHeight);
			
			for( var i = 0 ; i < nextIndexStep ; i++ ) {
				var sourceIndex = sourceLastIndex + i + 1;
				if ( sourceIndex >= this._source.length ) {
					break;
				}
				this._renderElement(this._elementArray[nextIndex], sourceIndex);
				nextIndex = this._getNextArrayIndex(nextIndex);
			}
			this._elementArrayStartIndex = nextIndex;
			this._elementArrayEndIndex = this._getPreviousArrayIndex(nextIndex);
		}	
	};
	InfiniteCircularScroller.prototype._scrollUp = function( top, lastElementTop ) {
		var exceededDistance = lastElementTop + this.options.elementHeight - top - this._viewAreaHeight - this._threshold; 
		if ( exceededDistance >= 0 ) {
			var sourceFirstIndex = this._getDataIndex(this._elementArrayStartIndex);
			var previousIndex = this._elementArrayEndIndex;
			var previousIndexStep = parseInt(exceededDistance / this.options.elementHeight);
			
			for( var i = 0 ; i < previousIndexStep ; i++ ) {
				var sourceIndex = sourceFirstIndex - (i + 1);
				if ( sourceIndex < 0 ) {
					break;
				}
				this._renderElement(this._elementArray[previousIndex], sourceIndex);
				previousIndex = this._getPreviousArrayIndex(previousIndex);
			}
			this._elementArrayEndIndex = previousIndex;
			this._elementArrayStartIndex = this._getNextArrayIndex(previousIndex);
		}	
	};
	InfiniteCircularScroller.prototype.setSource = function( source ) {
		this._source= source;
		return this;
	};
	InfiniteCircularScroller.prototype.setViewAreaHeight = function( viewAreaHeight ) {
		this._viewAreaHeight = viewAreaHeight;
		return this;
	};
	InfiniteCircularScroller.prototype.setRenderFunc = function( fnRender ) {
		this._renderFunc = fnRender;
		return this;
	};
	InfiniteCircularScroller.prototype.reset = function() {
		this._initVar();
		this._calculateElementStartCount();
		this._initElementArray();
		return this;
	};
	InfiniteCircularScroller.prototype.getContentsAreaHeight = function() {
		return this.options.elementHeight * this._source.length;
	};
	InfiniteCircularScroller.prototype.destroy = function() {
		this.$wrapper.parent().off("scroll");
	};
	InfiniteCircularScroller.prototype.resize = function( resizedViewAreaHeight ) {
		var currentRow = parseInt(this.options.scroller.scrollTop() / this.options.elementHeight);
		this.setViewAreaHeight(resizedViewAreaHeight);
		this.reset();
		this.moveByRow( currentRow );
	};
	InfiniteCircularScroller.prototype.moveByRow = function( row ) {
		var self = this;
		setTimeout(function() {
			self.options.scroller.scrollTop(0);
		},0);
	};
	InfiniteCircularScroller.prototype.moveByValue = function(from, func) {
		for( var i = from ; i < this._source.length ; i++ ) {
			if ( func(this._source[i]) ) {
				this.moveByRow(i);
				break;
			}
		}
	};
	/*
	 * icscroller.moveByValue( 0, function( elementData ) {
	 * 	return (elementData[scope.key.end] - elementData[scope.key.begin]) >= selfExecutionTime;
	 * });
	 */
	
	pinpointApp.directive('timelineDirective', function () {
	    return {
	        restrict: 'EA',
	        replace: true,
	        templateUrl: 'features/timeline/timeline.html',
	        link: function postLink(scope, element, attrs) {

	        	// define private variables of methods
	            var initialize, getColorByString, filterCallStacks, viewAreaHeight, initBarCount, renderCallStack;
	            var colorSet = [
	                "#66CCFF", "#FFCCFF", "#66CC00", "#FFCC33", "#669999", "#FF9999", "#6666FF", "#FF6633", "#66FFCC", "#006666",
	                "#FFFF00", "#66CCCC", "#FFCCCC", "#6699FF", "#FF99FF", "#669900", "#FF9933", "#66FFFF", "#996600", "#66FF00" 
	            ], colorSetIndex = [];
	            
	            var icscroller = new InfiniteCircularScroller({
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
	            icscroller.setRenderFunc(function( $element, index, elementData ) {
                	var marginLeft = getMarginLeft(elementData);
                	$element.find("div.clickable-bar").css({
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
	                icscroller.setSource( scope.newCallStacks )
	                	.setViewAreaHeight( $(element).parentsUntil("div.wrapper").height() - 70 ) // 70 is header area height
	                	.reset(); 
	                scope.maxHeight = icscroller.getContentsAreaHeight();
	                
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
	            	$at($at.CALLSTACK, $at.CLK_CALL);
	            	scope.$emit( "transactionDetail.selectDistributedCallFlowRow", scope.newCallStacks[parseInt($(this).attr("data-index"))][6] );
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
	        }
	    };
	});
})(jQuery);