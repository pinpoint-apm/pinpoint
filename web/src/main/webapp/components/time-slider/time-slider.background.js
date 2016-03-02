(function(w, $) {
    var ts = w.TimeSlider;
    ts.Background = function( timeSlider, svgGroup, options ) {
        this.timeSlider = timeSlider;
        this.opt = options;
        this.group = svgGroup;
        this._addElements();
    };
    ts.Background.prototype._addElements = function() {
        this.rect = this.timeSlider.snap.rect(this.opt.left, this.opt.top, this.opt.width, this.opt.height);
        this.group.add(this.rect);
    };
    ts.Background.prototype.reset = function() {
        this.rect.animate({
            "width": this.timeSlider.oPositionManager.getSliderEndPosition()
        }, this.opt.duration, mina.easein);
    };
})(window, jQuery);
