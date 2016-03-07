(function(global, $) {
	'use strict';
	function DataLoadManager( application, from, to, filter, option, cbLoaded ) {
		this._to = to;
		this._from = from;
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

		this._oAjax = $.ajax({
			"url": this.getUrl(),
			"data": {
				"to": this._callCount === 0 ? this._to : this._lastLoadTime - 1,
				"from": this._from,
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

		var start = Date.now();
		this._oRealtimeAjax = $.ajax({
			"url": this.getUrl(),
			"data": {
				"to": this._nextTo ||  this._to + this.option( "realtimeInterval" ),
				"from": this._nextFrom || this._to,
				"limit": this.option( "fetchLimit" ),
				"filter": "",
				"application": this._application,
				"xGroupUnit": widthOfPixel,
				"yGroupUnit": heightOfPixel
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

	global.BigScatterChart2.DataLoadManager = DataLoadManager;
})(window, jQuery);