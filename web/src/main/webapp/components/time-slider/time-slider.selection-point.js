(function(w, $) {
    var ts = w.TimeSlider;
    ts.SelectionPoint = function( timeSlider, svgGroup, options ) {
        this.timeSlider = timeSlider;
        this.group = svgGroup;
        this.opt = options;
        this._addElements();
        this.onMouseClick( options.x );
    };
    ts.SelectionPoint.prototype._addElements = function() {
        this.group.add(
            this.timeSlider.snap.line( 0, 0, 0, this.opt.height ),
            this.timeSlider.snap.circle( 0, this.opt.height / 2, this.opt.radius ).attr({
                filter: this.timeSlider.snap.filter( Snap.filter.shadow(0, 0, 4, "#FF0", 1))
            })
        );
    };
    ts.SelectionPoint.prototype.onMouseClick = function( x ) {
        this.group.animate({
            "transform": "translate(" + x +", " + this.opt.y + ")"
        }, this.opt.duration, mina.easein);
    };
})(window, jQuery);
