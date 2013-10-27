'use strict';

pinpointApp.factory('ProgressBar', [ '$timeout', function ($timeout) {

    return function (parent) {
        this.$parent = parent || null;
        this.nPercentage = 0;
        this.bAutoIncrease = true;
        this.nTimePromise = null;

        this.setParent = function (parent) {
            this.$parent = parent;
            return this;
        }.bind(this);

        this.getParent = function () {
            return this.$parent;
        }.bind(this);

        this.startLoading = function (autoIncrease) {
            this.bAutoIncrease = autoIncrease || true;
            this.setLoading(0);
            $timeout(function () {
                this.getProgress().show();
                this.autoIncrease();
            }.bind(this));
        }.bind(this);

        this.stopLoading = function () {
            $timeout.cancel(this.nTimePromise);
            $timeout(function () {
                this.getProgress().hide();
            }.bind(this), 300);
        }.bind(this);

        this.setLoading = function (p) {
            if (p < this.nPercentage) {
                return this;
            }
            this.nPercentage = p;
            this.getProgressBar().width(p + '%');
            return this;
        }.bind(this);

        this.getProgress = function () {
            return this.$parent ? $('.progress', this.$parent) : $('.progress');
        }.bind(this);

        this.getProgressBar = function () {
            return this.$parent ? $('.progress .bar', this.$parent) : $('.progress .bar');
        }.bind(this);

        this.autoIncrease = function () {
            if (this.bAutoIncrease === false) {
                return;
            }
            var nRandom = _.random(1, 4);
            if (this.nPercentage + nRandom <= 99) {
                this.setLoading(this.nPercentage + nRandom);

                this.nTimePromise = $timeout(function () {
                    this.autoIncrease();
                }.bind(this), 500);
            }
        }.bind(this);
    };
}]);
