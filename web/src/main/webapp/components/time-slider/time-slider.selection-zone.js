(function(w, $) {
    var ts = w.TimeSlider;
    ts.SelectionZone = function( timeSlider, svgGroup, options ) {
        this.timeSlider = timeSlider;
        this.group = svgGroup;
        this.opt = options;
        this._addElements();
    };
    ts.SelectionZone.prototype._addElements = function() {
        var aSelectionZone = this.timeSlider.oPositionManager.getSelectionPosition();
        this.elZone = this.timeSlider.snap.rect(aSelectionZone[0], this.opt.top, aSelectionZone[1] - aSelectionZone[0], this.opt.height);
        this.group.add(this.elZone);
    };
    ts.SelectionZone.prototype.redraw = function() {
        var aSelectionZone = this.timeSlider.oPositionManager.getSelectionPosition();
        this.elZone.animate({
            "x": aSelectionZone[0],
            "width": aSelectionZone[1] - aSelectionZone[0]
        }, this.opt.duration);
    };
	ts.SelectionZone.prototype.onDragXStart = function(x) {
		var aSelectionZone = this.timeSlider.oPositionManager.getSelectionPosition();
		this.elZone.attr({
			"x": x,
			"width": aSelectionZone[1] - x
		});
	};
	ts.SelectionZone.prototype.onDragXEnd = function(x) {
		var aSelectionZone = this.timeSlider.oPositionManager.getSelectionPosition();
		this.elZone.attr({
			"x": aSelectionZone[0],
			"width": x - aSelectionZone[0]
		});
	};
})(window, jQuery);
