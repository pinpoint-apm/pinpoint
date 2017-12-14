(function(w, $) {
    var ts = w.TimeSlider;
    ts.Events = function( timeSlider, svgGroup, options ) {
        this.timeSlider = timeSlider;
        this.group = svgGroup;
        this._oTimelineData = timeSlider.oTimelineData;

        this._init(options);
        this._addEventElements();
        this._addEvents();
    };
    ts.Events.prototype._init = function( options ) {
        this._aEventGroupElement = [];
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
        var len = this._oTimelineData.eventCount();
        for ( var i = 0 ; i < len ; i++ ) {
            this._addEventElement( this._oTimelineData.getEventDataByIndex(i), i );
        }
    };
    ts.Events.prototype._addEventElement = function( oEvent, index ) {
        var el = this._makeElement( oEvent, index );
        this.group.append( el );
        return el;
    };
    ts.Events.prototype._makeElement = function( oEvent, index ) {
        var opt = this.opt;
        var time = oEvent.startTimestamp + ( oEvent.endTimestamp - oEvent.startTimestamp ) / 2;
		var oTextInfo = this._getEventTextInfo( oEvent.value.totalCount );

        var elEventGroup = this.group.g().attr({
            "data-id": index,
            "data-time": time,
            "transform": "translate(" + this.timeSlider.oPositionManager.getPositionFromTime( time ) + ",0)"
        }).add(
            this.timeSlider.snap.line( 0, opt.y, 0, opt.y + opt.barLength ),
            this.timeSlider.snap.circle( 0, opt.y + opt.circleRadius + opt.gapBarNCircle + opt.barLength, opt.circleRadius ).attr({
				"fill": "#bdb76b",
                "class": "event",
                "filter": this._filterShadow,
                "data-time": time
            }),
			this.timeSlider.snap.text( oTextInfo.x, oTextInfo.y, oTextInfo.text ).attr({
				"fill": "#FFF",
				"class": "event",
				"font-size": "11px"
			})
        );
        this._aEventGroupElement.push(elEventGroup);
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
	ts.Events.prototype._getEventTextInfo = function( totalCount ) {
		return {
			x: totalCount < 10 ? -(this.opt.circleRadius/3) : -(this.opt.circleRadius/4) * 3,
			y: this.opt.y + this.opt.circleRadius + (this.opt.circleRadius/2) + this.opt.gapBarNCircle + this.opt.barLength,
			text: totalCount >= 100 ? "..." : totalCount
		};
	};
    ts.Events.prototype._fireEvent = function( eventType, event, x, y ) {
        this.timeSlider.fireEvent(eventType, [x, y, this._oTimelineData.getEventDataByIndex( parseInt(event.srcElement.parentNode.getAttribute("data-id")) )] );
    };
    ts.Events.prototype.reset = function() {
        var oldLen = this._aEventGroupElement.length;
        var newLen = this._oTimelineData.eventCount();

        if ( oldLen === newLen ) {
			for( var i = 0 ; i < newLen ; i++ ) {
				this.reposition(this._aEventGroupElement[i], i);
			}
		} else if ( oldLen > newLen ) {
			for( var i = newLen ; i < oldLen ; i++ ) {
				this.hide( this._aEventGroupElement[i] );
			}
			for( var i = 0 ; i < newLen ; i++ ) {
				this.reposition(this._aEventGroupElement[i], i);
			}
		} else { // oldLen < newLen
			for( var i = 0 ; i < oldLen ; i++ ) {
				this.reposition(this._aEventGroupElement[i], i);
			}
			for( var i = oldLen ; i < newLen ; i++ ) {
				this._addEventElement( this._oTimelineData.getEventDataByIndex(i), i );
			}
		}
    };
	ts.Events.prototype.reposition = function( elEventGroup, index ) {
		var oEvent = this._oTimelineData.getEventDataByIndex(index);
		var time = oEvent.startTimestamp + ( oEvent.endTimestamp - oEvent.startTimestamp ) / 2;
		var x = this.timeSlider.oPositionManager.getPositionFromTime( time );
		var oTextInfo = this._getEventTextInfo( oEvent.value.totalCount );
		elEventGroup[2].attr({
			x: oTextInfo.x,
			y: oTextInfo.y,
			text: oTextInfo.text
		});
		this.show(elEventGroup);
		elEventGroup.animate({
			"transform": "translate(" + x + ",0)"
		}, this.opt.duration);
	};
    ts.Events.prototype.changeData = function() {
    	this.emptyData();
        this.reset();
    };
    ts.Events.prototype.emptyData = function() {
    	var self = this;
		this._aEventGroupElement.forEach(function(el) {
			self.hide(el);
		});
    };
    ts.Events.prototype.show = function( el ) {
        el.attr("display", "block");
    };
    ts.Events.prototype.hide = function( el ) {
        el.attr("display", "none");
    };

})(window, jQuery);
