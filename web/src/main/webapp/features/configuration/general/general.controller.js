(function($) {
	'use strict';
	/**
	 * (en)GeneralCtrl 
	 * @ko GeneralCtrl
	 * @group Controller
	 * @name GeneralCtrl
	 * @class
	 */
	pinpointApp.constant( "GeneralConfig", {
	    menu: {
	    	GENERAL: "general",
	    	ALRAM: "alram"
	    }
	});	

	pinpointApp.controller( "GeneralCtrl", [ "GeneralConfig", "$scope", "$rootScope", "$timeout", "$element", "$document", "PreferenceService", "AnalyticsService", "helpContentService",
	    function ($config, $scope, $rootScope, $timeout, $element, $document, preferenceService, analyticsService, helpContentService) {

			$element.find("div.general-warning").html(helpContentService.configuration.general.warning);
			$element.find("div.favorite-empty").html(helpContentService.configuration.general.empty);
			$scope.$on("general.configuration.show", function() {
			});
			$scope.depthList = preferenceService.getDepthList();
			$scope.periodTypes = preferenceService.getPeriodTypes();
			$scope.caller = preferenceService.getCaller();
			$scope.callee = preferenceService.getCallee();
			$scope.period = preferenceService.getPeriod();
			$scope.savedFavoriteList = preferenceService.getFavoriteList();

			var $applicationList = $element.find(".applicationList");
			var $depthPopup = $element.find(".inout-bound");
			var bCloseDepthPopup = false;
			
			$scope.changeCaller = function( caller ) {
				$scope.caller = caller;
				analyticsService.send( analyticsService.CONST.MAIN, analyticsService.CONST.CLK_GENERAL_SET_DEPTH, $scope.caller );
				preferenceService.setCaller( $scope.caller );
			};
			$scope.changeCallee = function( callee ) {
				$scope.callee = callee;
				analyticsService.send( analyticsService.CONST.MAIN, analyticsService.CONST.CLK_GENERAL_SET_DEPTH, $scope.callee );
				preferenceService.setCallee( $scope.callee );
			};
			$scope.changePeriod = function() {
				analyticsService.send( analyticsService.CONST.MAIN, analyticsService.CONST.CLK_GENERAL_SET_PERIOD, $scope.period );
				preferenceService.setPeriod( $scope.period );
			};
			$scope.removeFavorite = function( applicationName ) {
				analyticsService.send( analyticsService.CONST.MAIN, analyticsService.CONST.CLK_GENERAL_SET_FAVORITE );
				preferenceService.removeFavorite( applicationName );
				$scope.savedFavoriteList = preferenceService.getFavoriteList();
				$rootScope.$broadcast("navbarDirective.changedFavorite");
			};

			function addToFavoriteList( applicationName ) {
				analyticsService.send( analyticsService.CONST.MAIN, analyticsService.CONST.CLK_GENERAL_SET_FAVORITE );
				preferenceService.addFavorite( applicationName );
				$scope.$apply(function() {
					$scope.savedFavoriteList = preferenceService.getFavoriteList();
					$rootScope.$broadcast("navbarDirective.changedFavorite");
				});
			}
			function formatOptionText(state) {
				if (!state.id) {
					return state.text;
				}
				var chunk = state.text.split("@");
				if (chunk.length > 1) {
					var $img = $("<img>").attr({
						"src":"images/icons/" + chunk[1] + ".png"
					}).css({
						"height": "25px",
						"paddingRight": "3px"
					});
					return $img.get(0).outerHTML + "<span>" + chunk[0] + "</span>";
				} else {
					return state.text;
				}
			}
			function initApplicationSelect() {
				$applicationList.select2({
	                placeholder: "Select an application.",
	                searchInputPlaceholder: "Input your application name.",
	                allowClear: false,
	                templateResult: formatOptionText,
	                templateSelection: formatOptionText,
	                escapeMarkup: function (m) {
	                    return m;
	                }
				});
				$applicationList.on("select2:select", function(e) {
					$timeout(function() {
						addToFavoriteList( $applicationList.val() );
					});
				});

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
			$scope.closeInOut = function() {
				bCloseDepthPopup = true;
				$depthPopup.trigger("click.bs.dropdown");
			};
			$scope.$on("configuration.general.applications.set", function( event, applicationData ) {
				$scope.applications = applicationData;
				initApplicationSelect();
			});
			$scope.$on("configuration.general.initClose", function() {
				$scope.closeInOut();
			});
		}
	]);
})(jQuery);
	