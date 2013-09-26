'use strict';

var pinpointApp = angular.module('pinpointApp', [ 'ngResource' ]);

pinpointApp.config(function ($routeProvider, $locationProvider) {
    $locationProvider.html5Mode(false).hashPrefix(''); // 해쉬뱅을 사용 안할 수 있다.
    $routeProvider.when('/main', {
        templateUrl: 'views/ready.html',
        controller: 'MainCtrl'
    }).when('/main/:application/:period/:queryEndTime', {
        templateUrl: 'views/main.html',
        controller: 'MainCtrl'
    }).when('/main/:application/:period/:queryEndTime/:filter', {
        templateUrl: 'views/main.html',
        controller: 'MainCtrl'
    }).when('/spy/:application/:period/:queryEndTime', {
        templateUrl: 'views/spy.html',
        controller: 'SpyCtrl'
    }).when('/spy/:application/:period/:queryEndTime/:agentId', {
        templateUrl: 'views/spy.html',
        controller: 'SpyCtrl'
    }).when('/filtermap/:appliation/:period/:queryEndTime/:filter', {
        templateUrl: 'views/filtermap.html',
        controller: 'FiltermapCtrl'
    }).otherwise({
        redirectTo: '/main'
    });
});
