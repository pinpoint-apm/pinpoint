(function(global, $) {
	'use strict';
	function RendererManager( option, oSizeCoordinateManager, aAgentList, $elContainer ) {
		this._option = option;
		this._oSCManager = oSizeCoordinateManager;
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
	RendererManager.prototype.option = function( v ) {
		return this._option[v];
	};
	RendererManager.prototype._initVariable = function() {
		this._oElScrollCanvas = {};
		this._oScrollCtx = {};

		this._ticksOfX = this.option("ticksOfX") - 1;
		this._ticksOfY = this.option("ticksOfY") - 1;

		this._aElAxisX = [];
		this._aElAxisY = [];

		var oCheckboxImageData = this.option("checkBoxImage");
		this._oCheckedBoxImage = new Image();
		this._oCheckedBoxImage.src = oCheckboxImageData.checked;
		this._oUncheckedBoxImage = new Image();
		this._oUncheckedBoxImage.src = oCheckboxImageData.unchecked;
	};
	RendererManager.prototype._makeGridCanvas = function() {
		this._$elGuideCanvas = $("<canvas>").attr({
			"width": this._oSCManager.getWidth(),
			"height": this._oSCManager.getHeight()
		}).css({
			"top": 0,
			"z-index": 0,
			"position": "absolute"
		}).append( this._getNotSupportMarkup()).appendTo( this._$elWrapper );

		this._oCtxGrid = this._$elGuideCanvas.get(0).getContext("2d");
	};
	RendererManager.prototype._makeAxisCanvas = function() {
		this._$elAxisCanvas = $("<canvas>").attr({
			"width": this._oSCManager.getWidth(),
			"height": this._oSCManager.getHeight()
		}).css({
			"top": 0,
			"z-index": 10,
			"position": "absolute"
		}).append( this._getNotSupportMarkup() ).appendTo( this._$elWrapper);

		this._oCtxAxis = this._$elAxisCanvas.get(0).getContext("2d");
	};
	RendererManager.prototype._getNotSupportMarkup = function() {
		return $("<div>Your browser does not support the canvas element, get a better one!</div>").css({
			"color": "#fff",
			"width": this._oSCManager.getWidth(),
			"height": this._oSCManager.getHeight(),
			"text-align": "center",
			"background-color": "#8b2e19"
		});
	};
	RendererManager.prototype._drawGridLine = function() {

		var width = this._oSCManager.getWidth();
		var height = this._oSCManager.getHeight();
		var oPadding = this._oSCManager.getPadding();
		var bubbleSize = this._oSCManager.getBubbleSize();
		var tickX = this._oSCManager.getWidthOfChartSpace() / this._ticksOfX;
		var tickY = this._oSCManager.getHeightOfChartSpace() / this._ticksOfY;
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

	RendererManager.prototype._drawAxisLine = function() {
		var width = this._oSCManager.getWidth();
		var height = this._oSCManager.getHeight();
		var oPadding = this._oSCManager.getPadding();
		var bubbleSize = this._oSCManager.getBubbleSize();
		var lineColor = this.option( "lineColor" );
		var gridAxisStyle = this.option( "gridAxisStyle" );
		var tickX = this._oSCManager.getWidthOfChartSpace() / this._ticksOfX;
		var tickY = this._oSCManager.getHeightOfChartSpace() / this._ticksOfY;
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
	RendererManager.prototype._setStyle = function( ctx, oStyle ) {
		$.each( oStyle, function( key, value ) {
			if ( key === "lineDash" ) {
				ctx.setLineDash( value );
			} else {
				ctx[key] = value;
			}
		});
	};
	RendererManager.prototype._moveTo = function( ctx, x, y ) {
		if (x  % 1 === 0) {
			x += 0.5;
		}
		if (y  % 1 === 0) {
			y += 0.5;
		}
		ctx.moveTo(x, y);
	};
	RendererManager.prototype._lineTo = function( ctx, x, y ) {
		if (x  % 1 === 0) {
			x += 0.5;
		}
		if (y  % 1 === 0) {
			y += 0.5;
		}
		ctx.lineTo(x, y);
	};
	RendererManager.prototype._drawAxisValue = function() {
		var tickX = this._oSCManager.getWidthOfChartSpace() / this._ticksOfX;
		var tickY = this._oSCManager.getHeightOfChartSpace() / this._ticksOfY;
		var width = this._oSCManager.getWidth();
		var height = this._oSCManager.getHeight();
		var oPadding = this._oSCManager.getPadding();
		var bubbleSize = this._oSCManager.getBubbleSize();

		var sPrefix = this.option( "sPrefix" );
		var lineColor = this.option( "lineColor" );
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
				"top": ( height - oPadding.bottom + 10) + "px",
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
	RendererManager.prototype._makeDataCanvas = function() {
		var self = this;
		var bubbleSize = this._oSCManager.getBubbleSize();
		var sPrefix = this.option("sPrefix");
		var zIndex = 100;
		var aBubbleTypeInfo = this.option("typeInfo");

		var widthOfChartSpace = this._oSCManager.getWidthOfChartSpace();
		var heightOfChartSpace = this._oSCManager.getHeightOfChartSpace();
		var canvasWidth = this._oSCManager.getCanvasWidth();
		this._$elScroller = $("<div>").css({
			"top": "0px",
			"left": "0px",
			"height": ( heightOfChartSpace + bubbleSize * 2 ) + "px",
			"position": "absolute",
			"background-color": "gray"
		}).addClass( "canvas-scroller" );

		$("<div>").css({
			"top": this._oSCManager.getTopOfChartSpace() + "px",
			"left": ( this._oSCManager.getLeftOfChartSpace() + bubbleSize ) + "px",
			"width": widthOfChartSpace + "px",
			"height": ( heightOfChartSpace + bubbleSize * 2 ) + "px",
			"z-index": zIndex++,
			"overflow": "hidden",
			"position": "absolute"
		}).addClass( "canvas-wrapper" ).append( this._$elScroller ).appendTo( this._$elWrapper );

		$.each( this._aAgentList, function( index, agentName ) {
			$.each( aBubbleTypeInfo, function( index, aValue ) {
				var key = BigScatterChart2.Util.makeKey( agentName, sPrefix, aValue[0] );
				self._oElScrollCanvas[key] = [
					$("<canvas>").attr({
						"width": canvasWidth,
						"height": ( heightOfChartSpace + bubbleSize * 2 ) + "px",
						"data-key": key
					}).css({
						"top": "0px",
						"left": "0px",
						"z-index": zIndex++,
						"position": "absolute"
					}).appendTo( self._$elScroller ),
					$("<canvas>").attr({
						"width": canvasWidth,
						"height": ( heightOfChartSpace + bubbleSize * 2 ) + "px",
						"data-key": key
					}).css({
						"top": "0px",
						"left": canvasWidth + "px",
						//"background-color": "rgba( 240, 240, 240, 0.3 )",
						"z-index": zIndex++,
						"position": "absolute"
					}).appendTo( self._$elScroller )
				];

				self._oScrollCtx[key] = [
					self._oElScrollCanvas[key][0].get(0).getContext("2d"),
					self._oElScrollCanvas[key][1].get(0).getContext("2d")
				];
			});
		});
		this._oScrollIndexOrder = [ 0, 1 ];

	};
	RendererManager.prototype.getChartAsImage = function( type, oElType ) {
		return this._mergeAllDisplay( oElType ).get(0).toDataURL("image/" + type.toLowerCase() );
	};
	RendererManager.prototype.updateXYAxis = function( minX, maxX, minY, maxY ) {
		var self = this;
		if ($.isNumeric(minX)) {
			//this._minX = this.option("minX", minX);
		}
		if ($.isNumeric(maxX)) {
			//this._maxX = this.option("minX", maxX);
		}
		if ($.isNumeric(minY)) {
			//this._minY = this.option("minY", minY);
		}
		if ($.isNumeric(maxY)) {
			//this._maxY = this.option("maxY", maxY);
		}

		var fnXAxisFormat = this.option( "fnXAxisFormat" );
		var oRangeX = this._oSCManager.getX();
		var tickX = ( this._oSCManager.getGapX() ) / this._ticksOfX;
		$.each( this._aElAxisX, function ( index, $el ) {
			if ( $.isFunction( fnXAxisFormat ) ) {
				$el.html( fnXAxisFormat.call( self, tickX, index, oRangeX.min ) );
			} else {
				$el.html( ( tickX * index + oRangeX.min ).round() );
			}
		});

		var fnYAxisFormat = this.option( "fnYAxisFormat" );
		var oRangeY = this._oSCManager.getY();
		var tickY = ( this._oSCManager.getGapY() ) / this._ticksOfY;
		$.each( this._aElAxisY, function ( index, $el ) {
			if ( $.isFunction( fnYAxisFormat ) ) {
				$el.html( fnYAxisFormat.call( self, tickY, index, oRangeY.min, oRangeY.max ) );
			} else {
				$el.html( BigScatterChart2.Util.addComma( ( oRangeY.max + oRangeY.min ) - ( ( tickY * index ) + oRangeY.min ) ) );
			}
		});
	};
	RendererManager.prototype.clear = function() {
		var width = this._oSCManager.getCanvasWidth();
		var height = this._oSCManager.getHeight();

		$.each( this._oScrollCtx, function( key, aCtx ) {
			$.each( aCtx, function( i, ctx ) {
				ctx.clearRect(0, 0, width, height);
			});
		});
	};
	RendererManager.prototype.selectType = function( agentName, type ) {
		$.each( this._oElScrollCanvas, function( key, aCanvas ) {
			$.each( aCanvas, function( i, $elCanvas ) {
				if (BigScatterChart2.Util.endsWith(key, type)) {
					$elCanvas.show();
				} else {
					$elCanvas.hide();
				}
			});
		});
	};
	RendererManager.prototype.toggle = function( bIsAll, agentName, type ) {
		$.each( this._oElScrollCanvas, function( key, aCanvas ) {
			$.each( aCanvas, function( i, $elCanvas ) {
				if ( ( bIsAll || BigScatterChart2.Util.startsWith(key, agentName) ) && BigScatterChart2.Util.endsWith(key, type)) {
					$elCanvas.toggle();
				}
			});
		});
	};
	RendererManager.prototype.showSelectedAgent = function( bIsAll, agentName, oTypeCheckInfo ) {
		var self = this;
		$.each( this._oElScrollCanvas, function( key, aCanvas ) {
			if ( bIsAll ) {
				var visibleCmd = self._visible( key.split("-"), oTypeCheckInfo ) ? "show" : "hide";
				$.each( aCanvas, function( i, $elCanvas ) {
					$elCanvas[ visibleCmd ]();
				});
			} else {
				$.each( aCanvas, function( i, $elCanvas ) {
					if (BigScatterChart2.Util.startsWith(key, agentName)) {
						$elCanvas[self._visible(key.split("-"), oTypeCheckInfo) ? "show" : "hide"]();
					} else {
						$elCanvas.hide();
					}
				});
			}
		});
	};
	RendererManager.prototype._visible = function( aData, oTypeCheckInfo ) {
		return oTypeCheckInfo[aData[ aData.length - 1 ]];
	};
	RendererManager.prototype.drawBubble = function( key, color, aBubbleData ) {
		var oPropertyIndex = this.option( "propertyIndex" );
		var bubbleRadius = this.option( "bubbleRadius" );
		var oRangeY = this._oSCManager.getY();

		var x = ( aBubbleData[oPropertyIndex.x] - this._oSCManager.getStartX() ) * this._oSCManager.getPixelPerTime();
		var y = this._oSCManager.parseYDataToYChart( BigScatterChart2.Util.getBoundaryValue( oRangeY, aBubbleData[oPropertyIndex.y]), false );
		var r = this._oSCManager.parseZDataToZChart( aBubbleData.r || bubbleRadius );

		var canvasWidth = this._oSCManager.getCanvasWidth();
		var ctxIndex = this._oScrollIndexOrder[0];
		var zeroLeft = parseInt( this._oElScrollCanvas[key][ctxIndex].css( "left" ) );
		var currentMaxX = zeroLeft + canvasWidth;
		if ( x > currentMaxX ) {
			ctxIndex = this._oScrollIndexOrder[1];
			x -= currentMaxX;
		} else {
			x -= zeroLeft;
		}

		this._oScrollCtx[key][ctxIndex].beginPath();
		this._oScrollCtx[key][ctxIndex].fillStyle = color;
		this._oScrollCtx[key][ctxIndex].strokeStyle = color;
		this._oScrollCtx[key][ctxIndex].arc( x, y, r, 0, Math.PI * 2, true );
		this._oScrollCtx[key][ctxIndex].globalAlpha = 0.3 + ( 0.1 * aBubbleData[oPropertyIndex.groupCount] );
		this._oScrollCtx[key][ctxIndex].fill();
	};
	RendererManager.prototype.moveChart = function( moveXValue, aniTime ) {
		var self = this;
		var canvasWidth = this._oSCManager.getCanvasWidth();
		var height = this._oSCManager.getHeight();
		var nextLeft = parseInt( this._$elScroller.css( "left" ), 10 ) - moveXValue;
		this._$elScroller.animate({
			"left": nextLeft
		}, aniTime, function() {
			var temp = self._oScrollIndexOrder[0];
			var bOverBoundary = false;
			$.each( self._oElScrollCanvas, function( key, aCanvas ) {
				if ( Math.abs( nextLeft ) > ( parseInt( aCanvas[self._oScrollIndexOrder[0]].css( "left" ) ) + canvasWidth ) ) {
					bOverBoundary = true;
					aCanvas[self._oScrollIndexOrder[0]].css( "left", parseInt( aCanvas[self._oScrollIndexOrder[1]].css( "left" ) ) + canvasWidth );
					self._oScrollCtx[key][self._oScrollIndexOrder[0]].clearRect( 0, 0, canvasWidth, height );
				}
			});
			if ( bOverBoundary ) {
				self._oScrollIndexOrder[0] = self._oScrollIndexOrder[1];
				self._oScrollIndexOrder[1] = temp;
			}
		});
	};
	RendererManager.prototype.reset = function() {
		var canvasWidth = this._oSCManager.getCanvasWidth();
		this._$elScroller.css( "left", 0 );
		this._oScrollIndexOrder = [ 0, 1 ];
		$.each( this._oElScrollCanvas, function( key, aCanvas ) {
			$.each( aCanvas, function( index, $elCanvas ) {
				$elCanvas.css( "left", index * canvasWidth );
			});
		});
		this.clear();
	};
	RendererManager.prototype._mergeAllDisplay = function( oElType )  {
		var self = this;
		var oPadding = this._oSCManager.getPadding();
		var bubbleSize = this._oSCManager.getBubbleSize();
		var width = this._oSCManager.getWidth();
		var height = this._oSCManager.getHeight();

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
		$.each( this._oElScrollCanvas, function( key, aCanvas ) {
			$.each( aCanvas, function( i, $elCanvas ) {
				if ( $elCanvas.is(":visible") ) {
					oCtx.drawImage( $elCanvas.get(0), oPadding.left + bubbleSize, oPadding.top );
				}
			});
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
			oCtx.fillText( $elAxis.text(), parseInt( $elAxis.css( "left" ), 10 ) + $elAxis.get(0).getBoundingClientRect().width / 2, parseInt( $elAxis.css( "top" ), 10 ) );
		});

		// y axis
		oCtx.textAlign = "right";
		$.each(this._aElAxisY, function (index, $elAxis) {
			oCtx.font = $elAxis.css("font");
			oCtx.fillStyle = $elAxis.css("color");
			oCtx.fillText( $elAxis.text(), parseInt( $elAxis.css( "left" ), 10 ) + $elAxis.get(0).getBoundingClientRect().width, parseInt($elAxis.css("top"), 10) );
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

	global.BigScatterChart2.RendererManager = RendererManager;
})(window, jQuery);