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

	pinpointApp.controller('GeneralCtrl', [ 'GeneralConfig', '$scope', '$rootScope', '$element', '$document', 'PreferenceService', 'AnalyticsService', 'helpContentService',
	    function ($config, $scope, $rootScope, $element, $document, preferenceService, analyticsService, helpContentService) {

			$element.find("span.general-warning").html(helpContentService.configuration.general.warning);
			$scope.$on("general.configuration.show", function() {
			});
			$scope.depthList = preferenceService.getDepthList();
			$scope.periodTypes = preferenceService.getPeriodTypes();
			$scope.depth = preferenceService.getDepth();
			$scope.period = preferenceService.getPeriod();
			$scope.favoriteList = preferenceService.getFavoriteList();
			
			$scope.changeDepth = function() {
				analyticsService.send( analyticsService.CONST.MAIN, analyticsService.CONST.CLK_GENERAL_SET_DEPTH, $scope.depth );
				preferenceService.setDepth( $scope.depth );
			};
			$scope.changePeriod = function() {
				analyticsService.send( analyticsService.CONST.MAIN, analyticsService.CONST.CLK_GENERAL_SET_PERIOD, $scope.period );
				preferenceService.setPeriod( $scope.period );
			};
			$scope.removeFavorite = function( applicationName ) {
				analyticsService.send( analyticsService.CONST.MAIN, analyticsService.CONST.CLK_GENERAL_SET_FAVORITE );
				preferenceService.removeFavorite( applicationName );
				$scope.favoriteList = preferenceService.getFavoriteList();
				$rootScope.$broadcast("navbarDirective.changedFavorite");
			};
			var $applicationList = $element.find(".applicationList");
			function addToFavoriteList( applicationName ) {
				analyticsService.send( analyticsService.CONST.MAIN, analyticsService.CONST.CLK_GENERAL_SET_FAVORITE );
				preferenceService.addFavorite( applicationName );
				$scope.$apply(function() {
					$scope.favoriteList = preferenceService.getFavoriteList();
					$rootScope.$broadcast("navbarDirective.changedFavorite");
				});
			}
			function formatOptionText(state) {
                if (!state.id) {
                    return state.text;
                }
                var chunk = state.text.split("@");
                if (chunk.length > 1) {
                    var img = $document.get(0).createElement("img");
                    img.src = "/images/icons/" + chunk[1] + ".png";
                    img.style.height = "25px";
                    img.style.paddingRight = "3px";
                    return img.outerHTML + chunk[0];
                } else {
                    return state.text;
                }
			}
			function initApplicationSelect() {
				$applicationList.select2({
	                placeholder: "Select an application.",
	                searchInputPlaceholder: "Input your application name.",
	                allowClear: false,
	                formatResult: formatOptionText,
	                formatSelection: formatOptionText,
	                escapeMarkup: function (m) {
	                    return m;
	                }
	            }).on("change", function (e) {
	            	addToFavoriteList( $applicationList.select2('val') );
	            });
			}
			
			
			$scope.$on("configuration.general.applications.set", function( event, applicationData ) {
				$scope.applications = applicationData;
				initApplicationSelect();
			});  
		}
	]);
})(jQuery);
	