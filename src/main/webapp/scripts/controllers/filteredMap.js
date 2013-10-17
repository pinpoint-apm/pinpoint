'use strict';

pinpointApp.controller('FilteredMapCtrl', [ '$scope', '$routeParams', '$timeout', 'TimeSliderDao', function ($scope, $routeParams, $timeout, TimeSliderDao) {

    // define private variables of methods
    var getQueryPeriod, broadcast;

    /**
     * get query period
     */
    getQueryPeriod = function () {
        return $scope.period * 1000 * 60;
    };

    /**
     * _boardcast as applicationChanged with args
     */
    broadcast = function () {
        var splitedApp = $scope.application.split('@'),
            applicationData = {
                application: $scope.application,
                applicationName: splitedApp[0],
                serviceType: splitedApp[1],
                period: $scope.period,
                queryPeriod: $scope.queryPeriod,
                queryStartTime: $scope.queryStartTime,
                queryEndTime: $scope.queryEndTime
            };

        var oTimeSliderDao = new TimeSliderDao()
                .setFrom($scope.queryStartTime)
                .setTo($scope.queryEndTime)
                .setInnerFrom($scope.queryStartTime + 10000000)
                .setInnerTo($scope.queryEndTime);

        $timeout(function () {
            $scope.$emit('timeSlider.initialize', oTimeSliderDao);
            $scope.$emit('servermap.initializeWithApplicationData', applicationData);
            $scope.$emit('scatter.initializeWithApplicationData', applicationData);
        });
    };

    /**
     * initialize
     */
    $timeout(function () {
        if ($routeParams.application) {
            $scope.application = $routeParams.application;
        }
        if ($routeParams.period) {
            $scope.period = $routeParams.period;
            $scope.queryPeriod = getQueryPeriod();
        }
        if ($routeParams.filter) {
            $scope.filter = $routeParams.filter;
        }
        if ($routeParams.queryEndTime) {
            $scope.queryEndTime = parseInt($routeParams.queryEndTime, 10);
        }
        $scope.queryStartTime = $scope.queryEndTime - $scope.queryPeriod;
        $scope.$digest();
        broadcast();
    });

}]);
