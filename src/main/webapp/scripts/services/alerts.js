'use strict';

pinpointApp.factory('alerts', ['$timeout', function ($timeout) {

    var parent;

    return {
        setParent: function (element) {
            parent = element;
            return this;
        },
        getParent: function () {
            return parent;
        },
        showWarning: function (msg) {
            $timeout(function () {
                $('.warning', parent).show();
                $('.warning .msg', parent).text(msg);
            }, 300);
        },
        showInfo: function (msg) {
            $timeout(function () {
                $('.info', parent).show();
                $('.info .msg', parent).text(msg);
            }, 300);
        }
    };
}]);
