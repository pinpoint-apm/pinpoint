(function(w, $) {
    var consts = {
        HANDLER_IMAGE_WIDTH: 30,
        HANDLER_IMAGE_HEIGHT: 18
    };
    var ts = w.TimeSlider;
    ts.Handler = function( timeSlider, svgGroup, options, callbackStart, callbackDrag, callbackEnd ) {
        this.timeSlider = timeSlider;
        this.group = svgGroup;
        this.opt = options;
        this._previousX = options.x;
        this._handleSrc = options.handleSrc || consts.NORMAL_IMG;
        this._handleDownSrc = this._getHandleDownSrc();
        this.callbackStart = callbackStart;
        this.callbackDrag = callbackDrag;
        this.callbackEnd = callbackEnd;
        this._addElements();
        this.setX( options.x );
        this.addEvent();
    };
    ts.Handler.prototype._getHandleDownSrc = function() {
        var lastIndex = this._handleSrc.lastIndexOf( "." );
        return this._handleSrc.substring( 0 , lastIndex ) + "_down" + this._handleSrc.substring( lastIndex );
    };
    ts.Handler.prototype._addElements = function() {
        //this.handlerGrip = this.timeSlider.snap.image( this._handleSrc, -(consts.HANDLER_IMAGE_WIDTH / 2), -(consts.HANDLER_IMAGE_HEIGHT / 2), consts.HANDLER_IMAGE_WIDTH, consts.HANDLER_IMAGE_HEIGHT );
		this.handlerGrip = this.timeSlider.snap.circle( 0, 3, 5 ).attr({
			"fill": "#777af9",
			"cursor": "pointer",
			"stroke": "#4E50C8",
			"stroke-width": "3px"
		});
        this.handlerGroup = this.group.g();
        this.handlerGroup.add(
            this.timeSlider.snap.line( 0, 0, 0, this.opt.height ),
			this.timeSlider.snap.circle( 0, 3, 7 ).attr({
				"fill": "#000",
				"filter": this.timeSlider.snap.filter( Snap.filter.shadow(0, 0, 2, "#000", .5))
			}),
            this.handlerGrip
        );
    };
    ts.Handler.prototype.addEvent = function( callback ) {
        var self = this;
        var lastX = -1;
        // onmove, onstart, onend
        this.handlerGrip.mousedown(function(event, x, y) {
            event.stopPropagation();
        });
        this.handlerGrip.click(function(event, x, y) {
            event.stopPropagation();
        });
        this.handlerGrip.drag(function(dx, dy, x, y, event) {
            var newX = x - self.opt.margin;
            if ( self._isInRestrictionZone( newX ) === false ) return;
            self.handlerGroup.attr({ transform: "translate(" + newX + ", 0)" });
            lastX = newX;
            self.callbackDrag( newX );
        }, function(x, y, event) {
            event.stopPropagation();
            //self.handlerGrip.attr( "href", self._handleDownSrc );
            self.callbackStart( x - self.opt.margin );
        }, function(event) {
            event.stopPropagation();
            //self.handlerGrip.attr( "href", self._handleSrc );
            if ( self._previousX !== lastX && lastX !== -1 ) {
                self.callbackEnd( true, lastX );
                self._previousX = lastX;
            } else {
                self.callbackEnd( false );
            }
        });
    };
    ts.Handler.prototype.setX = function( x ) {
        this.handlerGroup.animate({
            "transform": "translate(" + x + ", 0)"
        }, this.opt.duration, mina.easeout);
    };
    ts.Handler.prototype._isInRestrictionZone = function( x ) {
        return ( x <= this.opt.zone[0] || x >= this.opt.zone[1] ) ? false : true;
    };
    ts.Handler.prototype.setZone = function( start, end ) {
        this.opt.zone = [start, end];
    };
    ts.Handler.prototype.setPositionAndZone = function( x, aZone ) {
        this.setX( x );
        this.callbackDrag( x );
        this.setZone( aZone[0], aZone[1] );
    };

})(window, jQuery);
