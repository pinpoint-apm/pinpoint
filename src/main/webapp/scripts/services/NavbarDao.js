'use strict';

pinpointApp.factory('NavbarDao', function () {
    return function () {
        // define and initialize private variables;
        var self = this;
        this._sApplication = false;
        this._nPeriod = false;
        this._nQueryEndTime = false;

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
            return self._nPeriod  * 1000 * 60;
        };

        this.getApplicationName = function () {
            return self._sApplication.split('@')[0];
        };

        this.getServiceType = function () {
            return self._sApplication.split('@')[1];
        };

        this.getQueryStartTime = function () {
            return self._nQueryEndTime - self.getQueryPeriod();
        };

        this.getReady = function () {
            return self._sApplication && self._nPeriod && self._nQueryEndTime;
        };

    };
});
