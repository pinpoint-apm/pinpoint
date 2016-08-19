(function( $ ) {
	"use strict";

	pinpointApp.directive( "generalDirective", [ "PreferenceService", "AnalyticsService", "helpContentService",
		function ( PreferenceService, AnalyticsService, helpContentService ) {
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

					init();

					function init() {
						scope.depthList = PreferenceService.getDepthList();
						scope.caller = PreferenceService.getCaller();
						scope.callee = PreferenceService.getCallee();
						scope.periodTime = PreferenceService.getPeriodTime();
						scope.period = PreferenceService.getPeriod();
						scope.savedFavoriteList = PreferenceService.getFavoriteList();
						scope.timezone = moment.tz.names();
						scope.userTimezone = PreferenceService.getTimezone();
						scope.newUserTimezone = PreferenceService.getTimezone();

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
					function addToFavoriteList( newAppName ) {
						AnalyticsService.sendMain( AnalyticsService.CONST.CLK_GENERAL_SET_FAVORITE );
						PreferenceService.addFavorite( newAppName );
						scope.$apply(function() {
							scope.savedFavoriteList = PreferenceService.getFavoriteList();
							scope.$emit( "up.changed.favorite" );
						});
					}
					scope.changeCaller = function( caller ) {
						scope.caller = caller;
						AnalyticsService.sendMain( AnalyticsService.CONST.CLK_GENERAL_SET_DEPTH, scope.caller );
						PreferenceService.setCaller( scope.caller );
					};
					scope.changeCallee = function( callee ) {
						scope.callee = callee;
						AnalyticsService.sendMain( AnalyticsService.CONST.CLK_GENERAL_SET_DEPTH, scope.callee );
						PreferenceService.setCallee( scope.callee );
					};
					scope.changePeriod = function() {
						AnalyticsService.sendMain( AnalyticsService.CONST.CLK_GENERAL_SET_PERIOD, scope.period );
						PreferenceService.setPeriod( scope.period );
					};
					scope.removeFavorite = function( applicationName ) {
						AnalyticsService.sendMain( AnalyticsService.CONST.CLK_GENERAL_SET_FAVORITE );
						PreferenceService.removeFavorite( applicationName );
						scope.savedFavoriteList = PreferenceService.getFavoriteList();
						scope.$emit( "up.changed.favorite" );
					};
					scope.applyNReload = function() {
						AnalyticsService.sendMain( AnalyticsService.CONST.CLK_GENERAL_SET_TIMEZONE, scope.newUserTimezone );
						PreferenceService.setTimezone( scope.newUserTimezone );
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
					scope.$on( "up.changed.application", function( event, invokeId, newAppName ) {
						addToFavoriteList( newAppName, invokeId );
						event.stopPropagation();
					});
				}
			};
		}
	]);
})( jQuery );