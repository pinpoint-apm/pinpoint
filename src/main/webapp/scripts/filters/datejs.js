'use strict';

pinpointApp.filter('datejs', function () {
    return function (input, format) {
        return new Date(input).format(format);
    };
});
