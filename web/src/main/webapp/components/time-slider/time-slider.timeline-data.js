(function(w, $) {
    var ts = w.TimeSlider;
    ts.TimelineData = function( aRawData ) {
        this._init( aRawData );
    };
    ts.TimelineData.prototype._init = function( aRawData ) {
    	this._initStatusData( aRawData.agentStatusTimeline || {} );
    	this._initEventData( aRawData.agentEventTimeline || {} );
    };
	ts.TimelineData.prototype._initStatusData = function( oStatusRawData ) {
		var self = this;
		this._aStatusRawData = oStatusRawData.timelineSegments || [];
		this._oStatusRawHash = {};
		this._aStatusRawData.forEach(function( o ) {
			self._oStatusRawHash[self.makeID( o )] = o;
		});
	};
	ts.TimelineData.prototype._initEventData = function( oEventRawData ) {
		this._aEventRawData = oEventRawData.timelineSegments || [];
	};
    ts.TimelineData.prototype.makeID = function( oEvent ) {
    	return oEvent.endTimestamp;
    };
	ts.TimelineData.prototype.eventCount = function() {
		return this._aEventRawData.length;
	};
    ts.TimelineData.prototype.count = function() {
        return this._aStatusRawData.length;
    };
    ts.TimelineData.prototype.getDataByIndex = function( index ) {
        return this._aStatusRawData[index];
    };
    ts.TimelineData.prototype.getDataByKey = function( key ) {
        return this._oStatusRawHash[key];
    };
	ts.TimelineData.prototype.getEventDataByIndex = function( index ) {
		return this._aEventRawData[index];
	};
    ts.TimelineData.prototype.emptyData = function() {
        this._aStatusRawData = [];
        this._oStatusRawHash = {};
		this._aEventRawData = [];
    };
    ts.TimelineData.prototype.addData = function( oNewData ) {
    	this._init( oNewData );
    };

})(window, jQuery);
