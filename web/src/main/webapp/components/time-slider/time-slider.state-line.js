(function(w, $) {
    var consts = {
        AGENT_CONNECT: 10100,
        AGENT_SHUTDOWN: 10200,
        ID_POSTFIX: "+state-line",
        ID_SPLITER: "+"
    };
    var ts = w.TimeSlider;
    ts.StateLine = function( timeSlider, svgGroup, options ) {
        this.timeSlider = timeSlider;
        this.group = svgGroup;
        this.opt = options;
        this._oEventData = timeSlider.oEventData;

        this._init();
        this._addBaseLine();
        this._addEventElements();
        this._resetBaseLineColor();
    };
    ts.StateLine.prototype._init = function() {
        this._hasDurationData = false;
        this._filterShadow = this.timeSlider.snap.filter( Snap.filter.shadow(0, 1, 0.5, "#000", 0.2));
        this._oLineElementHash = {};
    };
    ts.StateLine.prototype._addBaseLine = function() {
        this._defaultBaseColor = TimeSlider.EventColor["base"];
        this._aBaseLine = [
			this._makeRect( 0, this.opt.width, this.opt.topLineY, this.opt.bottomLineY - this.opt.topLineY, "base", "base-" + Date.now() )
			// this._makeLine( 0, this.opt.width, this.opt.topLineY, "base", "base-" + Date.now() )//,
			// this._makeLine( 0, this.opt.width, this.opt.bottomLineY, "base", "base-" + Date.now() )
		];
        //this.group.add( this._aBaseLine[0], this._aBaseLine[1] );
		this.group.add( this._aBaseLine[0] );
    };
    ts.StateLine.prototype._addEventElements = function( ) {
        var len = this._oEventData.count();
        for( var i = 0 ; i < len ; i++ ) {
            this._addEventElement( this._oEventData.getDataByIndex(i), i );
        }
    };
    ts.StateLine.prototype._addEventElement = function( oEvent, index ) {
        if ( typeof oEvent.durationStartTimestamp !== "undefined" ) {
            if ( this.timeSlider.oPositionManager.isInSliderTimeSeries( oEvent.durationStartTimestamp ) === false && this.timeSlider.oPositionManager.isInSliderTimeSeries( oEvent.durationEndTimestamp ) === false ) return;
            this._hasDurationData = true;
            this._addLine(
                this.timeSlider.oPositionManager.getPositionFromTime( oEvent.durationStartTimestamp ),
                this._getX2( oEvent ),
                oEvent
            );
			if ( index === 0 ) {
				this._resetBaseLineColor( TimeSlider.EmptyEventColor[oEvent.eventTypeCode+ ""] );
			}
        }
    };
    ts.StateLine.prototype._makeID = function( oEvent ) {
        return this._oEventData.makeID( oEvent ) + consts.ID_POSTFIX;
    };
    ts.StateLine.prototype._addLine = function( x, x2, oEvent ) {
        var lineID = this._makeID( oEvent );
		var elRect = this._makeRect( x, x2, this.opt.topLineY, this.opt.bottomLineY - this.opt.topLineY, oEvent.eventTypeCode, lineID );
		// var elLineTop = this._makeLine( x, x2, this.opt.topLineY, oEvent.eventTypeCode, lineID );
        //var elLineBottom = this._makeLine( x, x2, this.opt.bottomLineY, oEvent.eventTypeCode, lineID );
        //this._oLineElementHash[lineID] = [ elLineTop, elLineBottom ];
        //this.group.add( elLineTop, elLineBottom );
		// this._oLineElementHash[lineID] = [ elLineTop ];
		// this.group.add( elLineTop );
		this._oLineElementHash[lineID] = [ elRect ];
		this.group.add( elRect );
    };
	ts.StateLine.prototype._makeRect = function( x, x2, y, y2, eventType, id ) {
		return  this.timeSlider.snap.rect( x, y, x2, y2 ).attr({
			//"filter": this._filterShadow,
			"fill": TimeSlider.EventColor[eventType],
			"data-id": id
		});
	};
    // ts.StateLine.prototype._makeLine = function( x, x2, y, eventType, id ) {
    //     return  this.timeSlider.snap.line( x, y, x2, y ).attr({
    //         //"filter": this._filterShadow,
    //         "stroke": TimeSlider.EventColor[eventType],
    //         "data-id": id,
    //         "strokeWidth": this.opt.thickness
    //     });
    // };
    ts.StateLine.prototype._hasEventData = function( id ) {
        return typeof this._oLineElementHash[id] === "undefined" ? false : true;
    };
    ts.StateLine.prototype._getX2 = function( oEvent ) {
        return this.timeSlider.oPositionManager.getPositionFromTime( oEvent.durationEndTimestamp == -1 ? this.timeSlider.oPositionManager.getSliderEndTime() : oEvent.durationEndTimestamp );
    };
    ts.StateLine.prototype.changeData = function() {
        var len = this._oEventData.count();
        for( var i = 0 ; i < len ; i++ ) {
            var oEvent = this._oEventData.getDataByIndex(i);
            if ( this._hasEventData( this._makeID( oEvent ) ) === false ) {
                this._addEventElement( oEvent, i );
            }
        }
        this.reset();
    };
    ts.StateLine.prototype.emptyData = function() {
        for( var p in this._oLineElementHash ) {
            var aLine = this._oLineElementHash[p];
            aLine[0].remove();
            //aLine[1].remove();
        }
        this._oLineElementHash = {};
        this._hasDurationData = false;
    };
    ts.StateLine.prototype.setDefaultStateLineColor = function( color ) {
        this._defaultBaseColor = color;
        if ( this._hasDurationData === true ) return;
		this._resetBaseLineColor();
    };
    ts.StateLine.prototype._resetBaseLineColor = function( baseColor ) {
        var self = this;
        this._aBaseLine.forEach(function( elLine ) {
            //elLine.attr("stroke", self._hasDurationData === true ? TimeSlider.EventColor["base"] : self._defaultBaseColor );
			elLine.attr("fill", self._hasDurationData === true ? baseColor : self._defaultBaseColor );
        });
    };
    ts.StateLine.prototype.reset = function() {
        var self = this;
        var oPM = this.timeSlider.oPositionManager;
		var lessDurationStartTime = Number.MAX_VALUE;
		var lessEventTypeCode = "";
        for( var p in this._oLineElementHash ) {
            var aLine = this._oLineElementHash[p];
            var oEvent = this._oEventData.getDataByKey(p.split(consts.ID_SPLITER)[0]);
            if ( oPM.isInSliderTimeSeries( oEvent.durationStartTimestamp ) || oPM.isInSliderTimeSeries( oEvent.durationEndTimestamp ) ) {
                aLine.forEach(function( elLine ) {
                    self.show( elLine );
                    // elLine.animate({
                    //     "x1": oPM.getPositionFromTime( oEvent.durationStartTimestamp ),
                    //     "x2": self._getX2( oEvent )
                    // }, self.opt.duration);
					elLine.animate({
					    "x": oPM.getPositionFromTime( oEvent.durationStartTimestamp ),
					    "width": self._getX2( oEvent ) - oPM.getPositionFromTime( oEvent.durationStartTimestamp )
					}, self.opt.duration);
                });
				if ( oEvent.durationStartTimestamp < lessDurationStartTime ) {
					lessDurationStartTime = oEvent.durationStartTimestamp;
					lessEventTypeCode = oEvent.eventTypeCode;
				}
            } else {
                aLine.forEach(function( elLine ) {
                    self.hide( elLine );
                });
            }
        }
        this._aBaseLine.forEach(function( elBase ) {
            elBase.animate({
                // "x2": oPM.getSliderEndPosition()
				"width": oPM.getSliderEndPosition()
            }, self.opt.duration);
        });
		if ( lessEventTypeCode != "" ) {
			this._resetBaseLineColor( TimeSlider.EmptyEventColor[lessEventTypeCode] );
		}
    };
    ts.StateLine.prototype.show = function( el ) {
        el.attr("display", "block");
    };
    ts.StateLine.prototype.hide = function( el ) {
        el.attr("display", "none");
    };
})(window, jQuery);
