'use strict';

pinpointApp.constant('filteredMapConfig', {
    FILTER_DELIMETER: "^",
    FILTER_ENTRY_DELIMETER: "|"
});
pinpointApp.controller('FilteredMapCtrl', [ 'filteredMapConfig', '$scope', '$routeParams', '$timeout', 'TimeSliderVo', 'NavbarVo', 'encodeURIComponentFilter', '$window',
    function (cfg, $scope, $routeParams, $timeout, TimeSliderVo, NavbarVo, encodeURIComponentFilter, $window) {

        // define private variables
        var oNavbarVo, oTimeSliderVo;

        // define private variables of methods
        var openFilteredMapWithFilterDataSet, getStartValueForFilterByLabel;

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
            if ($routeParams.filter) {
                oNavbarVo.setFilter($routeParams.filter);
            }
            oNavbarVo.autoCalculateByQueryEndTimeAndPeriod();

            oTimeSliderVo = new TimeSliderVo()
                .setFrom(oNavbarVo.getQueryStartTime())
                .setTo(oNavbarVo.getQueryEndTime())
                .setInnerFrom(oNavbarVo.getQueryEndTime() - 1)
                .setInnerTo(oNavbarVo.getQueryEndTime());

            $timeout(function () {
                $scope.$emit('timeSlider.initialize', oTimeSliderVo);
                $scope.$emit('serverMap.initialize', oNavbarVo);
                $scope.$emit('scatter.initialize', oNavbarVo);
            });
        }, 100);

        /**
         * open filtered map with filter data set
         * @param filterDataSet
         */
        openFilteredMapWithFilterDataSet = function (filterDataSet) {
            var application = oNavbarVo.getApplication();
            if (filterDataSet.srcApplicationName === 'USER') {
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
         * scope event on serverMap.fetched
         */
        $scope.$on('serverMap.fetched', function (event, lastFetchedTimestamp, mapData) {
            oTimeSliderVo.setInnerFrom(lastFetchedTimestamp);
            $scope.$emit('timeSlider.setInnerFromTo', oTimeSliderVo);

            // auto trying fetch
            if (mapData.applicationMapData.nodeDataArray.length === 0 && mapData.applicationMapData.linkDataArray.length === 0) {
                $timeout(function () {
                    $scope.$emit('timeSlider.moreClicked');
                }, 500);
            } else {
                $scope.$emit('timeSlider.enableMore');
            }
        });

        /**
         * scope event on serverMap. allFetched
         */
        $scope.$on('serverMap.allFetched', function (event) {
            oTimeSliderVo.setInnerFrom(oTimeSliderVo.getFrom());
            $scope.$emit('timeSlider.setInnerFromTo', oTimeSliderVo);
            $scope.$emit('timeSlider.changeMoreToDone');
            $scope.$emit('timeSlider.disableMore');
        });

        /**
         * scope event of timeSlider.moreClicked
         */
        $scope.$on('timeSlider.moreClicked', function (event) {
            var newNavbarVo = new NavbarVo();
            newNavbarVo.setApplication(oNavbarVo.getApplication());
            newNavbarVo.setQueryStartTime(oNavbarVo.getQueryStartTime());
            newNavbarVo.setQueryEndTime(oTimeSliderVo.getInnerFrom());
            newNavbarVo.autoCalcultateByQueryStartTimeAndQueryEndTime();
            $scope.$emit('timeSlider.disableMore');
            $scope.$emit('serverMap.fetch', newNavbarVo.getQueryPeriod(), newNavbarVo.getQueryEndTime());
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
    }]);
