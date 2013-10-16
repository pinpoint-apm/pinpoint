'use strict';

pinpointApp.factory('TimeSliderDao', function () {

    return function () {
        // define and initialize private variables;
        this.nFrom = null;
        this.nTo = null;
        this.nInnerFrom = null;
        this.nInnerTo = null;
        this.nCount = 0;
        this.nTotal = 0;

        this.setFrom = function (from) {
            if (angular.isNumber(from)) {
                if (angular.isNumber(this.nTo) && from >= this.nTo) {
                    throw 'timeSliderDao:setFrom, It should be smaller than To value.';
                }
                this.nFrom = from;
            }
            return this;
        };
        this.getFrom = function () {
            return this.nFrom;
        };

        this.setTo = function (to) {
            if (angular.isNumber(to)) {
                if (angular.isNumber(to) && this.nFrom >= to) {
                    throw 'timeSliderDao:setTo, It should be bigger than From value.';
                }
                this.nTo = to;
            }
            return this;
        };
        this.getTo = function () {
            return this.nTo;
        };

        this.setInnerFrom = function (innerFrom) {
            if (angular.isNumber(innerFrom)) {
                if (angular.isNumber(this.nInnerTo) && innerFrom >= this.nInnerTo) {
                    throw 'timeSliderDao:setInnerFrom, It should be smaller than InnerTo value.';
                }
                this.nInnerFrom = innerFrom;
            }
            return this;
        };
        this.getInnerFrom = function () {
            return this.nInnerFrom;
        };

        this.setInnerTo = function (innerTo) {
            if (angular.isNumber(innerTo)) {
                if (angular.isNumber(this.nInnerFrom) && this.nInnerFrom >= innerTo) {
                    throw 'timeSliderDao:setInnerTo, It should be bigger than InnerFrom value.';
                }
                this.nInnerTo = innerTo;
            }
            return this;
        };
        this.getInnerTo = function () {
            return this.nInnerTo;
        };

        this.setCount = function (count) {
            if (angular.isNumber(count)) {
                if (angular.isNumber(this.nTotal) && count > this.nTotal) {
                    throw 'timeSliderDao:setCount, It should be smaller than Total value.';
                }
                this.nCount = count;
            }
            return this;
        };
        this.addCount = function (count) {
            if (angular.isNumber(count)) {
                if (angular.isNumber(this.nTotal) && (count + this.nCount) > this.nTotal) {
                    throw 'timeSliderDao:setCount, It should be smaller than Total value.';
                }
                this.nCount += count;
            }
            return this;
        };
        this.getCount = function () {
            return this.nCount;
        };

        this.setTotal = function (total) {
            if (angular.isNumber(total)) {
                if (angular.isNumber(this.nCount) && this.nCount > total) {
                    throw 'timeSliderDao:setTotal, It should be bigger than Count value.';
                }
                this.nTotal = total;
            }
            return this;
        };
        this.getTotal = function () {
            return this.nTotal;
        };
    };
});
