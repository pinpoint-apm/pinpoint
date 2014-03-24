'use strict';

nv.dev = false;
var pinpointApp = angular.module('pinpointApp', [ 'ngRoute', 'ngResource', 'ngSanitize', 'webStorageModule', 'uiSlider', 'base64', 'mgcrea.ngStrap', 'ngCookies']);

pinpointApp.config(['$routeProvider', '$locationProvider', '$modalProvider', function ($routeProvider, $locationProvider, $modalProvider) {
    $locationProvider.html5Mode(false).hashPrefix(''); // 해쉬뱅을 사용 안할 수 있다.
    $routeProvider.when('/main', {
        templateUrl: 'views/ready.html',
        controller: 'MainCtrl'
    }).when('/main/:application/:period/:queryEndTime', {
        templateUrl: 'views/main.html',
        controller: 'MainCtrl'
    }).when('/filteredMap/:application/:period/:queryEndTime/:filter', {
        templateUrl: 'views/filteredMap.html',
        controller: 'FilteredMapCtrl'
    }).when('/inspecator/:pplication/:period/:queryEndTime', {
        templateUrl: 'views/inspector.html',
        controller: 'InspectorCtrl'
    }).when('/inspector/:application/:period/:queryEndTime/:agentId', {
        templateUrl: 'views/inspector.html',
        controller: 'InspectorCtrl'
    }).when('/transactionList', {
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
    }).when('/scatterFullScreenMode/:application/:period/:queryEndTime', {
        templateUrl: 'views/scatterFullScreenMode.html',
        controller: 'ScatterFullScreenModeCtrl'
    }).when('/scatterFullScreenMode/:application/:period/:queryEndTime/:filter', {
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
            });
        }

        // initialize variables
        var bIsLoginModalOpened, oLoginModal;

        // initialize variables of methods
        var checkLoginSession;

        console.log('$cookies', $cookies);
        console.log('$location.host()', $location.host());
        if ($location.host() === 'pinpoint.nhncorp.com') {
            $interval(function () {
                if (checkLoginSession() === false && bIsLoginModalOpened === false) {
                    oLoginModal.show();
                    bIsLoginModalOpened = true;
                }
            });
        }
        $rootScope.hide = function () {
            oLoginModal.hide();
            bIsLoginModalOpened = false;
        };

        checkLoginSession = function () {
            return angular.isDefined($cookies.SMSESSION);
        };

        oLoginModal = $modal({template: 'views/login.modal.html', backdrop: 'static', placement: 'center'});
    }
]);
