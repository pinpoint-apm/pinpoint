'use strict';

pinpointApp.controller('FilteredMapCtrl', [ '$scope', '$routeParams', '$timeout', function ($scope, $routeParams, $timeout) {

    /**
     * get query period
     */
    var getQueryPeriod = function () {
        return $scope.period * 1000 * 60;
    };

    /**
     * _boardcast as applicationChanged with args
     */
    var broadcast = function () {

        var splitedApp = $scope.application.split('@'),
            applicationData = {
                application: $scope.application,
                applicationName: splitedApp[0],
                serviceType: splitedApp[1],
                period: $scope.period,
                queryPeriod: $scope.queryPeriod,
                queryStartTime: $scope.queryEndTime - $scope.queryPeriod,
                queryEndTime: $scope.queryEndTime
            };
        $timeout(function () {
            $scope.$emit('navbar2.initializeWithApplicationData', applicationData);
            $scope.$emit('servermap.initializeWithApplicationData', applicationData);
            $scope.$emit('scatter.initializeWithApplicationData', applicationData);
        });
    };

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
            $scope.queryEndTime = parseInt($routeParams.queryEndTime);
        }
        $scope.$digest();
        broadcast();
    });

}]);
