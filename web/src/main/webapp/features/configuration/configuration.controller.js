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
	    	HELP: "help"
	    }
	});	

	pinpointApp.controller('ConfigurationCtrl', [ '$scope','$element', 'ConfigurationConfig', 'AnalyticsService',
	    function ($scope, $element, $constant, analyticsService) {

			$scope.selectMember = true;
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
				$scope.$broadcast("configuration.general.initClose");
			});
			
			$scope.setCurrentTab = function( tab ) {
				if ( $scope.currentTab == tab ) return;
				$scope.currentTab = tab;
				switch( tab ) {
					case $constant.menu.GENERAL:
						analyticsService.send( analyticsService.CONST.MAIN, analyticsService.CONST.CLK_GENERAL );
						$scope.$broadcast( "general.configuration.show");
						break;
					case $constant.menu.ALARM:
						analyticsService.send( analyticsService.CONST.MAIN, analyticsService.CONST.CLK_ALARM );
						$scope.$broadcast( "alarmUserGroup.configuration.show");
						break;
					case $constant.menu.HELP:
						analyticsService.send( analyticsService.CONST.MAIN, analyticsService.CONST.CLK_HELP );
						break;
				}
			};
			$scope.getMemberButtonStyle = function() {
				return $scope.selectMember ? "btn-primary" : "btn-default";
			};
			$scope.getAlarmButtonStyle = function() {
				return $scope.selectMember ? "btn-default" : "btn-primary";
			};
			$scope.showMember = function() {
				$scope.selectMember = true;
			};
			$scope.showAlarm = function() {
				$scope.selectMember = false;
			};
			$scope.$on("configuration.show", function() {
				analyticsService.send( analyticsService.CONST.MAIN, analyticsService.CONST.CLK_CONFIGURATION );
				$element.modal('show');
				
			});
		} 
	]);
})(jQuery);
	