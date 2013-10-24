'use strict';

pinpointApp.factory('Alerts', ['$timeout', function ($timeout) {

    return function (parent) {
        this.$parent = parent || null;

        this.setParent = function (parent) {
            this.$parent = parent;
            return this;
        }.bind(this);

        this.getParent = function () {
            return this.$parent;
        }.bind(this);

        this.showError = function (msg) {
            $timeout(function () {
                this.getElement('.error').show();
                this.getElement('.error .msg').text(msg);
            }.bind(this), 300);
        }.bind(this);

        this.showWarning = function (msg) {
            $timeout(function () {
                this.getElement('.warning').show();
                this.getElement('.warning .msg').text(msg);
            }.bind(this), 300);
        }.bind(this);

        this.showInfo = function (msg) {
            $timeout(function () {
                this.getElement('.info').show();
                this.getElement('.info .msg').text(msg);
            }.bind(this), 300);
        }.bind(this);

        this.getElement = function (selector) {
            return this.$parent ? $(selector, this.$parent) : $(selector);
        }.bind(this);
    };
}]);
