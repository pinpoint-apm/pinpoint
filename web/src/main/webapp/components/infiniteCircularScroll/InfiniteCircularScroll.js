(function(window, $) {
	'use strict';
	var ROW_SPARE_COUNT = 8;
	
	var ICS = function( options ){
		this.options = options;
		this._init();
		this._initOption();
		this._initVar();
		this._initEvent();			
		this._renderScrollArea();
	};
	ICS.prototype._init = function() {
		this.$wrapper = this.options.wrapper;
		this._threshold = this.options.elementHeight * (ROW_SPARE_COUNT / 4); // 2 * elementHeight		
	};
	ICS.prototype._initOption = function() {
		this._viewAreaHeight = this.options.viewAreaHeight;
		this._renderFunc = this.options.renderFunc || function() {};
		this._source = this.options.source;
		this._previousTop = 0;			
		this._previousTime = -1;
		this._selectedRow = -1;
	}
	ICS.prototype._initVar = function() {
		this._elementArray = [];
		this._elementArrayStartIndex = 0;
		this._elementArrayEndIndex = 0;
		this._elementArraySize = 0;
		this._previousTop = 0;			
	};
	ICS.prototype._initEvent = function() {
		var self = this;
		this.options.scroller.on("scroll", function(event) {
	    	self._scrollEventHandler(event, $(this));
	    });
	},
	ICS.prototype._isDefined = function( v ) {
		return typeof v != "undefined";
	};
	ICS.prototype._renderScrollArea = function() {
		if ( this._isDefined( this._source ) && this._isDefined( this.options.viewAreaHeight ) ) {
			this.reset();
		}
	},
	ICS.prototype._calculateElementStartCount = function() {
		var temp = parseInt( this._viewAreaHeight / this.options.elementHeight ) + ROW_SPARE_COUNT;
	    this._elementArraySize = temp > this._source.length ? this._source.length : temp;
	    this._elementArrayEndIndex = this._elementArraySize - 1; 
		this.$wrapper.height( this._source.length * this.options.elementHeight );
	};
	ICS.prototype._initElementArray = function() {
		this.$wrapper.empty();
		for( var i = 0 ; i < this._elementArraySize ; i++ ) {
	    	var $element = $(this.options.template);
	    	this._renderElement($element, i);
	    	this._elementArray.push($element);
	    	this.$wrapper.append($element);
	    }
	};
	ICS.prototype._renderElement = function($element, index)  {
		this._renderFunc.call( this, $element, index, this._source[index] );
		$element.attr("data-index", index).css("top", (index * this.options.elementHeight) + "px" );
	};
	ICS.prototype._getDataIndex = function(index) {
		return parseInt(this._elementArray[index].attr("data-index"));
	};
	ICS.prototype._getTopByIndex = function(index) {
		return parseInt(this._elementArray[index].css("top"));
	};
	ICS.prototype._getNextArrayIndex = function(index) {
		return index + 1 >= this._elementArraySize ? 0 : index + 1;
	};
	ICS.prototype._getPreviousArrayIndex = function(index) {
		return ( index - 1 < 0 ? this._elementArraySize : index ) - 1;
	};
	ICS.prototype._scrollEventHandler = function(event, $targetElement) {
		var top = $targetElement.scrollTop();
		var mTime = new Date().valueOf();
		if ( top == this._previousTop ) return;
		
		var bIsDown = top - this._previousTop > 0;
		if ( bIsDown ) { 	// scroll down 
			this._scrollDown(top, this._getTopByIndex(this._elementArrayStartIndex));
		} else {			// scroll up
			this._scrollUp(top, this._getTopByIndex(this._elementArrayEndIndex));
		}		
		this._previousTop = top;
		this._previousTime = mTime;
	};
	ICS.prototype._scrollDown = function( top, firstElementTop ) {
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
				if ( nextIndexStep - i <= this._elementArraySize * 2 ) {
					this._renderElement(this._elementArray[nextIndex], sourceIndex);						
				}
				nextIndex = this._getNextArrayIndex(nextIndex);
			}
			this._elementArrayStartIndex = nextIndex;
			this._elementArrayEndIndex = this._getPreviousArrayIndex(nextIndex);
		}
	},
	ICS.prototype._scrollUp = function( top, lastElementTop ) {
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
				if ( previousIndexStep - i <= this._elementArraySize * 2 ) {	
					this._renderElement(this._elementArray[previousIndex], sourceIndex);
				}
				previousIndex = this._getPreviousArrayIndex(previousIndex);
			}
			this._elementArrayEndIndex = previousIndex;
			this._elementArrayStartIndex = this._getNextArrayIndex(previousIndex);
		}
	};
	ICS.prototype.source = function() {
		if ( arguments.length == 1 ) {
			this.options.source = arguments[0];
			return this;
		} else {
			return this._source;
		}
	};
	ICS.prototype.viewAreaHeight = function() {
		if ( arguments.length == 1 ) {
			this.options.viewAreaHeight = arguments[0];
			return this;			
		} else {
			return this._viewAreaHeight;
		}
	};
	ICS.prototype.renderFunc = function() {
		if ( arguments.length == 1 ) {
			this.options.renderFunc = arguments[0];
			return this;
		} else {
			return this._renderFunc;
		}
	};
	ICS.prototype.reset = function() {
		this._initOption();
		this._initVar();
		this._calculateElementStartCount();
		this._initElementArray();
		return this;
	};
	ICS.prototype.contentsAreaHeight = function() {
		return this.options.elementHeight * this._source.length;
	};
	ICS.prototype.destroy = function() {
		this.options.scroller.off("scroll");
	};
	ICS.prototype.resize = function( resizedViewAreaHeight ) {
		if ( this._isDefined( this._source ) === false ) return;
		var currentRow = parseInt(this.options.scroller.scrollTop() / this.options.elementHeight);
//		this.setViewAreaHeight(resizedViewAreaHeight);
		this.reset();
		this.moveByRow( currentRow );
	};
	ICS.prototype.moveByRow = function( row ) { // from 0
		var self = this;
		setTimeout(function() {
			self.options.scroller.scrollTop(row * self.options.elementHeight);
		},0);
	};
	ICS.prototype.searchRow = function( from, to, func) {
		var resultRow = -1;
		to = (to == -1 ? this._source.length : to);
		for( var i = from ; i < to ; i++ ) {
			if ( func(this._source[i]) ) {
				resultRow = i;
				break;
			}
		}
		return resultRow;
	};
	ICS.prototype.selectedRow = function(row, func) {
		if ( arguments.length == 0 ) {
			return this._selectedRow;
		} else {
			func.call(this, row);
			this._selectedRow = row;
			return this;
		}
	};
	window.InfiniteCircularScroll = ICS;
})(window, jQuery);