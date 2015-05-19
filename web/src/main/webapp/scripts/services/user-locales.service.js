(function() {
	'use strict';
	
	pinpointApp.factory("UserLocalesService", function( $window) {
		var defaultLocale = "en";
	    // May not be the best way to get locale.
	    var localeCode = $window.navigator.userLanguage || $window.navigator.language;
	    if ($.type(localeCode) === "string" && localeCode.length >= 2) {
	        localeCode = localeCode.substring(0, 2);
	    } else {
	        localeCode = defaultLocale;
	    }
	    return {
	    	"userLocale" : localeCode, 
	    	"defaultLocale" : defaultLocale 
	    };
	});
})();