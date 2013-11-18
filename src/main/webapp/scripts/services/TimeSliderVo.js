'use strict';

pinpointApp.factory('TimeSliderVo', function () {
    return function () {
        // define and initialize private variables;
        this._nFrom = false;
        this._nTo = false;
        this._nInnerFrom = false;
        this._nInnerTo = false;
        this._nCount = false;
        this._nTotal = false;

        this.setFrom = function (from) {
            if (angular.isNumber(from)) {
                if (angular.isNumber(this._nTo) && from >= this._nTo) {
                    throw 'timeSliderVo:setFrom, It should be smaller than To value.';
                }
                this._nFrom = from;
            }
            return this;
        }.bind(this);
        this.getFrom = function () {
            return this._nFrom;
        }.bind(this);

        this.setTo = function (to) {
            if (angular.isNumber(to)) {
                if (angular.isNumber(to) && this._nFrom >= to) {
                    throw 'timeSliderVo:setTo, It should be bigger than From value.';
                }
                this._nTo = to;
            }
            return this;
        }.bind(this);
        this.getTo = function () {
            return this._nTo;
        }.bind(this);

        this.setInnerFrom = function (innerFrom) {
            if (angular.isNumber(innerFrom)) {
                if (angular.isNumber(this._nInnerTo) && innerFrom >= this._nInnerTo) {
                    throw 'timeSliderVo:setInnerFrom, It should be smaller than InnerTo value.';
                }
                this._nInnerFrom = innerFrom;
            }
            return this;
        }.bind(this);
        this.getInnerFrom = function () {
            return this._nInnerFrom;
        }.bind(this);

        this.setInnerTo = function (innerTo) {
            if (angular.isNumber(innerTo)) {
                if (angular.isNumber(this._nInnerFrom) && this._nInnerFrom >= innerTo) {
                    throw 'timeSliderVo:setInnerTo, It should be bigger than InnerFrom value.';
                }
                this._nInnerTo = innerTo;
            }
            return this;
        }.bind(this);
        this.getInnerTo = function () {
            return this._nInnerTo;
        }.bind(this);

        this.setCount = function (count) {
            if (angular.isNumber(count)) {
                if (angular.isNumber(this._nTotal) && count > this._nTotal) {
                    throw 'timeSliderVo:setCount, It should be smaller than Total value.';
                }
                this._nCount = count;
            }
            return this;
        }.bind(this);
        this.addCount = function (count) {
            if (angular.isNumber(count)) {
                if (angular.isNumber(this._nTotal) && (count + this._nCount) > this._nTotal) {
                    throw 'timeSliderVo:setCount, It should be smaller than Total value.';
                }
                this._nCount += count;
            }
            return this;
        }.bind(this);
        this.getCount = function () {
            return this._nCount;
        }.bind(this);

        this.setTotal = function (total) {
            if (angular.isNumber(total)) {
                if (angular.isNumber(this._nCount) && this._nCount > total) {
                    throw 'timeSliderVo:setTotal, It should be bigger than Count value.';
                }
                this._nTotal = total;
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
