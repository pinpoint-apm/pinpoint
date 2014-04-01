'use strict';

angular.module('pinpointApp')
  .filter('humanReadableNumberFormat', function () {
        return function (bytes, precision, type) {
            if (isNaN(parseFloat(bytes)) || !isFinite(bytes)) return '-';
            if (typeof precision === 'undefined') precision = 1;
            var units = type ? ['bytes', 'kB', 'MB', 'GB', 'TB', 'PB'] : ['', 'k', 'M', 'G', 'T', 'P'],
            number = bytes ? Math.floor(Math.log(bytes) / Math.log(1024)) : 0;
            return (bytes / Math.pow(1024, Math.floor(number))).toFixed(precision) + ' ' + units[number];
        }
    });
