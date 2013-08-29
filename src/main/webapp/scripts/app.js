'use strict';

var pinpointApp = angular.module('pinpointApp', [ 'ngResource' ]);

pinpointApp.config(function($routeProvider, $locationProvider) {
	$locationProvider.html5Mode(false).hashPrefix(''); // 해쉬뱅을 사용 안할 수 있
	$routeProvider.when('/main', {
		templateUrl : 'views/ready.html',
		controller : 'MainCtrl',
	}).when('/main/:application/:period/:queryEndTime', {
		templateUrl : 'views/main.html',
		controller : 'MainCtrl',
	}).when('/spy/:agentId', {
		templateUrl : 'views/spy.html',
		controller : 'SpyCtrl'
	}).otherwise({
		redirectTo : '/main'
	});
});
