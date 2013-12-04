'use strict';

pinpointApp.controller('MainCtrl', [ '$scope', '$timeout', '$routeParams', 'location', 'NavbarVo', function ($scope, $timeout, $routeParams, location, NavbarVo) {

    // define private variables
    var oNavbarVo;

    // define private variables of methods
    var getFirstPathOfLocation, changeLocation;

    /**
     * initialize
     */
    $timeout(function () {
        oNavbarVo = new NavbarVo();
        if ($routeParams.application) {
            oNavbarVo.setApplication($routeParams.application);
        }
        if ($routeParams.period) {
            oNavbarVo.setPeriod(Number($routeParams.period, 10));
        }
        if ($routeParams.queryEndTime) {
            oNavbarVo.setQueryEndTime(Number($routeParams.queryEndTime, 10));
        }
        oNavbarVo.autoCalculateByQueryEndTimeAndPeriod();
        $scope.$emit('navbar.initialize', oNavbarVo);
        $scope.$emit('scatter.initialize', oNavbarVo);
        $scope.$emit('serverMap.initialize', oNavbarVo);
    }, 100);

    /**
     * get first path of loction
     * @returns {*|string}
     */
    getFirstPathOfLocation = function () {
        var splitedPath = location.path().split('/');
        return splitedPath[1] || 'main';
    };

    /**
     * change location
     */
    changeLocation = function () {
        var url = '/' + getFirstPathOfLocation() + '/' + oNavbarVo.getApplication() + '/' + oNavbarVo.getPeriod() + '/' + oNavbarVo.getQueryEndTime();
        if (location.path() !== url) {
            if (location.path() === '/main') {
                location.path(url).replace();
            } else {
                location.skipReload().path(url).replace();
            }
            if (!$scope.$$phase) {
                $scope.$apply();
            }
        }
    };

    /**
     * scope event on navbar.changed
     */
    $scope.$on('navbar.changed', function (event, navbarVo) {
        oNavbarVo = navbarVo;
        changeLocation(oNavbarVo);
        $scope.$emit('scatter.initialize', oNavbarVo);
        $scope.$emit('serverMap.initialize', oNavbarVo);
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
        $scope.$emit('nodeInfoDetails.initialize', e, query, node, data, oNavbarVo);
        $scope.$emit('linkInfoDetails.reset', e, query, node, data, oNavbarVo);
    });

    /**
     * scope event on serverMap.linkClicked
     */
    $scope.$on('serverMap.linkClicked', function (event, e, query, link, data) {
        $scope.$emit('nodeInfoDetails.reset', e, query, link, data, oNavbarVo);
        $scope.$emit('linkInfoDetails.initialize', e, query, link, data, oNavbarVo);
    });

} ]);
