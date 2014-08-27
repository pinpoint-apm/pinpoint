'use strict';

pinpointApp.controller('InspectorCtrl', [ '$scope', '$timeout', '$routeParams', 'location', 'NavbarVo',
    function ($scope, $timeout, $routeParams, location, NavbarVo) {

        // define private variables
        var oNavbarVo, oAgent;

        // define private variables of methods
        var getFirstPathOfLocation, changeLocation, getLocation, isLocationChanged;

        /**
         * initialize
         */
        $timeout(function () {
            oNavbarVo = new NavbarVo();
            if ($routeParams.application) {
                oNavbarVo.setApplication($routeParams.application);
            }
            if ($routeParams.readablePeriod) {
                oNavbarVo.setReadablePeriod($routeParams.readablePeriod);
            }
            if ($routeParams.queryEndDateTime) {
                oNavbarVo.setQueryEndDateTime($routeParams.queryEndDateTime);
            }
            if ($routeParams.agentId) {
                oNavbarVo.setAgentId($routeParams.agentId);
            }
            oNavbarVo.autoCalculateByQueryEndDateTimeAndReadablePeriod();
            $scope.$emit('navbar.initializeWithStaticApplication', oNavbarVo);
            $scope.$emit('agentList.initialize', oNavbarVo);
        }, 500);

        /**
         * scope event on navbar.changed
         */
        $scope.$on('navbar.changed', function (event, navbarVo) {
            oNavbarVo = navbarVo;
            changeLocation();
        });

        /**
         * scope event of agentList.agentChanged
         */
        $scope.$on('agentList.agentChanged', function (event, agent) {
            oAgent = agent;
            oNavbarVo.setAgentId(agent.agentId);

            if (isLocationChanged()) {
                changeLocation();
            }
            if (oAgent) {
                $scope.$emit('agentInfo.initialize', oNavbarVo, oAgent);
            }
        });

        /**
         * get first path of loction
         * @returns {*|string}
         */
        getFirstPathOfLocation = function () {
            var splitedPath = location.path().split('/');
            return splitedPath[1] || 'inspector';
        };

        /**
         * change location
         */
        changeLocation = function () {
            var url = getLocation();
            if (isLocationChanged()) {
                if (location.path() === '/inspector') {

                } else {
                    location.skipReload().path(url).replace();
                }
                $scope.$emit('navbar.initializeWithStaticApplication', oNavbarVo);
                $scope.$emit('agentList.initialize', oNavbarVo);
            }
        };

        /**
         * get location
         * @returns {string}
         */
        getLocation = function () {
            var url = '/' + getFirstPathOfLocation() + '/' + oNavbarVo.getApplication() + '/' + oNavbarVo.getReadablePeriod() + '/' + oNavbarVo.getQueryEndDateTime();
            if (oNavbarVo.getAgentId()) {
                url += '/' + oNavbarVo.getAgentId();
            }
            return url;
        };

        /**
         * is location changed
         * @returns {boolean}
         */
        isLocationChanged = function () {
            var url = getLocation();
            if (location.path() !== url) {
                return true;
            }
            return false;
        };
    }]);
