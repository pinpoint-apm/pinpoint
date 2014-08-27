'use strict';

pinpointApp.filter('timeline', function () {
    return function (callStacks, key) {
        var newCallStacks = [];
        angular.forEach(callStacks, function (val) {
            if (val[key.isMethod] && !val[key.excludeFromTimeline] && val[key.service] !== '') {
                newCallStacks.push(val);
            }
        });
        return newCallStacks;
    };
});
