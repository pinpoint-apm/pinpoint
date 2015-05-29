(function() {
	'use strict';
	
	pinpointApp.factory('helpContentService', [ '$window', '$injector', 'UserLocalesService', function($window, $injector, UserLocalesService) {

		var name = "helpContent-" + UserLocalesService.userLocale;
		var defaultName = "helpContent-" + UserLocalesService.defaultLocale;
		if ($injector.has(name)) {
			return $injector.get(name);
		} else {
			return $injector.get(defaultName);
		}
	}]);
})();