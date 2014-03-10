'use strict';

pinpointApp.filter('base64', [ '$base64', function ($base64) {
    return function (input, type) {
        if (type === 'encode') {
            return $base64.encode(input);
        } else {
            return $base64.decode(input);
        }
    };
}]);
