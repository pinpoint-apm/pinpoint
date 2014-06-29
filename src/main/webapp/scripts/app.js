'use strict';

nv.dev = false;
var pinpointApp = angular.module('pinpointApp', [ 'ngRoute', 'ngResource', 'ngSanitize', 'webStorageModule', 'uiSlider', 'base64', 'mgcrea.ngStrap', 'ngCookies', 'angular-intro-plus', 'ngAnimate']);

pinpointApp.config(['$routeProvider', '$locationProvider', '$modalProvider', function ($routeProvider, $locationProvider, $modalProvider) {
    $locationProvider.html5Mode(false).hashPrefix(''); // 해쉬뱅을 사용 안할 수 있다.
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

pinpointApp.run([ '$rootScope', '$timeout', '$modal', '$location', '$cookies', '$interval',
    function ($rootScope, $timeout, $modal, $location, $cookies, $interval) {
        if (Modernizr.canvas === false) {
            $timeout(function () {
                $('#supported-browsers').modal();
            }, 500);
        }

        // initialize variables
        var bIsLoginModalOpened, oLoginModal;

        // initialize variables of methods
        var checkLoginSession;

        if ($location.host() === 'pinpoint.nhncorp.com') {
            $timeout(function () {
                if (checkLoginSession() === false && bIsLoginModalOpened === false) {
                    oLoginModal.show();
                    bIsLoginModalOpened = true;
                }
            }, 700);
            $interval(function () {
                if (checkLoginSession() === false && bIsLoginModalOpened === false) {
                    oLoginModal.show();
                    bIsLoginModalOpened = true;
                } else if (checkLoginSession() === true && bIsLoginModalOpened === true) {
                    oLoginModal.hide();
                    bIsLoginModalOpened = false;
                }
            }, 3000);
        }

        checkLoginSession = function () {
            return angular.isDefined($cookies.SMSESSION);
        };

        oLoginModal = $modal({template: 'views/login.modal.html', backdrop: 'static', placement: 'center', show: false});

    }
]);
