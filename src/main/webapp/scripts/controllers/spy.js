'use strict';

pinpointApp.controller('SpyCtrl', [ '$scope', '$timeout', '$routeParams', '$location', 'NavbarDao',
    function ($scope, $timeout, $routeParams, $location, NavbarDao) {

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
            $scope.$emit('navbar.initializeWithStaticApplication', oNavbarDao);
            $scope.$emit('agentList.initialize', oNavbarDao);
        }, 100);

        /**
         * scope event on navbar.changed
         */
        $scope.$on('navbar.changed', function (event, navbarDao) {
            oNavbarDao = navbarDao;
            changeLocation(oNavbarDao);
        });

        /**
         * get first path of loction
         * @returns {*|string}
         */
        getFirstPathOfLocation = function () {
            var splitedPath = $location.path().split('/');
            return splitedPath[1] || 'spy';
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
    }]);
