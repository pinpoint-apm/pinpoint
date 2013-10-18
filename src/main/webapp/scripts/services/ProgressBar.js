'use strict';

pinpointApp.factory('ProgressBar', [ '$timeout', function ($timeout) {

    return function (parent) {
        this.$parent = parent || null;

        this.setParent = function (parent) {
            this.$parent = parent;
            return this;
        }.bind(this);

        this.getParent = function () {
            return this.$parent;
        }.bind(this);

        this.startLoading = function () {
            this.setLoading(0);
            $timeout(function () {
                this.getProgress().show();
            }.bind(this));
        }.bind(this);

        this.stopLoading = function () {
            $timeout(function () {
                this.getProgress().hide();
            }.bind(this), 300);
        }.bind(this);

        this.setLoading = function (p) {
            this.getProgressBar().width(p + '%');
        }.bind(this);

        this.getProgress = function () {
            return this.$parent ? $('.progress', this.$parent) : $('.progress');
        }.bind(this);

        this.getProgressBar = function () {
            return this.$parent ? $('.progress .bar', this.$parent) : $('.progress .bar');
        }.bind(this);
    };
}]);
