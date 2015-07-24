(function() {
	'use strict';
	/**
	 * (en)helpContentService 
	 * @ko helpContentService
	 * @group Service
	 * @name helpContentService
	 * @class
	 */
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