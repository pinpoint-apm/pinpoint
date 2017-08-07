(function(w, $) {
    var ts = w.TimeSlider;
    ts.SelectionManager = function( timeSlider, options ) {
        this.timeSlider = timeSlider;
        this.opt = options;
        this.initClass();
    };
    ts.SelectionManager.prototype.initClass = function() {
        var self = this;
        var aHandlerPosition = this.timeSlider.oPositionManager.getSelectionPosition();
        this.oSelectionZone = new TimeSlider.SelectionZone( this.timeSlider, this.timeSlider.getGroup("selection-zone", TimeSlider.GROUP_TYPE.CONTENT_BASE, TimeSlider.oDrawOrder["selection-zone"]), {
            "top": 0,
            "left": aHandlerPosition[0],
            "width": aHandlerPosition[1] - aHandlerPosition[0],
            "height": this.opt.contentZoneHeight,
            "duration": this.opt.duration
        });
        this.oSelectionPoint = new TimeSlider.SelectionPoint( this.timeSlider, this.timeSlider.getGroup("selection-point", TimeSlider.GROUP_TYPE.CONTENT_BASE, TimeSlider.oDrawOrder["selection-point"]), {
            "y": this.opt.headerZoneHeight,
            "x": this.timeSlider.oPositionManager.getSelectPosition(),
            "radius": this.opt.selectionPointRadius,
            "height": this.opt.contentZoneHeight,
            "duration": this.opt.duration,
            "eventStartY": this.opt.headerZoneHeight,
            "eventEndY": this.opt.height - this.opt.eventZoneHeight
        });
        this.oLeftHandler = new TimeSlider.Handler( this.timeSlider, this.timeSlider.getGroup("left-handler", TimeSlider.GROUP_TYPE.CONTENT_BASE, TimeSlider.oDrawOrder["left-handler"]), {
            "x": aHandlerPosition[0],
            "zone": [0, aHandlerPosition[1]],
            "height": this.opt.contentZoneHeight,
            "margin": this.opt.margin,
            "duration": this.opt.duration,
            "handleSrc": this.opt.handleSrc
        }, function( x ) {

            self.oLeftTimeSignboard.onDragStart( x );
        }, function( x ) {
			self.oSelectionZone.onDragXStart(x);
            self.oLeftTimeSignboard.onDrag( x );
        }, function( bIsDraged, x ) {
            self.oLeftTimeSignboard.onDragEnd();
            if ( bIsDraged ) {
                self.movedLeftHandler( x );
            }
        });
        this.oRightHandler = new TimeSlider.Handler( this.timeSlider, this.timeSlider.getGroup("right-handler", TimeSlider.GROUP_TYPE.CONTENT_BASE, TimeSlider.oDrawOrder["right-handler"]), {
            "x": aHandlerPosition[1],
            "zone": [ aHandlerPosition[0], this.timeSlider.oPositionManager.getSliderEndPosition() ],
            "height": this.opt.contentZoneHeight,
            "margin": this.opt.margin,
            "duration": this.opt.duration,
            "handleSrc": this.opt.handleSrc
        }, function( x ) {
            self.oRightTimeSignboard.onDragStart( x );
        }, function( x ) {
        	self.oSelectionZone.onDragXEnd(x);
            self.oRightTimeSignboard.onDrag( x );
        }, function( bIsDraged, x ) {
            self.oRightTimeSignboard.onDragEnd();
            if ( bIsDraged ) {
                self.movedRightHandler( x );
            }
        });
        this.oLeftTimeSignboard = new TimeSlider.TimeSignboard( this.timeSlider, this.timeSlider.getGroup("time-left-signboard", TimeSlider.GROUP_TYPE.CONTENT_BASE, TimeSlider.oDrawOrder["time-signboard"]), {
            "x": aHandlerPosition[0],
            "direction": "left"
        });
        this.oRightTimeSignboard = new TimeSlider.TimeSignboard( this.timeSlider, this.timeSlider.getGroup("time-right-signboard", TimeSlider.GROUP_TYPE.CONTENT_BASE, TimeSlider.oDrawOrder["time-signboard"]), {
            "x": aHandlerPosition[1],
            "direction": "right"
        });
    };
    ts.SelectionManager.prototype.movedLeftHandler = function( x ) {
        var aCurrentSelectionTimeSeries = this.timeSlider.oPositionManager.getSelectionTimeSeries();
        var newLeftTime = this.timeSlider.oPositionManager.getTimeFromPosition( x );
        if ( this.timeSlider.oPositionManager.isInMaxSelectionTimeSeries( newLeftTime, aCurrentSelectionTimeSeries[1] ) ) {
            this.oRightHandler.setZone( x, this.timeSlider.oPositionManager.getSliderEndPosition() );
            this.timeSlider.oPositionManager.setSelectionStartPosition( x );

			if ( this.timeSlider.oPositionManager.isInSelectionZone() === false ) {
				this.timeSlider.oPositionManager.setSelectTime( newLeftTime );
				this.oSelectionPoint.onMouseClick( x );
				this.timeSlider.fireEvent( "selectTime", newLeftTime );
			}
        } else {
            var aNewSelectionTimeSeries = this.timeSlider.oPositionManager.getNewSelectionTimeSeriesFromStart( newLeftTime );
            var newRightX = this.timeSlider.oPositionManager.getPositionFromTime( aNewSelectionTimeSeries[1] );
            this.oRightHandler.setZone( x, this.timeSlider.oPositionManager.getSliderEndPosition() );
            this.timeSlider.oPositionManager.setSelectionStartPosition( x );
            this.oRightHandler.setX( newRightX );
            this.oRightTimeSignboard.onDrag( newRightX );
            this.oLeftHandler.setZone( 0, newRightX );
            this.timeSlider.oPositionManager.setSelectionEndPosition( newRightX );

			if ( this.timeSlider.oPositionManager.isInSelectionZone() === false ) {
				this.timeSlider.oPositionManager.setSelectTime( aNewSelectionTimeSeries[1] );
				this.oSelectionPoint.onMouseClick( newRightX );
				this.timeSlider.fireEvent( "selectTime", aNewSelectionTimeSeries[1] );
			}
        }
        this.oSelectionZone.redraw();
        this._fireChangeZoneEvent();

    };
    ts.SelectionManager.prototype.movedRightHandler = function( x ) {
        var aCurrentSelectionTimeSeries = this.timeSlider.oPositionManager.getSelectionTimeSeries();
        var newRightTime = this.timeSlider.oPositionManager.getTimeFromPosition( x );
        if ( this.timeSlider.oPositionManager.isInMaxSelectionTimeSeries( aCurrentSelectionTimeSeries[0], newRightTime ) ) {
            this.oLeftHandler.setZone( 0, x );
            this.timeSlider.oPositionManager.setSelectionEndPosition( x );

			if ( this.timeSlider.oPositionManager.isInSelectionZone() === false ) {
				this.timeSlider.oPositionManager.setSelectTime( newRightTime );
				this.oSelectionPoint.onMouseClick( x );
				this.timeSlider.fireEvent( "selectTime", newRightTime );
			}
        } else {
            var aNewSelectionTimeSeries = this.timeSlider.oPositionManager.getNewSelectionTimeSeriesFromEnd( newRightTime );
            var newLeftX = this.timeSlider.oPositionManager.getPositionFromTime( aNewSelectionTimeSeries[0] );
            this.oLeftHandler.setZone( 0, x );
            this.timeSlider.oPositionManager.setSelectionEndPosition( x );
            this.oLeftHandler.setX( newLeftX );
            this.oLeftTimeSignboard.onDrag( newLeftX );
            this.oRightHandler.setZone( newLeftX, this.timeSlider.oPositionManager.getSliderEndPosition() );
            this.timeSlider.oPositionManager.setSelectionStartPosition( newLeftX );

            if ( this.timeSlider.oPositionManager.isInSelectionZone() === false ) {
				this.timeSlider.oPositionManager.setSelectTime( aNewSelectionTimeSeries[0] );
                this.oSelectionPoint.onMouseClick( newLeftX );
                this.timeSlider.fireEvent( "selectTime", aNewSelectionTimeSeries[0] );
            }
        }
        this.oSelectionZone.redraw();
        this._fireChangeZoneEvent();
    };
    ts.SelectionManager.prototype.moveSelectionAndHandler = function( x ) {
        var aNewSelectionZone = this.timeSlider.oPositionManager.getSelectionPosition();
        this.oLeftHandler.setPositionAndZone( aNewSelectionZone[0], [0, aNewSelectionZone[1] ] );
        this.oRightHandler.setPositionAndZone( aNewSelectionZone[1], [ aNewSelectionZone[0], this.timeSlider.oPositionManager.getSliderEndPosition() ] );
        this.oSelectionZone.redraw();
        this._fireChangeZoneEvent();
    };
    ts.SelectionManager.prototype.onMouseClick = function( event, x, y ) {
        if ( y > this.opt.headerZoneHeight && y < (this.opt.headerZoneHeight + this.opt.contentZoneHeight ) ) {
            this._setSelectTime( this.timeSlider.oPositionManager.getTimeFromPosition(x) );
        }
    };
    ts.SelectionManager.prototype._fireChangeZoneEvent = function() {
        var aNewSelectionZone = this.timeSlider.oPositionManager.getSelectionPosition();
        this.timeSlider.fireEvent( "changeSelectionZone", [ this.timeSlider.oPositionManager.getTimeFromPosition(aNewSelectionZone[0]), this.timeSlider.oPositionManager.getTimeFromPosition(aNewSelectionZone[1]) ] );
    };
    ts.SelectionManager.prototype.setSelectTime = function( time, bIsNow ) {
        if ( this.timeSlider.oPositionManager.isInSliderTimeSeries( time ) ) {
            this._setSelectTime( time );
        } else {
            this.timeSlider.oPositionManager.resetBySelectTime( time, bIsNow );
            this.timeSlider.reset();
			this._fireChangeZoneEvent();
            this.timeSlider.fireEvent( "selectTime", time );
        }
    };
    ts.SelectionManager.prototype._setSelectTime = function( time ) {
        this.timeSlider.oPositionManager.setSelectTime( time );
        if ( this.timeSlider.oPositionManager.isInSelectionZone() === false ) {
            this.timeSlider.oPositionManager.calcuSelectionZone();
            this.moveSelectionAndHandler();
        }
        this.oSelectionPoint.onMouseClick( this.timeSlider.oPositionManager.getSelectPosition() );
        this.timeSlider.fireEvent( "selectTime", time );
    };
    ts.SelectionManager.prototype.reset = function() {
        var aNewSelectionZone = this.timeSlider.oPositionManager.getSelectionPosition();
        this.oLeftHandler.setPositionAndZone( aNewSelectionZone[0], [0, aNewSelectionZone[1] ] );
        this.oRightHandler.setPositionAndZone( aNewSelectionZone[1], [ aNewSelectionZone[0], this.timeSlider.oPositionManager.getSliderEndPosition() ] );
        this.oSelectionZone.redraw();
        this.oSelectionPoint.onMouseClick( this.timeSlider.oPositionManager.getSelectPosition() );
    };
})(window, jQuery);
