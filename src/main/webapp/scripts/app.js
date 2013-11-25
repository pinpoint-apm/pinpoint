'use strict';

nv.dev = false;
var pinpointApp = angular.module('pinpointApp', [ 'ngRoute', 'ngResource', 'webStorageModule' ]);

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
    }).when('/inspector/:application/:period/:queryEndTime', {
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
    }).otherwise({
        redirectTo: '/main'
    });
}]);

pinpointApp.run(function () {
//    console.log('run');
});
