(function(global, $) {
	'use strict';
	function CanvasManager( option, aAgentList, $elContainer ) {
		this._option = option;
		this._aAgentList = aAgentList;
		this._$elWrapper = $elContainer;
		this._initVariable();
		this._makeGridCanvas();
		this._makeAxisCanvas();
		this._drawGridLine();
		this._drawAxisLine();
		this._drawAxisValue();
		this.updateXYAxis();
		this._makeDataCanvas();
	}
	CanvasManager.prototype.option = function( v ) {
		return this._option[v];
	};
	CanvasManager.prototype._initVariable = function() {
		this._oElCanvas = {};
		this._oCtx = {};

		var oPadding = this.option( "padding" );
		var bubbleSize = this.option( "bubbleSize" );
		this._ticksOfX = this.option("ticksOfX") - 1;
		this._ticksOfY = this.option("ticksOfY") - 1;

		this._widthForDraw = ( this.option( "width" ) - ( oPadding.left + oPadding.right ) ) - bubbleSize * 2;
		this._heightForDraw = ( this.option( "height" ) - ( oPadding.top + oPadding.bottom ) ) - bubbleSize * 2;

		this._maxX = this.option("maxX");
		this._minX = this.option("minX");
		this._maxY = this.option("maxY");
		this._minY = this.option("minY");
		this._maxZ = this.option("maxZ");
		this._minZ = this.option("minZ");

		this._aElAxisX = [];
		this._aElAxisY = [];

		var oCheckboxImageData = this.option("checkBoxImage");
		this._oCheckedBoxImage = new Image();
		this._oCheckedBoxImage.src = oCheckboxImageData.checked;
		this._oUncheckedBoxImage = new Image();
		this._oUncheckedBoxImage.src = oCheckboxImageData.unchecked;
	};
	CanvasManager.prototype._makeGridCanvas = function() {
		this._$elGuideCanvas = $("<canvas>").attr({
			"width": this.option( "width" ),
			"height": this.option( "height" )
		}).css({
			"top": 0,
			"z-index": 0,
			"position": "absolute"
		}).append( this._getNotSupportMarkup()).appendTo( this._$elWrapper );

		this._oCtxGrid = this._$elGuideCanvas.get(0).getContext("2d");
	};
	CanvasManager.prototype._makeAxisCanvas = function() {
		this._$elAxisCanvas = $("<canvas>").attr({
			"width": this.option("width"),
			"height": this.option("height")
		}).css({
			"top": 0,
			"z-index": 10,
			"position": "absolute"
		}).append( this._getNotSupportMarkup() ).appendTo( this._$elWrapper);

		this._oCtxAxis = this._$elAxisCanvas.get(0).getContext("2d");
	};
	CanvasManager.prototype._getNotSupportMarkup = function() {
		return $("<div>Your browser does not support the canvas element, get a better one!</div>").css({
			"color": "#fff",
			"width": this.option( "width" ),
			"height": this.option( "height" ),
			"text-align": "center",
			"background-color": "#8b2e19"
		});
	};
	CanvasManager.prototype._drawGridLine = function() {

		var width = this.option("width");
		var height = this.option("height");
		var oPadding = this.option("padding");
		var bubbleSize = this.option( "bubbleSize" );
		var tickX = this._widthForDraw / this._ticksOfX;
		var tickY = this._heightForDraw / this._ticksOfY;
		var i = 0, mov = 0;

		this._setStyle( this._oCtxGrid, this.option("gridAxisStyle") );

		for (i = 0; i <= this._ticksOfX; i++) {
			mov = oPadding.left + bubbleSize + tickX * i;
			this._oCtxGrid.beginPath();
			this._moveTo(this._oCtxGrid, mov, oPadding.top);
			this._lineTo(this._oCtxGrid, mov, height - oPadding.bottom);
			this._oCtxGrid.stroke();
		}
		for (i = 0; i <= this._ticksOfY; i++) {
			mov = height - (oPadding.bottom + bubbleSize + tickY * i);
			this._oCtxGrid.beginPath();
			this._moveTo(this._oCtxGrid, oPadding.left, mov);
			this._lineTo(this._oCtxGrid, width - oPadding.right, mov);
			this._oCtxGrid.stroke();
		}
	};

	CanvasManager.prototype._drawAxisLine = function() {
		var width = this.option( "width" );
		var height = this.option( "height" );
		var oPadding = this.option( "padding" );
		var lineColor = this.option( "lineColor" );
		var bubbleSize = this.option( "bubbleSize" );
		var gridAxisStyle = this.option( "gridAxisStyle" );
		var tickX = this._widthForDraw / this._ticksOfX;
		var tickY = this._heightForDraw / this._ticksOfY;
		var i = 0, mov = 0;

		this._oCtxAxis.lineWidth = gridAxisStyle.lineWidth;
		this._oCtxAxis.globalAlpha = 1;
		this._oCtxAxis.lineCap = "round";
		this._oCtxAxis.strokeStyle = lineColor;

		this._oCtxAxis.beginPath();
		this._moveTo(this._oCtxAxis, oPadding.left, oPadding.top);
		this._lineTo(this._oCtxAxis, oPadding.left, height - oPadding.bottom);
		this._lineTo(this._oCtxAxis, width - oPadding.right, height - oPadding.bottom);
		this._oCtxAxis.stroke();

		for ( i = 0 ; i <= this._ticksOfX ; i++ ) {
			mov = oPadding.left + bubbleSize + tickX * i;
			this._oCtxAxis.beginPath();
			this._moveTo(this._oCtxAxis, mov, height - oPadding.bottom);
			this._lineTo(this._oCtxAxis, mov, height - oPadding.bottom + 10);
			this._oCtxAxis.stroke();
		}

		for ( i = 0 ; i <= this._ticksOfY ; i++ ) {
			mov = height - (oPadding.bottom + bubbleSize + tickY * i);
			this._oCtxAxis.beginPath();
			this._moveTo(this._oCtxAxis, oPadding.left, mov);
			this._lineTo(this._oCtxAxis, oPadding.left - 10, mov);
			this._oCtxAxis.stroke();
		}
	};
	CanvasManager.prototype._setStyle = function( ctx, oStyle ) {
		$.each( oStyle, function( key, value ) {
			if ( key === "lineDash" ) {
				ctx.setLineDash( value );
			} else {
				ctx[key] = value;
			}
		});
	};
	CanvasManager.prototype._moveTo = function( ctx, x, y ) {
		if (x  % 1 === 0) {
			x += 0.5;
		}
		if (y  % 1 === 0) {
			y += 0.5;
		}
		ctx.moveTo(x, y);
	};
	CanvasManager.prototype._lineTo = function( ctx, x, y ) {
		if (x  % 1 === 0) {
			x += 0.5;
		}
		if (y  % 1 === 0) {
			y += 0.5;
		}
		ctx.lineTo(x, y);
	};
	CanvasManager.prototype._drawAxisValue = function() {
		var tickX = this._widthForDraw / this._ticksOfX;
		var tickY = this._heightForDraw / this._ticksOfY;
		var width = this.option( "width" );
		var height = this.option( "height" );
		var sPrefix = this.option( "sPrefix" );
		var oPadding = this.option( "padding" );
		var lineColor = this.option( "lineColor" );
		var bubbleSize = this.option( "bubbleSize" );
		var axisLabelStyle = this.option( "axisLabelStyle" );
		var i = 0;

		this._welCoordinate = $("<div>").css({
			"top": 0,
			"width": width,
			"height": height,
			"cursor": "crosshair",
			"z-index": 10,
			"position": "absolute",
			"background-color": "rgba(0,0,0,0)" // for ie10
		}).addClass("coordinate").appendTo( this._$elWrapper );

		for ( i = 0 ; i <= this._ticksOfX ; i++ ) {
			this._aElAxisX.push(
				$("<div>").css({
					"top": (height - oPadding.bottom + 10) + "px",
					"left": oPadding.left - (tickX / 2) + i * tickX + "px",
					"color": lineColor,
					"width": tickX + "px",
					"position": "absolute",
					"text-align": "center"
				}).css( axisLabelStyle ).text(" ")
			);
		}
		// y axis
		for ( i = 0; i <= this._ticksOfY; i++ ) {
			this._aElAxisY.push(
				$("<div>").css({
					"top": ( bubbleSize + (i * tickY) + oPadding.top - 10) + "px",
					"left": "0px",
					"width": (oPadding.left - 15) + "px",
					"color": lineColor,
					"position": "absolute",
					"text-align": "right",
					"vertical-align": "middle"
				}).css( axisLabelStyle ).text(" ")
			);
		}
		this._welCoordinate.append( this._aElAxisX ).append( this._aElAxisY );

		// labelX
		var labelX = this.option( "labelX" );
		if ( BigScatterChart2.Util.isString( labelX ) && labelX.length > 0 ) {
			$("<div>")
			.text(labelX )
			.css( axisLabelStyle )
			.css({
				"top": ( this._oArea.height - oPadding.bottom + 10) + "px",
				"right": 0,
				"color": lineColor,
				"position": "absolute",
				"text-align": "center"
			}).appendTo( this._welCoordinate );
		}

		// labeY
		var labelY = this.option( "labelY" );
		if ( BigScatterChart2.Util.isString( labelY ) && labelY.length > 0 ) {
			$("<div>")
			.text( labelY )
			.css( axisLabelStyle )
			.css({
				"top": ( bubbleSize + oPadding.top + 10) + "px",
				"left": "0px",
				"color": lineColor,
				"width": ( oPadding.left - 15) + "px",
				"position": "absolute",
				"text-align": "right",
				"vertical-align": "middle"
			}).appendTo( this._welCoordinate );
		}
	};
	CanvasManager.prototype._makeDataCanvas = function() {
		var self = this;
		var width = this.option("width");
		var height = this.option("height");
		var sPrefix = this.option("sPrefix");
		var zIndex = 20;
		var aBubbleTypeInfo = this.option("typeInfo");
		// make canvas : (agent count * type count)
		$.each(this._aAgentList, function( index, agentName ) {
			$.each( aBubbleTypeInfo, function ( index, aValue ) {
				var key = BigScatterChart2.Util.makeKey( agentName, sPrefix, aValue[0] );
				self._oElCanvas[key] = $("<canvas>").attr({
					"width": width,
					"height": height,
					"data-key": key
				}).css({
					"top": 0,
					"z-index": zIndex++,
					"position": "absolute"
				}).appendTo(self._$elWrapper);

				self._oCtx[key] = self._oElCanvas[key].get(0).getContext("2d");
			});
		});
	};
	CanvasManager.prototype.getChartAsImage = function( type, oElType ) {
		return this._mergeAllDisplay( oElType ).get(0).toDataURL("image/" + type.toLowerCase() );
	};
	CanvasManager.prototype.moveChartLeftwardly = function( x, y, width, height ) {
		var self = this;
		var sPrefix = this.option("sPrefix");
		var bubbleSize = this.option( "bubbleSize" );
		var paddingLeft = this.option("padding").left;
		var aBubbleTypeInfo = this.option("typeInfo");

		$.each(this._aAgentList, function( index, agentName ) {
			$.each( aBubbleTypeInfo, function ( innerIndex, aValue ) {
				var key = BigScatterChart2.Util.makeKey( agentName, sPrefix, aValue[0] );
				self._oCtx[key].putImageData( self._oCtx[key].getImageData(x, y, width, height), paddingLeft + bubbleSize, 0 );
			});
		});
	};
	CanvasManager.prototype.updateXYAxis = function( minX, maxX, minY, maxY ) {
		var self = this;
		if ($.isNumeric(minX)) {
			this._minX = this.option("minX", minX);
		}
		if ($.isNumeric(maxX)) {
			this._maxX = this.option("minX", maxX);
		}
		if ($.isNumeric(minY)) {
			this._minY = this.option("minY", minY);
		}
		if ($.isNumeric(minY)) {
			this._maxY = this.option("maxY", maxY);
		}

		console.log( "updateXYAxis", minX, maxX, minY, maxY );
		var fnXAxisFormat = this.option("fXAxisFormat");
		var tickX = (this._maxX - this._minX) / this._ticksOfX;
		$.each( this._aElAxisX, function ( index, $el ) {
			if ( $.isFunction( fnXAxisFormat ) ) {
				$el.html( fnXAxisFormat.call( self, tickX, index ) );
			} else {
				$el.html( ( tickX * index + self._minX ).round() );
			}
		});

		var fnYAxisFormat = this.option("fYAxisFormat");
		var tickY = (this._maxY - this._minY) / this._ticksOfY;
		$.each( this._aElAxisY, function ( index, $el ) {
			if ( $.isFunction( fnYAxisFormat ) ) {
				$el.html( fnYAxisFormat.call( self, tickY, index ) );
			} else {
				$el.html( BigScatterChart2.Util.addComma( ( self._maxY + self._minY ) - ( ( tickY * index ) + self._minY ) ) );
			}
		});
	};
	CanvasManager.prototype.clear = function() {
		var	width = this.option("width");
		var	height = this.option("height");

		$.each( this._oCtx, function( key, ctx ) {
			ctx.clearRect( 0, 0, width, height );
		});
	};
	CanvasManager.prototype.selectType = function( agentName, type ) {
		$.each( this._oElCanvas, function( key, $elCanvas ) {
			if ( BigScatterChart2.Util.endsWith( key, type ) ) {
				$elCanvas.show();
			} else {
				$elCanvas.hide();
			}
		});
	};
	CanvasManager.prototype.toggle = function( type ) {
		$.each( this._oElCanvas, function( key, $elCanvas ) {
			if ( BigScatterChart2.Util.endsWith( key, type ) ) {
				$elCanvas.toggle();
			}
		});
	};
	CanvasManager.prototype.showSelectedAgent = function( bIsAll, agentName, oTypeCheckInfo ) {
		var self = this;
		$.each( this._oElCanvas, function( key, $elCanvas ) {
			if ( bIsAll ) {
				$elCanvas[ self._visible( key.split("-"), oTypeCheckInfo ) ? "show" : "hide" ]();
			} else {
				if ( BigScatterChart2.Util.startsWith( key, agentName ) ) {
					$elCanvas[ self._visible( key.split("-"), oTypeCheckInfo ) ? "show" : "hide" ]();
				} else {
					$elCanvas.hide();
				}
			}
		});
	};
	CanvasManager.prototype._visible = function( aData, oTypeCheckInfo ) {
		return oTypeCheckInfo[aData[ aData.length - 1 ]];
	};
	CanvasManager.prototype.drawBubble = function( key, color, x, y, r, a ) {
		this._oCtx[key].beginPath();
		this._oCtx[key].fillStyle = color;
		this._oCtx[key].strokeStyle = color;
		this._oCtx[key].arc(x, y, r, 0, Math.PI * 2, true);
		this._oCtx[key].globalAlpha = 0.3 + a;
		this._oCtx[key].fill();
	};
	CanvasManager.prototype._mergeAllDisplay = function( oElType )  {
		var self = this;
		var width = this.option("width");
		var height = this.option("height");

		var $elDownloadCanvas = $("<canvas>").attr({
			"width": width,
			"height": height
		});
		var oCtx = $elDownloadCanvas.get(0).getContext("2d");
		oCtx.fillStyle = "#FFFFFF";
		oCtx.fillRect(0, 0, width, height);

		// grid
		oCtx.drawImage(this._$elGuideCanvas.get(0), 0, 0);

		// scatter
		$.each( this._oElCanvas, function( key, $elCanvas ) {
			if ( $elCanvas.is(":visible") ) {
				oCtx.drawImage( $elCanvas.get(0), 0, 0 );
			}
		});

		// xy axis
		oCtx.drawImage(this._$elAxisCanvas.get(0), 0, 0);

		// common setting
		oCtx.textBaseline = "top";


		// count
		var oWrapperOffset = this._$elWrapper.offset();
		oCtx.textAlign = "left";
		$.each( oElType, function (sKey, $elLI) {
			var oOffset = $elLI.offset();
			var x = oOffset.left - oWrapperOffset.left;
			var y = oOffset.top - oWrapperOffset.top;

			oCtx.font = $elLI.css("font");
			oCtx.fillStyle = $elLI.css("color");
			oCtx.fillText( $elLI.text(), x + parseInt($elLI.css("padding-left"), 10), y );

			if ($elLI.hasClass("unchecked")) {
				oCtx.drawImage(self._oUncheckedBoxImage, x, y);
			} else {
				oCtx.drawImage(self._oCheckedBoxImage, x, y);
			}
		});

		// x axis
		oCtx.textAlign = "center";
		$.each(this._aElAxisX, function ( index, $elAxis) {
			oCtx.font = $elAxis.css("font");
			oCtx.fillStyle = $elAxis.css("color");
			oCtx.fillText( $elAxis.text(), parseInt( $elAxis.css( "left" ), 10 ) + $elAxis.width() / 2, parseInt( $elAxis.css( "top" ), 10 ) );
		});

		// y axis
		oCtx.textAlign = "right";
		$.each(this._aElAxisY, function (index, $elAxis) {
			oCtx.font = $elAxis.css("font");
			oCtx.fillStyle = $elAxis.css("color");
			oCtx.fillText( $elAxis.text(), parseInt( $elAxis.css( "left" ), 10 ) + $elAxis.width(), parseInt($elAxis.css("top"), 10) );
		});

		/*
		// x label
		if (this._welXLabel) {
			oCtx.textAlign = "right";
			oCtx.font = this._welXLabel.css("font");
			oCtx.fillStyle = this._welXLabel.css("color");
			oCtx.fillText(this._welXLabel.text(), width, parseInt(this._welXLabel.css("top"), 10));
		}

		// y label
		if (this._welYLabel) {
			oCtx.textAlign = "right";
			oCtx.font = this._welYLabel.css("font");
			oCtx.fillStyle = this._welYLabel.css("color");
			oCtx.fillText(this._welYLabel.text(), parseInt(this._welYLabel.css("left"), 10) + this._welYLabel.width(), parseInt(this._welYLabel.css("top"), 10));
		}
		*/

		// nodata
		//if ( $elNoData.css("display") === "block" ) {
		//	oCtx.textAlign = "center";
		//	oCtx.font = $elNoData.css("font");
		//	oCtx.fillStyle = $elNoData.css("color");
		//	oCtx.fillText( $elNoData.text(), parseInt( $elNoData.css("top") ), width / 2);
		//}

		//// drag-selecting
		//var sDragToSelectClassName = this.option("sDragToSelectClassName"),
		//	welDragToSelect = $("." + sDragToSelectClassName);
		//oCtx.rect(parseInt(welDragToSelect.css("left")), parseInt(welDragToSelect.css("top")), welDragToSelect.width(), welDragToSelect.height());
		//oCtx.globalAlpha = welDragToSelect.css("opacity");
		//oCtx.fillStyle = welDragToSelect.css("background-color");
		//oCtx.fill();
		//oCtx.strokeStyle = welDragToSelect.css("border-color");
		//oCtx.stroke();

		return $elDownloadCanvas;
	};

	global.BigScatterChart2.CanvasManager = CanvasManager;
})(window, jQuery);