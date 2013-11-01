'use strict';

pinpointApp.controller('InspectorCtrl', [ '$scope', '$timeout', '$routeParams', '$location', 'NavbarDao',
    function ($scope, $timeout, $routeParams, $location, NavbarDao) {

        // define private variables
        var oNavbarDao, oAgent;

        // define private variables of methods
        var getFirstPathOfLocation, changeLocation, getLocation, isLocationChanged;

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
            if ($routeParams.agentId) {
                oNavbarDao.setAgentId($routeParams.agentId);
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
            changeLocation();
        });

        /**
         * scope event of agentList.agentChanged
         */
        $scope.$on('agentList.agentChanged', function (event, agent) {
            oAgent = agent;
            oNavbarDao.setAgentId(agent.agentId);

            if (isLocationChanged()) {
                changeLocation();
            }
            if (oAgent) {
                $scope.$emit('agentInfo.initialize', oNavbarDao, oAgent);
            }
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
            var url = getLocation();
            if (isLocationChanged()) {
                $location.path(url);
            }
        };

        /**
         * get location
         * @returns {string}
         */
        getLocation = function () {
            var url = '/' + getFirstPathOfLocation() + '/' + oNavbarDao.getApplication() + '/' + oNavbarDao.getPeriod() + '/' + oNavbarDao.getQueryEndTime();
            if (oNavbarDao.getAgentId()) {
                url += '/' + oNavbarDao.getAgentId();
            }
            return url;
        };

        /**
         * is location changed
         * @returns {boolean}
         */
        isLocationChanged = function () {
            var url = getLocation();
            if ($location.path() !== url) {
                return true;
            }
            return false;
        };
    }]);
