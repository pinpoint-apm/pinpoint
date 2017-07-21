/**
 * @namespace pinpointApp
 */
'use strict';

//nv.dev = false;
var pinpointApp = angular.module('pinpointApp', [ 'ngRoute', 'ngResource', 'ngSanitize', 'webStorageModule', 'uiSlider', 'base64', 'mgcrea.ngStrap', 'ngCookies', 'ngAnimate', 'timer']);

pinpointApp.config(['$routeProvider', '$locationProvider', '$modalProvider', function ($routeProvider, $locationProvider, $modalProvider) {
    $locationProvider.html5Mode(false).hashPrefix(''); // hashbang mode - could use other modes (html5 etc)
    $routeProvider.when('/main', {
        templateUrl: 'pages/main/ready.html',
        controller: 'MainCtrl'
    }).when('/main/:application', {
        templateUrl: 'pages/main/main.html',
        controller: 'MainCtrl'
	}).when('/main/:application/:readablePeriod', {
		templateUrl: 'pages/main/main.html',
		controller: 'MainCtrl'
    }).when('/main/:application/:readablePeriod/:queryEndDateTime', {
        templateUrl: 'pages/main/main.html',
        controller: 'MainCtrl'
    }).when('/filteredMap/:application/:readablePeriod/:queryEndDateTime/:filter', {
        templateUrl: 'pages/filteredMap/filteredMap.html',
        controller: 'FilteredMapCtrl'
    }).when('/filteredMap/:application/:readablePeriod/:queryEndDateTime/:filter/:hint', {
        templateUrl: 'pages/filteredMap/filteredMap.html',
        controller: 'FilteredMapCtrl'
    }).when('/inspector/:application/:readablePeriod/:queryEndDateTime', {
        templateUrl: 'pages/inspector/inspector.html',
        controller: 'InspectorCtrl'
    }).when('/inspector/:application/:readablePeriod/:queryEndDateTime/:agentId', {
        templateUrl: 'pages/inspector/inspector.html',
        controller: 'InspectorCtrl'
    }).when('/transactionList/:application/:readablePeriod/:queryEndDateTime', {
        templateUrl: 'pages/transactionList/transactionList.html',
        controller: 'TransactionListCtrl'
    }).when('/transactionList/:application/:readablePeriod/:queryEndDateTime/:transactionInfo', {
        templateUrl: 'pages/transactionList/transactionList.html',
        controller: 'TransactionListCtrl'
    }).when('/transactionDetail', {
        templateUrl: 'pages/transactionDetail/readyForTransactionDetail.html',
        controller: 'TransactionDetailCtrl'
    }).when('/transactionDetail/:traceId/:focusTimestamp/:agentId/:spanId', {
        templateUrl: 'pages/transactionDetail/transactionDetail.html',
        controller: 'TransactionDetailCtrl'
    }).when('/transactionView/:agentId/:traceId/:focusTimestamp/:spanId', {
        templateUrl: 'pages/transactionView/transactionView.html',
        controller: 'TransactionViewCtrl'
    }).when('/scatterFullScreenMode/:application/:readablePeriod/:queryEndDateTime/:agentList', {
        templateUrl: 'pages/scatterFullScreenMode/scatterFullScreenMode.html',
        controller: 'ScatterFullScreenModeCtrl'
    }).when('/scatterFullScreenMode/:application/:readablePeriod/:queryEndDateTime/:filter', {
        templateUrl: 'pages/scatterFullScreenMode/scatterFullScreenMode.html',
        controller: 'ScatterFullScreenModeCtrl'
	}).when('/threadDump/:application/:agentId', {
		templateUrl: 'pages/threadDump/threadDump.html',
		controller: 'ThreadDumpCtrl'
	}).when('/realtime/:application/:page', {
		templateUrl: 'pages/realtime/realtime.html',
		controller: 'RealtimeCtrl'
    }).otherwise({
        redirectTo: '/main'
    });

    angular.extend($modalProvider.defaults, {
        animation: 'am-flip-x'
    });
    // Completely disable SCE.  For demonstration purposes only!
    // Do not use in new projects.
//    $sceProvider.enabled(false);
}]);

pinpointApp.run([ "$rootScope", "$window", "$timeout", "$location", "$route", "SystemConfigurationService", "UserConfigurationService",
    function ($rootScope, $window, $timeout, $location, $route, SystemConfigService, UserConfigService ) {
        var original = $location.path;
        $location.path = function (path, reload) {
            if (reload === false) {
                var lastRoute = $route.current;
                var un = $rootScope.$on('$locationChangeSuccess', function () {
                    $route.current = lastRoute;
                    un();
                });
            }
            return original.apply($location, [path]);
        };

		SystemConfigService.getConfig().then(function(oSystemConfig) {

			if ( oSystemConfig.errorCode == 302 ) {
				$window.location = oSystemConfig.redirect;
				return;
			}
		});

        if (!isCanvasSupported()) {
            $timeout(function () {
                $('#supported-browsers').modal();
            }, 500);
        }
        moment.tz.setDefault( UserConfigService.getTimezone() );
    }
]);

function isCanvasSupported(){
  var elem = document.createElement('canvas');
  return !!(elem.getContext && elem.getContext('2d'));
}
