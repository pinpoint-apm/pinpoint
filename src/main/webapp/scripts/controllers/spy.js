'use strict';

pinpointApp.controller('SpyCtrl', [ '$scope', '$timeout', '$routeParams', 'NavbarDao', function ($scope, $timeout, $routeParams, NavbarDao) {

    // define private variables
    var oNavbarDao;

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
    }, 100);

    /**
     * scope event on navbar.changed
     */
    $scope.$on('navbar.changed', function (event, navbarDao) {
        oNavbarDao = navbarDao;
        $scope.$emit('agentList.initialize', oNavbarDao);
    });
}]);
