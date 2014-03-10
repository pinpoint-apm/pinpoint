'use strict';

pinpointApp.filter('applicationNameToClassName', function () {
    return function (input) {
        return input.replace(/\./gi,'_').replace(/\^/gi,'~');
    };
});
