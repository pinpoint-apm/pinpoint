'use strict';

pinpointApp.controller('ScatterFullScreenModeCtrl', [ '$scope', '$rootScope', '$routeParams', '$timeout', 'NavbarVo',
    function ($scope, $rootScope, $routeParams, $timeout, NavbarVo) {

        // define private variables
        var oNavbarVo;

        // initialize
        $rootScope.wrapperClass = 'no-navbar';
        $rootScope.wrapperStyle = {
            'padding-top': '0px'
        };

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
            $scope.$emit('scatter.initialize', oNavbarVo);
            $scope.$emit('scatter.initializeWithNode', {applicationName: oNavbarVo.getApplicationName()}, 800, 600)
        }, 500);
    }]);
