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
     * scope event on serverMap.linkClicked
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

    $scope.$on('timeSlider.moreClicked', function (event) {
        oNavbarDao.setQueryEndTime(oTimeSliderDao.getInnerFrom());
        oNavbarDao.autoCalcultateByQueryStartTimeAndQueryEndTime();
        $scope.$emit('serverMap.initialize', oNavbarDao);
    });
}]);
