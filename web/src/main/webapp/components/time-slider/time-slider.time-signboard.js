(function(w, $) {
    var ts = w.TimeSlider;
    ts.TimeSignboard = function( timeSlider, svgGroup, options ) {
        this.timeSlider = timeSlider;
        this.group = svgGroup;
        this.opt = options;
        this._textMaxWidth = 100;
        this._xPadding = 10;
        this._addElements();
    };
    ts.TimeSignboard.prototype._addElements = function() {
        var isIn = this._isIn( this.opt.x );
        var x = this.opt.x + ( isIn ? this._xPadding : -this._xPadding );
        this.timeText = this.group.text(x, 26, this.timeSlider.oPositionManager.getFullTimeStr(this.opt.x) ).attr({
            "textAnchor": isIn ? "start" : "end"
        });
        this.group.add( this.timeText );
    };
    ts.TimeSignboard.prototype.setX = function( x ) {
        var isIn = this._isIn( x );
        this.timeText.attr({
            "x": x + ( isIn ? this._xPadding : -this._xPadding ),
            "text": this.timeSlider.oPositionManager.getFullTimeStr(x),
            "textAnchor": isIn ? "start" : "end"
        });

    };
    ts.TimeSignboard.prototype._isIn = function( x ) {
        if ( this.opt.direction == "left" ) {
            return x < this._textMaxWidth;
        } else {
            return x + this._textMaxWidth < this.timeSlider.oPositionManager.getSliderEndPosition();
        }
    };
    ts.TimeSignboard.prototype.onDragStart = function( x ) {
        this.setX( x );
    };
    ts.TimeSignboard.prototype.onDragEnd = function() {
    };
    ts.TimeSignboard.prototype.onDrag = function( x ) {
        this.setX( x );
    };
})(window, jQuery);
