(function(w, $) {
    var consts = {
        SIZE: 30
    };
    var ts = w.TimeSlider;
    ts.LoadingIndicator = function( timeSlider, svgGroup, options ) {
        this.timeSlider = timeSlider;
        this.opt = options;
        this.group = svgGroup;
        this._bRunn = false;
        this._addElements();
        this.show();
    };
    ts.LoadingIndicator.prototype._addElements = function() {
        var halfSize = consts.SIZE / 2;
        var x = this.opt.width / 2 - halfSize;
        var y = this.opt.height / 2 - halfSize;
        this.group.add( this.timeSlider.snap.rect(0, 0, this.opt.width, this.opt.height) );
        this.elClockwiseBox = this.timeSlider.snap.rect(x, y, consts.SIZE, consts.SIZE).attr({
            "stroke": "rgba(197, 197, 197, .9)"
        });
        this.elAnitclockwiseBox = this.timeSlider.snap.rect(x, y, consts.SIZE, consts.SIZE).attr({
            "stroke": "rgba(239, 246, 105, .9)"
        });
        this.group.add( this.elClockwiseBox, this.elAnitclockwiseBox );
    };
    ts.LoadingIndicator.prototype.show = function() {
        this.group.attr("display", "block");
        this._bRunn = true;
        this._animate( this.elClockwiseBox, 0, 360, mina.easeout );
        this._animate( this.elAnitclockwiseBox, 45, -315, mina.easein );
    };
    ts.LoadingIndicator.prototype._animate = function( ele, from, to, fnEase ) {
        var self = this;
        Snap.animate( from, to, function(val) {
            ele.attr("transform", "rotate(" + val + "deg)");
        }, self.opt.duration, fnEase, function() {
            if ( self._bRunn === true ) {
                self._animate( ele, to, from, fnEase );
            }
        });
    };
    ts.LoadingIndicator.prototype.hide = function() {
        this.group.attr("display", "none");
        this._bRunn = false;
    };
})(window, jQuery);
