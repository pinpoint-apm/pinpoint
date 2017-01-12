(function( $ ) {
	"use strict";

	pinpointApp.directive( "generalDirective", [ "PreferenceService", "UserConfigurationService", "AnalyticsService", "helpContentService",
		function ( PreferenceService, UserConfigService, AnalyticsService, helpContentService ) {
			return {
				restrict: "EA",
				replace: true,
				templateUrl: "features/configuration/general/general.html?v=" + G_BUILD_TIME,
				scope: {
					namespace: "@"
				},
				link: function( scope, element, attr ) {
					var $element = element;
					var myName = attr["name"];
					var $depthPopup = $element.find(".inout-bound");
					var bCloseDepthPopup = false;
					$element[ attr["initState"] ]();
					scope.savedFavoriteList = [];

					init();

					function init() {
						scope.depthList = PreferenceService.getDepthList();
						scope.caller = UserConfigService.getCaller();
						scope.callee = UserConfigService.getCallee();
						scope.periodTime = PreferenceService.getPeriodTime();
						scope.period = UserConfigService.getPeriod();
						UserConfigService.getFavoriteList(function(aFavoriteList) {
							scope.savedFavoriteList = aFavoriteList;
						}, true);
						scope.timezone = moment.tz.names();
						scope.userTimezone = UserConfigService.getTimezone();
						scope.newUserTimezone = UserConfigService.getTimezone();

						$element.find( "div.general-warning" ).html( helpContentService.configuration.general.warning );
						$element.find( "div.favorite-empty" ).html( helpContentService.configuration.general.empty );

						$depthPopup.on("hide.bs.dropdown", function( event ) {
							if ( bCloseDepthPopup === false ) {
								event.preventDefault();
								bCloseDepthPopup = false;
							}
						});
						$depthPopup.on("click", function() {
							bCloseDepthPopup = false;
						});
					}
					function addToFavoriteList( newAppName, newAppCode ) {
						AnalyticsService.sendMain( AnalyticsService.CONST.CLK_GENERAL_SET_FAVORITE );
						UserConfigService.addFavorite( newAppName, newAppCode, function() {
							UserConfigService.getFavoriteList(function (aFavoriteList) {
								scope.savedFavoriteList = aFavoriteList;
							}, true);
							scope.$emit("up.changed.favorite");
						});
					}
					scope.changeCaller = function( caller ) {
						scope.caller = caller;
						AnalyticsService.sendMain( AnalyticsService.CONST.CLK_GENERAL_SET_DEPTH, scope.caller );
						UserConfigService.setCaller( scope.caller );
					};
					scope.changeCallee = function( callee ) {
						scope.callee = callee;
						AnalyticsService.sendMain( AnalyticsService.CONST.CLK_GENERAL_SET_DEPTH, scope.callee );
						UserConfigService.setCallee( scope.callee );
					};
					scope.changePeriod = function() {
						AnalyticsService.sendMain( AnalyticsService.CONST.CLK_GENERAL_SET_PERIOD, scope.period );
						UserConfigService.setPeriod( scope.period );
					};
					scope.removeFavorite = function( appName, appType ) {
						AnalyticsService.sendMain( AnalyticsService.CONST.CLK_GENERAL_SET_FAVORITE );
						UserConfigService.removeFavorite( appName, appType, function() {
							UserConfigService.getFavoriteList(function (aFavoriteList) {
								scope.savedFavoriteList = aFavoriteList;
							}, true);
							scope.$emit("up.changed.favorite");
						});
					};
					scope.applyNReload = function() {
						AnalyticsService.sendMain( AnalyticsService.CONST.CLK_GENERAL_SET_TIMEZONE, scope.newUserTimezone );
						UserConfigService.setTimezone( scope.newUserTimezone );
						window.location.reload(true);
					};

					scope.closeInOut = function() {
						bCloseDepthPopup = true;
						$depthPopup.trigger( "click.bs.dropdown" );
					};
					scope.$on( "configuration.general.initClose", function() {
						scope.closeInOut();
					});

					scope.$on( "configuration.selectMenu", function( event, selectedName ) {
						if ( myName === selectedName ) {
							$element.show();
						} else {
							$element.hide();
						}
					});
					scope.$on( "up.changed.application", function( event, invokeId, newAppName, newAppCode ) {
						addToFavoriteList( newAppName, newAppCode, invokeId );
						event.stopPropagation();
					});
				}
			};
		}
	]);
})( jQuery );