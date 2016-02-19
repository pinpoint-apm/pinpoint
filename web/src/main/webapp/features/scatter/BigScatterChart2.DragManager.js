(function(global, $) {
	'use strict';
	function DragManager( option, $elContainer, oCallback ) {
		this._option = option;
		this._oCallback = oCallback;
		this._$elContainer = $elContainer;
		this._initVariable();
		this._initElement( $elContainer );
		this._initEvent();
	}
	DragManager.prototype.option = function( key ) {
		return this._option[key];
	};
	DragManager.prototype._initVariable = function() {
		var oPadding = this.option( "padding" );
		var bubbleSize = this.option( "bubbleSize" );

		this._oArea = {
			"minX": this.option("minX"),
			"maxX": this.option("maxX"),
			"minY": this.option("minY"),
			"maxY": this.option("maxY"),
			"width": this.option("width"),
			"height": this.option("height"),
			"widthOfDraggable": ( this.option("width") - ( oPadding.left + oPadding.right ) ) - bubbleSize * 2,
			"heightOfDraggable": ( this.option("height") - ( oPadding.top + oPadding.bottom ) ) - bubbleSize * 2
		};
	};
	DragManager.prototype._initElement = function( $elContainer ) {
		var oPadding = this.option( "padding" );
		var lineColor = this.option( "lineColor" );
		var bubbleSize = this.option( "bubbleSize" );
		var axisLabelStyle = this.option( "axisLabelStyle" );

		this._$element = $("<div>").css({
			"top": "0px",
			"left": "0px",
			"width": this._oArea.width + "px",
			"height": this._oArea.height + "px",
			"cursor": "crosshair",
			"z-index": 500,
			"position": "absolute",
			"background-color": "rgba(0,0,0,0)" // for ie10
		}).addClass("overlay").appendTo( $elContainer );

		this._welXGuideNumber = $("<div>")
			.css({
				"top": ( this._oArea.height - oPadding.bottom + 10) + "px",
				"left": "0px",
				"color": lineColor,
				"width": "56px",
				"height": "22px",
				"border": "1px solid #ccc",
				"display": "none",
				"position": "absolute",
				"text-align": "center",
				"background": "#fff",
				"line-height": "22px",
				"margin-left": "-28px",
				"border-radius": "5px"
			})
			.css(axisLabelStyle)
			.append( $("<span></span>") )
			.append( $("<div>").css({
				"top": "-10px",
				"left": "27px",
				"height": "10px",
				"position": "absolute",
				"border-left": "1px solid red"
			}))
			.appendTo( this._$element );

		this._welYGuideNumber = $("<div>")
			.css({
				"top": "0px",
				"left": "0px",
				"color": lineColor,
				"width": (56 - 15) + "px",
				"height": "22px",
				"border": "1px solid #ccc",
				"display": "none",
				"position": "absolute",
				"margin-top": "-10px",
				"text-align": "right",
				"background": "#fff",
				"line-height": "22px",
				"padding-right": "3px",
				"border-radius": "5px",
				"vertical-align": "middle"
			})
			.css(axisLabelStyle)
			.append($("<span></span>"))
			.append( $("<div>").css({
				"top": "9px",
				"right": "-10px",
				"width": "10px",
				"position": "absolute",
				"border-top": "1px solid red"
			}))
			.appendTo( this._$element );
	};
	DragManager.prototype._initEvent = function() {
		var self = this;
		var bGuideLineStart = false;

		this._$element.dragToSelect({
			className: "jquery-drag-to-select",
			onHide: function ( $elDragArea ) {
				var oDragAreaPosition = self._adjustSelectBoxForChart( $elDragArea );
				self._oCallback.onSelect( oDragAreaPosition, self._parseCoordinatesToXY( oDragAreaPosition ) );
				(self._$elDragArea = $elDragArea).hide();
				bGuideLineStart = false;
				self._hideGuideValue();
			},
			onMove: function (e) {
				if (!self.option( "bUseMouseGuideLine" ) ) {
					return false;
				}
				if ( self._checkMouseXYInChart(e.pageX, e.pageY ) ) {
					if (!bGuideLineStart) {
						self._showGuideValue();
						bGuideLineStart = true;
					}
					self._moveGuideValue(e.pageX, e.pageY);
				} else {
					bGuideLineStart = false;
					self._hideGuideValue();
				}
			},
			onLeave: function(e) {
				bGuideLineStart = false;
				self._hideGuideValue();
			}
		});
	};
	DragManager.prototype._checkMouseXYInChart = function( x, y ) {
		var oPadding = this.option( "padding" );
		var bubbleSize = this.option( "bubbleSize" );
		var oContainerOffset = this._$elContainer.offset();
		var minX = oContainerOffset.left + oPadding.left + bubbleSize;
		var maxX = oContainerOffset.left + this._$elContainer.width() - oPadding.right - bubbleSize;
		var minY = oContainerOffset.top + oPadding.top + bubbleSize;
		var maxY = oContainerOffset.top + this._$elContainer.height() - oPadding.bottom - bubbleSize;

		if ( x >= minX && x <= maxX && y >= minY && y <= maxY ) {
			return true;
		} else {
			return false;
		}
	};
	DragManager.prototype.triggerDrag = function( welFakeSelectBox ) {
		var oDragAreaPosition = self._adjustSelectBoxForChart( welFakeSelectBox );
		this._oCallback.onSelect( oDragAreaPosition, self._parseCoordinatesToXY( oDragAreaPosition ) );
	};
	DragManager.prototype._adjustSelectBoxForChart = function(welSelectBox) {
		var oPadding = this.option( "padding" );
		var bubbleSize = this.option( "bubbleSize" );
		var nMinTop =  oPadding.top + bubbleSize;
		var nMinLeft = oPadding.left + bubbleSize;
		var nMaxRight = this._oArea.width - oPadding.right - bubbleSize ;
		var nMaxBottom = this._oArea.height - oPadding.bottom - bubbleSize;

		var nLeft = parseInt(welSelectBox.css("left"), 10);
		var nRight = nLeft + welSelectBox.width();
		var nTop = parseInt(welSelectBox.css("top"), 10);
		var nBottom = nTop + welSelectBox.height();

		nTop = Math.max( nTop, nMinTop );
		nLeft = Math.max( nLeft, nMinLeft );
		nRight = Math.min( nRight, nMaxRight );
		nBottom = Math.min( nBottom, nMaxBottom );

		var oNextInfo = {
			"top": nTop,
			"left": nLeft,
			"width": nRight - nLeft,
			"height": nBottom - nTop
		};
		welSelectBox.animate( oNextInfo, 200 );
		return oNextInfo;
	};
	DragManager.prototype._parseCoordinatesToXY = function( oPosition ) {
		var oPadding = this.option( "padding" );
		var bubbleSize = this.option( "bubbleSize" );
		return {
			"fromX": this._parseMouseXToXData( oPosition.left - oPadding.left - bubbleSize ),
			"toX": this._parseMouseXToXData( oPosition.left + oPosition.width - oPadding.left - bubbleSize ),
			"fromY": this._parseMouseYToYData( this._oArea.height - ( oPadding.bottom + bubbleSize ) - ( oPosition.top + oPosition.height ) ),
			"toY": this._parseMouseYToYData( this._oArea.height - ( oPadding.bottom + bubbleSize ) - oPosition.top )
		};
	};
	DragManager.prototype._parseXDataToXChart = function( x ) {
		return Math.round(((x - this._oArea.minX) / (this._oArea.maxX - this._oArea.minX)) * this._oArea.widthOfDraggable) + this.option("padding").left + this.option( "bubbleSize" );
	};
	DragManager.prototype._parseMouseXToXData = function( x ) {
		return Math.round((x / this._oArea.widthOfDraggable) * (this._oArea.maxX - this._oArea.minX)) + this._oArea.minX;
	};

	DragManager.prototype._parseYDataToYChart = function( y ) {
		return Math.round(this._oArea.heightOfDraggable - (((y - this._oArea.minY) / (this._oArea.maxY - this._oArea.minY)) * this._oArea.heightOfDraggable)) + this.option("padding").top + this.option( "bubbleSize" );
	};
	DragManager.prototype._parseMouseYToYData = function( y ) {
		return Math.round((y / this._oArea.heightOfDraggable) * (this._oArea.maxY - this._oArea.minY));
	};
	DragManager.prototype._showGuideValue = function() {
		this._welXGuideNumber.show();
		this._welYGuideNumber.show();
	};
	DragManager.prototype._moveGuideValue = function( x, y ) {
		var oPadding = this.option("padding");
		var bubbleSize = this.option( "bubbleSize" );
		var oContainerOffset = this._$elContainer.offset();
		var coordinateX = x - oContainerOffset.left;
		var coordinateY = y - oContainerOffset.top;

		this._welXGuideNumber.css("left", coordinateX ).find("span").text( moment( this._parseMouseXToXData( coordinateX - oPadding.left, bubbleSize ) ).format("HH:mm:ss"));
		this._welYGuideNumber.css( "top", coordinateY ).find("span").text( BigScatterChart2.Util.addComma( this._parseMouseYToYData( this._oArea.height - coordinateY - oPadding.bottom - bubbleSize )));
	};
	DragManager.prototype._hideGuideValue = function() {
		this._welXGuideNumber.hide();
		this._welYGuideNumber.hide();
	};
	DragManager.prototype.moveDragArea = function( nXGap ) {
		if ( !this._$elDragArea || this._$elDragArea.width() < 2 ) return;

		var nPositionXGap = ( nXGap / ( this._oArea.maxX - this._oArea.minX ) ) * this._oArea.widthOfDraggable;

		var dragAreaOffsetLeft = parseInt( this._$elDragArea.css( "left" ), 10 );
		var drawAreaWidth = this._$elDragArea.width();
		var bubbleSize = this.option( "bubbleSize" );
		var minX = this.option( "padding" ).left + bubbleSize;
		var newOffsetLeft = dragAreaOffsetLeft - nPositionXGap;

		if ( dragAreaOffsetLeft > minX ) {
			if ( newOffsetLeft > minX ) {
				this._$elDragArea.css( "left", newOffsetLeft );
			} else {
				this._$elDragArea.css( "left", minX );
				this._$elDragArea.width( drawAreaWidth + newOffsetLeft );
			}
		} else {
			this._$elDragArea.width( drawAreaWidth - nPositionXGap );
		}

		if ( dragAreaOffsetLeft - nPositionXGap > minX ) {
			this._$elDragArea.css( "left", dragAreaOffsetLeft - nPositionXGap );
		}
	};
	DragManager.prototype.setRangeOfY = function( min, max ) {
		this._oArea.minY = min;
		this._oArea.maxY = max;
	};
	DragManager.prototype.hide = function() {
		this._hideGuideValue();
		this._$elContainer.find( ".jquery-drag-to-select" ).hide();
	};

	global.BigScatterChart2.DragManager = DragManager;
})(window, jQuery);