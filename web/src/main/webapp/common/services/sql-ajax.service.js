(function() {
	'use strict';
	/**
	 * (en)SQLAjaxService
	 * @ko SQLAjaxService
	 * @group Service
	 * @name SQLAjaxService
	 * @class
	 */
	pinpointApp.constant('SQLAjaxServiceConfig', {
	});
	
	pinpointApp.service('SQLAjaxService', [ 'SQLAjaxServiceConfig', function(cfg) {
	
		var self = this;
	
		this.getSQLBind = function(url, data, cb) {
			jQuery.ajax({
				type: 'POST',
				url: url,
				data: data,
				cache: false,
				dataType: 'json',
				success: function (result) {
					if (angular.isFunction(cb)) {
						cb(result);
					}
				},
				error: function (xhr, status, error) {
					if (angular.isFunction(cb)) {
						cb(error);
					}
				}
			});
		};
	}]);
})();