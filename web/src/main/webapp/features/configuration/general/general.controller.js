(function($) {
	'use strict';
	/**
	 * (en)GeneralCtrl 
	 * @ko GeneralCtrl
	 * @group Controller
	 * @name GeneralCtrl
	 * @class
	 */
	pinpointApp.constant('GeneralConfig', {
	    menu: {
	    	GENERAL: "general",
	    	ALRAM: "alram"
	    }
	});	

	pinpointApp.controller('GeneralCtrl', [ '$scope','$element', 'GeneralConfig',
	    function ($scope, $element, $constant) {
			$scope.$on("general.configuration.show", function() {

			});
		}
	]);
})(jQuery);
	