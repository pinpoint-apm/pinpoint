(function( $ ) {
	'use strict';
	pinpointApp.constant( "navbar2DirectiveConfig", {
		ID: "NAVBAR2_DRTV_"
	});

	pinpointApp.directive( "navbar2Directive", [ "navbar2DirectiveConfig", "$rootScope", "TooltipService", "CommonUtilService",
		function ( cfg, $rootScope, TooltipService, CommonUtilService ) {
			return {
				restrict: 'EA',
				replace: true,
				templateUrl: 'features/navbar2/navbar2.html?v=' + G_BUILD_TIME,
				link: function (scope, element) {
					cfg.ID += CommonUtilService.getRandomNum();

					scope.$broadcast( "down.initialize", cfg.ID );
					element.bind('selectstart', function (e) {
						return false;
					});
					TooltipService.init( "navbar" );

					scope.showConfig = function() {
						$rootScope.$broadcast("configuration.show");
					};
				}
			};
		}
	]);
})( jQuery );
