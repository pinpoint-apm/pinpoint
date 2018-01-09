(function(w, $) {
	var ts = w.TimeSlider;
	ts.Focus = function( timeSlider, svgGroup, options ) {
		this.timeSlider = timeSlider;
		this.group = svgGroup;
		this.opt = options;
		this._addElements();
	};
	ts.Focus.prototype._addElements = function() {
		this.group.add(
			this.timeSlider.snap.line( 0, 0, 0, this.opt.height )
		).attr("display", "none");
	};
	ts.Focus.prototype.hide = function() {
		this.group.attr("display", "none");
	};
	ts.Focus.prototype.show = function( x ) {
		if ( this.group.attr("display") === "none" ) {
			this.group.attr("display", "block");
		}
		this.group.animate({
			"transform": "translate(" + x +", " + this.opt.y + ")"
		}, this.opt.duration, mina.easein);
	};
})(window, jQuery);
