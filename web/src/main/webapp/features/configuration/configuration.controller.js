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
	    	ALARM: "alarm",
	    	HELP: "help",
	    }
	});	

	pinpointApp.controller('ConfigurationCtrl', [ '$scope','$element', 'ConfigurationConfig', 'AnalyticsService',
	    function ($scope, $element, $constant, analyticsService) {

			var $elBody = $element.find(".modal-body");
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
			
			$($element).on("hidden.bs.modal", function(e) {
				$scope.currentTab = $constant.menu.GENERAL;
				$scope.$broadcast("configuration.alarm.initClose");
			});
			
			$scope.setCurrentTab = function( tab ) {
				if ( $scope.currentTab == tab ) return;
				$scope.currentTab = tab;
				switch( tab ) {
					case $constant.menu.GENERAL:
						analyticsService.send( analyticsService.CONST.MAIN, analyticsService.CONST.CLK_GENERAL );
						$elBody.css("background-color", "#e9eaed");
						$scope.descriptionOfCurrentTab = "Set your option";
						$scope.$broadcast( "general.configuration.show");
						break;
					case $constant.menu.ALARM:
						analyticsService.send( analyticsService.CONST.MAIN, analyticsService.CONST.CLK_ALARM );
						$elBody.css("background-color", "#e9eaed");
						$scope.descriptionOfCurrentTab = "Set your alarm rules";
						$scope.$broadcast( "alarmUserGroup.configuration.show");
						break;
					case $constant.menu.HELP:
						analyticsService.send( analyticsService.CONST.MAIN, analyticsService.CONST.CLK_HELP );
						$elBody.css("background-color", "#FFF");
						$scope.descriptionOfCurrentTab = "";
						break;	
				}
			};
			$scope.$on("configuration.show", function() {
				analyticsService.send( analyticsService.CONST.MAIN, analyticsService.CONST.CLK_CONFIGURATION );
				$element.modal('show');
				
			});
		}
	]);
})(jQuery);
	