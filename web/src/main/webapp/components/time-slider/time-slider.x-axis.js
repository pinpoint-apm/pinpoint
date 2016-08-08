(function(w, $) {
    w.TimeSlider.XAxis = function( timeSlider, svgGroup, options ) {
        this.timeSlider = timeSlider;
        this.group = svgGroup;
        this.opt = options;
        this.aSubGroup = [];
        this.aText = [];
        this.init();
    };
    w.TimeSlider.XAxis.prototype.init = function() {
        var aXBarPosition = this.timeSlider.oPositionManager.getXBarPosition();
        var halfX = this.opt.width / 2;
        var self = this;
        for( var i = 0 ; i < aXBarPosition.length ; i++ ) {
            var g = self.group.g();
            self.aText.push( self.timeSlider.snap.text(0, self.opt.textY, aXBarPosition[i].time) );
            g.attr("transform", "translate(" + halfX + ", 0)");
            g.add(
                self.aText[i],
                self.timeSlider.snap.line(0, self.opt.startY, 0, self.opt.endY)
            );
            self.group.add( g );
            self.aSubGroup.push( g );
            self.setX( i, aXBarPosition[i].x, halfX );
        }
    };
    w.TimeSlider.XAxis.prototype.setX = function( index, x, startX ) {
        var ele = this.aSubGroup[index];
        if ( arguments.length === 3 ) {
            Snap.animate( startX, x, function( val ) {
                ele.attr("transform", "translate(" + val + ", 0)");
            }, this.opt.duration);
        } else {
            ele.attr("transform", "translate(" + x + ", 0)");
        }
    };
    w.TimeSlider.XAxis.prototype.reset = function() {
        var self = this;
        var aYBarPosition = this.timeSlider.oPositionManager.getXBarPosition();
        var halfX = this.opt.width / 2;
        for ( var i = 0 ; i < aYBarPosition.length ; i++ ) {
            self.aSubGroup[i].attr( "transform", "translate(" + aYBarPosition[i].x + ",0)");
            self.aText[i].attr("text", aYBarPosition[i].time );
        }
    };
})(window, jQuery);
