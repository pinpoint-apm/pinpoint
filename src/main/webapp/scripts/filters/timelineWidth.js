'use strict';

pinpointApp.filter('timelineWidth', function () {
    return function (stack, key, barRatio) {
        return ((stack[key.end] - stack[key.begin]) * barRatio) + 0.9;
    };
});
