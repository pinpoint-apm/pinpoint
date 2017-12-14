(function() {
	'use strict';

	pinpointApp.service( "CommonUtilService", [ function() {
		var dateTimeFormat = "YYYY-MM-DD-HH-mm-ss";
		this.getRandomNum = function () {
			return Math.round(Math.random() * 10000);
		};
		this.formatDate = function (time, format) {
			return moment(new Date(time)).format(format || dateTimeFormat);
		};
		this.getMilliSecond = function (timeStr) {
			return moment(timeStr, dateTimeFormat).valueOf();
		};
		this.isEmpty = function (target) {
			return angular.isUndefined(target) || target === null || target === "";
		};
		this.random = function (start, end) {
			return Math.floor(Math.random() * ( end - start + 1 )) + start;
		};
		this.addComma = function (num) {
			return (num + "").replace(/(\d)(?=(?:\d{3})+(?!\d))/g, '$1,');
		};
	}]);
})();