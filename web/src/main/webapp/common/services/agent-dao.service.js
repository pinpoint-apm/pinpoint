(function() {
	'use strict';
	
	/**
	 * (en)Agent의 Chart 정보를 로드하고 chart library에 사용할 수 있도록 데이터를 가공함. 
	 * @ko Agent의 Chart 정보를 로드하고 chart library에 사용할 수 있도록 데이터를 가공함.
	 * @group Service
	 * @name AgentDaoService
	 * @class
	 */	
	pinpointApp.constant( "agentDaoServiceConfig", {
	    agentStatUrl: "/getAgentStat.pinpoint",
		dateFormat: "YYYY-MM-DD HH:mm:ss"
	});
	
	pinpointApp.service( "AgentDaoService", [ "agentDaoServiceConfig",
	    function AgentDaoService( cfg ) {
		
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
	            }
	
	            var currTime, currCount, prevTime, prevCount; // for gc
	
	            for (var i = 0; i < pointsCount.length; ++i) {
	                var thisData = {
	                    time: moment(pointsTime[i].xVal).format( cfg.dateFormat )
	                };
	                for (var k in info.line) {
	                    if (info.line[k].isFgc) {
	                        var gcCount = 0;
	                        var gcTime = 0;
	                        currTime = pointsTime[i].maxYVal;
	                        currCount = pointsCount[i].maxYVal;
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
	                    	var value = agentStat.charts[info.line[k].id].points[i].maxYVal;
	                    	if ( value >= 0 ) {
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
	            pointsJvmCpuLoad = jvmCpuLoadData.points,
	            pointsSystemCpuLoad = systemCpuLoadData.points;
	            
	            if (pointsJvmCpuLoad.length !== pointsSystemCpuLoad.length) {
	                throw new Error('assertion error', 'jvmCpuLoad.length != systemCpuLoad.length');
	            }
	            
	            for (var i = 0; i < pointsJvmCpuLoad.length; ++i) {
	                if (pointsJvmCpuLoad[i].xVal !== pointsSystemCpuLoad[i].xVal) {
	                	throw new Error('assertion error', 'timestamp mismatch between jvmCpuLoad and systemCpuLoad');
	                }
	                var thisData = {
						time: moment(pointsJvmCpuLoad[i].xVal).format( cfg.dateFormat ),
	                    maxCpuLoad: 100
	                };
	                var jvmCpuLoad = typeof agentStat.charts['CPU_LOAD_JVM'].points[i].maxYVal == "number" ? agentStat.charts['CPU_LOAD_JVM'].points[i].maxYVal.toFixed(2) : 0.00;
	                var systemCpuLoad = typeof agentStat.charts['CPU_LOAD_SYSTEM'].points[i].maxYVal == "number" ? agentStat.charts['CPU_LOAD_SYSTEM'].points[i].maxYVal.toFixed(2) : 0.00;
	                if ( jvmCpuLoad >= 0 ) {
	                    thisData.jvmCpuLoad = jvmCpuLoad;
	                }
	                if ( systemCpuLoad >= 0 ) {
	                    thisData.systemCpuLoad = systemCpuLoad;
	                }
	                newData.push(thisData);
	            }
	            return newData;
	        };
    
            /**
             * parse tps chart data for amcharts
             * @param tps
             * @param agentStat
             * @returns {Array}
             */
	        this.parseTpsChartDataForAmcharts = function (tps, agentStat) {
	            // TPS data availability check
	        	var aSampledContinuationData = agentStat.charts['TPS_SAMPLED_CONTINUATION'].points;
	        	var aSampledNewData = agentStat.charts['TPS_SAMPLED_NEW'].points;
	        	var aUnsampledContinuationData = agentStat.charts['TPS_UNSAMPLED_CONTINUATION'].points;
	        	var aUnsampledNewData = agentStat.charts['TPS_UNSAMPLED_NEW'].points;
	        	var aTotalData = agentStat.charts['TPS_TOTAL'].points;
	        	
	        	var tpsLength = aTotalData.length;
	        	if ( tpsLength > 0 ) {
	        		tps.isAvailable = true;
	        	} else {
	        		return;
	        	}
	            var newData = [],
	            DATA_UNAVAILABLE = -1;
	            
	            for ( var i = 0 ; i < tpsLength ; i++ ) {
	                var thisData = {
						time: moment(aSampledContinuationData[i].xVal).format( cfg.dateFormat )
	                };
	                var sampledContinuationTps     = typeof aSampledContinuationData[i].avgYVal == "number" ? aSampledContinuationData[i].avgYVal.toFixed(2) : 0.00;
	                var sampledNewTps              = typeof aSampledNewData[i].avgYVal == "number" ? aSampledNewData[i].avgYVal.toFixed(2) : 0.00;
	                var unsampledContinuationTps   = typeof aUnsampledContinuationData[i].avgYVal == "number" ? aUnsampledContinuationData[i].avgYVal.toFixed(2) : 0.00;
	                var unsampledNewTps            = typeof aUnsampledNewData[i].avgYVal == "number" ? aUnsampledNewData[i].avgYVal.toFixed(2) : 0.00;
	                var totalTps                   = typeof aTotalData[i].avgYVal == "number" ? aTotalData[i].avgYVal.toFixed(2) : 0.00;
	                if ( sampledContinuationTps != DATA_UNAVAILABLE ) {
                        thisData.sampledContinuationTps = sampledContinuationTps;
	                }
	                if ( sampledNewTps != DATA_UNAVAILABLE ) {
	                    thisData.sampledNewTps = sampledNewTps;
	                }
	                if ( unsampledContinuationTps != DATA_UNAVAILABLE ) {
	                    thisData.unsampledContinuationTps = unsampledContinuationTps;
	                }
	                if ( unsampledNewTps != DATA_UNAVAILABLE ) {
	                    thisData.unsampledNewTps = unsampledNewTps;
	                }
	                if ( totalTps != DATA_UNAVAILABLE ) {
	                    thisData.totalTps = totalTps;
	                }
	                newData.push(thisData);
	            }
	            
	            return newData;
	        };
			this.parseActiveTraceChartDataForAmcharts = function (activeTrace, agentStat) {
				var aActiveTraceFastData = agentStat.charts[ "ACTIVE_TRACE_FAST" ].points;
				var aActiveTraceNormal = agentStat.charts[ "ACTIVE_TRACE_NORMAL" ].points;
				var aActiveTraceSlow = agentStat.charts[ "ACTIVE_TRACE_SLOW" ].points;
				var aActiveTraceVerySlow = agentStat.charts[ "ACTIVE_TRACE_VERY_SLOW" ].points;

				if ( aActiveTraceFastData || aActiveTraceNormal || aActiveTraceSlow || aActiveTraceVerySlow ) {
					activeTrace.isAvailable = true;
				} else {
					return;
				}
				var newData = [],
					DATA_UNAVAILABLE = -1;

				for ( var i = 0 ; i < aActiveTraceFastData.length ; i++ ) {
					var thisData = {
						time: moment(aActiveTraceFastData[i].xVal).format( cfg.dateFormat )
					};
					var traceFast     	= typeof aActiveTraceFastData[i].avgYVal == "number" ? aActiveTraceFastData[i].avgYVal.toFixed(2) : 0.00;
					var traceNormal     = typeof aActiveTraceNormal[i].avgYVal == "number" ? aActiveTraceNormal[i].avgYVal.toFixed(2) : 0.00;
					var traceSlow   	= typeof aActiveTraceSlow[i].avgYVal == "number" ? aActiveTraceSlow[i].avgYVal.toFixed(2) : 0.00;
					var traceVerySlow	= typeof aActiveTraceVerySlow[i].avgYVal == "number" ? aActiveTraceVerySlow[i].avgYVal.toFixed(2) : 0.00;
					if ( traceFast != DATA_UNAVAILABLE ) {
						thisData.fast = traceFast;
						thisData.fastTitle = aActiveTraceFastData[i].title;
					}
					if ( traceNormal != DATA_UNAVAILABLE ) {
						thisData.normal = traceNormal;
						thisData.normalTitle = aActiveTraceNormal[i].title;
					}
					if ( traceSlow != DATA_UNAVAILABLE ) {
						thisData.slow = traceSlow;
						thisData.slowTitle = aActiveTraceSlow[i].title;
					}
					if ( traceVerySlow != DATA_UNAVAILABLE ) {
						thisData.verySlow = traceVerySlow;
						thisData.verySlowTitle = aActiveTraceVerySlow[i].title;
					}
					newData.push(thisData);
				}

				return newData;
			};
	    }
	]);
})();
