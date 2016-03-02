(function(w, $) {
    var ts = w.TimeSlider;
    ts.Configuration = function( options ) {
        this._init( options );
    };
    ts.Configuration.prototype._init = function( options ) {
        console.log( options );
        var defaultOptions = {
            "duration": 300,
            "xAxisTicks": 5,
            "eventZoneHeight": 30,        // 하단 이벤트 영역의 height
            "headerZoneHeight": 20,       // 상단 시간 표시영역의 height
            "stateLineThickness": 4,       // 상태선의 두께
            "minSliderTimeSeries": 6000,             // 6sec
            "maxSliderTimeSeries": 172800000,         // 2day
            "headerTextTopPadding": 10,   // 상단 상태선과 시간 text의 간격
            "selectionPointRadius": 5
        };
        var backgroundOptions = {
            "backgroundTop": 0,
            "backgroundLeft": 0
        };
        var loadingIndicatorOptions = {
            "loadingDuration": 2000
        };
        this._setOptions( defaultOptions );
        this._setOptions( backgroundOptions );
        this._setOptions( loadingIndicatorOptions );
        this._setOptions( options );

        this._checkMinSliderTimeSeries();
        this._contentZoneHeight = this["height"] - this["headerZoneHeight"] - this["eventZoneHeight"];
    };
    ts.Configuration.prototype._checkMinSliderTimeSeries = function( opt ) {
        var minSliderTimeSeries = ( this.xAxisTicks + 1 ) * 1000 ;
        this.minSliderTimeSeries = this.minSliderTimeSeries < minSliderTimeSeries ? minSliderTimeSeries : this.minSliderTimeSeries;
    };
    ts.Configuration.prototype.setOffset = function( offset ) {
        this["offsetLeft"] = offset.left;
        this["offsetTop"] = offset.top;
    };
    ts.Configuration.prototype._setOptions = function( options ) {
        for( var p in options ) {
            this[p] = options[p];
        };
    };
    ts.Configuration.prototype.getContentZoneHeight = function() {
        return this._contentZoneHeight;
    };
})(window, jQuery);
