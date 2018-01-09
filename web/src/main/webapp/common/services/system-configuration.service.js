(function( $ ) {
	"use strict";
	pinpointApp.constant("SystemConfigurationServiceConfig", {
		URL: "configuration.pinpoint"
	});

	pinpointApp.service( "SystemConfigurationService", [ "SystemConfigurationServiceConfig", "$http", function( cfg, $http ) {
		this.getConfig = function() {
			return $http.get( cfg.URL ).then(function(result) {
				if ( result.data.showApplicationStat !== true ) {
					result.data.showApplicationStat = false;
				}
				return result.data;
			});
		};
	}]);
})( jQuery );