(function(global, $) {
	'use strict';
	function DataManager( application, from, to, filter, option ) {
		console.log( application, from, to );
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
	DataManager.prototype.loadData = function( callbackComplete, callbackSuccess ) {
		var self = this;

		this._oAjax = $.ajax({
			"url": this.getUrl(),
			"data": {
				"to": this._callCount === 0 ? this._to : this._lastLoadFrom - 1,
				"from": this._from,
				"limit": this.option( "nFetchLimit" ),
				"filter": this._filter || "",
				"application": this._application,
				"xGroupUnit": 100,
				"yGroupUnit": 100
			},
			"headers": { "accept": "application/json" },
			"dataType": "json",
			"complete": function() {
				callbackComplete();
			},
			"success": function( oResultData ) {
				if ( oResultData.exception ) {

				} else {
					self._callCount += 1;
					self._lastLoadFrom = oResultData.resultFrom;
					callbackSuccess(oResultData, self._hasNextData());
				}
			}
		});
	};
	DataManager.prototype._hasNextData = function() {
		if ( this._lastLoadFrom - 1 > this._from ) {
			if (this.option( "useIntervalForFetching" ) ) {
				return this.option( "nFetchingInterval" );
			}
			return 0;
		}
		return -1;
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
		if (this._oAjax) {
			this._oAjax.abort();
		}
	};

	global.BigScatterChart2.DataManager = DataManager;
})(window, jQuery);