(function(global, $) {
	'use strict';
	function SizeCoordinateManager( option ) {
		this._option = option;
		this._initVar();
	}
	SizeCoordinateManager.prototype.option = function( key, value ) {
		if ( arguments.length === 1 ) {
			return this._option[key];
		} else {
			this._option[key] = value;
		}
	};
	SizeCoordinateManager.prototype._initVar = function() {
		var oPadding = this.option("padding");
		var	bubbleSize = this.option("bubbleSize");

		this._startX = this.option( "minX" );
		this._widthOfChartSpace = ( this.option("width") - ( oPadding.left + oPadding.right ) ) - bubbleSize * 2;
		this._heightOfChartSpace = ( this.option("height") - ( oPadding.top + oPadding.bottom ) ) - bubbleSize * 2;

		this._calcuUnitValue();
	};
	SizeCoordinateManager.prototype._calcuUnitValue = function() {
		this._timePerPixel = this.getGapX() / this._widthOfChartSpace;
		this._pixelPerTime = this._widthOfChartSpace / this.getGapX();
	};
	SizeCoordinateManager.prototype.getStartX = function() {
		return this._startX;
	};
	SizeCoordinateManager.prototype.getLeftOfChartSpace = function() {
		return this.option( "padding" ).left;
	};
	SizeCoordinateManager.prototype.getTopOfChartSpace = function() {
		return this.option( "padding" ).top;
	};
	SizeCoordinateManager.prototype.getCanvasWidth = function() {
		return this._widthOfChartSpace + 50;
	};
	SizeCoordinateManager.prototype.getWidth = function() {
		return this.option( "width" );
	};
	SizeCoordinateManager.prototype.getHeight = function() {
		return this.option( "height" );
	};
	SizeCoordinateManager.prototype.getX = function() {
		return {
			"min": this.option( "minX" ),
			"max": this.option( "maxX" )
		};
	};
	SizeCoordinateManager.prototype.setX = function( min, max, bReset ) {
		this.option( "minX", min );
		this.option( "maxX", max );
		if ( bReset === true ) {
			this._startX = min;
		}
		this._calcuUnitValue();
	};
	SizeCoordinateManager.prototype.getY = function() {
		return {
			"min": this.option( "minY" ),
			"max": this.option( "maxY" )
		};
	};
	SizeCoordinateManager.prototype.setY = function( min, max ) {
		this.option( "minY", min );
		this.option( "maxY", max );
	};
	SizeCoordinateManager.prototype.getPadding = function() {
		return this.option( "padding" );
	};
	SizeCoordinateManager.prototype.getBubbleSize = function() {
		return this.option( "bubbleSize" );
	};
	SizeCoordinateManager.prototype.getWidthOfChartSpace = function() {
		return this._widthOfChartSpace;
	};
	SizeCoordinateManager.prototype.getHeightOfChartSpace = function() {
		return this._heightOfChartSpace;
	};
	SizeCoordinateManager.prototype.getPixelPerTime = function() {
		return this._pixelPerTime;
	};
	SizeCoordinateManager.prototype.getTimePerPixel = function() {
		return this._timePerPixel;
	};
	//SizeCoordinateManager.prototype.parseXDataToXChart = function( x, plusPadding ) {
	//	return Math.round( ( ( x - this.option("minX") ) / this.getGapX() ) * this._widthOfChartSpace ) + this.option( "bubbleSize" ) + ( plusPadding ? this.option("padding").left : 0 );
	//};
	SizeCoordinateManager.prototype.parseYDataToYChart = function( y, plusPadding ) {
		return Math.round(this._heightOfChartSpace - (((y - this.option("minY") ) / this.getGapY() ) * this._heightOfChartSpace)) + this.option( "bubbleSize" ) + ( plusPadding ? this.option("padding").top : 0 );
	};
	SizeCoordinateManager.prototype.parseZDataToZChart = function( z ) {
		return Math.round( ( ( z - this.option( "minZ" ) ) / this.getGapZ() ) * this.option( "bubbleSize" ) );
	};
	SizeCoordinateManager.prototype.getLeftOfPluginArea = function() {
		return this.option("width") - this.option("padding").right;
	};
	SizeCoordinateManager.prototype.getXOfPixel = function() {
		return Math.round( this.getGapX() / this._widthOfChartSpace );
	};
	SizeCoordinateManager.prototype.getYOfPixel = function() {
		return Math.round( this.getGapY() / this._heightOfChartSpace );
	};
	SizeCoordinateManager.prototype.parseMouseXToXData = function( x ) {
		return Math.round((x / this._widthOfChartSpace ) * this.getGapX() ) + this.option( "minX" );
	};
	SizeCoordinateManager.prototype.parseMouseYToYData = function( y ) {
		return Math.round( this.option("minY") + ( ( y / this._heightOfChartSpace) * this.getGapY() ) );
	};
	SizeCoordinateManager.prototype.getGapX = function() {
		return this.option( "maxX" ) - this.option( "minX" );
	};
	SizeCoordinateManager.prototype.getGapY = function() {
		return this.option( "maxY" ) - this.option( "minY" );
	};
	SizeCoordinateManager.prototype.getGapZ = function() {
		return this.option( "maxZ" ) - this.option( "minZ" );
	};

	global.BigScatterChart2.SizeCoordinateManager = SizeCoordinateManager;
})(window, jQuery);