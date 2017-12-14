(function(global, $) {
	'use strict';
	function DataLoadManager( application, filter, option, cbLoaded ) {
		this._filter = filter;
		this._option = option;
		this._application = application;
		this._cbLoaded = cbLoaded;
		this._initVar();
	}
	DataLoadManager.prototype._initVar = function() {
		this._callCount = 0;
		this._bLoadCompleted = false;
		this._lastLoadTime = -1;
	};
	DataLoadManager.prototype.option = function( k ) {
		return this._option[k];
	};
	DataLoadManager.prototype.loadData = function( cbComplete, cbSuccess, cbFail, widthOfPixel, heightOfPixel ) {
		var self = this;
		var oFromTo = this._oSCManager.getX();

		this._oAjax = $.ajax({
			"url": this.getUrl(),
			"data": {
				"to": this._callCount === 0 ? oFromTo.max : this._lastLoadTime - 1,
				"from": oFromTo.min,
				"limit": this.option( "fetchLimit" ),
				"filter": this._filter || "",
				"application": this._application,
				"xGroupUnit": widthOfPixel,
				"yGroupUnit": heightOfPixel
			},
			"headers": { "accept": "application/json" },
			"dataType": "json"
		}).done(function( oResultData ) {
			if ( oResultData.exception ) {
				cbFail();
			} else {
				self._callCount += 1;
				self._bLoadCompleted = oResultData.complete;
				self._lastLoadTime = oResultData.resultFrom;
				cbSuccess(oResultData, !self._bLoadCompleted, self._getIntervalTime() );
			}
		}).always(function() {
			cbComplete();
		});
	};
	DataLoadManager.prototype.loadRealtimeData = function( callbackRealtimeSuccess, callbackRealtimeFail, widthOfPixel, heightOfPixel ) {
		var self = this;
		var oFromTo = this._oSCManager.getX();

		var beforeRequest = Date.now();
		var currentFrom = this._nextFrom || oFromTo.max;
		var currentTo = this._nextTo ||  oFromTo.max + this.option( "realtimeInterval" );

		this._oRealtimeAjax = $.ajax({
			"url": this.getUrl(),
			"data": {
				"to": currentTo,
				"from": currentFrom,
				"limit": this.option( "fetchLimit" ),
				"filter": "",
				"application": this._application,
				"xGroupUnit": widthOfPixel,
				"yGroupUnit": heightOfPixel,
				"backwardDirection": false
			},
			"headers": { "accept": "application/json" },
			"dataType": "json"
		}).done(function( oResultData ) {
			if ( oResultData.exception ) {
				callbackRealtimeFail();
			} else {

				self._nextFrom = oResultData.complete ? oResultData.to : oResultData.resultTo;
				self._nextTo = self._nextFrom + self.option( "realtimeInterval" );

				callbackRealtimeSuccess( oResultData, self._calcuRealtimeIntervalTime( oResultData.currentServerTime, Date.now() - beforeRequest ), self._isResetRealtime( oResultData.currentServerTime ), oResultData.currentServerTime );
				self._cbLoaded( self._oSCManager.getX(), self._nextFrom, self._nextTo );
			}
		}).fail(function() {
			setTimeout( function() {
				self.loadRealtimeData(callbackRealtimeSuccess, callbackRealtimeFail, widthOfPixel, heightOfPixel);
			}, ( self.option( "realtimeInterval" ) / 2 ) );
		});
	};
	DataLoadManager.prototype._isResetRealtime = function( currentServerTime ) {
		return ( currentServerTime - this._nextTo ) >= this.option( "realtimeResetTimeGap" );
	};
	DataLoadManager.prototype._calcuRealtimeIntervalTime = function( currentServerTime, requestGap ) {
		var interval = parseInt( this.option( "realtimeInterval" ), 0 );
		if ( requestGap > interval ) {
			return 0;
		} else {
			var gapTime = ( currentServerTime - this._nextTo ) - this.option( "realtimeDefaultTimeGap" );
			if ( gapTime < 0 ) {
				return Math.max( interval - requestGap - gapTime, interval );
			} else {
				return Math.max( interval - requestGap - gapTime, 0 );
			}
		}
	};
	DataLoadManager.prototype.setRealtimeFrom = function( from ) {
		this._nextFrom = from;
		this._nextTo = from + this.option( "realtimeInteraval" );
	};
	DataLoadManager.prototype._getIntervalTime = function() {
		if (this.option( "useIntervalForFetching" ) ) {
			return this.option( "fetchingInterval" );
		}
		return 0;
	};
	DataLoadManager.prototype.getRealtimeInterval = function() {
		return this.option( "realtimeDefaultTimeGap" );
	};
	DataLoadManager.prototype.getUrl = function() {
		return this.option( "url" );
	};

	DataLoadManager.prototype.initCallCount = function() {
		this._callCount = 0;
	};
	DataLoadManager.prototype.isFirstRequest = function() {
		return this._callCount === 0;
	};
	DataLoadManager.prototype.isCompleted = function() {
		return this._bLoadCompleted;
	};
	DataLoadManager.prototype.abort = function() {
		if ( this._oAjax ) {
			this._oAjax.abort();
		}
		if ( this._oRealtimeAjax ) {
			this._oRealtimeAjax.abort();
		}
	};
	DataLoadManager.prototype.setTimeManager = function( oSCManager ) {
		this._oSCManager = oSCManager;
	};
	DataLoadManager.prototype.reset = function() {
		this._bLoadCompleted = false;
		this.initCallCount();
	};


	global.BigScatterChart2.DataLoadManager = DataLoadManager;
})(window, jQuery);