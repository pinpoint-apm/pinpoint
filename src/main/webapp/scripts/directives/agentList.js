'use strict';

pinpointApp.constant('agentListConfig', {
    agentGroupUrl: '/getAgentGroup.pinpoint'
});

pinpointApp
    .directive('agentList', [ 'agentListConfig', '$rootScope', function (cfg, $rootScope) {
        return {
            restrict: 'EA',
            replace: true,
            templateUrl: 'views/agentList.html',
            link: function postLink(scope, element, attrs) {

                var getAgentGroup = function (query, cb) {
                    cb([
                        {
                            "host": "HOST_NAME1",
                            "status": "good",
                            "agentList" : [
                                {
                                    "name": "AGENT_ID1",
                                    "status": "good"
                                },
                                {
                                    "name": "AGENT_ID2",
                                    "status": "good"
                                },
                                {
                                    "name": "AGENT_ID3",
                                    "status": "good"
                                }
                            ]
                        },
                        {
                            "host": "HOST_NAME2",
                            "status": "good",
                            "agentList" : [
                                {
                                    "name": "AGENT_ID1",
                                    "status": "good"
                                },
                                {
                                    "name": "AGENT_ID2",
                                    "status": "good"
                                }
                            ]
                        }
                    ]);
//                    jQuery.ajax({
//                        type: 'GET',
//                        url: cfg.agentGroupUrl,
//                        cache: false,
//                        dataType: 'json',
//                        data: {
//                            application: query.applicationName,
//                            serviceType: query.serviceType,
//                            from: query.from,
//                            to: query.to,
//                            hideIndirectAccess: query.hideIndirectAccess
//                        },
//                        success: function (result) {
//                            callback(query, result);
//                        },
//                        error: function (xhr, status, error) {
//                            console.log("ERROR", status, error);
//                        }
//                    });
                };

                var showAgentGroup = function (applicationName, serviceType, to, period) {
                    var query = {
                        applicationName: applicationName,
                        serviceType: serviceType,
                        from: to - period,
                        to: to,
                        period: period
                    };
                    getAgentGroup(query, function (result) {
                        scope.agentGroup = result;
                    });
                };

                scope.select = function (agent) {
                    scope.currentAgent = agent;
                    $rootScope.$broadcast('agentList.agentChanged', agent);
                };

                scope.$on('navbar.applicationChanged', function (event, data) {
                    scope.navbar = data;
                    showAgentGroup(data.applicationName, data.serviceType, data.queryEndTime, data.queryPeriod);
                });
            }
        };
    }]);
