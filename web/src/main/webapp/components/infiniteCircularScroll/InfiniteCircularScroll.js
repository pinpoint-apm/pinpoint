(function(window, $) {
	'use strict';
	var ROW_SPARE_COUNT = 8;
	window.InfiniteCircularScroll = $.Class({
		$init: function(options) {
			this.option(options);
			this._init();
			this._initVar();
			this._initEvent();			
		},
		_init: function() {
			this.$wrapper = this.option("wrapper");
			this._threshold = this.option("elementHeight") * (ROW_SPARE_COUNT / 4); // 2 * elementHeight
			this._previousTop = 0;			
			this._previousTime = -1;
			this._selectedRow = -1;
		},
		_initVar: function() {
			this._elementArray = [];
			this._elementArrayStartIndex = 0;
			this._elementArrayEndIndex = 0;
			this._elementArraySize = 0;
			this._previousTop = 0;			
		},
		_initEvent: function() {
			var self = this;
			this.option("scroller").on("scroll", function(event) {
		    	self._scrollEventHandler(event, $(this));
		    });
		},
		_calculateElementStartCount: function() {
			var temp = parseInt( this._viewAreaHeight / this.option("elementHeight") ) + ROW_SPARE_COUNT;
		    this._elementArraySize = temp > this._source.length ? this._source.length : temp;
		    this._elementArrayEndIndex = this._elementArraySize - 1; 
		},
		_initElementArray: function() {
			this.$wrapper.empty();
			for( var i = 0 ; i < this._elementArraySize ; i++ ) {
		    	var $element = $(this.option("template"));
		    	this._renderElement($element, i);
		    	this._elementArray.push($element);
		    	this.$wrapper.append($element);
		    }
		},
		_renderElement: function($element, index)  {
			this._renderFunc.call( this, $element, index, this._source[index] );
			$element.attr("data-index", index).css("top", (index * this.option("elementHeight")) + "px" );
		},
		_getDataIndex: function(index) {
			return parseInt(this._elementArray[index].attr("data-index"));
		},
		_getTopByIndex: function(index) {
			return parseInt(this._elementArray[index].css("top"));
		},
		_getNextArrayIndex: function(index) {
			return index + 1 >= this._elementArraySize ? 0 : index + 1;
		},
		_getPreviousArrayIndex: function(index) {
			return ( index - 1 < 0 ? this._elementArraySize : index ) - 1;
		},
		_scrollEventHandler: function(event, $targetElement) {
			var top = $targetElement.scrollTop();
			var mTime = new Date().valueOf();
			if ( top == this._previousTop ) return;
//			if ( this._previousTime != -1 && ( mTime - this._previousTime ) < 16 ) return;
			
			var bIsDown = top - this._previousTop > 0;
			if ( bIsDown ) { 	// 아래로 스크롤 - top 값은 커진다. 
				this._scrollDown(top, this._getTopByIndex(this._elementArrayStartIndex));
			} else {			// 위로 스크롤 - top 값은 작아진다.
				this._scrollUp(top, this._getTopByIndex(this._elementArrayEndIndex));
			}		
			this._previousTop = top;
			this._previousTime = mTime;
		},
		_scrollDown: function( top, firstElementTop ) {
			var exceededDistance = top - firstElementTop - this._threshold;
			if ( exceededDistance >= 0 ) {
				var sourceLastIndex = this._getDataIndex(this._elementArrayEndIndex);
				var nextIndex = this._elementArrayStartIndex;
				var nextIndexStep = parseInt(exceededDistance / this.option("elementHeight"));

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
		},
		_scrollUp: function( top, lastElementTop ) {
			var exceededDistance = lastElementTop + this.option("elementHeight") - top - this._viewAreaHeight - this._threshold;
			if ( exceededDistance >= 0 ) {
				var sourceFirstIndex = this._getDataIndex(this._elementArrayStartIndex);
				var previousIndex = this._elementArrayEndIndex;
				var previousIndexStep = parseInt(exceededDistance / this.option("elementHeight"));
				
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
		},
		setSource: function( source ) {
			this._source= source;
			return this;
		},
		setViewAreaHeight: function( viewAreaHeight ) {
			this._viewAreaHeight = viewAreaHeight;
			return this;
		},
		setRenderFunc: function( fnRender ) {
			this._renderFunc = fnRender;
			return this;
		},
		reset: function() {
			this._initVar();
			this._calculateElementStartCount();
			this._initElementArray();
			return this;
		},
		getContentsAreaHeight: function() {
			return this.option("elementHeight") * this._source.length;
		},
		destroy: function() {
			this.$wrapper.parent().off("scroll");
		},
		resize: function( resizedViewAreaHeight ) {
			var currentRow = parseInt(this.option("scroller").scrollTop() / this.option("elementHeight"));
			this.setViewAreaHeight(resizedViewAreaHeight);
			this.reset();
			this.moveByRow( currentRow );
		},
		moveByRow: function( row ) { // from 0
			var self = this;
			setTimeout(function() {
				self.option("scroller").scrollTop(row * self.option("elementHeight"));
			},0);
		},
		searchRow: function( from, to, func) {
			var resultRow = -1;
			to = to == -1 ? this._source.length : to;
			for( var i = from ; i < to ; i++ ) {
				if ( func(this._source[i]) ) {
					resultRow = i;
					break;
				}
			}
			return resultRow;
		},
		setSelectedRow: function(row, func) {
			func.call(this, row);
			this._selectedRow = row;
			return this;
		}
	});
})(window, jQuery);
