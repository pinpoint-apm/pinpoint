(function($) {
	'use strict';

	pinpointApp.service("InstallationAjaxService", [ "$http", function ($http) {
		this.getAgentInstallationInfo = function(callback) {
			retrieve("getAgentInstallationInfo.pinpoint", {}, callback);
		};
		this.isAvailableApplicationName = function(data, callback ) {
			retrieve("isAvailableApplicationName.pinpoint", data, callback);
		};
		this.isAvailableAgentId = function(data, callback ) {
			retrieve("isAvailableAgentId.pinpoint", data, callback);
		};
		function retrieve(url, data, callback) {
			$http.get( url + "?" + $.param( data ) ).then(function(result) {
				callback(result.data);
			}, function(error) {
				callback(error);
			});
		}
	}]);
})(jQuery);