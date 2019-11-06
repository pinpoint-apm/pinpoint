(function() {
	"use strict";

	pinpointApp.factory("NavbarVoService", [ "UserConfigurationService", "CommonUtilService", function ( UserConfigService, CommonUtilService ) {
	    return function () {
	        // define and initialize private variables;
	        var self = this;
	        this._sApplication = false;
			this._periodType = "";
	        this._nPeriod = false;
	        this._nQueryEndTime = false;
	        this._sFilter = false;
	        this._sAgentId = false;
	
	        this._nQueryPeriod = false;
	        this._nQueryStartTime = false;
	
	        this._sReadablePeriod = false;
	        this._sQueryEndDateTime = false;

			this._nCalleeRange = UserConfigService.getCallee();
	        this._nCallerRange = UserConfigService.getCaller();
	        this._bBidirectional = UserConfigService.getBidirectional();
	        this._bWasOnly = UserConfigService.getWasOnly();
	        
	        this._sHint = false;

	        this.setApplication = function (application) {
	            if (angular.isString(application) && application.indexOf('@') > 0) {
	                self._sApplication = application;
	            }
	            return self;
	        };
	        this.getApplication = function () {
	            return self._sApplication;
	        };
	
	        this.setPeriod = function (period) {
	            if (angular.isNumber(period) && period > 0) {
	                self._nPeriod = period;
	            }
	            return self;
	        };
	        this.getPeriod = function () {
	            return self._nPeriod;
	        };
	
	        this.setQueryEndTime = function (queryEndTime) {
	            if (angular.isNumber(queryEndTime) && queryEndTime > 0) {
	                self._nQueryEndTime = queryEndTime;
	            }
	            return self;
	        };
	        this.getQueryEndTime = function () {
	            return self._nQueryEndTime;
	        };
	
	        this.getQueryPeriod = function () {
	            return self._nQueryPeriod;
	        };
	
	        this.getApplicationName = function () {
	            return self._sApplication.split('@')[0];
	        };
	
	        this.getServiceTypeName = function () {
	            return self._sApplication.split('@')[1];
	        };
			this.getCalleeRange = function() {
				return self._nCalleeRange;
			};
	        this.getCallerRange = function() {
	        	return self._nCallerRange;
	        };
	        this.getBidirectional = function() {
	        	return self._bBidirectional;
			};
	        this.getWasOnly = function() {
	        	return self._bWasOnly;
			};
			this.setCalleeRange = function( calleeRange ) {
				self._nCalleeRange = calleeRange;
			};
	        this.setCallerRange = function( callerRange ) {
	        	self._nCallerRange = callerRange;
	        };
	        this.setBidirectional = function( bidirectional ) {
	        	self._bBidirectional = bidirectional;
			};
	        this.setWasOnly = function( wasOnly ) {
	        	self._bWasOnly = wasOnly;
			};
	        this.setQueryStartTime = function (queryStartTime) {
	            if (angular.isNumber(queryStartTime) && queryStartTime > 0) {
	                self._nQueryStartTime = queryStartTime;
	            }
	            return self;
	        };
	
	        this.getQueryStartTime = function () {
	            return self._nQueryStartTime;
	        };
	
	        this.getReady = function () {
	            return self._sApplication && self._nPeriod && self._nQueryEndTime;
	        };
	
	        this.setFilter = function (filter) {
	            if (angular.isString(filter)) {
	                self._sFilter = filter;
	            }
	            return self;
	        };
	        this.getFilter = function () {
	            return self._sFilter;
	        };
	        this.getFilterAsJson = function () {
	            return JSON.parse(self._sFilter);
	        };
	
	        this.setHint = function (Hint) {
	            if (angular.isString(Hint)) {
	                self._sHint = Hint;
	            }
	            return self;
	        };
	        this.getHint = function () {
	            return self._sHint;
	        };
	
	        this.setAgentId = function (agentId) {
	            if (angular.isString(agentId)) {
	                self._sAgentId = agentId;
	            }
	            return self;
	        };
	        this.getAgentId = function () {
	            return self._sAgentId;
	        };
	
	        this.setReadablePeriod = function (readablePeriod) {
	            var regex = /^(\d)+(s|m|h|d|w|M|y)$/,
	                match = regex.exec(readablePeriod);
	            if (match) {
	                self._sReadablePeriod = readablePeriod;
	                var period = parseInt(readablePeriod, 10);
	                switch (match[2]) {
	                    case 'm':
	                        self.setPeriod(period * 60);
	                        break;
	                    case 'h':
	                        self.setPeriod(period * 60 * 60);
	                        break;
	                    case 'd':
	                        self.setPeriod(period * 60 * 60 * 24);
	                        break;
	                    case 'w':
	                        self.setPeriod(period * 60 * 60 * 24 * 7);
	                        break;
	                    case 'M':
	                        self.setPeriod(period * 60 * 60 * 24 * 30);
	                        break;
	                    case 'y':
	                        self.setPeriod(period * 60 * 60 * 24 * 30 * 12);
	                        break;
	                    default:
	                        self.setPeriod(period);
	                        break;
	                }
	            } else {
					if ( readablePeriod === "realtime" ) {
						self.setPeriodType( "realtime" );
						self._sReadablePeriod = "1m";
						self.setPeriod( 300 );
					}
				}
	            return self;
	        };
			this.isRealtime = function() {
				return this._periodType === "realtime";
			};
	        this.getReadablePeriod = function () {
	            return self._sReadablePeriod;
	        };
	
	        this.setQueryEndDateTime = function (queryEndDateTime) {
	            var regex = /^(19[7-9][0-9]|20\d{2})-(0[0-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])-(0[0-9]|1[0-9]|2[0-3])-([0-5][0-9])-([0-5][0-9])$/;
	            if (regex.test(queryEndDateTime)) {
	                self._sQueryEndDateTime = queryEndDateTime;
	                self.setQueryEndTime(self._parseQueryEndDateTimeToTimestamp(queryEndDateTime));
	            }
	            return self;
	        };
	        this.getQueryEndDateTime = function () {
	            return self._sQueryEndDateTime;
	        };
	        this._parseQueryEndDateTimeToTimestamp = function (queryEndDateTime) {
	            return CommonUtilService.getMilliSecond( queryEndDateTime );
	        };
	        this.autoCalcultateByQueryStartTimeAndQueryEndTime = function () {
	            self._nQueryPeriod = self._nQueryEndTime - self._nQueryStartTime;
	            self._nPeriod = self._nQueryPeriod / 1000 / 60;
	            self._sReadablePeriod = self._nQueryPeriod / 1000 / 60 + 'm';
	            self._sQueryEndDateTime = CommonUtilService.formatDate( self._nQueryEndTime );
	            return self;
	        };
	        this.autoCalculateByQueryEndDateTimeAndReadablePeriod = function () {
	            self._nQueryPeriod = self._nPeriod  * 1000;
	            self._nQueryStartTime = self._nQueryEndTime - self._nQueryPeriod;
	            return self;
	        };
			this.getPartialURL = function( bAddApplication, bAddFilter) {
				return (bAddApplication ? self.getApplication() + "/" : "" ) + self.getReadablePeriod() + "/" + self.getQueryEndDateTime() + ( bAddFilter ? ( self.getFilter() ? "/" + self.getFilter() : "" ) : "" );
			};
			this.setPeriodType = function( type ) {
				this._periodType = type;
			};
			this.getPeriodType = function() {
				return this._periodType;
			};

	    };
	}]);
})();