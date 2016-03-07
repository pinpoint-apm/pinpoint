(function(global, $) {
	'use strict';
	function WideOpenPlugin( sImage ) {
		this._init( sImage );
	}
	WideOpenPlugin.prototype._init = function( sImage ) {
		this._featureImage = sImage;
		this._aCallback = [];
		this._bDisabeld = false;
	};
	WideOpenPlugin.prototype.initElement = function( $elParent, $elPlugin ) {
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
	WideOpenPlugin.prototype.initEvent = function( oChart ) {
		var self = this;
		this._$element.on("click", function( event ) {
			event.preventDefault();
			$.each( self._aCallback, function( index, fn ) {
				fn( oChart, self._$element );
			});

		});
		return this;
	};
	WideOpenPlugin.prototype.addCallback = function( fn ) {
		this._aCallback.push( fn );
		return this;
	};

	global.BigScatterChart2.WideOpenPlugin = WideOpenPlugin;
})(window, jQuery);