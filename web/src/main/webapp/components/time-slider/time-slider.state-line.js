(function(w, $) {
	var ID_SPLITER = "+";
	var ID_POSTFIX = ID_SPLITER + "state-line";

    var ts = w.TimeSlider;
    ts.StateLine = function( timeSlider, svgGroup, options ) {
        this.timeSlider = timeSlider;
        this.group = svgGroup;
        this.opt = options;
        this._oTimelineData = timeSlider.oTimelineData;

        this._init();
        this._addBaseLine();
        this._addEventElements();
        this._resetBaseLineColor();
    };
    ts.StateLine.prototype._init = function() {
        this._filterShadow = this.timeSlider.snap.filter( Snap.filter.shadow(0, 1, 0.5, "#000", 0.2));
        this._aLineElement = [];
    };
    ts.StateLine.prototype._addBaseLine = function() {
        this._aBaseLine = [
			this._makeRect( 0, this.opt.width, this.opt.topLineY, this.opt.bottomLineY - this.opt.topLineY, TimeSlider.StatusColor["BASE"], "base-" + Date.now() )
		];
		this.group.add( this._aBaseLine[0] );
    };
    ts.StateLine.prototype._addEventElements = function( ) {
        var len = this._oTimelineData.count();
        for( var i = 0 ; i < len ; i++ ) {
            this._addEventElement( this._oTimelineData.getDataByIndex(i), i );
        }
    };
    ts.StateLine.prototype._addEventElement = function( oEvent, index ) {
		if ( this.timeSlider.oPositionManager.isInSliderTimeSeries( oEvent.startTimestamp ) === false && this.timeSlider.oPositionManager.isInSliderTimeSeries( oEvent.endTimestamp ) === false ) return;
		this._addLine(
			this.timeSlider.oPositionManager.getPositionFromTime( oEvent.startTimestamp ),
			this._getX2( oEvent ),
			oEvent
		);
		// if ( index === 0 ) {
		// 	this._resetBaseLineColor( TimeSlider.StatusColor["BASE"] );
		// }
    };
    ts.StateLine.prototype._makeID = function( oEvent ) {
        return this._oTimelineData.makeID( oEvent ) + ID_POSTFIX;
    };
    ts.StateLine.prototype._addLine = function( x, x2, oEvent ) {
        var lineID = this._makeID( oEvent );
		var elRect = this._makeRect( x, x2, this.opt.topLineY, this.opt.bottomLineY - this.opt.topLineY, TimeSlider.StatusColor[oEvent.value], lineID );
		this._aLineElement.push(elRect);
		this.group.add( elRect );
    };
	ts.StateLine.prototype._makeRect = function( x, x2, y, y2, color, id ) {
		return  this.timeSlider.snap.rect( x, y, x2, y2 ).attr({
			"fill": color,
			"data-id": id
		});
	};
    ts.StateLine.prototype._getX2 = function( oEvent ) {
        return this.timeSlider.oPositionManager.getPositionFromTime( oEvent.endTimestamp == -1 ? this.timeSlider.oPositionManager.getSliderEndTime() : oEvent.endTimestamp );
    };
    ts.StateLine.prototype.changeData = function() {
    	var curLen = this._aLineElement.length;
        var newLen = this._oTimelineData.count();
        if ( curLen === newLen ) {
			for( var i = 0 ; i < curLen ; i++ ) {
				this.show( this._aLineElement[i] );
			}
		} else if ( curLen > newLen ) {
			for( var i = newLen ; i < curLen ; i++ ) {
				this.hide( this._aLineElement[i] );
			}
		} else { // curLen < newLen
			for( var i = 0 ; i < curLen ; i++ ) {
				this.show( this._aLineElement[i] );
			}
			for( var i = curLen ; i < newLen ; i++ ) {
				var oEvent = this._oTimelineData.getDataByIndex(i);
				this._addEventElement( oEvent, i );
			}
		}
        this.reset();
    };
    ts.StateLine.prototype.emptyData = function() {
    	var self = this;
    	this._aLineElement.forEach(function(line) {
    		self.hide( line );
		});
    };
    ts.StateLine.prototype.setDefaultStateLineColor = function( color ) {
        this._defaultBaseColor = color ;
        // if ( this._hasDurationData === true ) return;
		this._resetBaseLineColor(this._defaultBaseColor);
    };
    ts.StateLine.prototype._resetBaseLineColor = function( baseColor ) {
    	baseColor = baseColor ||  ts.StatusColor.BASE;
        // var self = this;
        this._aBaseLine.forEach(function( elLine ) {
			// elLine.attr("fill", self._hasDurationData === true ? baseColor : self._defaultBaseColor );
			elLine.attr("fill", baseColor);
        });
    };
    ts.StateLine.prototype.reset = function() {
    	var self = this;
		var oPM = this.timeSlider.oPositionManager;
    	for( var i = 0 ; i < this._aLineElement.length ; i++ ) {
			var elLine = this._aLineElement[i];
			var oEvent = this._oTimelineData.getDataByIndex(i);
			if ( oEvent ) {
				this.show(elLine);
				elLine.animate({
					"x": oPM.getPositionFromTime(oEvent.startTimestamp),
					"fill": TimeSlider.StatusColor[oEvent.value],
					"width": this._getX2(oEvent) - oPM.getPositionFromTime(oEvent.startTimestamp)
				}, this.opt.duration);
			} else {
				this.hide(elLine);
			}
		}
        this._aBaseLine.forEach(function( elBase ) {
            elBase.animate({
				"width": oPM.getSliderEndPosition()
            }, self.opt.duration);
        });
    };
    ts.StateLine.prototype.show = function( el ) {
        el.attr("display", "block");
    };
    ts.StateLine.prototype.hide = function( el ) {
        el.attr("display", "none");
    };
})(window, jQuery);
