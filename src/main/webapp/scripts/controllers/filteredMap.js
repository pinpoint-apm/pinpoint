'use strict';

pinpointApp.controller('FilteredMapCtrl', [ '$scope', '$routeParams', '$timeout', function ($scope, $routeParams, $timeout) {

    $timeout(function () {
        if ($routeParams.application) {
            $scope.application = $routeParams.application;
        }
        if ($routeParams.period) {
            $scope.period = $routeParams.period;
        }
        if ($routeParams.filter) {
            $scope.filter = $routeParams.filter;
        }
        if ($routeParams.queryEndTime) {
            $scope.queryEndTime = $routeParams.queryEndTime;
        }

        broadcast();
    });

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

        $scope.queryPeriod = getQueryPeriod();

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
        $scope.$emit('servermap.initializeWithApplicationData', applicationData);
        $scope.$emit('scatter.initializeWithApplicationData', applicationData);
    };
}]);
