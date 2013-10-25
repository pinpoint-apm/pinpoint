'use strict';

pinpointApp.controller('MainCtrl', [ '$scope', '$timeout', '$routeParams', '$location', 'NavbarDao', function ($scope, $timeout, $routeParams, $location, NavbarDao) {

    // define private variables
    var oNavbarDao;

    // define private variables of methods
    var getFirstPathOfLocation, changeLocation;

    /**
     * initialize
     */
    $timeout(function () {
        oNavbarDao = new NavbarDao();
        if ($routeParams.application) {
            oNavbarDao.setApplication($routeParams.application);
        }
        if ($routeParams.period) {
            oNavbarDao.setPeriod(Number($routeParams.period, 10));
        }
        if ($routeParams.queryEndTime) {
            oNavbarDao.setQueryEndTime(Number($routeParams.queryEndTime, 10));
        }
        oNavbarDao.autoCalculateByQueryEndTimeAndPeriod();
        $scope.$emit('navbar.initialize', oNavbarDao);
        $scope.$emit('scatter.initialize', oNavbarDao);
        $scope.$emit('serverMap.initialize', oNavbarDao);
    }, 100);

    /**
     * get first path of loction
     * @returns {*|string}
     */
    getFirstPathOfLocation = function () {
        var splitedPath = $location.path().split('/');
        return splitedPath[1] || 'main';
    };

    /**
     * change location
     */
    changeLocation = function () {
        var url = '/' + getFirstPathOfLocation() + '/' + oNavbarDao.getApplication() + '/' + oNavbarDao.getPeriod() + '/' + oNavbarDao.getQueryEndTime();
        if ($location.path() !== url) {
            $location.path(url);
        }
    };

    /**
     * scope event on navbar.changed
     */
    $scope.$on('navbar.changed', function (event, navbarDao) {
        oNavbarDao = navbarDao;
        changeLocation(oNavbarDao);
    });

    /**
     * scope event on serverMap.passingTransactionResponseToScatterChart
     */
    $scope.$on('serverMap.passingTransactionResponseToScatterChart', function (event, node) {
        $scope.$emit('scatter.initializeWithNode', node);
    });

    /**
     * scope event on serverMap.nodeClicked
     */
    $scope.$on('serverMap.nodeClicked', function (event, e, query, node, data) {
        $scope.$emit('nodeInfoDetails.initialize', e, query, node, data);
        $scope.$emit('linkInfoDetails.reset', e, query, node, data);
    });

    /**
     * scope event on serverMap.linkClicked
     */
    $scope.$on('serverMap.linkClicked', function (event, e, query, link, data) {
        $scope.$emit('nodeInfoDetails.reset', e, query, link, data);
        $scope.$emit('linkInfoDetails.initialize', e, query, link, data);
    });

} ]);
