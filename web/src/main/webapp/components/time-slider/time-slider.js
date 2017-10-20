(function(w, $) {
    'use strict';
    var TimeSlider = function( id, options ) {
        this.$snap = $("#" + id);
        this.snap = Snap("#" + id);
        this._init( options );
        this._initControlClass();
        this._addInnerEventListener();
    };
    TimeSlider.prototype._init = function( options ) {
        this.groups = {};
        this._aListener = {
            "inEvent": [],
            "outEvent": [],
            "clickEvent": [],
            "selectTime": [],
            "changeSliderTimeSeries": [],
            "changeSelectionZone": []
        };
        this.opt = {
            "duration": 0,
            "xAxisTicks": 5,
            "eventZoneHeight": 30,        // 하단 이벤트 영역의 height
            "headerZoneHeight": 20,       // 상단 시간 표시영역의 height
            "stateLineThickness": 4,       // 상태선의 두께
            "minSliderTimeSeries": 6000,   // 6sec
			"maxSelectionTimeSeries": TimeSlider.MAX_TIME_RANGE,	// 7day
            "headerTextTopPadding": 10,   // 상단 상태선과 시간 text의 간격
            "selectionPointRadius": 4
        };
        this.opt.minSliderTimeSeries = ( this.opt.xAxisTicks + 1 ) * 1000;
        for( var p in options ) {
            this.opt[p] = options[p];
        }
        this.oTimelineData = new TimeSlider.TimelineData( this.opt.timelineData || {} );
        this._checkOffset();
    };
    TimeSlider.prototype._initControlClass = function() {

        this.oPositionManager = new TimeSlider.PositionManager( {
            "width": this.opt.width,
            "selectTime": this.opt.selectTime,
            "xAxisTicks": this.opt.xAxisTicks,
            "handleTimeSeries": this.opt.handleTimeSeries,
            "sliderTimeSeries": this.opt.timeSeries,
            "minSliderTimeSeries": this.opt.minSliderTimeSeries,
            "maxSelectionTimeSeries": this.opt.maxSelectionTimeSeries
        });
        var contentZoneHeight = this.opt.height - this.opt.headerZoneHeight - this.opt.eventZoneHeight;

        this.oLoading = new TimeSlider.LoadingIndicator( this, this.getGroup("loading", TimeSlider.GROUP_TYPE.TOP_BASE, TimeSlider.oDrawOrder["loading"]), {
            "width": this.opt.width,
            "height": this.opt.height,
            "duration": 2000
        });
        this.oBackground = new TimeSlider.Background( this, this.getGroup("background", TimeSlider.GROUP_TYPE.CONTENT_BASE, TimeSlider.oDrawOrder["background"]), {
            "top": 0,
            "left": 0,
            "width": this.opt.width,
            "height": contentZoneHeight,
            "duration": this.opt.duration
        });
		this.oStateLine = new TimeSlider.StateLine( this, this.getGroup("state-line", TimeSlider.GROUP_TYPE.TOP_BASE, TimeSlider.oDrawOrder["state-line"]), {
			"width": this.opt.width,
			"duration": this.opt.duration,
			"topLineY": this.opt.headerZoneHeight,
			"thickness": this.opt.stateLineThickness,
			"timeSeries": this.opt.timeSeries,
			"bottomLineY": this.opt.height - this.opt.eventZoneHeight
		} );
        this.oSelectionManager = new TimeSlider.SelectionManager( this, {
            "margin": this.opt.left,
            "height": this.opt.height,
            "duration": this.opt.duration,
            "handleSrc": this.opt.handleSrc,
            "eventZoneHeight": this.opt.eventZoneHeight,
            "headerZoneHeight": this.opt.headerZoneHeight,
            "contentZoneHeight": contentZoneHeight,
            "selectionPointRadius": this.opt.selectionPointRadius
        });
        this.oXAxis = new TimeSlider.XAxis( this, this.getGroup("x-axis", TimeSlider.GROUP_TYPE.TOP_BASE, TimeSlider.oDrawOrder["x-axis"]), {
            "endY": this.opt.height - this.opt.eventZoneHeight,
            "width": this.opt.width,
            "textY": this.opt.headerTextTopPadding,
            "startY": this.opt.headerZoneHeight,
            "duration": this.opt.duration
        });
        this.oEvents = new TimeSlider.Events( this, this.getGroup("events", TimeSlider.GROUP_TYPE.BOTTOM_BASE, TimeSlider.oDrawOrder["events"]), {
            "duration": this.opt.duration
        } );

        this.oLoading.hide();
    };
    TimeSlider.prototype.getGroup = function(name, type, zIndex) {
        if( this.groups[name] ) {
            return this.groups[name];
        }
        var g = this.groups[name] = this.snap.g().attr({
            "class": name,
            "data-order": zIndex
        });
        this._setTransform( g, type );
        this._sortGroup( g, zIndex );
        return this.groups[name];
    };
    TimeSlider.prototype._setTransform = function( newGroup, type ) {
        switch( type ) {
            case TimeSlider.GROUP_TYPE.TOP_BASE:
                break;
            case TimeSlider.GROUP_TYPE.CONTENT_BASE:
                newGroup.attr({ transform: "translate(0, " + this.opt.headerZoneHeight + ")" });
                break;
            case TimeSlider.GROUP_TYPE.BOTTOM_BASE:
                newGroup.attr({ transform: "translate(0, " + (this.opt.height - this.opt.eventZoneHeight) + ")" });
                break;
        }
    };
    TimeSlider.prototype._sortGroup = function( newGroup, zIndex ) {
        var aGroups = this.snap.selectAll("g");
        var afterGroup = null;
        for( var i = aGroups.length - 1; i >= 0 ; i-- ) {
            if( aGroups[i] == newGroup ) continue;
            if ( zIndex < parseInt( aGroups[i].attr("data-order") ) ) {
                afterGroup = aGroups[i];
            }
        }
        if ( afterGroup !== null ) {
            afterGroup.before( newGroup );
        }
    };
    TimeSlider.prototype._checkOffset = function() {
        var o = this.$snap.offset();
        this.opt.top = o.top;
        this.opt.left = o.left;
    };
    TimeSlider.prototype._addInnerEventListener = function() {
        var self = this;
        var mousedownX = -1;
        $(window).on("resize", function() {
            self._checkOffset();
            self._resize();
        });
        this.snap.mousemove(function(event, x, y) {
        });
        this.snap.mouseout(function(event, x, y) {
        });
        this.snap.mousedown(function(event, x, y) {
            mousedownX = x;
        });
        this.snap.click(function(event, x, y) {
            if ( mousedownX !== x ) return;
            self.oSelectionManager.onMouseClick(event, x - self.opt.left, y - self.opt.top);
        });
    };
    TimeSlider.prototype.fireEvent = function( eventType, argu ) {
        this._aListener[eventType].forEach(function(fn) {
            fn( argu );
        });
    };
    TimeSlider.prototype.addEvent = function( eventType, listener ) {
        this._aListener[eventType].push( listener );
        return this;
    };
    TimeSlider.prototype.addData = function( oNewData ) {
        this.oLoading.show();
        this.oTimelineData.addData( oNewData );
        this.oEvents.changeData();
        this.oStateLine.changeData();
        this.oLoading.hide();
    };
    TimeSlider.prototype.reset = function() {
        // this.oTimeSeriesSignboard.reset();
        this.oXAxis.reset();
        this.oSelectionManager.reset();
        this.oStateLine.reset();
        this.oEvents.reset();
    };
    TimeSlider.prototype._resize = function() {
        //this.oPositionManager.setWidth( this.$snap.width() );
		this.oPositionManager.setWidth( this.$snap.get(0).getBoundingClientRect().width );
        this.oBackground.reset();
        // this.oTimeSeriesSignboard.resize();
        this.reset();
    };
    TimeSlider.prototype.zoomIn = function() {
        // 1/2배씩
        this.oPositionManager.zoomIn();
        this.reset();
    };
    TimeSlider.prototype.zoomOut = function() {
        // 2배씩
        this.oPositionManager.zoomOut();
        this.reset();
    };
	TimeSlider.prototype.resetTimeSeriesAndSelectionZone = function( aSelectionFromTo, aFromTo, selectedTime ) {
		this.oPositionManager.setSliderTimeSeries( aFromTo[0], aFromTo[1] );
		this.oPositionManager.setSelectionStartTime( aSelectionFromTo[0] );
		this.oPositionManager.setSelectionEndTime( aSelectionFromTo[1] );
		this.oPositionManager.setSelectTime( selectedTime || aSelectionFromTo[1] );
		this.emptyData();
		this.reset();
	};
	TimeSlider.prototype.movePrev = function() {
		var prevTime = this.oPositionManager.getPrevTime();
		this.oSelectionManager.setSelectTime( prevTime );
	};
	TimeSlider.prototype.moveNext = function() {
		var nextTime = this.oPositionManager.getNextTime();
		this.oSelectionManager.setSelectTime( nextTime );
	};
	TimeSlider.prototype.moveHead = function() {
		this.oSelectionManager.setSelectTime( Date.now(), true );
	};
    TimeSlider.prototype.getSliderTimeSeries = function() {
        return this.oPositionManager.getSliderTimeSeries();
    };
    TimeSlider.prototype.getSelectionTimeSeries = function() {
        return this.oPositionManager.getSelectionTimeSeries();
    };
    TimeSlider.prototype.setDefaultStateLineColor = function( color ) {
        this.oStateLine.setDefaultStateLineColor( color );
    };
    TimeSlider.prototype.emptyData = function() {
        this.oLoading.show();
        this.oTimelineData.emptyData();
        this.oEvents.emptyData();
        this.oStateLine.emptyData();
        this.oLoading.hide();
    };
    TimeSlider.prototype.setSelectTime = function( time ) {
        this.oSelectionManager.setSelectTime( time );
    };
    TimeSlider.prototype.getSelectTime = function() {
    	return this.oPositionManager.getSelectTime();
	}

    TimeSlider.GROUP_TYPE = {
        TOP_BASE: "top-base",
        CONTENT_BASE: "content-base",
        BOTTOM_BASE: "bottom-base"
    };
    TimeSlider.oDrawOrder = {
        "background": 0,
		"state-line": 3,
        "selection-zone": 5,
        "time-series-signboard": 7,
        "x-axis": 10,
        "events": 10,
        "time-signboard": 15,
        "selection-point": 15,
        "left-handler": 25,
        "right-handler": 25,
        "guide": 30,
        "loading": 100
    };
    TimeSlider.StatusColor = {
    	"BASE": "rgba(187, 187, 187, .3)",
		"UNKNOWN": "rgba(220, 214, 214, .8)",
    	"RUNNING": "rgba(0, 158, 0, .4 )",
		"SHUTDOWN": "rgba(209, 82, 96, .7)",
		"UNSTABLE_RUNNING": "rgba(255, 102, 0, .4)",
		"EMPTY": "rgba(165, 219, 245, .7)"
	};
	TimeSlider.MAX_TIME_RANGE = 604800000;
    w.TimeSlider = TimeSlider;
})(window, jQuery);
