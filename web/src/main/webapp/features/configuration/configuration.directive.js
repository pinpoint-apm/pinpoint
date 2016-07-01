(function( $ ) {
	'use strict';
	/**
	 * (en)configurationDirective
	 * @ko configurationDirective
	 * @group Directive
	 * @name configurationDirective
	 * @class
	 */
	pinpointApp.constant('ConfigurationDirectiveConfig', {
		menu: [{
			"view": "General",
			"value": "general"
		}, {
			"view": "User Group",
			"value": "userGroup"
		}, {
			"view": "Application",
			"value": "application"
		}, {
			"view": "Help",
			"value": "help"
		}]
	});
	pinpointApp.directive( "configurationDirective", [ "ConfigurationDirectiveConfig", "$rootScope", "$timeout", "AnalyticsService",
		function ( cfg, $rootScope, $timeout, AnalyticsService ) {
			return {
				restrict: "EA",
				replace: true,
				// templateUrl: "features/configuration/configurationTemplate.html?v=" + G_BUILD_TIME,
				templateUrl: "features/configuration/configuration.html?v=" + G_BUILD_TIME,
				scope: {
					namespace: "@"
				},
				link: function(scope, element, attr) {
					// scope.configurationHtml = "features/configuration/configurationEmpty.html?v=" + G_BUILD_TIME;
					var $element = $(element);
					var currentMenuName = "";
					// var bIsFirstOpen = true;
					var bChangedFavorite = false;
					scope.menuInfo = cfg.menu;

					init();
					function init() {
						// scope.configurationHtml = "features/configuration/configuration.html?v=" + G_BUILD_TIME;
						// $timeout(function() {
						// 	$element = $element.find("div.modal");

							initCurrentMenu();
							$element.on("show.bs.modal", function (e) {
								$(this).removeAttr("tabindex");
							});
							$element.on("hidden.bs.modal", function (e) {
								if (bChangedFavorite) {
									$rootScope.$broadcast("navbarDirective.changedFavorite");
									$rootScope.$broadcast("down.changed.favorite");
								} else {
									//console.log("not changed");
								}
								// initCurrentMenu();
								// $scope.$broadcast("configuration.alarm.initClose");
								scope.$broadcast("configuration.general.initClose");
							});
							// bIsFirstOpen = false;
							// $element.modal( "show" );
						// });
					}
					function initCurrentMenu() {
						currentMenuName = scope.menuInfo[0].value;
					}

					scope.isSelected = function( menuName ) {
						return currentMenuName === menuName;
					};
					scope.selectMenu = function( menuName ) {
						currentMenuName = menuName;
						scope.$broadcast( "configuration.selectMenu", currentMenuName );
					};
					scope.$on( "configuration.open", function() {
						AnalyticsService.sendMain( AnalyticsService.CONST.CLK_CONFIGURATION );
						bChangedFavorite = false;
						// if ( bIsFirstOpen === true ) {
						// 	init();
						// } else {
							$element.modal( "show" );
						// }
					});
					scope.$on( "up.changed.favorite", function( event ) {
						bChangedFavorite = true;
						event.stopPropagation();
					});
				}
			};
		}
	]);
})( jQuery );