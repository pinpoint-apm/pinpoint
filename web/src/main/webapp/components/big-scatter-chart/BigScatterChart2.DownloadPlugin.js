(function(global, $) {
	'use strict';
	function DownloadPlugin( imageData ) {
		this._init( imageData );
	}
	DownloadPlugin.prototype._init = function( imageData ) {
		this._featureImage = imageData;
		this._aCallback = [];
		this._bDisabled = false;
	};
	DownloadPlugin.prototype.initElement = function( $elParent, $elPlugin, option ) {
		this._bDisabled = option["realtime"];
		this._$element = $("<div>").css({
			"cursor": "pointer",
			"padding": "4px 0px 4px 20px"
		}).append(
			$("<a>").attr({
				"alt": "Download Scatter Chart",
				"href": "",
				"title": "Download Scatter Chart",
				"download": ""
			}).append(
				$("<img>").attr({
					"src": this._featureImage
				}).css({
					"opacity": this._bDisabled ? 0.2 : 1
				})
			)
		).appendTo( $elPlugin );
		return this;
	};
	DownloadPlugin.prototype.initEvent = function( oChart ) {
		var self = this;
		this._$element.find("a").on("click", function(event) {
			if ( self._bDisabled ) {
				event.preventDefault();
				return;
			}
			$(this).attr({
				"href": oChart.getChartAsImage( "png" ),
				"download": "Pinpoint_Scatter_Chart[" + moment( oChart.option( "minX" ) ).format( "YYYYMMDD_HHmm" ) + "~" + moment( oChart.option( "maxX" ) ).format( "YYYYMMDD_HHmm" ) + "]"
			});
			$.each( self._aCallback, function( index, fn ) {
				fn( oChart );
			});
		});
		return this;
	};
	DownloadPlugin.prototype.addCallback = function( fn ) {
		this._aCallback.push( fn );
		return this;
	};

	global.BigScatterChart2.DownloadPlugin = DownloadPlugin;
})(window, jQuery);