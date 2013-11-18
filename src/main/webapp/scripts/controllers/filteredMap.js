'use strict';

pinpointApp.controller('FilteredMapCtrl', [ '$scope', '$routeParams', '$timeout', 'TimeSliderVo', 'NavbarVo', function ($scope, $routeParams, $timeout, TimeSliderVo, NavbarVo) {

    // define private variables
    var oNavbarVo, oTimeSliderVo;

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
}]);
