'use strict';

pinpointApp.factory('NavbarDao', function () {
    return function () {
        // define and initialize private variables;
        var self = this;
        this._sApplication = false;
        this._nPeriod = false;
        this._nQueryEndTime = false;
        this._sFilter = false;
        this._sAgentId = false;

        this._nQueryPeriod = false;
        this._nQueryStartTime = false;

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

        this.getServiceType = function () {
            return self._sApplication.split('@')[1];
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

        this.setAgentId = function (agentId) {
            if (angular.isString(agentId)) {
                self._sAgentId = agentId;
            }
            return self;
        };
        this.getAgentId = function () {
            return self._sAgentId;
        };

        this.autoCalculateByQueryEndTimeAndPeriod = function () {
            self._nQueryPeriod = self._nPeriod  * 1000 * 60;
            self._nQueryStartTime = self._nQueryEndTime - self._nQueryPeriod;
            return self;
        };

        this.autoCalcultateByQueryStartTimeAndQueryEndTime = function () {
            self._nQueryPeriod = self._nQueryEndTime - self._nQueryStartTime;
            self._nPeriod = self._nQueryPeriod / 1000 / 60;
            return self;
        };
    };
});
