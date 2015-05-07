'use strict';

//nv.dev = false;
var pinpointApp = angular.module('pinpointApp', [ 'ngRoute', 'ngResource', 'ngSanitize', 'webStorageModule', 'uiSlider', 'base64', 'mgcrea.ngStrap', 'ngCookies', 'angular-intro-plus', 'ngAnimate', 'timer']);

pinpointApp.config(['$routeProvider', '$locationProvider', '$modalProvider', function ($routeProvider, $locationProvider, $modalProvider) {
    $locationProvider.html5Mode(false).hashPrefix(''); // hashbang mode - could use other modes (html5 etc)
    $routeProvider.when('/main', {
        templateUrl: 'views/ready.html',
        controller: 'MainCtrl'
    }).when('/main/:application/:readablePeriod/:queryEndDateTime', {
        templateUrl: 'views/main.html',
        controller: 'MainCtrl'
    }).when('/filteredMap/:application/:readablePeriod/:queryEndDateTime/:filter', {
        templateUrl: 'views/filteredMap.html',
        controller: 'FilteredMapCtrl'
    }).when('/filteredMap/:application/:readablePeriod/:queryEndDateTime/:filter/:hint', {
        templateUrl: 'views/filteredMap.html',
        controller: 'FilteredMapCtrl'
    }).when('/inspector/:application/:readablePeriod/:queryEndDateTime', {
        templateUrl: 'views/inspector.html',
        controller: 'InspectorCtrl'
    }).when('/inspector/:application/:readablePeriod/:queryEndDateTime/:agentId', {
        templateUrl: 'views/inspector.html',
        controller: 'InspectorCtrl'
    }).when('/transactionList/:application/:readablePeriod/:queryEndDateTime', {
        templateUrl: 'views/transactionList.html',
        controller: 'TransactionListCtrl'
    }).when('/transactionDetail', {
        templateUrl: 'views/readyForTransactionDetail.html',
        controller: 'TransactionDetailCtrl'
    }).when('/transactionDetail/:traceId/:focusTimestamp', {
        templateUrl: 'views/transactionDetail.html',
        controller: 'TransactionDetailCtrl'
    }).when('/transactionView/:agentId/:traceId/:focusTimestamp', {
        templateUrl: 'views/transactionView.html',
        controller: 'TransactionViewCtrl'
    }).when('/scatterFullScreenMode/:application/:readablePeriod/:queryEndDateTime', {
        templateUrl: 'views/scatterFullScreenMode.html',
        controller: 'ScatterFullScreenModeCtrl'
    }).when('/scatterFullScreenMode/:application/:readablePeriod/:queryEndDateTime/:filter', {
        templateUrl: 'views/scatterFullScreenMode.html',
        controller: 'ScatterFullScreenModeCtrl'
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
pinpointApp.factory("UserLocales", function( $window) {
	var defaultLocale = "en";
    // May not be the best way to get locale.
    var localeCode = $window.navigator.userLanguage || $window.navigator.language;
    if ($.type(localeCode) === "string" && localeCode.length >= 2) {
        localeCode = localeCode.substring(0, 2);
    } else {
        localeCode = defaultLocale;
    }
    return {
    	"userLocale" : localeCode, 
    	"defaultLocale" : defaultLocale 
    };
});

pinpointApp.factory('helpContent', [ '$window', '$injector', 'UserLocales', function($window, $injector, UserLocales) {
//    var defaultLocale = "en";
//    // May not be the best way to get locale.
//    var localeCode = $window.navigator.userLanguage || $window.navigator.language;
//    if ($.type(localeCode) === "string" && localeCode.length >= 2) {
//        localeCode = localeCode.substring(0, 2);
//    } else {
//        localeCode = defaultLocale;
//    }
	var name = "helpContent-" + UserLocales.userLocale;
	var defaultName = "helpContent-" + UserLocales.defaultLocale;
//    var name = "helpContent-" + localeCode;
//    var defaultName = "helpContent-" + defaultLocale;
    if ($injector.has(name)) {
      return $injector.get(name);
    } else {
      return $injector.get(defaultName);
    }
}]);

pinpointApp.run([ '$rootScope', '$timeout', '$modal', '$location', '$cookies', '$interval',
    function ($rootScope, $timeout, $modal, $location, $cookies, $interval) {
        if (Modernizr.canvas === false) {
            $timeout(function () {
                $('#supported-browsers').modal();
            }, 500);
        }
    }
]);
