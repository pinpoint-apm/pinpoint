(function(global, $) {
	'use strict';
	function BubbleTypeManager( option, oSizeCoordinateManager, $elContainer, oCallback ) {
		this._option = option;
		this._oSCManager = oSizeCoordinateManager;
		this._oCallback = oCallback;
		this._initVar();
		this._initElements( $elContainer );
		this._initEvents();
	}
	BubbleTypeManager.prototype.option = function( key ) {
		return this._option[key];
	};
	BubbleTypeManager.prototype._initVar = function() {
		this._oReferenceLI = {};
		this._oReferenceSpan = {};
	};
	BubbleTypeManager.prototype._initElements = function( $elContainer ) {
		var self = this;
		var oPadding = this._oSCManager.getPadding();

		this._$element = $("<div>").css({
			"top": 0,
			"width": this._oSCManager.getWidth() - oPadding.right,
			"height": oPadding.top,
			"cursor": "pointer",
			"z-index": 510,
			"position": "absolute",
			"background-color": "rgba(0,0,0,0)" // for ie10
		}).addClass("bubble-type");

		this._$elTypeUL = $("<ul>").css({
			"top": "5px",
			"margin": "0",
			"padding": "0",
			"position": "absolute",
			"font-size": "12px",
			"list-style": "none"
		});

		$.each( this._sortTypeInfo(), function ( index, aValue ) {
			// <li style="background-image:">TypeName : <span></span></li>
			self._$elTypeUL.append( self._oReferenceLI[aValue[0]] = $("<li>").attr("data-type", aValue[0]).css({
				"color": aValue[1],
				"margin": "0 0 0 20px",
				"padding": "0 0 0 19px",
				"display": "inline-block",
				"line-height": "15px",
				"background-image": "url(" + self.option("checkBoxImage").checked + ")",
				"background-repeat": "no-repeat"
			}).text(aValue[0] + " : ").append( self._oReferenceSpan[aValue[0]] = $("<span>").text("0") ) );
		});
		this._$element.append( this._$elTypeUL).appendTo( $elContainer );
	};
	BubbleTypeManager.prototype._sortTypeInfo = function() {
		var aOriginal = [];
		$.each( this.option("typeInfo"), function( key, a ) {
			aOriginal.push( a );
		});
		var mapped = aOriginal.map(function( a, i ) {
			return { index: i, value: a };
		});
		mapped.sort(function(a, b) {
			return a.value[2] - b.value[2];
		});

		return mapped.map(function( o ){
			return aOriginal[ o.index ];
		});
	};
	BubbleTypeManager.prototype._initEvents = function() {
		var self = this;
		var oCheckBoxImageData = this.option("checkBoxImage");

		this._$elTypeUL.on( "mousedown", function (e) {
			e.stopPropagation();
		}).on( "click", "li", function( e ) {
			e.preventDefault();
			var $elThis = $(this);
			var type = $elThis.attr("data-type");
			var bChecked = $elThis.hasClass("unchecked") === false;
			$elThis[ bChecked ? "addClass" : "removeClass"]("unchecked").css({
				"background-image": "url(" + oCheckBoxImageData[ bChecked ? "unchecked" : "checked" ] + ")"
			});
			self._oCallback.onChange( type );
			self._oCallback.onSend( type, !bChecked );
		});
	};
	BubbleTypeManager.prototype.showTypeCount = function( oTypeCount ) {
		var self = this;
		$.each( oTypeCount, function (sKey, sVal) {
			self._oReferenceSpan[sKey].text( BigScatterChart2.Util.addComma( sVal ) );
		});
	};
	BubbleTypeManager.prototype.getVisibleType = function() {
		var a = [];
		$.each(this._oReferenceLI, function( sKey, $elTypeLI ) {
			if ( $elTypeLI.hasClass("unchecked") === false ) {
				a.push(sKey);
			}
		});
		return a;
	};
	BubbleTypeManager.prototype.selectType = function( type ) {
		var oCheckBoxImageData = this.option("checkBoxImage");
		$.each( this._oReferenceLI, function( key, $elLI ) {
			if ( key === type ) {
				$elLI.removeClass("unchecked").css("background-image", "url(" + oCheckBoxImageData.checked + ")");
			} else {
				$elLI.addClass("unchecked").css("background-image", "url(" + oCheckBoxImageData.unchecked + ")");
			}
		});
	};
	BubbleTypeManager.prototype.selectAll = function() {
		var oCheckBoxImageData = this.option("checkBoxImage");
		$.each( this._oReferenceLI, function( key, $elLI ) {
			$elLI.removeClass("unchecked").css("background-image", "url(" + oCheckBoxImageData.checked + ")");
		})
	};
	BubbleTypeManager.prototype.isChecked = function( type ) {
		return this._oReferenceLI[type].hasClass("unchecked") === false;
	};
	BubbleTypeManager.prototype.getElementLI = function() {
		return this._oReferenceLI;
	};

	global.BigScatterChart2.BubbleTypeManager = BubbleTypeManager;
})(window, jQuery);