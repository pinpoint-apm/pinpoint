(function() {
	'use strict';
	
	pinpointApp.factory('helpContent', [ '$window', '$injector', 'UserLocales', function($window, $injector, UserLocales) {
	//  var defaultLocale = "en";
	//  // May not be the best way to get locale.
	//  var localeCode = $window.navigator.userLanguage || $window.navigator.language;
	//  if ($.type(localeCode) === "string" && localeCode.length >= 2) {
	//      localeCode = localeCode.substring(0, 2);
	//  } else {
	//      localeCode = defaultLocale;
	//  }
		var name = "helpContent-" + UserLocales.userLocale;
		var defaultName = "helpContent-" + UserLocales.defaultLocale;
	//  var name = "helpContent-" + localeCode;
	//  var defaultName = "helpContent-" + defaultLocale;
	  if ($injector.has(name)) {
	    return $injector.get(name);
	  } else {
	    return $injector.get(defaultName);
	  }
	}]);
})();