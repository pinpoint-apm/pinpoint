(function() {
	'use strict';
	/**
	 * (en)TimeSliderVoService 
	 * @ko TimeSliderVoService
	 * @group Service
	 * @name TimeSliderVoService
	 * @class
	 */
	pinpointApp.factory('TimeSliderVoService', function () {
	    return function () {
	        // define and initialize private variables;
	        this._nFrom = false;
	        this._nTo = false;
	        this._nInnerFrom = false;
	        this._nInnerTo = false;
	        this._nCount = false;
	        this._nTotal = false;
	
	        this.setFrom = function (from) {
	            from = parseInt(from, 10);
	            if (angular.isNumber(from)) {
	                if (angular.isNumber(this._nTo) && from >= this._nTo) {
	                    throw 'timeSliderVoService:setFrom, It should be smaller than To value.';
	                }
	                this._nFrom = from;
	            } else {
	                throw new Error('timeSliderVoService:setFrom, It should be number. ' + from);
	            }
	            return this;
	        }.bind(this);
	        this.getFrom = function () {
	            return this._nFrom;
	        }.bind(this);
	
	        this.setTo = function (to) {
	            to = parseInt(to, 10);
	            if (angular.isNumber(to)) {
	                if (angular.isNumber(to) && this._nFrom >= to) {
	                    throw 'timeSliderVoService:setTo, It should be bigger than From value.';
	                }
	                this._nTo = to;
	            } else {
	                throw new Error('timeSliderVoService:setTo It should be number. ' + to);
	            }
	            return this;
	        }.bind(this);
	        this.getTo = function () {
	            return this._nTo;
	        }.bind(this);
	
	        this.setInnerFrom = function (innerFrom) {
	            innerFrom = parseInt(innerFrom, 10);
	            if (angular.isNumber(innerFrom)) {
	                if (angular.isNumber(this._nInnerTo) && innerFrom >= this._nInnerTo) {
	                    throw 'timeSliderVo:setInnerFrom, It should be smaller than InnerTo value.';
	                }
	                this._nInnerFrom = innerFrom;
	            } else {
	                throw new Error('timeSliderVo:setInnerFrom It should be number. ' + innerFrom);
	            }
	            return this;
	        }.bind(this);
	        this.getInnerFrom = function () {
	            return this._nInnerFrom;
	        }.bind(this);
	
	        this.setInnerTo = function (innerTo) {
	            innerTo = parseInt(innerTo, 10);
	            if (angular.isNumber(innerTo)) {
	                if (angular.isNumber(this._nInnerFrom) && this._nInnerFrom >= innerTo) {
	                    throw 'timeSliderVoService:setInnerTo, It should be bigger than InnerFrom value.';
	                }
	                this._nInnerTo = innerTo;
	            }    else {
	                throw new Error('timeSliderVoService:setInnerTo It should be number. ' + innerTo);
	            }
	            return this;
	        }.bind(this);
	        this.getInnerTo = function () {
	            return this._nInnerTo;
	        }.bind(this);
	
	        this.setCount = function (count) {
	            count = parseInt(count, 10);
	            if (angular.isNumber(count)) {
	                if (angular.isNumber(this._nTotal) && count > this._nTotal) {
	                    throw 'timeSliderVoService:setCount, It should be smaller than Total value.';
	                }
	                this._nCount = count;
	            }   else {
	                throw new Error('timeSliderVoService:setCount It should be number. ' + count);
	            }
	            return this;
	        }.bind(this);
	        this.addCount = function (count) {
	            count = parseInt(count, 10);
	            if (angular.isNumber(count)) {
	                if (angular.isNumber(this._nTotal) && (count + this._nCount) > this._nTotal) {
	                    throw 'timeSliderVoService:setCount, It should be smaller than Total value.';
	                }
	                this._nCount += count;
	            }          else {
	                throw new Error('timeSliderVoService:setCount It should be number. ' + count);
	            }
	            return this;
	        }.bind(this);
	        this.getCount = function () {
	            return this._nCount;
	        }.bind(this);
	
	        this.setTotal = function (total) {
	            total = parseInt(total, 10);
	            if (angular.isNumber(total)) {
	                if (angular.isNumber(this._nCount) && this._nCount > total) {
	                    throw 'timeSliderVoService:setTotal, It should be bigger than Count value.';
	                }
	                this._nTotal = total;
	            } else {
	                throw new Error('timeSliderVoService:setTotal It should be number. ' + total);
	            }
	            return this;
	        }.bind(this);
	        this.getTotal = function () {
	            return this._nTotal;
	        }.bind(this);
	
	        this.getReady = function () {
	            return this._nFrom && this._nTo && this._nInnerFrom && this._nInnerTo;
	        }.bind(this);
	    };
	});
})();