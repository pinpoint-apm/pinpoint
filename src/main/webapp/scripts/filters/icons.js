'use strict';

pinpointApp.filter('icons', function () {
    return function (input) {
        var icons = 'UNKNOWN';
        if (input.indexOf('UNKNOWN_') !== 0) {
            icons = input;
        }
        return icons;
    };
});
