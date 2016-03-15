(function() {
	'use strict';
	/**
	 * (en)ServerMapFilterVoService 
	 * @ko ServerMapFilterVoService
	 * @group Service
	 * @name ServerMapFilterVoService
	 * @class
	 */
	pinpointApp.factory('ServerMapFilterVoService', [  function () {
	    return function ServerMapFilterVoService(dataSet) {
	        var self = this;
	
	        this._sMainApplication = null;
	        this._nMainServiceTypeCode = null;
	        this._sMainServiceTypeName = null;
	        this._sFromApplication = null;
	        this._sFromServiceType = null;
	        this._sFromAgentName = null;
	        this._sToApplication = null;
	        this._sToServiceType = null;
	        this._sToAgentName = null;
	        this._sResponseFrom = 0;
	        this._sResponseTo = 'max';
	        this._bIncludeException = null;
	        this._sRequestUrlPattern = '';
	
	        this.setMainApplication = function (mainApplication) {
	            if (angular.isString(mainApplication)) {
	                self._sMainApplication = mainApplication;
	            } else {
	                throw new Error('mainApplication should be string in ServerMapFilterVo. : ', mainApplication);
	            }
	            return self;
	        };
	        this.getMainApplication = function () {
	            return self._sMainApplication;
	        };
	
	        this.setMainServiceTypeCode = function (mainServiceTypeCode) {
	            if (angular.isNumber(mainServiceTypeCode)) {
	                self._nMainServiceTypeCode = mainServiceTypeCode;
	            } else {
	                throw new Error('mainServiceTypeCode should be number in ServerMapFilterVo. : ', mainServiceTypeCode);
	            }
	            return self;
	        };
	        this.setMainServiceTypeName = function(mainServiceTypeName) {
	        	if (angular.isString(mainServiceTypeName)) {
	                self._sMainServiceTypeName = mainServiceTypeName;
	            } else {
	                throw new Error('mainServiceTypeName should be string in ServerMapFilterVo. : ', mainServiceTypeName);
	            }
	            return self;
	        };
	        this.getMainServiceTypeCode = function () {
	            return self._nMainServiceTypeCode;
	        };
	        this.getMainServiceTypeName = function() {
	        	return self._sMainServiceTypeName;
	        };
	
	        this.setFromApplication = function (fromApplication) {
	            if (angular.isString(fromApplication)) {
	                self._sFromApplication = fromApplication;
	            } else {
	                throw new Error('fromApplication should be string in ServerMapFilterVo. : ', fromApplication);
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
	                throw new Error('fromServiceType should be string in ServerMapFilterVo. : ', fromServiceType);
	            }
	            return self;
	        };
	        this.getFromServiceType = function () {
	            return self._sFromServiceType;
	        };
	
	        this.setFromAgentName = function (fromAgentName) {
	            if (angular.isString(fromAgentName)) {
	                self._sFromAgentName = fromAgentName;
	            } else {
	                throw new Error('fromAgentName should be string in ServerMapFilterVo. : ', fromAgentName);
	            }
	            return self;
	        };
	        this.getFromAgentName = function () {
	            return self._sFromAgentName;
	        };
	
	        this.setToApplication = function (toApplication) {
	            if (angular.isString(toApplication)) {
	                self._sToApplication = toApplication;
	            } else {
	                throw new Error('toApplication should be string in ServerMapFilterVo. : ', toApplication);
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
	                throw new Error('toServiceType should be string in ServerMapFilterVo. : ', toServiceType);
	            }
	            return self;
	        };
	        this.getToServiceType = function () {
	            return self._sToServiceType;
	        };
	
	        this.setToAgentName = function (toAgentName) {
	            if (angular.isString(toAgentName)) {
	                self._sToAgentName = toAgentName;
	            } else {
	                throw new Error('toAgentName should be string in ServerMapFilterVo. : ', toAgentName);
	            }
	            return self;
	        };
	        this.getToAgentName = function () {
	            return self._sToAgentName;
	        };
	
	        this.setResponseFrom = function (responseFrom) {
	            if (angular.isString(responseFrom)) {
	                self._sResponseFrom = parseInt(responseFrom, 10);
	            } else if (angular.isNumber(responseFrom)) {
	                self._sResponseFrom = responseFrom;
	            } else {
	                throw new Error('responseFrom should be string in ServerMapFilterVo. : ', responseFrom);
	            }
	            return self;
	        };
	        this.getResponseFrom = function () {
	            return self._sResponseFrom;
	        };
	
	        this.setResponseTo = function (responseTo) {
	            if (responseTo === 'max') {
	                self._sResponseTo = 'max';
	            } else if (angular.isNumber(responseTo) || angular.isString(responseTo)){
	                responseTo = parseInt(responseTo, 10);
	                if (responseTo >= 30000) {
	                    self._sResponseTo = 'max';
	                } else {
	                    self._sResponseTo = responseTo;
	                }
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
	//                throw new Error('requestUrlPattern should be string in ServerMapFilterVo.');
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
	                tst: self._sToServiceType,
	                ie: self._bIncludeException
	            };
	
	            if (!(self._sResponseFrom === 0 && self._sResponseTo === 'max')) {
	                filter.rf = self._sResponseFrom;
	                filter.rt = self._sResponseTo;
	            }
	            if (self._sRequestUrlPattern) {
	                filter.url = self._sRequestUrlPattern;
	            }
	            if (self._sFromAgentName) {
	                filter.fan = self._sFromAgentName;
	            }
	            if (self._sToAgentName) {
	                filter.tan = self._sToAgentName;
	            }
	
	            return filter;
	        };
	
	
	        /**
	         * initialize
	         */
	        if (dataSet && angular.isObject(dataSet)) {
	            this
	                .setFromApplication(dataSet.fa)
	                .setFromServiceType(dataSet.fst)
	                .setToApplication(dataSet.ta)
	                .setToServiceType(dataSet.tst)
	                .setIncludeException(dataSet.ie);
	
	            if (angular.isNumber(dataSet.rf) && dataSet.rt) {
	                this
	                    .setResponseFrom(dataSet.rf)
	                    .setResponseTo(dataSet.rt);
	            }
	            if (dataSet.url) {
	                this.setRequestUrlPattern(dataSet.url);
	            }
	            if (dataSet.fan) {
	                this.setFromAgentName(dataSet.fan);
	            }
	            if (dataSet.tan) {
	                this.setToAgentName(dataSet.tan);
	            }
	        }
	    };
	}]);
})();