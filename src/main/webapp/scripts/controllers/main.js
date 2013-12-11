'use strict';

pinpointApp.constant('mainConfig', {
    FILTER_DELIMETER: "^",
    FILTER_ENTRY_DELIMETER: "|"
});
pinpointApp.controller('MainCtrl', [ 'mainConfig', '$scope', '$timeout', '$routeParams', 'location', 'NavbarVo', 'encodeURIComponentFilter', '$window',
    function (cfg, $scope, $timeout, $routeParams, location, NavbarVo, encodeURIComponentFilter, $window) {

        // define private variables
        var oNavbarVo;

        // define private variables of methods
        var getFirstPathOfLocation, changeLocation, openFilteredMapWithFilterDataSet, getStartValueForFilterByLabel;

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
         * open filtered map with filter data set
         * @param filterDataSet
         */
        openFilteredMapWithFilterDataSet = function (filterDataSet) {
            var application = oNavbarVo.getApplication();
            if (filterDataSet.srcApplicationName === 'USER' || filterDataSet.srcApplicationName === 'CLIENT') {
                application = filterDataSet.destApplicationName + '@1010';
            } else {
                application = filterDataSet.srcApplicationName + '@1010';
            }

            var prevFilter = oNavbarVo.getFilter();
            var newFilter = ((prevFilter) ? prevFilter + cfg.FILTER_DELIMETER : "")
                + filterDataSet.srcServiceType + cfg.FILTER_ENTRY_DELIMETER
                + filterDataSet.srcApplicationName + cfg.FILTER_ENTRY_DELIMETER
                + filterDataSet.destServiceType + cfg.FILTER_ENTRY_DELIMETER
                + filterDataSet.destApplicationName;

            if (angular.isString(filterDataSet.label)) {
                if (filterDataSet.label === 'error') {
                    newFilter += cfg.FILTER_ENTRY_DELIMETER + filterDataSet.label;
                } else if (filterDataSet.label.indexOf('+') > 0) {
                    newFilter += cfg.FILTER_ENTRY_DELIMETER + parseInt(filterDataSet.label, 10) + ',9999999999';
                } else {
                    var startValue = getStartValueForFilterByLabel(filterDataSet.label, filterDataSet.values);
                    newFilter += cfg.FILTER_ENTRY_DELIMETER + startValue + ',' + filterDataSet.label;
                }

            }
            var url = '#/filteredMap/' + application + '/' + oNavbarVo.getPeriod() + '/' + oNavbarVo.getQueryEndTime() + '/' + encodeURIComponentFilter(newFilter);
            $window.open(url, "");
        };

        /**
         * get start value for filter by label
         * @param label
         * @param values
         * @returns {number}
         */
        getStartValueForFilterByLabel = function (label, values) {
            var labelKey = (function () {
                    for (var key in values) {
                        if (values[key].label === label) {
                            return key;
                        }
                    }
                    return false;
                })(),
                startValue = 0;

            if (labelKey > 0) {
                startValue = parseInt(values[labelKey - 1].label, 10);
            }
            return startValue;
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

        /**
         * scope event on serverMap.openFilteredMap
         */
        $scope.$on('serverMap.openFilteredMap', function (event, filterDataSet) {
            openFilteredMapWithFilterDataSet(filterDataSet);
        });

        /**
         * scope event on linkInfoDetails.ResponseSummary.barClicked
         */
        $scope.$on('linkInfoDetails.ResponseSummary.barClicked', function (event, filterDataSet) {
            openFilteredMapWithFilterDataSet(filterDataSet);
        });

    } ]);
