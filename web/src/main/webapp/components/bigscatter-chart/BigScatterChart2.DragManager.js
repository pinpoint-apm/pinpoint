(function(global, $) {
	'use strict';
	var DRAG_TO_SELECT_COMPONENT_CLASS = "jquery-drag-to-select";
	function DragManager( option, oSizeCoordinateManager, $elContainer, oCallback ) {
		this._option = option;
		this._oCallback = oCallback;
		this._oSCManager = oSizeCoordinateManager;
		this._$elContainer = $elContainer;

		this._initVariable();
		this._initElement( $elContainer );
		this._initEvent();
	}
	DragManager.prototype.option = function( key ) {
		return this._option[key];
	};
	DragManager.prototype._initVariable = function() {
	};
	DragManager.prototype._initElement = function( $elContainer ) {
		var oPadding = this._oSCManager.getPadding();
		var lineColor = this.option( "lineColor" );
		var axisLabelStyle = this.option( "axisLabelStyle" );

		this._$element = $("<div>").css({
			"top": "0px",
			"left": "0px",
			"width": this._oSCManager.getWidth() + "px",
			"height": this._oSCManager.getHeight() + "px",
			"cursor": "crosshair",
			"z-index": 500,
			"position": "absolute",
			"background-color": "rgba(0,0,0,0)" // for ie10
		}).addClass("overlay").appendTo( $elContainer );

		this._welXGuideNumber = $("<div>")
			.css({
				"top": ( this._oSCManager.getHeight() - oPadding.bottom + 10) + "px",
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
			className: DRAG_TO_SELECT_COMPONENT_CLASS,
			onHide: function ( $elDragArea ) {
				var oDragAreaPosition = self._adjustSelectBoxForChart( $elDragArea );
				self._oCallback.onSelect( oDragAreaPosition, self._parseCoordinatesToXY( oDragAreaPosition ) );
				self._$elDragArea = $elDragArea;
				self._$elDragArea.hide();
				self._hideGuideValue();
				bGuideLineStart = false;
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
		var oPadding = this._oSCManager.getPadding();
		var bubbleSize = this._oSCManager.getBubbleSize();
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
		var oDragAreaPosition = this._adjustSelectBoxForChart( welFakeSelectBox );
		this._oCallback.onSelect( oDragAreaPosition, this._parseCoordinatesToXY( oDragAreaPosition ) );
	};
	DragManager.prototype._adjustSelectBoxForChart = function(welSelectBox) {
		var oPadding = this._oSCManager.getPadding();
		var bubbleSize = this._oSCManager.getBubbleSize();
		var nMinTop =  oPadding.top + bubbleSize;
		var nMinLeft = oPadding.left + bubbleSize;
		var nMaxRight = this._oSCManager.getWidth() - oPadding.right - bubbleSize ;
		var nMaxBottom = this._oSCManager.getHeight() - oPadding.bottom - bubbleSize;

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
		var oPadding = this._oSCManager.getPadding();
		var bubbleSize = this._oSCManager.getBubbleSize();
		return {
			"fromX": this._oSCManager.parseMouseXToXData( oPosition.left - oPadding.left - bubbleSize ),
			"toX": this._oSCManager.parseMouseXToXData( oPosition.left + oPosition.width - oPadding.left - bubbleSize ),
			"fromY": this._oSCManager.parseMouseYToYData( this._oSCManager.getHeight() - ( oPadding.bottom + bubbleSize ) - ( oPosition.top + oPosition.height ) ),
			"toY": this._oSCManager.parseMouseYToYData( this._oSCManager.getHeight() - ( oPadding.bottom + bubbleSize ) - oPosition.top )
		};
	};
	DragManager.prototype._showGuideValue = function() {
		this._welXGuideNumber.show();
		this._welYGuideNumber.show();
	};
	DragManager.prototype._moveGuideValue = function( x, y ) {
		var oPadding = this._oSCManager.getPadding();
		var bubbleSize = this._oSCManager.getBubbleSize();
		var oContainerOffset = this._$elContainer.offset();
		var coordinateX = x - oContainerOffset.left;
		var coordinateY = y - oContainerOffset.top;

		this._welXGuideNumber.css("left", coordinateX ).find("span").text( moment( this._oSCManager.parseMouseXToXData( coordinateX - oPadding.left, bubbleSize ) ).format("HH:mm:ss"));
		this._welYGuideNumber.css( "top", coordinateY ).find("span").text( BigScatterChart2.Util.addComma( this._oSCManager.parseMouseYToYData( this._oSCManager.getHeight() - coordinateY - oPadding.bottom - bubbleSize )));
	};
	DragManager.prototype._hideGuideValue = function() {
		this._welXGuideNumber.hide();
		this._welYGuideNumber.hide();
	};
	//DragManager.prototype.moveDragArea = function( nXGap ) {
	//	if ( !this._$elDragArea || this._$elDragArea.width() < 2 ) return;
	//
	//	var nPositionXGap = ( nXGap / ( this._oSCManager.getGapX() ) ) * this._oSCManager.getWidthOfChartSpace();
	//
	//	var dragAreaOffsetLeft = parseInt( this._$elDragArea.css( "left" ), 10 );
	//	var drawAreaWidth = this._$elDragArea.width();
	//	var bubbleSize = this._oSCManager.getBubbleSize();
	//	var minX = this._oSCManager.getPadding().left + bubbleSize;
	//	var newOffsetLeft = dragAreaOffsetLeft - nPositionXGap;
	//
	//	if ( dragAreaOffsetLeft > minX ) {
	//		if ( newOffsetLeft > minX ) {
	//			this._$elDragArea.css( "left", newOffsetLeft );
	//		} else {
	//			this._$elDragArea.css( "left", minX );
	//			this._$elDragArea.width( drawAreaWidth + newOffsetLeft );
	//		}
	//	} else {
	//		this._$elDragArea.width( drawAreaWidth - nPositionXGap );
	//	}
	//
	//	if ( dragAreaOffsetLeft - nPositionXGap > minX ) {
	//		this._$elDragArea.css( "left", dragAreaOffsetLeft - nPositionXGap );
	//	}
	//};
	DragManager.prototype.hide = function() {
		this._hideGuideValue();
		this._$elContainer.find( "." + DRAG_TO_SELECT_COMPONENT_CLASS ).hide();
	};

	global.BigScatterChart2.DragManager = DragManager;
})(window, jQuery);