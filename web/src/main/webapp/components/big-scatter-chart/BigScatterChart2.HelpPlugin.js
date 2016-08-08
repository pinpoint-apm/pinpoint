(function(global, $) {
	'use strict';
	function HelpPlugin( tooltipService ) {
		this._init( tooltipService );
	}
	HelpPlugin.prototype._init = function( tooltipService ) {
		this._tooltipService = tooltipService;
		this._aCallback = [];
	};
	HelpPlugin.prototype.initElement = function( $elParent, $elPlugin ) {
		var self = this;
		this._$element = $("<div>")
			.css({ "padding": "4px 0px 4px 20px" })
			.append(
				$("<span>").addClass("glyphicon glyphicon-question-sign scatterTooltip")
				.css("cursor", "pointer")
			)
			.appendTo( $elPlugin );

		setTimeout(function() {
			self._tooltipService.init("scatter");
		},0);
		return this;
	};
	HelpPlugin.prototype.initEvent = function( oChart ) {
		return this;
	};
	HelpPlugin.prototype.addCallback = function( fn ) {
		this._aCallback.push( fn );
	};

	global.BigScatterChart2.HelpPlugin = HelpPlugin;
})(window, jQuery);