(function(global, $) {
	'use strict';
	function DataManager( application, from, to, filter, option ) {
		this._option = option;
		this._application = application;
		this._from = from;
		this._to = to;
		this._filter = filter;
		this._initVar();
	}
	DataManager.prototype._initVar = function() {
		this._callCount = 0;
		this._lastLoadFrom = 0;
	};
	DataManager.prototype.option = function( k ) {
		return this._option[k];
	};
	DataManager.prototype.loadData = function( callbackComplete, callbackSuccess, widthOfPixel, heightOfPixel ) {
		var self = this;

		this._oAjax = $.ajax({
			"url": this.getUrl(),
			"data": {
				"to": this._callCount === 0 ? this._to : this._lastLoadFrom - 1,
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

			} else {
				self._callCount += 1;
				self._lastLoadFrom = oResultData.resultFrom;
				callbackSuccess(oResultData, self._hasNextData());
			}
		}).always(function() {
			callbackComplete();
		});
	};
	DataManager.prototype.loadRealtimeData = function( callbackRealtimeSuccess, widthOfPixel, heightOfPixel ) {
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
				self._nextFrom = oResultData.resultTo;
				self._nextTo = self._nextFrom + self.option( "realtimeInterval" );

				callbackRealtimeSuccess( oResultData, self.option( "realtimeInterval" ) - ( Date.now() - start ) );
			}
		}).fail(function() {
			self.loadRealtimeData( callbackRealtimeSuccess, widthOfPixel, heightOfPixel );
		});
	};
	DataManager.prototype._hasNextData = function() {
		if ( this._lastLoadFrom - 1 > this._from ) {
			if (this.option( "useIntervalForFetching" ) ) {
				return this.option( "fetchingInterval" );
			}
			return 0;
		}
		return -1;
	};
	DataManager.prototype.getRealtimeInterval = function() {
		return this.option( "realtimeInterval" );
	}
	DataManager.prototype.hasRealtime = function() {
		return this.option( "realtime" );
	};
	DataManager.prototype.getUrl = function() {
		return this.option( "url" );
	};

	DataManager.prototype.initCallCount = function() {
		this._callCount = 0;
	};
	DataManager.prototype.isFirstRequest = function() {
		return this._callCount === 0;
	};
	DataManager.prototype.abort = function() {
		if ( this._oAjax ) {
			this._oAjax.abort();
		}
		if ( this._oRealtimeAjax ) {
			this._oRealtimeAjax.abort();
		}
	};

	global.BigScatterChart2.DataManager = DataManager;
})(window, jQuery);