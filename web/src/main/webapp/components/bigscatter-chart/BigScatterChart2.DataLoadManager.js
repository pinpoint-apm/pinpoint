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
				self._lastLoadTime = oResultData.resultTo;
				cbSuccess(oResultData, !this._bLoadCompleted, self._getIntervalTime() );
			}
		}).always(function() {
			cbComplete();
		});
	};
	DataLoadManager.prototype.loadRealtimeData = function( callbackRealtimeSuccess, widthOfPixel, heightOfPixel ) {
		var self = this;
		var oFromTo = this._oSCManager.getX();

		var start = Date.now();
		this._oRealtimeAjax = $.ajax({
			"url": this.getUrl(),
			"data": {
				"to": this._nextTo ||  oFromTo.max + this.option( "realtimeInterval" ),
				"from": this._nextFrom || oFromTo.max,
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

			} else {
				self._nextFrom = oResultData.complete ? oResultData.to : oResultData.resultTo;
				self._nextTo = self._nextFrom + self.option( "realtimeInterval" );

				callbackRealtimeSuccess( oResultData, self.option( "realtimeInterval" ) - ( Date.now() - start ) );
				self._cbLoaded( self._nextFrom );
			}
		}).fail(function() {
			self.loadRealtimeData( callbackRealtimeSuccess, widthOfPixel, heightOfPixel );
		});
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
		return this.option( "realtimeInterval" );
	}
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