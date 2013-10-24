'use strict';

var pinpointApp = angular.module('pinpointApp', [ 'ngResource', 'webStorageModule' ]);

pinpointApp.config(['$routeProvider', '$locationProvider', function ($routeProvider, $locationProvider) {
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
    }).when('/spy/:application/:period/:queryEndTime', {
        templateUrl: 'views/spy.html',
        controller: 'SpyCtrl'
    }).when('/spy/:application/:period/:queryEndTime/:agentId', {
        templateUrl: 'views/spy.html',
        controller: 'SpyCtrl'
    }).when('/transactionList', {
        templateUrl: 'views/transactionList.html',
        controller: 'TransactionListCtrl'
    }).when('/transactionDetail', {
        templateUrl: 'views/readyForTransactionDetail.html',
        controller: 'TransactionDetailCtrl'
    }).when('/transactionDetail/:traceId/:focusTimestamp', {
        templateUrl: 'views/transactionDetail.html',
        controller: 'TransactionDetailCtrl'
    }).otherwise({
        redirectTo: '/main'
    });
}]);
