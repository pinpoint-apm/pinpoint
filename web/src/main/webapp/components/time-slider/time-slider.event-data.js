(function(w, $) {
    var ts = w.TimeSlider;
    ts.EventData = function( aRawData ) {
        this._init( aRawData );
    };
    ts.EventData.prototype._init = function( aRawData ) {
        var self = this;
        this._aRawData = aRawData;
        this._oHash = {};
        this._aRawData.forEach(function( o ) {
            self._oHash[self.makeID(o)] = o;
        });
    };
    ts.EventData.prototype.makeID = function( oEvent ) {
        return oEvent.eventTypeCode + "-" + oEvent.eventTimestamp;
    };
    ts.EventData.prototype.count = function() {
        return this._aRawData.length;
    };
    ts.EventData.prototype.getDataByIndex = function( index ) {
        return this._aRawData[index];
    };
    ts.EventData.prototype.getDataByKey = function( key ) {
        return this._oHash[key];
    };
    ts.EventData.prototype.emptyData = function() {
        this._aRawData = [];
        this._oHash = {};
    };
    ts.EventData.prototype.addData = function( aNewData ) {
        if ( aNewData.length === 0 ) return;

        var i, j;
        var beforeBoundary = this._getBeforeDataBoundary( aNewData );
        for ( i = beforeBoundary ; i >= 0 ; i-- ) {
            this._aRawData.unshift( aNewData[i] );
            this._oHash[this.makeID(aNewData[i])] = aNewData[i];
        }

        var afterBoundary = this._getAfterDataBoundary( aNewData, beforeBoundary );
        for ( i = afterBoundary ; i < aNewData.length ; i++ ) {
            this._aRawData.push( aNewData[i] );
            this._oHash[this.makeID(aNewData[i])] = aNewData[i];
        }

        beforeBoundary = beforeBoundary === -1 ? 0 : beforeBoundary + 1;
        afterBoundary = afterBoundary === Number.MAX_SAFE_INTEGER ? aNewData.length - 1 : afterBoundary - 1;

        var skip = beforeBoundary;
        if ( afterBoundary > beforeBoundary ) {
            for( i = beforeBoundary ; i < this._aRawData.length ; i++ ) {
                var rawTimestamp = this._aRawData[i].eventTimestamp;
                for( j = skip; j < aNewData.length ; j++ ) {
                    var oNewEventData = aNewData[j];
                    if ( oNewEventData.eventTimestamp < rawTimestamp ) {
                        this._aRawData.splice( i, 0, oNewEventData );
                        this._oHash[this.makeID(oNewEventData)] = oNewEventData;
                        i++;
                        skip = j + 1;
                    } else if ( oNewEventData.eventTimestamp === rawTimestamp ) {
                        skip = j + 1;
                        break;
                    }
                }
                if ( skip === aNewData.length ) break;
            }
        }
    };
    ts.EventData.prototype._getBeforeDataBoundary = function( aNewData ) {
        var boundary = -1;
        if ( this._aRawData.length === 0 ) {
            boundary = aNewData.length - 1;
        } else {
            var startTime = this._aRawData[0].eventTimestamp;
            var len = aNewData.length;
            for ( var i = 0 ; i < len ; i++ ) {
                var time = aNewData[i].eventTimestamp;
                if ( time < startTime ) {
                    boundary = i;
                } else {
                    break;
                }
            }
        }
        return boundary;
    };
    ts.EventData.prototype._getAfterDataBoundary = function( aNewData, skipIndex ) {
        var boundary = Number.MAX_SAFE_INTEGER;
        var startIndex = skipIndex + 1;
        var len = aNewData.length;
        if ( len > startIndex ) {
            var endTime = this._aRawData[this._aRawData.length - 1].eventTimestamp;
            for ( var i = startIndex ; i < len ; i++ ) {
                var time = aNewData[i].eventTimestamp;
                if ( time > endTime ) {
                    boundary = i;
                    break;
                }
            }
        }
        return boundary;
    };

})(window, jQuery);
