'use strict';

pinpointApp.constant('agentDaoConfig', {
    agentStatUrl: '/getAgentStat.pinpoint'
});

pinpointApp.service('AgentDao', [ 'agentDaoConfig',
    function AgentDao(cfg) {

        this.getAgentStat = function (query, cb) {
            jQuery.ajax({
                type: 'GET',
                url: cfg.agentStatUrl,
                cache: false,
                dataType: 'json',
                data: query,
                success: function (result) {
                    if (angular.isFunction(cb)) {
                        cb(null, result);
                    }
                },
                error: function (xhr, status, error) {
                    if (angular.isFunction(cb)) {
                        cb(error, {});
                    }
                }
            });
        };

        /**
         * calculate a sampling rate based on the given period
         * @param period in minutes
         */
        this.getSampleRate = function (period) {
            var MAX_POINTS = 100;
            var points = period / 5;
            var rate = Math.floor(points / MAX_POINTS);
            return points <= MAX_POINTS ? 1 : rate;
        };

    }
]);
