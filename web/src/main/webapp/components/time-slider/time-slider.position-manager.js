(function(w, $) {
    var consts = {
        xAxisTicks: 5
    };
    var ts = w.TimeSlider;
    ts.PositionManager = function( options ) {
        this._width = options.width;
        this._minSliderTimeSeries = options.minSliderTimeSeries;
        this._maxSelectionTimeSeries = options.maxSelectionTimeSeries;

        this._initInnerVar();
        this.setSliderTimeSeries( options.sliderTimeSeries[0], options.sliderTimeSeries[1] );
        this._initSelectionTimeSeries( options.handleTimeSeries || [] );
        this._resetSelectionByTime();
        this._xAxisTicks = options.xAxisTicks || consts.xAxisTicks;
        this.setSelectTime( options.selectTime );
    };
    ts.PositionManager.prototype._initInnerVar = function() {
        this._aSelectionTimeSeries = [];
        this._aSelectionPosition = [];
    };
    ts.PositionManager.prototype.setSliderTimeSeries = function( start, end ) {
        this._startTime = start;
        this._endTime = end;
        this._calcuTimePerPoint();
    };
    ts.PositionManager.prototype._initSelectionTimeSeries = function( aTime ) {
        if ( aTime.length != 2 ) {
            aTime = [ this._startTime, this._endTime ]; // TODO ??
        }
        if ( aTime[1] - aTime[0] > this._maxSelectionTimeSeries ) {
            aTime[0] = aTime[1] - this._maxSelectionTimeSeries;
        }
        this._setSelectionTimeSeries( aTime[0], aTime[1] );
    };
    ts.PositionManager.prototype._setSelectionTimeSeries = function( start, end ) {
        this._aSelectionTimeSeries[0] = start === null ? this._aSelectionTimeSeries[0] : start;
        this._aSelectionTimeSeries[1] = end === null ? this._aSelectionTimeSeries[1] : end;
    };
    ts.PositionManager.prototype._resetSelectionByTime = function() {
        this._setSelectionPosition( this.getPositionFromTime( this._aSelectionTimeSeries[0] ), this.getPositionFromTime( this._aSelectionTimeSeries[1] ) );
    };
    ts.PositionManager.prototype._setSelectionPosition = function( start, end ) {
        this._aSelectionPosition[0] = start === null ? this._aSelectionPosition[0] : start;
        this._aSelectionPosition[1] = end === null ? this._aSelectionPosition[1] : end;
    };
    ts.PositionManager.prototype.isInMaxSelectionTimeSeries = function( start, end ) {
        return ( end - start ) <= this._maxSelectionTimeSeries;
    };
    ts.PositionManager.prototype.getNewSelectionTimeSeriesFromStart = function( start ) {
        return [ start, start + this._maxSelectionTimeSeries ];
    };
    ts.PositionManager.prototype.getNewSelectionTimeSeriesFromEnd = function( end ) {
        return [ end - this._maxSelectionTimeSeries, end ];
    };
    ts.PositionManager.prototype.isBeforeSliderStartTime = function( time ) {
        return time < this._startTime;
    };
    ts.PositionManager.prototype.isAfterSliderEndTime = function( time ) {
        return time > this._endTime;
    };
    ts.PositionManager.prototype.getSliderEndTime = function() {
        return this._endTime;
    };
    ts.PositionManager.prototype.setWidth = function( width ) {
        this._width = width;
        this._calcuTimePerPoint();
        this._reset();
    };
    ts.PositionManager.prototype.getSliderEndPosition = function() {
        return this._width;
    };
    ts.PositionManager.prototype.getSliderStartTimeStr = function() {
        return this._formatDate( new Date(this._startTime) );
    };
    ts.PositionManager.prototype.getSliderEndTimeStr = function() {
        return this._formatDate( new Date(this._endTime) );
    };
    ts.PositionManager.prototype.getFullTimeStr = function( x ) {
        return this._formatDate( new Date(this.getTimeFromPosition(x)) );
    };
    ts.PositionManager.prototype._formatDate = function( d ) {
    	return moment( d ).format( "YYYY.MM.DD HH:mm:ss" );
    };
    ts.PositionManager.prototype.isInSelectionZone = function() {
		return ( this._selectTime >= this._aSelectionTimeSeries[0] && this._selectTime <= this._aSelectionTimeSeries[1] ) ? true : false;
    };
    ts.PositionManager.prototype.isInSliderTimeSeries = function( time ) {
        return ( time >= this._startTime && time <= this._endTime ) ? true : false;
    };
    ts.PositionManager.prototype.getSliderTimeSeries = function() {
        return [ this._startTime, this._endTime ];
    };
    ts.PositionManager.prototype.getSelectionTimeSeries = function() {
        return [ this._aSelectionTimeSeries[0], this._aSelectionTimeSeries[1] ];
    };
    ts.PositionManager.prototype.getSelectionPosition = function() {
        return [ this._aSelectionPosition[0], this._aSelectionPosition[1] ];
    };
    ts.PositionManager.prototype.getSelectPosition = function() {
        return this._selectPosition;
    };
    ts.PositionManager.prototype.setSelectTime = function( time ) {
        this._selectTime = time;
        this._selectPosition = this.getPositionFromTime( time );
    };
    ts.PositionManager.prototype.getSelectTime = function() {
    	return this._selectTime;
	};
	ts.PositionManager.prototype.getPrevTime = function() {
		var gap = this._aSelectionTimeSeries[1] - this._aSelectionTimeSeries[0];
		return this._aSelectionTimeSeries[0] - parseInt( gap / 2 ) - 1;
	};
	ts.PositionManager.prototype.getNextTime = function() {
		var gap = this._aSelectionTimeSeries[1] - this._aSelectionTimeSeries[0];
		var nextTime = this._aSelectionTimeSeries[1] + parseInt( gap / 2 ) + 1;
		if ( nextTime > Date.now() ) {
			return Date.now();
		} else {
			return nextTime;
		}
	};
    ts.PositionManager.prototype.getTimeFromPosition = function( x ) {
        return this._startTime + parseInt( this._timePerPoint * x );
    };
    ts.PositionManager.prototype.getPositionFromTime = function( time ) {
        return parseInt( ( time - this._startTime ) / this._timePerPoint );
    };
    ts.PositionManager.prototype.calcuSelectionZone = function() {
		var currentSelectionSize = this._aSelectionTimeSeries[1] - this._aSelectionTimeSeries[0];
		var currentSelectionHalfSize = Math.round( currentSelectionSize / 2 );
		var selectionStart = this._selectTime - currentSelectionHalfSize;
		var selectionEnd = this._selectTime + currentSelectionHalfSize;
		if ( selectionStart < this._startTime ) {
			selectionEnd = selectionStart + currentSelectionSize;
			selectionStart = this._startTime;
		} else if ( selectionEnd > this._endTime ) {
			selectionStart = this._endTime - currentSelectionSize;
			selectionEnd = this._endTime;
		}
		this._setSelectionTimeSeries( selectionStart, selectionEnd );
		this._setSelectionPosition( this.getPositionFromTime( selectionStart ), this.getPositionFromTime( selectionEnd ) );
    };
    ts.PositionManager.prototype.getXBarPosition = function() {
        var self = this;
        var max = this._xAxisTicks + 1;
        var space = parseInt( this._width / max );
        var a = [];
        for( var i = 0 ; i < max ; i++ ) {
            if ( i === 0 ) continue;
            var x = i * space;
            a.push( {
                x: x,
                time: self.getTimeStr( x )
            });
        }
        return a;
    };
    ts.PositionManager.prototype.getTimeStr = function( x ) {
        var timeX = parseInt( x * this._timePerPoint ) + this._startTime;
        var d = new Date( timeX );
		return moment( new Date( timeX ) ).format( "MM.DD HH:mm" );
    };
	ts.PositionManager.prototype.setSelectionStartTime = function( time ) {
		this._setSelectionTimeSeries( time, null );
		this._setSelectionPosition( this.getPositionFromTime( time ), null );
	};
	ts.PositionManager.prototype.setSelectionEndTime = function( time ) {
		this._setSelectionTimeSeries( null, time );
		this._setSelectionPosition( null, this.getPositionFromTime( time ) );
	};
    ts.PositionManager.prototype.setSelectionStartPosition = function( x ) {
        this._setSelectionTimeSeries( this.getTimeFromPosition(x), null );
        this._setSelectionPosition( x, null );
    };
    ts.PositionManager.prototype.setSelectionEndPosition = function( x ) {
        this._setSelectionTimeSeries( null, this.getTimeFromPosition(x) );
        this._setSelectionPosition( null, x );
    };
    ts.PositionManager.prototype.zoomIn = function() {
        // 선택 영역 중심으로 확대
        if( this._startTime === this._aSelectionTimeSeries[0] && this._endTime === this._aSelectionTimeSeries[1] ) return;
        var quarterSliderTime = parseInt( (this._endTime - this._startTime) / 4 );
        var tempStartTime = this._selectTime - quarterSliderTime;
        var tempEndTime = this._selectTime + quarterSliderTime;

        if ( tempEndTime - tempStartTime < this._minSliderTimeSeries ) {
            var minHalf = parseInt( this._minSliderTimeSeries / 2 );
            tempStartTime = this._selectTime - minHalf;
            tempEndTime = this._selectTime + minHalf;
        }
        var gap;
        if ( this._aSelectionTimeSeries[0] < tempStartTime ) {
            gap = tempStartTime - this._aSelectionTimeSeries[0];
            var tempSelectionEndTime = (this._aSelectionTimeSeries[1] + gap > tempEndTime) ? tempEndTime : this._aSelectionTimeSeries[1] + gap;
            this._setSelectionTimeSeries( tempStartTime, tempSelectionEndTime );
        }
        if ( this._aSelectionTimeSeries[1] > tempEndTime ) {
            gap = this._aSelectionTimeSeries[1] - tempEndTime;
            var tempSelectionStartTime = ( this._aSelectionTimeSeries[0] - gap < tempStartTime ) ? tempStartTime : this._aSelectionTimeSeries[0] - gap;
            this._setSelectionTimeSeries( tempSelectionStartTime, tempEndTime );
        }
        this.setSliderTimeSeries( tempStartTime, tempEndTime );
        this._reset();
    };
    ts.PositionManager.prototype.zoomOut = function() {
        var one = this._endTime - this._startTime;
        var tempCenterTime = this._aSelectionTimeSeries[0] + parseInt( ( this._aSelectionTimeSeries[1] - this._aSelectionTimeSeries[0] ) / 2 );
        this.setSliderTimeSeries( tempCenterTime - one, tempCenterTime + one );
        this._reset();
    };
    ts.PositionManager.prototype.resetBySelectTime = function( time, bIsNow ) {
        var halfSliderTimeSeries = parseInt( ( this._endTime - this._startTime ) / 2 );
        var halfSelectionTimeSeries = parseInt( ( this._aSelectionTimeSeries[1] - this._aSelectionTimeSeries[0] ) / 2 );
		if ( bIsNow === true ) {
			this.setSliderTimeSeries( time - halfSliderTimeSeries * 2, time );
			this._setSelectionTimeSeries( time - halfSelectionTimeSeries * 2, time );
		} else {
			this.setSliderTimeSeries( time - halfSliderTimeSeries, time + halfSliderTimeSeries );
			this._setSelectionTimeSeries( time - halfSelectionTimeSeries, time + halfSelectionTimeSeries );
		}
        this._resetSelectionByTime();
        this.setSelectTime( time );
    };
    ts.PositionManager.prototype._reset = function() {
        this.setSelectTime( this._selectTime );
        this._resetSelectionByTime();
    };
    ts.PositionManager.prototype._calcuTimePerPoint = function() {
        this._timePerPoint = ( this._endTime - this._startTime ) / this._width;
    };
})(window, jQuery);
