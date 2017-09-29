(function( $ ) {
	'use strict';
	pinpointApp.constant("ThreadDumpConfig", {
	});

	pinpointApp.controller("ThreadDumpCtrl", ["ThreadDumpConfig", "$scope", "$routeParams", "$rootScope", "$window", "$http",
		function (cfg, $scope, $routeParams, $rootScope) {
			$rootScope.wrapperStyle = {
				'padding-top': '0px'
			};
			$rootScope.$broadcast( "thread-dump-info-window.open", $routeParams.application.split("@")[0], $routeParams.agentId );
		}
	]);
})( jQuery );