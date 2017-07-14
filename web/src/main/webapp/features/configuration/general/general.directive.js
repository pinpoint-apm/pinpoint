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
					var $elFavoriteList = $element.find("ul.favorite-list");
					var myName = attr["name"];
					var $depthPopup = $element.find(".inout-bound");
					var bCloseDepthPopup = false;
					$element[ attr["initState"] ]();
					scope.savedFavoriteList = [];

					init();

					function renderList() {
						$elFavoriteList.empty();
						for( var i = 0 ; i < scope.savedFavoriteList.length ; i++ ) {
							var oFavor = scope.savedFavoriteList[i];
							renderElement( oFavor.applicationName, oFavor.serviceType, oFavor.code );
						}
					}
					function renderElement( appName, appType, code ) {
						$elFavoriteList.append(
							'<li data-name="' + appName + '" data-type="' + appType + '" data-code="' + code + '"><img src="images/icons/' + appType + '.png" height="25px"/>' + appName + '<button class="btn btn-danger btn-xs" style="float:right"><span class="glyphicon glyphicon-remove" aria-hidden="true"></span></button></li>'
						);
					}
					function init() {
						scope.depthList = PreferenceService.getDepthList();
						scope.caller = UserConfigService.getCaller();
						scope.callee = UserConfigService.getCallee();
						scope.periodTime = PreferenceService.getPeriodTime();
						scope.period = UserConfigService.getPeriod();
						UserConfigService.getFavoriteList(function(aFavoriteList) {
							scope.savedFavoriteList = aFavoriteList;
							renderList();
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
								scope.$emit("up.changed.favorite");
								renderElement( newAppName.split("@")[0], newAppName.split("@")[1], newAppCode );
							}, true);
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
					scope.removeFavorite = function( $event ) {
						var tagName = $event.target.tagName.toLowerCase();
						if ( tagName === "button" || tagName === "span" ) {
							var $elLi = $( $event.target ).parents("li");
							AnalyticsService.sendMain( AnalyticsService.CONST.CLK_GENERAL_SET_FAVORITE );
							UserConfigService.removeFavorite( $elLi.attr("data-name"), $elLi.attr("data-type"), function() {
								UserConfigService.getFavoriteList(function (aFavoriteList) {
									scope.savedFavoriteList = aFavoriteList;
									scope.$emit("up.changed.favorite");
									$elLi.remove();
								}, true);
							});
						}
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