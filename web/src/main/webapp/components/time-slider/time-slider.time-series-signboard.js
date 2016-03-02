(function(w, $) {
    var ts = w.TimeSlider;
    ts.TimeSeriesSignboard = function( timeSlider, svgGroup, options ) {
        this.timeSlider = timeSlider;
        this.group = svgGroup;
        this.opt = options;
        this._init();
        this._addElements();
    };
    ts.TimeSeriesSignboard.prototype._init = function() {
        this._halfHeight = this.opt.height - 12;
    };
    ts.TimeSeriesSignboard.prototype._addElements = function() {
        this._elStartTime = this.group.text(0, this._halfHeight, this.timeSlider.oPositionManager.getSliderStartTimeStr() ).attr({
            "text-anchor": "start"
        });
        this._elEndTime = this.group.text(this.timeSlider.oPositionManager.getSliderEndPosition(), this._halfHeight, this.timeSlider.oPositionManager.getSliderEndTimeStr() ).attr({
            "text-anchor": "end"
        });
        this.group.add( this._elStartTime, this._elEndTime );
    };
    ts.TimeSeriesSignboard.prototype.reset = function() {
        this._elStartTime.attr("text", this.timeSlider.oPositionManager.getSliderStartTimeStr() );
        this._elEndTime.attr("text", this.timeSlider.oPositionManager.getSliderEndTimeStr() );
    };
    ts.TimeSeriesSignboard.prototype.resize = function() {
        var self = this;
        this._elEndTime.animate({
            "x": self.timeSlider.oPositionManager.getSliderEndPosition()
        }, this.opt.duration, function() {
            self._elEndTime.attr("text", self.timeSlider.oPositionManager.getSliderEndTimeStr() );
        });
    };
})(window, jQuery);
