(function(global, $) {
	'use strict';
	function MessagePlugin() {
		this._init();
	}
	MessagePlugin.prototype._init = function() {
		this._aCallback = [];
	};
	MessagePlugin.prototype.initElement = function( $elParent, $elPlugin, option ) {
		this._$element = $("<div>")
		.css({
			"top": 0,
			"width": option["width"],
			"height": option["height"],
			"cursor": "crosshair",
			"display": "none",
			"z-index": 1100,
			"position": "absolute",
			"background-color": "rgba(0,0,0,0)" // for ie10
		})
		.addClass("message-display")
		.append(
			$("<div>")
			.css( option["noDataStyle"] )
			.css({
				"top": ( option["height"] / 2  ) + "px",
				"width": option["width"] + "px",
				"position": "absolute",
				"text-align": "center"
			}
		)).appendTo( $elParent );

		return this;
	};
	MessagePlugin.prototype.initEvent = function( /* oChart */ ) {
		return this;
	};
	MessagePlugin.prototype.addCallback = function( fn ) {
		this._aCallback.push( fn );
	};
	MessagePlugin.prototype.show = function( message ) {
		this._$element.find("> div").html( message).end().show();
	};
	MessagePlugin.prototype.hide = function() {
		this._$element.hide();
	};

	global.BigScatterChart2.MessagePlugin = MessagePlugin;
})(window, jQuery);