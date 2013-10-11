'use strict';

pinpointApp.factory('timeSliderDao', function () {

    // define private variables;
    var nFrom, nTo, nInnerFrom, nInnerTo, nCount, nTotal;

    // initialize private variables;
    nFrom = null;
    nTo = null;
    nInnerFrom = null;
    nInnerTo = null;
    nCount = 0;
    nTotal = 0;

    // define public methods;
    return {
        setFrom: function (from) {
            if (angular.isNumber(from)) {
                if (angular.isNumber(nTo) && from >= nTo) {
                    throw 'timeSliderDao:setFrom, It should be smaller than To value.';
                }
                nFrom = from;
            }
            return this;
        },
        getFrom: function () {
            return nFrom;
        },

        setTo: function (to) {
            if (angular.isNumber(to)) {
                if (angular.isNumber(to) && nFrom >= to) {
                    throw 'timeSliderDao:setTo, It should be bigger than From value.';
                }
                nTo = to;
            }
            return this;
        },
        getTo: function () {
            return nTo;
        },

        setInnerFrom: function (innerFrom) {
            if (angular.isNumber(innerFrom)) {
                if (angular.isNumber(nInnerTo) && innerFrom >= nInnerTo) {
                    throw 'timeSliderDao:setInnerFrom, It should be smaller than InnerTo value.';
                }
                nInnerFrom = innerFrom;
            }
            return this;
        },
        getInnerFrom: function () {
            return nInnerFrom;
        },

        setInnerTo: function (innerTo) {
            if (angular.isNumber(innerTo)) {
                if (angular.isNumber(nInnerFrom) && nInnerFrom >= innerTo) {
                    throw 'timeSliderDao:setInnerTo, It should be bigger than InnerFrom value.';
                }
                nInnerTo = innerTo;
            }
            return this;
        },
        getInnerTo: function () {
            return nInnerTo;
        },

        setCount: function (count) {
            if (angular.isNumber(count)) {
                if (angular.isNumber(nTotal) && count > nTotal) {
                    throw 'timeSliderDao:setCount, It should be smaller than Total value.';
                }
                nCount = count;
            }
            return this;
        },
        addCount: function (count) {
            if (angular.isNumber(count)) {
                if (angular.isNumber(nTotal) && (count + nCount) > nTotal) {
                    throw 'timeSliderDao:setCount, It should be smaller than Total value.';
                }
                nCount += count;
            }
            return this;
        },
        getCount: function () {
            return nCount;
        },

        setTotal: function (total) {
            if (angular.isNumber(total)) {
                if (angular.isNumber(nCount) && nCount > total) {
                    throw 'timeSliderDao:setTotal, It should be bigger than Count value.';
                }
                nTotal = total;
            }
            return this;
        },
        getTotal: function () {
            return nTotal;
        }
    };
});
