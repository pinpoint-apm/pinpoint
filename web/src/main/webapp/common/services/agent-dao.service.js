(function() {
	'use strict';
	
	/**
	 * (en)Agent의 Chart 정보를 로드하고 chart library에 사용할 수 있도록 데이터를 가공함. 
	 * @ko Agent의 Chart 정보를 로드하고 chart library에 사용할 수 있도록 데이터를 가공함.
	 * @group Service
	 * @name AgentDaoService
	 * @class
	 */	
	pinpointApp.constant('agentDaoServiceConfig', {
	    agentStatUrl: '/getAgentStat.pinpoint'
	});
	
	pinpointApp.service('AgentDaoService', [ 'agentDaoServiceConfig',
	    function AgentDaoService(cfg) {
		
			/**
			 * (en)선택한 Agent의 Chart 정보를 로드함.
			 * @ko 선택한 Agent의 Chart 정보를 로드함.
			 * @method AgentDaoService#getAgentStat
			 * @param {String} query
			 * @param {Function} cb
			 */
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
	         * parse memory chart data for amcharts
	         * @param info
	         * @param agentStat
	         * @returns {Array}
	         */
	        this.parseMemoryChartDataForAmcharts = function (info, agentStat) {
	            var newData = [],
	                pointsTime = agentStat.charts['JVM_GC_OLD_TIME'].points,
	                pointsCount = agentStat.charts['JVM_GC_OLD_COUNT'].points;
	
	            if (pointsTime.length !== pointsCount.length) {
	                throw new Error('assertion error', 'time.length != count.length');
	                return;
	            }
	
	            var currTime, currCount, prevTime, prevCount; // for gc
	
	            for (var i = 0; i < pointsCount.length; ++i) {
	                var thisData = {
	                    time: moment(pointsTime[i].timestamp).format('YYYY-MM-dd HH:mm:ss')
	                };
	                for (var k in info.line) {
	                    if (info.line[k].isFgc) {
	                        var gcCount = 0;
	                        var gcTime = 0;
	                        currTime = pointsTime[i].maxVal;
	                        currCount = pointsCount[i].maxVal;
	                        if (!prevTime || !prevCount) {
	                            prevTime = currTime;
	                            prevCount = currCount;
	                        } else {
	                            var countDelta = currCount - prevCount;
	                            var timeDelta = currTime - prevTime;
	                            var fgcOccurred = (Math.abs(countDelta) > 0) && (Math.abs(timeDelta) > 0);
	                            var jvmRestarted = countDelta < 0 && timeDelta < 0;
	                            
	                            if (fgcOccurred) {
	                                if (jvmRestarted) {
	                                    gcCount = currCount;
	                                    gcTime = currTime;
	                                } else {
	                                    gcCount = currCount - prevCount;
	                                    gcTime = currTime - prevTime;
	                                }
	                                prevCount = currCount;
	                                prevTime = currTime;
	                            }
	                        }
	                        if (gcCount > 0 && gcTime > 0) {
	                            thisData[info.line[k].key+"Count"] = gcCount;
	                        	thisData[info.line[k].key+"Time"] = gcTime;
	                        }
	                    } else {
	                    	var value = agentStat.charts[info.line[k].id].points[i].maxVal;
	                    	if (!(value < 0)) {
	                    		thisData[info.line[k].key] = value;
	                    	}
	                    }
	
	                }
	
	                newData.push(thisData);
	            }
	            return newData;
	        };
	
	        /**
	         * parse cpuLoad chart data for amcharts
	         * @param cpuLoad
	         * @param agentStat
	         * @returns {Array}
	         */
	        this.parseCpuLoadChartDataForAmcharts = function (cpuLoad, agentStat) {
	            // Cpu Load data availability check
	            var jvmCpuLoadData = agentStat.charts['CPU_LOAD_JVM'];
	            var systemCpuLoadData = agentStat.charts['CPU_LOAD_SYSTEM'];
	            if (jvmCpuLoadData || systemCpuLoadData) {
	            	cpuLoad.isAvailable = true;
	            } else {
	            	return;
	            }
	            var newData = [],
	            DATA_UNAVAILABLE = -1,
	            pointsJvmCpuLoad = jvmCpuLoadData.points,
	            pointsSystemCpuLoad = systemCpuLoadData.points;
	            
	            if (pointsJvmCpuLoad.length !== pointsSystemCpuLoad.length) {
	                throw new Error('assertion error', 'jvmCpuLoad.length != systemCpuLoad.length');
	                return;
	            }
	            
	            for (var i = 0; i < pointsJvmCpuLoad.length; ++i) {
	                if (pointsJvmCpuLoad[i].timestamp !== pointsSystemCpuLoad[i].timestamp) {
	                	throw new Error('assertion error', 'timestamp mismatch between jvmCpuLoad and systemCpuLoad');
	                	return;
	                }
	                var thisData = {
	                    time: moment(pointsJvmCpuLoad[i].timestamp).toString('YYYY-MM-dd HH:mm:ss'),
	                    maxCpuLoad: 100
	                };
	                var jvmCpuLoad = agentStat.charts['CPU_LOAD_JVM'].points[i].maxVal.toFixed(2);
	                var systemCpuLoad = agentStat.charts['CPU_LOAD_SYSTEM'].points[i].maxVal.toFixed(2);
	                if (!(jvmCpuLoad < 0)) {
	                    thisData.jvmCpuLoad = jvmCpuLoad;
	                }
	                if (!(systemCpuLoad < 0)) {
	                    thisData.systemCpuLoad = systemCpuLoad;
	                }
	                newData.push(thisData);
	            }
	            
	            return newData;
	        };
	    }
	]);
})();