'use strict';

pinpointApp.filter('icons', function () {
    return function (input) {
        var icons = 'UNKNOWN_CLOUD';
        if (input.indexOf('UNKNOWN_') !== 0) {
            icons = input;
        }
        return icons;
    };
});
