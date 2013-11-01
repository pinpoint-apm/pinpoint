'use strict';

pinpointApp.controller('FilteredMapCtrl', [ '$scope', '$routeParams', '$timeout', 'TimeSliderDao', 'NavbarDao', function ($scope, $routeParams, $timeout, TimeSliderDao, NavbarDao) {

    // define private variables
    var oNavbarDao, oTimeSliderDao;

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
        if ($routeParams.filter) {
            oNavbarDao.setFilter($routeParams.filter);
        }
        oNavbarDao.autoCalculateByQueryEndTimeAndPeriod();

        oTimeSliderDao = new TimeSliderDao()
            .setFrom(oNavbarDao.getQueryStartTime())
            .setTo(oNavbarDao.getQueryEndTime())
            .setInnerFrom(oNavbarDao.getQueryEndTime() - 1)
            .setInnerTo(oNavbarDao.getQueryEndTime());

        $timeout(function () {
            $scope.$emit('timeSlider.initialize', oTimeSliderDao);
            $scope.$emit('serverMap.initialize', oNavbarDao);
            $scope.$emit('scatter.initialize', oNavbarDao);
        });
    });

    /**
     * scope event on serverMap.fetched
     */
    $scope.$on('serverMap.fetched', function (event, lastFetchedTimestamp, nodeLength) {
        if (nodeLength === 0) {
            $scope.$emit('timeSlider.disableMore');
            oTimeSliderDao.setInnerFrom(oTimeSliderDao.getFrom());
        } else {
            oTimeSliderDao.setInnerFrom(lastFetchedTimestamp);
        }
        $scope.$emit('timeSlider.setInnerFromTo', oTimeSliderDao);
    });

    /**
     * scope event on serverMap. allFetched
     */
    $scope.$on('serverMap.allFetched', function (event) {
        oTimeSliderDao.setInnerFrom(oTimeSliderDao.getFrom());
        $scope.$emit('timeSlider.setInnerFromTo', oTimeSliderDao);
        $scope.$emit('timeSlider.disableMore');
    });

    /**
     * scope event of timeSlider.moreClicked
     */
    $scope.$on('timeSlider.moreClicked', function (event) {
        var newNavbarDao = new NavbarDao();
        newNavbarDao.setApplication(oNavbarDao.getApplication());
        newNavbarDao.setQueryStartTime(oNavbarDao.getQueryStartTime());
        newNavbarDao.setQueryEndTime(oTimeSliderDao.getInnerFrom());
        newNavbarDao.autoCalcultateByQueryStartTimeAndQueryEndTime();
        $scope.$emit('serverMap.fetch', newNavbarDao.getQueryPeriod(), newNavbarDao.getQueryEndTime());
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
        $scope.$emit('nodeInfoDetails.initialize', e, query, node, data);
        $scope.$emit('linkInfoDetails.reset', e, query, node, data);
    });


    /**
     * scope event on serverMap.linkClicked
     */
    $scope.$on('serverMap.linkClicked', function (event, e, query, link, data) {
        $scope.$emit('nodeInfoDetails.reset', e, query, link, data);
        $scope.$emit('linkInfoDetails.initialize', e, query, link, data);
    });
}]);
