(function() {
	'use strict';

	pinpointApp.constant( "agentDaoServiceConfig", {
		dateFormat: "YYYY-MM-DD HH:mm:ss"
	});

	pinpointApp.service( "AgentDaoService", [ "agentDaoServiceConfig",
		function AgentDaoService( cfg ) {

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

				// gc time may be spread across consecutive timeslots even for a single gc event
				var cumulativeGcTime = 0;
				for (var i = 0; i < pointsCount.length; ++i) {
					var thisData = {
						time: moment(pointsTime[i].xVal).format( cfg.dateFormat )
					};
					for (var k in info.line) {
						if (info.line[k].isFgc) {
							var gcCount = pointsCount[i].sumYVal;
							var gcTime = pointsTime[i].sumYVal;
							if (gcTime > 0) {
								cumulativeGcTime += gcTime;
							}
							if (gcCount > 0) {
								thisData[info.line[k].key+"Count"] = gcCount;
								thisData[info.line[k].key+"Time"] = cumulativeGcTime;
								cumulativeGcTime = 0;
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
				var newData = [];
				var DATA_UNAVAILABLE = -1;

				var tpsLength = aTotalData.length;
				if ( tpsLength > 0 ) {
					tps.isAvailable = true;
				} else {
					return newData;
				}

				for ( var i = 0 ; i < tpsLength ; i++ ) {
					var obj = {
						"time" : moment(aSampledContinuationData[i].xVal).format( cfg.dateFormat )
					};
					var sampledContinuationTps = getFloatValue( aSampledContinuationData[i].avgYVal );
					var sampledNewTps = getFloatValue( aSampledNewData[i].avgYVal );
					var unsampledContinuationTps = getFloatValue( aUnsampledContinuationData[i].avgYVal );
					var unsampledNewTps = getFloatValue( aUnsampledNewData[i].avgYVal );
					var totalTps = getFloatValue( aTotalData[i].avgYVal );

					if ( sampledContinuationTps != DATA_UNAVAILABLE ) {
						obj.sampledContinuationTps = sampledContinuationTps;
					}
					if ( sampledNewTps != DATA_UNAVAILABLE ) {
						obj.sampledNewTps = sampledNewTps;
					}
					if ( unsampledContinuationTps != DATA_UNAVAILABLE ) {
						obj.unsampledContinuationTps = unsampledContinuationTps;
					}
					if ( unsampledNewTps != DATA_UNAVAILABLE ) {
						obj.unsampledNewTps = unsampledNewTps;
					}
					if ( totalTps != DATA_UNAVAILABLE ) {
						obj.totalTps = totalTps;
					}
					newData.push( obj );
				}

				return newData;
			};
			this.parseActiveTraceChartDataForAmcharts = function (activeTrace, agentStat) {
				var aActiveTraceFastData = agentStat.charts[ "ACTIVE_TRACE_FAST" ].points;
				var aActiveTraceNormal = agentStat.charts[ "ACTIVE_TRACE_NORMAL" ].points;
				var aActiveTraceSlow = agentStat.charts[ "ACTIVE_TRACE_SLOW" ].points;
				var aActiveTraceVerySlow = agentStat.charts[ "ACTIVE_TRACE_VERY_SLOW" ].points;
				var newData = [];
				var DATA_UNAVAILABLE = -1;

				if ( aActiveTraceFastData || aActiveTraceNormal || aActiveTraceSlow || aActiveTraceVerySlow ) {
					activeTrace.isAvailable = true;
				} else {
					return newData;
				}

				for ( var i = 0 ; i < aActiveTraceFastData.length ; i++ ) {
					var obj = {
						"time": moment(aActiveTraceFastData[i].xVal).format(cfg.dateFormat)
					};

					var fast = getFloatValue( aActiveTraceFastData[i].avgYVal );
					var normal = getFloatValue( aActiveTraceNormal[i].avgYVal );
					var slow = getFloatValue( aActiveTraceSlow[i].avgYVal );
					var verySlow = getFloatValue( aActiveTraceVerySlow[i].avgYVal );

					if ( fast != DATA_UNAVAILABLE ) {
						obj.fast = fast;
						obj.fastTitle = aActiveTraceFastData[i].title;
					}
					if ( normal != DATA_UNAVAILABLE ) {
						obj.normal = normal;
						obj.normalTitle = aActiveTraceNormal[i].title;
					}
					if ( slow != DATA_UNAVAILABLE ) {
						obj.slow = slow;
						obj.slowTitle = aActiveTraceSlow[i].title;
					}
					if ( verySlow != DATA_UNAVAILABLE ) {
						obj.verySlow = verySlow;
						obj.verySlowTitle = aActiveTraceVerySlow[i].title;
					}
					newData.push( obj );
				}
				return newData;
			};
			this.parseResponseTimeChartDataForAmcharts = function(responseTime, aChartData) {
				var aAVG = aChartData.charts[ "AVG" ].points;
				var newData = [];
				if ( aAVG ) {
					responseTime.isAvailable = true;
				} else {
					return newData;
				}

				for ( var i = 0 ; i < aAVG.length ; i++ ) {
					newData.push({
						"avg" : getFloatValue( aAVG[i].avgYVal ),
						"time": moment(aAVG[i].xVal).format(cfg.dateFormat),
						"title": "AVG"
					});
				}
				return newData;
			};
			this.parseDataSourceChartDataForAmcharts = function (oInfo, aChartData, prefix) {
				var returnData = [];
				if ( aChartData.length === 0 ) {
					return returnData;
				}
				var maxAvg = 0;
				for( var groupIndex = 0 ; groupIndex < aChartData.length ; groupIndex++ ) {
					var oGroupData = aChartData[groupIndex];
					var targetId = oGroupData.id;
					var aAvgData = oGroupData.charts["ACTIVE_CONNECTION_SIZE"].points;

					if ( aAvgData.length === 0 ) {
						return returnData;
					}
					for( var fieldIndex = 0 ; fieldIndex < aAvgData.length ; fieldIndex++ ) {
						var oData = aAvgData[fieldIndex];
						if ( groupIndex === 0 ) {
							returnData[fieldIndex] = {
								"time": moment(oData.xVal).format(cfg.dateFormat)
							};
						}
						maxAvg = Math.max( maxAvg, oData["avgYVal"] );
						returnData[fieldIndex][prefix+targetId]  = oData["avgYVal"].toFixed(1);
					}
				}
				oInfo.isAvailable = true;
				return {
					max: parseInt( maxAvg ) + 1,
					data: returnData
				};
			};

			function getFloatValue( val ) {
				return angular.isNumber( val ) ? val.toFixed(2) : 0.00;
			}
		}
	]);
})();
