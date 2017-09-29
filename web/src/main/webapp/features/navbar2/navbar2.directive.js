(function( $ ) {
	'use strict';
	pinpointApp.constant( "navbar2DirectiveConfig", {
		ID: "NAVBAR2_DRTV_"
	});

	pinpointApp.directive( "navbar2Directive", [ "navbar2DirectiveConfig", "$rootScope", "$timeout", "UrlVoService", "TooltipService", "CommonUtilService",
		function ( cfg, $rootScope, $timeout, UrlVoService, TooltipService, CommonUtilService ) {
			return {
				restrict: 'EA',
				replace: true,
				templateUrl: 'features/navbar2/navbar2.html?v=' + G_BUILD_TIME,
				link: function (scope, element) {
					cfg.ID += CommonUtilService.getRandomNum();

					scope.paramInitApplication = UrlVoService.getApplication();

					element.bind('selectstart', function (e) {
						return false;
					});
					TooltipService.init( "navbar" );

					scope.showConfig = function() {
						$rootScope.$broadcast("configuration.open");
					};
					$timeout(function() {
						scope.$broadcast( "period-selector.initialize", cfg.ID );
					});
				}
			};
		}
	]);
})( jQuery );
