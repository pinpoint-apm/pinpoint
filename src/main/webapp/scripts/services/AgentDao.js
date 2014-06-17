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

        /**
         * parse chart data for amcharts
         * @param info
         * @param agentStat
         * @returns {Array}
         */
        this.parseChartDataForAmcharts = function (info, agentStat) {
            var newData = [],
                POINTS_TIMESTAMP = 0, POINTS_MIN = 1, POINTS_MAX = 2, POINTS_AVG = 3,
                pointsTime = agentStat.charts['jvmGcOldTime'].points,
                pointsCount = agentStat.charts['jvmGcOldCount'].points;

            if (pointsTime.length !== pointsCount.length) {
                throw new Error('assertion error', 'time.length != count.length');
                return;
            }

            var currTime, currCount, prevTime, prevCount; // for gc

            for (var i = pointsCount.length - 1; i >= 0; --i) {
                var thisData = {
                    time: new Date(pointsTime[i][POINTS_TIMESTAMP]).toString('yyyy-MM-dd HH:mm'),
                    Used: 0,
                    Max: 0,
                    GC: 0
                };
                for (var k in info.line) {
                    if (info.line[k].isFgc) {
                        var GC = 0;
                        currTime = pointsTime[i][POINTS_MAX];
                        currCount = pointsCount[i][POINTS_MAX];
                        if (!prevTime || !prevCount) {
                            prevTime = currTime;
                            prevCount = currCount;
                        } else {
                            if ((currCount - prevCount > 0) && (currTime - prevTime > 0)) {
                                GC = currTime - prevTime;
                                prevTime = currTime;
                                prevCount = currCount;
                            }
                        }
                        thisData[info.line[k].key] = GC;
                    } else {
                        thisData[info.line[k].key] = agentStat.charts[info.line[k].id].points[i][POINTS_MAX]
                    }

                }

                newData.push(thisData);
            }

            return newData;
        };

    }
]);
