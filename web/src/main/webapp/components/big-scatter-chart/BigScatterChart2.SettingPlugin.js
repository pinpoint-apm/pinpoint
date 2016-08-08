(function(global, $) {
	'use strict';
	function SettingPlugin( sImage ) {
		this._init( sImage );
	}
	SettingPlugin.prototype._init = function( sImage ) {
		this._featureImage = sImage;
		this._aCallback = [];
	};
	SettingPlugin.prototype.initElement = function( $elParent, $elPlugin, option ) {
		var width = option["width"];
		var height = option["height"];

		var nCenterOfWidth = width / 2;
		var nMiddleOfHeight = height / 2;
		var nConfigLayerWidth = 200;
		var nConfigLayerHeight = 130;
		var randomMinID = "setting-min-" + parseInt( Math.random() * 100000 );
		var randomMaxID = "setting-max-" + parseInt( Math.random() * 100000 );

		this._$elConfigButton = $("<div>").css({
			"padding": "4px 0px 4px 20px",
			"cursor": "pointer"
		}).append( $("<img>").attr({
			"src": this._featureImage,
			"alt" : "Open Config",
			"title" : "Open Config"
		})).appendTo( $elPlugin );

		var template = [
			'<div class="config-layer">',
				'<div class="config-bg cancel"></div>',
				'<div class="dropdown-menu config">',
					'<h5>Setting</h5>',
					'<label for="" class="label">Min of Y axis</label>',
					'<input type="text" id="" class="input" value=""/>',
					'<label for="" class="label">Max of Y axis</label>',
					'<input type="text" id="" class="input" vlaue=""/>',
					'<button type="button" class="apply btn btn-default btn-xs">Apply</button>',
					'<button type="button" class="cancel btn btn-default btn-xs">Cancel</buton>',
				'</div>',
			'</div>'
		].join("");
		this._$element = $( template );
		this._$element.css({
			"width": width + "px",
			"height": height + "px",
			"display": "none",
			"position": "relative"
		});
		this._$element.find(".config-bg").css({
			"width": width + "px",
			"height": height + "px",
			"opacity": 0.3,
			"z-index": 1100,
			"position": "absolute",
			"background-color": "#000",
		});
		this._$element.find(".dropdown-menu").css({
			"top": nMiddleOfHeight - nConfigLayerHeight / 2 + "px",
			"left": nCenterOfWidth - nConfigLayerWidth / 2 + "px",
			"width": nConfigLayerWidth + "px",
			"height": nConfigLayerHeight + "px",
			"display": "block",
			"z-index": 1101
		});
		this._$element.find("label:first").attr("for", randomMinID);
		this._$element.find("label:last").attr("for", randomMaxID);
		this._$element.find("input[type=text]:first").attr("id", randomMinID).val( option["minY"] );
		this._$element.find("input[type=text]:last").attr("id", randomMaxID).val( option["maxY"] );
		$elParent.append( this._$element );

		return this;
	};
	SettingPlugin.prototype.hide = function() {
		this._$element.hide();
	};
	SettingPlugin.prototype.show = function() {
		this._$element.show();
	};
	SettingPlugin.prototype.initEvent = function( oChart ) {
		var self = this;

		this._$elConfigButton.click(function() {
			self.show();
		});
		this._$element.find(".cancel").click(function() {
			self.hide();
		});
		this._$element.find(".apply").click( function( event ) {
			event.preventDefault();
			var minY = parseInt( self._$element.find("input[type=text]:first").val(), 10 );
			var maxY = parseInt( self._$element.find("input[type=text]:last").val(), 10 );

			if (minY >= maxY) {
				alert("Min of Y axis is should be smaller than " + maxY);
				return;
			}
			$.each( self._aCallback, function( index, fn ) {
				fn( oChart, { "min": minY, "max": maxY } );
			});
			self.hide();
		});
		return this;
	};
	SettingPlugin.prototype.addCallback = function( fn ) {
		this._aCallback.push( fn );
		return this;
	};

	global.BigScatterChart2.SettingPlugin = SettingPlugin;
})(window, jQuery);