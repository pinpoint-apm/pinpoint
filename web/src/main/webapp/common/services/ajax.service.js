(function() {
	'use strict';
	/**
	 * (en)AjaxService 
	 * @ko AjaxService
	 * @group Service
	 * @name AjaxService
	 * @class
	 */
	pinpointApp.constant('AjaxServiceConfig', {
	});
	
	pinpointApp.service('AjaxService', [ 'AjaxServiceConfig', function(cfg) {
	
		var self = this;
	
		this.getSQLBind = function(url, cb) {
			jQuery.ajax({
				type: 'POST',
				url: url,
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