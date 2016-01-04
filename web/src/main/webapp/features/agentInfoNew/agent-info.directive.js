(function( $ ) {
	'use strict';
	/**
	 * (en)agentInfoDirective 
	 * @ko agentInfoDirective
	 * @group Directive
	 * @name agentInfoDirective
	 * @class
	 */	
	pinpointApp.constant('agentInfoConfig', {
	    agentStatUrl: '/getAgentStat.pinpoint'
	});
	
	pinpointApp.directive('agentInfoDirective', [ 'agentInfoConfig', '$timeout', 'AlertsService', 'ProgressBarService', 'AgentDaoService', 'AgentAjaxService', 'TooltipService',
	    function (cfg, $timeout, AlertsService, ProgressBarService, AgentDaoService, agentAjaxService, tooltipService) {
	        return {
	            restrict: 'EA',
	            replace: true,
	            templateUrl: 'features/agentInfoNew/agentInfo.html?v' + G_BUILD_TIME,
	            link: function postLink(scope, element, attrs) {

	                // initialize
	                scope.agentInfoTemplate = 'features/agentInfoNew/agentInfoReady.html?v=' + G_BUILD_TIME;
					scope.showEventInfo = false;
					var oNavbarVoService, timeSlider, bInitTooltip = false;
	                var oAlertService = new AlertsService();
	                var oProgressBarService = new ProgressBarService();

					var initTimeSlider = function( from, to ) {
						timeSlider = new TimeSlider("timeSlider", {
							"width": $("#timeSlider").width(),
							"height": 90,
							"handleSrc": "images/handle.png",
							"timeSeries": calcuSliderTimeSeries(from, to),
							"handleTimeSeries": [from, to],
							"selectTime": to,
							"eventData": []
						}).addEvent("clickEvent", function ( aEvent ) {// [x, y, obj]
							agentAjaxService.getEvent({
								"agentId": scope.agent.agentId,
								"eventTimestamp": aEvent[2].eventTimestamp,
								"eventTypeCode": aEvent[2].eventTypeCode
							}, function( result ) {
								if ( result.errorCode || result.status ) {

								} else {
									scope.eventInfo = result;
									scope.showEventInfo = true;
								}
							});
						}).addEvent("inEvent", function ( aEvents ) {
						}).addEvent("outEvent", function ( aEvents ) {
						}).addEvent("selectTime", function ( time ) {
							oProgressBarService.startLoading();
							oProgressBarService.setLoading(40);
							agentAjaxService.getAgentInfo({
								"agentId": scope.agent.agentId,
								"timestamp": time
							}, function( result ) {
								console.log( "agent Info", result );
								var jvmGcType = scope.agent.jvmGcType;
								oProgressBarService.setLoading(80);
								scope.agent = result;
								initScopeInfo( result );
								scope.info.jvmGcType = jvmGcType;
								scope.currentServiceInfo = initServiceInfo(result);
								oProgressBarService.setLoading(100);
								oProgressBarService.stopLoading();
							});
						}).addEvent("changeSelectionZone", function ( aTime ) {
							oProgressBarService.startLoading();
							oProgressBarService.setLoading(40);
							agentAjaxService.getAgentStateForChart({
								"agentId": scope.agent.agentId,
								"from": aTime[0],
								"to": aTime[1],
								"sampleRate": AgentDaoService.getSampleRate((aTime[1] - aTime[0]) / 1000 / 60)
							}, function (result) {
								if ( result.errorCode || result.status ) {
									oProgressBarService.stopLoading();
									if ( err ) {
										oAlertService.showError('There is some error.');
									} else {
										//oAlertService.showError(result.exception);
									}
									return;
								}

								scope.agentStat = result;
								if (angular.isDefined(result.type) && result.type) {
									scope.info['jvmGcType'] =  result.type;
								}
								oProgressBarService.setLoading(80);
								showCharts(result);
								$timeout(function () {
									oProgressBarService.setLoading(100);
									oProgressBarService.stopLoading();
								}, 700);
							});
						}).addEvent("changeSliderTimeSeries", function (aEvents) {
							console.log( "zoom in or zoom out or window resize ");
						});
					};
					var calcuSliderTimeSeries = function( from, to ) {
						var twoDay = 172800000;
						var fromTo = to - from;

						if ( fromTo > twoDay  ) {
							return [to - twoDay, to];
						} else {
							return [ to - ( fromTo * 3 ), to ];
						}
					};

	                var initTooltip = function() {
	                	if ( bInitTooltip === false ) {
							tooltipService.init( "heap" );
							tooltipService.init( "permGen" );
							tooltipService.init( "cpuUsage" );
							tooltipService.init( "tps" );
							bInitTooltip = true;
	                	}
	                };
					var initScopeInfo = function( agent ) {
						scope.info = {
							'ip': agent.ip,
							'pid': agent.pid,
							'agentId': agent.agentId,
							'hostName': agent.hostName,
							'linkList': agent.linkList,
							'jvmGcType': '',
							'vmVersion': agent.vmVersion,
							'showDetail': false,
							'serviceType': agent.serviceType,
							'agentVersion': agent.agentVersion,
							'serverMetaData': agent.serverMetaData,
							'applicationName': agent.applicationName
						};
					};
	                var initServiceInfo = function (agent) {
	                    if (agent.serverMetaData && agent.serverMetaData.serviceInfos) {
	                        var serviceInfos = agent.serverMetaData.serviceInfos;
	                        for (var i = 0; i < serviceInfos.length; ++i) {
	                            if (serviceInfos[i].serviceLibs.length > 0) {
	                                return serviceInfos[i];
	                            }
	                        } 
	                    }
	                    return;
	                }
	
	                /**
	                 * show charts
	                 * @param agentStat
	                 */
	                var showCharts = function (agentStat) {
	
	                    var heap = { id: 'heap', title: 'Heap Usage', span: 'span12', line: [
	                        { id: 'JVM_MEMORY_HEAP_USED', key: 'Used', values: [], isFgc: false },
	                        { id: 'JVM_MEMORY_HEAP_MAX', key: 'Max', values: [], isFgc: false },
	                        { id: 'fgc', key: 'FGC', values: [], bar: true, isFgc: true }
	                    ]};
	
	                    var nonheap = { id: 'nonheap', title: 'PermGen Usage', span: 'span12', line: [
	                        { id: 'JVM_MEMORY_NON_HEAP_USED', key: 'Used', values: [], isFgc: false },
	                        { id: 'JVM_MEMORY_NON_HEAP_MAX', key: 'Max', values: [], isFgc: false },
	                        { id: 'fgc', key: 'FGC', values: [], bar: true, isFgc: true }
	                    ]};
	                    
	                    var cpuLoad = { id: 'cpuLoad', title: 'JVM/System Cpu Usage', span: 'span12', isAvailable: false};
	                    
	                    var tps = { id: 'tps', title: 'Transactions Per Second', span: 'span12', isAvailable: false };
	
	                    scope.memoryGroup = [ heap, nonheap ];
	                    scope.cpuLoadChart = cpuLoad;
	                    scope.tpsChart = tps;
	
	                    scope.$broadcast('jvmMemoryChartDirective.initAndRenderWithData.forHeap', AgentDaoService.parseMemoryChartDataForAmcharts(heap, agentStat), '100%', '270px');
	                    scope.$broadcast('jvmMemoryChartDirective.initAndRenderWithData.forNonHeap', AgentDaoService.parseMemoryChartDataForAmcharts(nonheap, agentStat), '100%', '270px');
	                    scope.$broadcast('cpuLoadChartDirective.initAndRenderWithData.forCpuLoad', AgentDaoService.parseCpuLoadChartDataForAmcharts(cpuLoad, agentStat), '100%', '270px');
	                    scope.$broadcast('tpsChartDirective.initAndRenderWithData.forTps', AgentDaoService.parseTpsChartDataForAmcharts(tps, agentStat), '100%', '270px');
	                };
	                
	                /**
	                 * get agent stat
	                 * @param agentId
	                 * @param from
	                 * @param to
	                 */
	                var getAgentStat = function (agentId, from, to, period) {
	                    oProgressBarService.startLoading();
	                    oProgressBarService.setLoading(40);
						agentAjaxService.getAgentStateForChart({
							"agentId": agentId,
							"from": from,
							"to": to,
							"sampleRate": AgentDaoService.getSampleRate(period)
						}, function (result) {
							console.log( result );
							if ( result.errorCode || result.status ) {
                                oProgressBarService.stopLoading();
                                if ( err ) {
                                	oAlertService.showError('There is some error.');
                                } else {
                                	//oAlertService.showError(result.exception);
                                }
                                return;
                            }
	                        
	                        scope.agentStat = result;
	                        if (angular.isDefined(result.type) && result.type) {
	                            scope.info['jvmGcType'] =  result.type;
	                        }
	                        oProgressBarService.setLoading(80);
	                        showCharts(result);
	                        $timeout(function () {
	                            oProgressBarService.setLoading(100);
	                            oProgressBarService.stopLoading();
	                        }, 700);
							initTooltip();
							initTimeSlider( oNavbarVoService.getQueryStartTime(), oNavbarVoService.getQueryEndTime() );
							//scope.$digest();

							getEventList( agentId, from, to );
						});

	                };
					var getEventList = function( agentId, from, to ) {
						agentAjaxService.getEventList({
							"agentId": agentId,
							"from": from,
							"to": to
						}, function( result ) {
							if ( result.errorCode || result.status ) {

							} else {
								console.log( result );
								timeSlider.addEventData(result);
							}
						});
					}
	                
	                var broadcastToCpuLoadChart = function(e, event) {
	                	if (scope.cpuLoadChart.isAvailable) {
	                        scope.$broadcast('cpuLoadChartDirective.showCursorAt.forCpuLoad', event.index);
	                	}
	                }
	                var broadcastToTpsChart = function(e, event) {
	                    if (scope.tpsChart.isAvailable) {
	                        scope.$broadcast('tpsChartDirective.showCursorAt.forTps', event.index);
	                    }
	                }

					scope.formatDate = function( time ) {
						return moment(time).format('YYYY-MM-DD HH:mm:ss');
					};
					scope.hideEventInfo = function() {
						scope.showEventInfo = false;
					};
					scope.zoomInTimeSlider = function() {
						timeSlider.zoomIn();
					};
					scope.zoomOutTimeSlider = function() {
						timeSlider.zoomOut();
						var aRange = timeSlider.getSliderTimeSeries();
						getEventList( scope.agent.agentId, aRange[0], aRange[1] );
					};

					scope.showDetail = function() {
						scope.info.showDetail = !scope.info.showDetail;
					};
	                scope.hasDuplicate = function( libName, index ) {
	                	var len = scope.currentServiceInfo.serviceLibs.length;
	                	var bHas = false;
	                	for( var i = 0 ; i < len ; i++ ) {
	                		if ( scope.currentServiceInfo.serviceLibs[i] == libName && i != index ) {
	                			bHas = true;
	                			break;
	                		}
	                	}
	                	return bHas ? "color:red" : "";
	                };

					scope.selectServiceInfo = function(serviceInfo) {
						if (serviceInfo.serviceLibs.length > 0) {
							scope.currentServiceInfo = serviceInfo;
						}
					};
					scope.$on('agentInfoDirective.initialize', function (event, navbarVoService, agent) {
						console.log( "init agent info : ", agent );
						oNavbarVoService = navbarVoService;
						scope.agentInfoTemplate = 'features/agentInfoNew/agentInfoMain.html?v=' + G_BUILD_TIME;
						scope.agent = agent;
						scope.chartGroup = null;
						initScopeInfo( agent );
						scope.currentServiceInfo = initServiceInfo(agent);

						$timeout(function () {
							getAgentStat(agent.agentId, oNavbarVoService.getQueryStartTime(), oNavbarVoService.getQueryEndTime(), oNavbarVoService.getPeriod());
							scope.$apply();
						});
					});
	                scope.$on('jvmMemoryChartDirective.cursorChanged.forHeap', function (e, event) {
	                    scope.$broadcast('jvmMemoryChart.showCursorAt.forNonHeap', event.index);
	                    broadcastToCpuLoadChart(e, event);
	                    broadcastToTpsChart(e, event);
	                });
	                scope.$on('jvmMemoryChartDirective.cursorChanged.forNonHeap', function (e, event) {
	                    scope.$broadcast('jvmMemoryChartDirective.showCursorAt.forHeap', event.index);
	                    broadcastToCpuLoadChart(e, event);
	                    broadcastToTpsChart(e, event);
	                });
	                scope.$on('cpuLoadChartDirective.cursorChanged.forCpuLoad', function (e, event) {
	                    scope.$broadcast('jvmMemoryChartDirective.showCursorAt.forHeap', event.index);
	                    scope.$broadcast('jvmMemoryChartDirective.showCursorAt.forNonHeap', event.index);
	                    broadcastToTpsChart(e, event);
	                });
                    scope.$on('tpsChartDirective.cursorChanged.forTps', function (e, event) {
                        scope.$broadcast('jvmMemoryChartDirective.showCursorAt.forHeap', event.index);
                        scope.$broadcast('jvmMemoryChartDirective.showCursorAt.forNonHeap', event.index);
                        broadcastToCpuLoadChart(e, event);
                    });
	            }
	        };
	    }
	]);
})(jQuery);