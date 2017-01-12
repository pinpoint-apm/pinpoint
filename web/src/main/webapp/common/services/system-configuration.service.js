(function( $ ) {
	"use strict";
	pinpointApp.constant("SystemConfigurationServiceConfig", {
		URL: "configuration.pinpoint"
	});

	pinpointApp.service( "SystemConfigurationService", [ "SystemConfigurationServiceConfig", "$http", function( cfg, $http ) {
		var oConfig = {};
		this.getConfig = function() {
			return $http.get( cfg.URL ).then(function(result) {
				return oConfig = result.data;
			});
		};
		this.get = function( type ) {
			return oConfig[type];
		};
	}]);
})( jQuery );