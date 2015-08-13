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
			//@TODO
			//통계 추가할 것.
			//$at($at.FILTEREDMAP_PAGE);
			console.log( "init GeneralConfig", $element );
			
			$scope.$on("general.configuration.show", function() {
				console.log( "general show");
			});
		}
	]);
})(jQuery);
	