'use strict';

pinpointApp.factory('ServerMapFilterVo', [ function () {
    return function () {
        var self = this;

        this._sFromApplication = null;
        this._sFromServiceType = null;
        this._sToApplication = null;
        this._sToServiceType = null;
        this._sResponseFrom = null;
        this._sResponseTo = null;
        this._bIncludeException = null;
        this._sRequestUrlPattern = null;

        this.setFromApplication = function (fromApplication) {
            if (angular.isString(fromApplication)) {
                self._sFromApplication = fromApplication;
            } else {
                throw new Error('fromApplication should be string in ServerMapFilterVo.');
            }
            return self;
        };
        this.getFromApplication = function () {
            return self._sFromApplication;
        };

        this.setFromServiceType = function (fromServiceType) {
            if (angular.isString(fromServiceType)) {
                self._sFromServiceType = fromServiceType;
            } else {
                throw new Error('fromServiceType should be string in ServerMapFilterVo.');
            }
            return self;
        };
        this.getFromServiceType = function () {
            return self._sFromServiceType;
        };

        this.setToApplication = function (toApplication) {
            if (angular.isString(toApplication)) {
                self._sToApplication = toApplication;
            } else {
                throw new Error('toApplication should be string in ServerMapFilterVo.');
            }
            return self;
        };
        this.getToApplication = function () {
            return self._sToApplication;
        };

        this.setToServiceType = function (toServiceType) {
            if (angular.isString(toServiceType)) {
                self._sToServiceType = toServiceType;
            } else {
                throw new Error('toServiceType should be string in ServerMapFilterVo.');
            }
            return self;
        };
        this.getToServiceType = function () {
            return self._sToServiceType;
        };

        this.setResponseFrom = function (responseFrom) {
            if (angular.isString(responseFrom) || angular.isNumber(responseFrom)) {
                self._sResponseFrom = responseFrom;
            } else {
                throw new Error('responseFrom should be string in ServerMapFilterVo.');
            }
            return self;
        };
        this.getResponseFrom = function () {
            return self._sResponseFrom;
        };


        this.setResponseTo = function (responseTo) {
            if (angular.isString(responseTo) || angular.isNumber(responseTo)) {
                self._sResponseTo = responseTo;
            } else {
                throw new Error('responseTo should be string in ServerMapFilterVo.');
            }
            return self;
        };
        this.getResponseTo = function () {
            return self._sResponseTo;
        };

        this.setIncludeException = function (includeException) {
            if (angular.isDefined(includeException)) {
                self._bIncludeException = includeException;
            } else {
                throw new Error('includeException should be defined in ServerMapFilterVo.');
            }
            return self;
        };
        this.getIncludeException = function () {
            return self._bIncludeException;
        };

        this.setRequestUrlPattern = function (requestUrlPattern) {
            if (angular.isString(requestUrlPattern)) {
                self._sRequestUrlPattern = requestUrlPattern;
            } else {
                throw new Error('requestUrlPattern should be string in ServerMapFilterVo.');
            }
            return self;
        };
        this.getRequestUrlPattern = function () {
            return self._sRequestUrlPattern;
        };

        this.toJson = function () {
            var filter = {
                fa: self._sFromApplication,
                fst: self._sFromServiceType,
                ta: self._sToApplication,
                tst: self._sToServiceType
            };

            if (self._sResponseFrom != 0 && self._sResponseTo != 30000) {
                filter.rf = self._sResponseFrom;
                filter.rt = self._sResponseTo;
            }
            if (self._bIncludeException) {
                filter.ie = self._bIncludeException;
            }
            if (self._sRequestUrlPattern) {
                filter.url = self._sRequestUrlPattern;
            }
            return filter;
        };
    };
}]);
