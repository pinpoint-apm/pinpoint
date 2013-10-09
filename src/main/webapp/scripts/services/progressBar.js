'use strict';

pinpointApp.factory('progressBar', [ '$timeout', function ($timeout) {

    var parent;

    // Public API here
    return {
        setParent: function (element) {
            parent = element;
            return this;
        },
        getParent: function () {
            return parent;
        },
        startLoading: function () {
            this.setLoading(0);
            $timeout(function () {
                $('.progress', parent).show();
            });
        },
        stopLoading: function () {
            $timeout(function () {
                $('.progress', parent).hide();
            }, 300);
        },
        setLoading: function (p) {
            $('.progress .bar', parent).width(p + '%');
        }
    };
}]);
