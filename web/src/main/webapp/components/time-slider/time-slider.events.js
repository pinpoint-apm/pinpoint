(function(w, $) {
    var consts = {
        ID_POSTFIX: "+event-circle",
        ID_SPLITER: "+"
    };
    var ts = w.TimeSlider;
    ts.Events = function( timeSlider, svgGroup, options ) {
        this.timeSlider = timeSlider;
        this.group = svgGroup;
        this._oEventData = timeSlider.oEventData;

        this._init(options);
        this._addEventElements();
        this._addEvents();
    };
    ts.Events.prototype._init = function( options ) {
        this._oEventGroupElementHash = {};
        this.opt = {
            "y": 4,
            "barLength": 4,
            "gapBarNCircle": 2,
            "circleRadius": 8
        };
        for( var p in options ) {
            this.opt[p] = options[p];
        }
        this._filterShadow = this.timeSlider.snap.filter( Snap.filter.shadow(1, 1, 1, "#000", 0.3));
    };
    ts.Events.prototype._addEventElements = function() {
        var len = this._oEventData.count();
        for ( var i = 0 ; i < len ; i++ ) {
            this._addEventElement( this._oEventData.getDataByIndex(i), true );
        }
    };
    ts.Events.prototype._addEventElement = function( oEvent, bAppend ) {
        var el = this._makeElement( oEvent );
        this.group[bAppend ? "append": "prepend"]( el );
        if ( this.timeSlider.oPositionManager.isInSliderTimeSeries( oEvent.eventTimestamp ) === false ) {
            this.hide( el );
        }
        return el;
    };
    ts.Events.prototype._makeID = function( oEvent ) {
        return this._oEventData.makeID( oEvent ) + consts.ID_POSTFIX;
    };
    ts.Events.prototype._makeElement = function( oEvent ) {
        var opt = this.opt;
        var time = oEvent.eventTimestamp;
        var groupID = this._makeID( oEvent );
        var elEventGroup = this.group.g().attr({
            "data-id": groupID,
            "data-time": time,
            "transform": "translate(" + this.timeSlider.oPositionManager.getPositionFromTime( time ) + ",0)"
        }).add(
            this.timeSlider.snap.line( 0, opt.y, 0, opt.y + opt.barLength ),
            this.timeSlider.snap.circle( 0, opt.y + opt.circleRadius + opt.gapBarNCircle + opt.barLength, opt.circleRadius ).attr({
                "fill": TimeSlider.EventColor[oEvent.eventTypeCode],
                "class": "event",
                "filter": this._filterShadow,
                "data-time": time
            })
        );
        this._oEventGroupElementHash[groupID] = elEventGroup;
        return elEventGroup;
    };
    ts.Events.prototype._addEvents = function() {
        var self = this;
        this.group.mouseover(function(event, x, y) {
            self._fireEvent( "inEvent", event, x, y );
        });
        this.group.mouseout(function(event, x, y) {
            self._fireEvent( "outEvent", event, x, y );
        });
        this.group.click(function(event, x, y) {
            self._fireEvent( "clickEvent", event, x, y );
        });
    };
    ts.Events.prototype._fireEvent = function( eventType, event, x, y ) {
        this.timeSlider.fireEvent(eventType, [x, y, this._oEventData.getDataByKey( event.srcElement.parentNode.getAttribute("data-id").split(consts.ID_SPLITER)[0] )] );
    };
    ts.Events.prototype.reset = function() {
        var self = this;
        for ( var p in this._oEventGroupElementHash ) {
            var elGroupEvent = this._oEventGroupElementHash[p];
            var time = this._oEventData.getDataByKey(p.split(consts.ID_SPLITER)[0]).eventTimestamp;

            if ( self.timeSlider.oPositionManager.isInSliderTimeSeries( time ) ) {
                self.show( elGroupEvent );
                (function( el, x ) {
                    el.animate({
                        "transform": "translate(" + x + ",0)"
                    }, self.opt.duration);
                })(elGroupEvent, this.timeSlider.oPositionManager.getPositionFromTime( time ));
            } else {
                (function( el, x ) {
                    el.animate({
                        "transform": "translate(" + x + ",0)"
                    }, self.opt.duration, function() {
                        self.hide( el );
                    });
                })(elGroupEvent, self.timeSlider.oPositionManager.isBeforeSliderStartTime( time ) ? 0 : self.timeSlider.oPositionManager.getSliderEndPosition() );
            }
        }
    };
    ts.Events.prototype.changeData = function() {
        var aGroupElements = this.group.selectAll("g");
        if ( aGroupElements.length === 0 ) {
            this._addEventElements();
            return;
        }
        var i, j, oEvent, skip = 0, lenData = this._oEventData.count(), lenElements = aGroupElements.length;
        var lastElement = aGroupElements[lenElements - 1];

        for( i = 0 ; i < lenElements ; i++ ) {
            var el = aGroupElements[i];
            var timestamp = parseInt( el.attr("data-time") );

            for( j = skip ; j < lenData ; j++ ) {
                oEvent = this._oEventData.getDataByIndex(j);
                if ( oEvent.eventTimestamp < timestamp ) {
                    el.before( this._makeElement( oEvent ) );
                    skip = j;
                } else if ( oEvent.eventTimestamp === timestamp ) {
                    skip = j + 1;
                    lastElement = el;
                    break;
                } else {
                    skip = j;
                    lastElement = el;
                    break;
                }
            }
        }
        for( skip ; skip < lenData ; skip++ ) {
            oEvent = this._oEventData.getDataByIndex(skip);
            var newEl = this._makeElement( oEvent );
            lastElement.after( newEl );
            lastElement = newEl;
        }
        this.reset();
    };
    ts.Events.prototype.emptyData = function() {
        for ( var p in this._oEventGroupElementHash ) {
            this._oEventGroupElementHash[p].remove();
        }
        this._oEventGroupElementHash = {};
    };
    ts.Events.prototype.show = function( el ) {
        el.attr("display", "block");
    };
    ts.Events.prototype.hide = function( el ) {
        el.attr("display", "none");
    };

})(window, jQuery);
