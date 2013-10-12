'use strict';

pinpointApp.factory('progressBar', [ '$timeout', function ($timeout) {

    var $elParent, getProgress, getProgressBar;

    $elParent = null;

    getProgress = function () {
        return $elParent ? $('.progress', $elParent) : $('.progress');
    };

    getProgressBar = function () {
        return $elParent ? $('.progress .bar', $elParent) : $('.progress .bar');
    };

    // Public API here
    return {
        setParent: function (element) {
            $elParent = element;
            return this;
        },
        getParent: function () {
            return $elParent;
        },
        startLoading: function () {
            this.setLoading(0);
            $timeout(function () {
                getProgress().show();
            });
        },
        stopLoading: function () {
            $timeout(function () {
                getProgress().hide();
            }, 300);
        },
        setLoading: function (p) {
            getProgressBar().width(p + '%');
        }
    };
}]);
