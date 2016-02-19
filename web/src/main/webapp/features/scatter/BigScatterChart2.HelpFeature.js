(function(global, $) {
	'use strict';
	function HelpFeature( tooltipService ) {
		this._init( tooltipService );
	}
	HelpFeature.prototype._init = function( tooltipService ) {
		this._tooltipService = tooltipService;
		this._aCallback = [];
	};
	HelpFeature.prototype.initElement = function( $elParent, $elPlugin ) {
		this._$element = $("<div>")
		.css({
			"padding": "4px 0px 4px 20px"
		})
		.append( $("<span>").addClass("glyphicon glyphicon-question-sign scatterTooltip").css("cursor", "pointer") )
		.appendTo( $elPlugin );

		this._tooltipService.init("scatter");
		return this;
	};
	HelpFeature.prototype.initEvent = function( oChart ) {
		return this;
	};
	HelpFeature.prototype.addCallback = function( fn ) {
		this._aCallback.push( fn );
	};

	global.BigScatterChart2.HelpFeature = HelpFeature;
})(window, jQuery);