(function($) {
	'use strict';
	/**
	 * (en)ConfigurationCtrl 
	 * @ko ConfigurationCtrl
	 * @group Controller
	 * @name ConfigurationCtrl
	 * @class
	 */
	pinpointApp.constant('ConfigurationConfig', {
	    menu: {
	    	GENERAL: "general",
	    	ALARM: "alarm"
	    }
	});	

	pinpointApp.controller('ConfigurationCtrl', [ '$scope','$element', 'ConfigurationConfig',
	    function ($scope, $element, $constant) {
			//@TODO
			//통계 추가할 것.
			//$at($at.FILTEREDMAP_PAGE);
			console.log( "init ConfigurationCtrl", $element );
						
			$scope.descriptionOfCurrentTab = "Set your option";
			$scope.currentTab = $constant.menu.GENERAL;
			
			// define isGeneral(), isAlram()... func. 
			for( var menu in $constant.menu ) {
				(function( currentMenu ) {
					var funcName = "is" + currentMenu.substring(0, 1).toUpperCase() + currentMenu.substring(1).toLowerCase();
					$scope[funcName] = function() {
						return $scope.currentTab == $constant.menu[currentMenu];
					};
				})(menu);
			}
			
			$scope.setCurrentTab = function( tab ) {
				if ( $scope.currentTab == tab ) return;
				$scope.currentTab = tab;
				switch( tab ) {
					case $constant.menu.GENERAL:
						$scope.descriptionOfCurrentTab = "Set your option";
						$scope.$broadcast( "general.configuration.show");
						break;
					case $constant.menu.ALARM:
						$scope.descriptionOfCurrentTab = "Set your alarm rules";
						$scope.$broadcast( "alarmUserGroup.configuration.show");
						break;
				}
			}
			$scope.$on("configuration.show", function() {
				$element.modal('show');
			});
		}
	]);
})(jQuery);
	