'use strict';

pinpointApp.filter('timelineMarginLeft', function () {
    return function (stack, key, barRatio, callStackStart) {
        return ((stack[key.begin] - callStackStart) * barRatio) + 0.9;
    };
});
