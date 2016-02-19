(function(global, $) {
	'use strict';
	function WideOpenFeature( sImage ) {
		this._init( sImage );
	}
	WideOpenFeature.prototype._init = function( sImage ) {
		this._featureImage = sImage;
		this._aCallback = [];
	};
	WideOpenFeature.prototype.initElement = function( $elParent, $elPlugin ) {
		this._$element = $("<div>").css({
			"cursor": "pointer",
			"padding": "4px 0px 4px 20px"
		}).append( $("<img>").attr({
			"src": this._featureImage,
			"alt" : "Full Screen Mode",
			"title" : "Full Screen Mode"
		}) ).appendTo( $elPlugin );
		return this;
	};
	WideOpenFeature.prototype.initEvent = function( oChart ) {
		var self = this;
		this._$element.on("click", function( event ) {
			event.preventDefault();
			$.each( self._aCallback, function( index, fn ) {
				fn( oChart, self._$element );
			});

		});
		return this;
	};
	WideOpenFeature.prototype.addCallback = function( fn ) {
		this._aCallback.push( fn );
		return this;
	};

	global.BigScatterChart2.WideOpenFeature = WideOpenFeature;
})(window, jQuery);