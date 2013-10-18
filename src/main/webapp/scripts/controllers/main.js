'use strict';

pinpointApp.controller('MainCtrl', [ '$scope', '$timeout', '$routeParams', 'NavbarDao', function ($scope, $timeout, $routeParams, NavbarDao) {

    // define private variables
    var oNavbarDao;

    // define private variables of methods
    var broadcast;

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
        $scope.$emit('navbar.initialize', oNavbarDao);
    });

    /**
     * broadcast
     */
    broadcast = function () {
        $scope.$emit('scatter.initialize', oNavbarDao);
        $scope.$emit('serverMap.initialize', oNavbarDao);
    };

    /**
     * scope event on navbar.changed
     */
    $scope.$on('navbar.changed', function (event, navbarDao) {
        oNavbarDao = navbarDao;
        broadcast();
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
        $scope.$emit('nodeInfoDetails.initializeWithNodeData', e, query, node, data);
        $scope.$emit('linkInfoDetails.initializeWithNodeData', e, query, node, data);
    });

    /**
     * scope event on serverMap.linkClicked
     */
    $scope.$on('serverMap.linkClicked', function (event, e, query, link, data) {
        $scope.$emit('nodeInfoDetails.initializeWithLinkData', e, query, link, data);
        $scope.$emit('linkInfoDetails.initializeWithLinkData', e, query, link, data);
    });

} ]);
