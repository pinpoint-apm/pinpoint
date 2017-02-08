(function( $ ) {
	pinpointApp.constant( "agentInfoDirectiveConfig", {
		ID: "AGENT_INFO_DRTV_",
		agentStatUrl: "/getAgentStat.pinpoint"
	});

	pinpointApp.directive( "agentInfoDirective", [ "agentInfoDirectiveConfig", "$timeout", "CommonUtilService", "UrlVoService", "AlertsService", "ProgressBarService", "AgentDaoService", "AgentAjaxService", "TooltipService", "AnalyticsService", "helpContentService",
		function ( cfg, $timeout, CommonUtilService, UrlVoService, AlertsService, ProgressBarService, AgentDaoService, AgentAjaxService, TooltipService, AnalyticsService, helpContentService ) {
			return {
				restrict: 'EA',
				replace: true,
				templateUrl: 'features/agentInfo/agentInfoMain.html?v' + G_BUILD_TIME,
				link: function postLink(scope, element, attrs) {
					cfg.ID += CommonUtilService.getRandomNum();
					scope.agent = {};
					scope.hasAgentData = false;
					scope.showEventInfo = false;
					scope.showDetail = false;
					scope.selectTime = -1;
					var $elDSMessage = element.find(".ds-detail");
					var timeSlider = null, bInitTooltip = false;
					var oAlertService = new AlertsService();
					var oProgressBarService = new ProgressBarService();

					function initTime( time ) {
						$("#target-picker").val( moment( time ).format( "YYYY-MM-DD HH:mm:ss" ) );
					}
					function initTimeSlider( aSelectionFromTo, aFromTo ) {
						if ( timeSlider !== null ) {
							timeSlider.resetTimeSeriesAndSelectionZone( aSelectionFromTo, aFromTo ? aFromTo : calcuSliderTimeSeries( aSelectionFromTo ) );
						} else {
							timeSlider = new TimeSlider( "timeSlider", {
								"width": $("#timeSlider").get(0).getBoundingClientRect().width,
								"height": 90,
								"handleSrc": "images/handle.png",
								"timeSeries": aFromTo ? aFromTo : calcuSliderTimeSeries( aSelectionFromTo ),
								"handleTimeSeries": aSelectionFromTo,
								"selectTime": aSelectionFromTo[1],
								"eventData": []
							}).addEvent("clickEvent", function( aEvent ) {// [x, y, obj]
								loadEventInfo(aEvent[2]);
							}).addEvent("selectTime", function( time ) {
								scope.selectTime = time;
								loadAgentInfo( time );
								setTimeSliderBaseColor();
								initTime( time );
							}).addEvent("changeSelectionZone", function( aTime ) {
								loadChartData( scope.agent.agentId, aTime, getPeriod(aTime[0], aTime[1] ), function() {});
							}).addEvent("changeSliderTimeSeries", function( aEvents ) {});
						}
					}
					function getPeriod( from, to ) {
						return (to - from) / 1000 / 60;
					}
					function setTimeSliderBaseColor() {
						timeSlider.setDefaultStateLineColor( TimeSlider.EventColor[ scope.agent.status.state.code == 100 ? TimeSlider.GREEN : TimeSlider.RED] );
					}

					function loadChartData( agentId, aFromTo, period, callback ) {
						var hasError = false;
						var responseCount = 0;
						var oParam = {
							"agentId": agentId,
							"from": aFromTo[0],
							"to": aFromTo[1],
							"sampleRate": AgentDaoService.getSampleRate( period )
						};
						oProgressBarService.startLoading();
						oProgressBarService.setLoading(20);

						AgentAjaxService.getJVMChartData( oParam, function (result) {
							if ( checkResponse( result ) ) {
								if (angular.isDefined(result.type) && result.type) {
									scope.agent['jvmGcType'] = result.type;
								}
								showJvmChart(result);
							}
						});
						AgentAjaxService.getCpuLoadChartData( oParam, function (result) {
							if ( checkResponse( result ) ) {
								showCpuLoadChart(result);
							}
						});
						AgentAjaxService.getTPSChartData( oParam, function (result) {
							if ( checkResponse( result ) ) {
								showTpsChart(result);
							}
						});
						AgentAjaxService.getActiveTraceChartData( oParam, function (result) {
							if ( checkResponse( result ) ) {
								showActiveTraceChart(result);
							}
						});
						AgentAjaxService.getDataSourceChartData( oParam, function (result) {
							if ( checkResponse( result ) ) {
								showDataSourceChart(result);
							}
						});
						function checkResponse( result ) {
							responseCount++;
							oProgressBarService.setLoading(20 + (responseCount * 16) );
							if ( responseCount >= 5 ) {
								oProgressBarService.stopLoading();
								if ( hasError ) {
									oAlertService.showError('There is some error.');
								}
								callback();
							}
							if ( result.errorCode || result.status ) {
								hasError = true;
								return false;
							}
							return true;
						}
					}
					function loadAgentInfo( time ) {
						oProgressBarService.startLoading();
						oProgressBarService.setLoading(40);
						AgentAjaxService.getAgentInfo({
							"agentId": scope.agent.agentId,
							"timestamp": time
						}, function( result ) {
							var jvmGcType = scope.agent.jvmGcType;
							oProgressBarService.setLoading(80);
							scope.agent = result;
							scope.agent.jvmGcType = jvmGcType;
							scope.currentServiceInfo = initServiceInfo(result);
							oProgressBarService.setLoading(100);
							oProgressBarService.stopLoading();
						});
					}
					function loadEventInfo( oEvent ) {
						AgentAjaxService.getEvent({
							"agentId": scope.agent.agentId,
							"eventTimestamp": oEvent.eventTimestamp,
							"eventTypeCode": oEvent.eventTypeCode
						}, function( result ) {
							if ( result.errorCode || result.status ) {
								oAlertService.showError('There is some error.');
							} else {
								scope.eventInfo = result;
								scope.showEventInfo = true;
							}
						});
					}
					function calcuSliderTimeSeries( aFromTo ) {
						var from = aFromTo[0], to = aFromTo[1];
						var maxDay = TimeSlider.MAX_TIME_RANGE;
						var fromTo = to - from;
						if ( fromTo > maxDay  ) {
							return [to - maxDay, to];
						} else {
							var calcuFrom = fromTo * 3;
							if ( calcuFrom > maxDay ) {
								return [ to - maxDay, to ];
							} else {
								return [ to - calcuFrom, to ];
							}
						}
					}
					function initTooltip() {
						if ( bInitTooltip === false ) {
							TooltipService.init( "heap" );
							TooltipService.init( "permGen" );
							TooltipService.init( "cpuUsage" );
							TooltipService.init( "tps" );
							TooltipService.init( "activeThread" );
							TooltipService.init( "dataSource" );
							bInitTooltip = true;
						}
					}
					function initServiceInfo(agent) {
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
					function showJvmChart( chartData ) {
						var heap = {
							id: 'heap',
							title: 'Heap Usage',
							line: [
								{ id: 'JVM_MEMORY_HEAP_USED', key: 'Used', isFgc: false },
								{ id: 'JVM_MEMORY_HEAP_MAX', key: 'Max', isFgc: false },
								{ id: 'fgc', key: 'FGC', isFgc: true }
							]
						};
						var nonheap = {
							id: 'nonheap',
							title: 'PermGen Usage',
							line: [
								{ id: 'JVM_MEMORY_NON_HEAP_USED', key: 'Used', isFgc: false },
								{ id: 'JVM_MEMORY_NON_HEAP_MAX', key: 'Max', isFgc: false },
								{ id: 'fgc', key: 'FGC', isFgc: true }
							]
						};
						scope.memoryGroup = [ heap, nonheap ];

						scope.$broadcast( "jvmMemoryChartDirective.initAndRenderWithData.forHeap", AgentDaoService.parseMemoryChartDataForAmcharts(heap, chartData), '100%', '270px');
						scope.$broadcast( "jvmMemoryChartDirective.initAndRenderWithData.forNonHeap", AgentDaoService.parseMemoryChartDataForAmcharts(nonheap, chartData), '100%', '270px');

					}
					function showCpuLoadChart( chartData ) {
						var cpuLoad = { id: 'cpuLoad', title: 'JVM/System Cpu Usage', isAvailable: false};
						scope.cpuLoadChart = cpuLoad;

						scope.$broadcast( "cpuLoadChartDirective.initAndRenderWithData.forCpuLoad", AgentDaoService.parseCpuLoadChartDataForAmcharts(cpuLoad, chartData), '100%', '270px');
					}
					function showTpsChart( chartData ) {
						var tps = { id: 'tps', title: 'Transactions Per Second', isAvailable: false };
						scope.tpsChart = tps;

						scope.$broadcast( "tpsChartDirective.initAndRenderWithData.forTps", AgentDaoService.parseTpsChartDataForAmcharts(tps, chartData), '100%', '270px');
					}
					function showActiveTraceChart( chartData ) {
						var activeTrace = { id: "activeTrace", title: "Active Thread", isAvailable: false};
						scope.activeTraceChart = activeTrace;

						scope.$broadcast( "activeTraceChartDirective.initAndRenderWithData.forActiveTrace", AgentDaoService.parseActiveTraceChartDataForAmcharts(activeTrace, chartData), '100%', '270px');
					}
					scope.dsChartData = [];
					function showDataSourceChart( chartData ) {
						var dataSource = { id: "dataSource", title: "Data Source", isAvailable: false };
						dataSource["keys"] = chartData.map(function(v, i) {
							return {
								display: v.id,
								value: i
							};
						});
						scope.dsChartData = chartData;
						scope.currentDS = 0;
						scope.dataSourceChart = dataSource;

						scope.$broadcast( "dsChartDirective.initAndRenderWithData.forDataSource", AgentDaoService.parseDataSourceChartDataForAmcharts(dataSource, chartData[scope.currentDS]), '100%', '270px');
					}
					function getEventList( agentId, aFromTo ) {
						AgentAjaxService.getEventList({
							"agentId": agentId,
							"from": aFromTo[0],
							"to": aFromTo[1]
						}, function( result ) {
							if ( result.errorCode || result.status ) {
								oAlertService.showError('There is some error.');
							} else {
								timeSlider.addEventData(result);
							}
						});
					}
					function broadcastToCpuLoadChart(e, event) {
						if (scope.cpuLoadChart.isAvailable) {
							scope.$broadcast('cpuLoadChartDirective.showCursorAt.forCpuLoad', event.index);
						}
					}
					function broadcastToTpsChart(e, event) {
						if (scope.tpsChart.isAvailable) {
							scope.$broadcast('tpsChartDirective.showCursorAt.forTps', event.index);
						}
					}
					function broadcastToActiveTraceChart(e, event) {
						if (scope.activeTraceChart.isAvailable) {
							scope.$broadcast('activeTraceChartDirective.showCursorAt.forActiveTrace', event.index);
						}
					}
					function broadcastToDataSourceChart(e, event) {
						if (scope.dataSourceChart.isAvailable) {
							scope.$broadcast('dsChartDirective.showCursorAt.forDataSource', event.index);
						}
					}
					scope.currentDS = 0;
					scope.changeDS = function() {
						var index = parseInt(scope.currentDS);
						scope.$broadcast( "dsChartDirective.initAndRenderWithData.forDataSource", AgentDaoService.parseDataSourceChartDataForAmcharts(scope.dataSourceChart, scope.dsChartData[index]), '100%', '270px');
					};
					scope.showDataSourceDetail = function() {
						$elDSMessage.toggle();
					};
					scope.toggleHelp = function() {
						$("._wrongApp").popover({
							"title": "<span class='label label-info'>" + UrlVoService.getApplicationName() + "</span> <span class='glyphicon glyphicon-resize-horizontal'></span> <span class='label label-info'>" + scope.agent.applicationName + "</span>",
							"content": helpContentService.inspector.wrongApp
								.replace(/\{\{application1\}\}/g, UrlVoService.getApplicationName() )
								.replace(/\{\{application2\}\}/g, scope.agent.applicationName )
								.replace(/\{\{agentId\}\}/g, scope.agent.agentId ),
							"html": true
						}).popover("toggle");
					};
					scope.isSameApplication = function() {
						return scope.agent.applicationName === UrlVoService.getApplicationName();
					};

					scope.formatDate = function( time ) {
						return moment(time).format('YYYY.MM.DD HH:mm:ss');
					};
					scope.hideEventInfo = function() {
						scope.showEventInfo = false;
					};
					scope.movePrev = function() {
						timeSlider.movePrev();
						getEventList( scope.agent.agentId, timeSlider.getSliderTimeSeries() );
					};
					scope.moveNext = function() {
						timeSlider.moveNext();
						getEventList( scope.agent.agentId, timeSlider.getSliderTimeSeries() );
					};
					scope.moveHead = function() {
						timeSlider.moveHead();
						getEventList( scope.agent.agentId, timeSlider.getSliderTimeSeries() );
					};
					scope.zoomInTimeSlider = function() {
						timeSlider.zoomIn();
					};
					scope.zoomOutTimeSlider = function() {
						timeSlider.zoomOut();
						getEventList( scope.agent.agentId, timeSlider.getSliderTimeSeries() );
					};

					scope.toggleShowDetail = function( $event ) {
						AnalyticsService.send( AnalyticsService.CONST.INSPECTOR, AnalyticsService.CONST.CLK_SHOW_SERVER_TYPE_DETAIL );
						scope.showDetail = !scope.showDetail;
						if ( scope.showDetail === true ) {
							$(".detailIndicator").animate({
								"left": $( $event.currentTarget ).position().left
							});
						} else {
							$(".detailIndicator").css("left", "0px");
						}
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
						return bHas ? "color:#F00" : "";
					};

					scope.selectServiceInfo = function(serviceInfo) {
						if (serviceInfo.serviceLibs.length > 0) {
							scope.currentServiceInfo = serviceInfo;
						}
					};
					scope.formatDate = function( time ) {
						return CommonUtilService.formatDate( time, "YYYY-MM-DD HH:mm:ss Z" );
					};
					scope.$on( "down.changed.agent", function ( event, invokerId, agent, bInvokedByTop ) {
						if( cfg.ID === invokerId ) return;
						if ( CommonUtilService.isEmpty( agent.agentId ) ) {
							scope.hasAgentData = false;
							scope.dsChartData = [];
							return;
						}
						$elDSMessage.hide();
						scope.showEventInfo = false;
						scope.hasAgentData = true;
						scope.agent = agent;
						scope.chartGroup = null;
						scope.currentServiceInfo = initServiceInfo(agent);

						var aFromTo, period, aSelectionFromTo = [];
						if ( timeSlider === null || bInvokedByTop ) {
							aSelectionFromTo[0] = UrlVoService.getQueryStartTime();
							aSelectionFromTo[1] = UrlVoService.getQueryEndTime();
							period = UrlVoService.getPeriod();
						} else {
							aSelectionFromTo = timeSlider.getSelectionTimeSeries();
							aFromTo = timeSlider.getSliderTimeSeries();
							period = getPeriod(aSelectionFromTo[0], aSelectionFromTo[1]);
						}
						if ( scope.selectTime === -1 || bInvokedByTop ) {
							scope.selectTime = UrlVoService.getQueryEndTime();
						} else {
							if ( scope.selectTime !== UrlVoService.getQueryEndTime() ) {
								loadAgentInfo( scope.selectTime );
							}
						}
						loadChartData(agent.agentId, aSelectionFromTo, period, function() {
							initTimeSliderUI( aSelectionFromTo, aFromTo );
						});

						initTooltip();

					});
					function initTimeSliderUI( aSelectionFromTo, aFromTo ) {
						initTime( scope.selectTime );
						initTimeSlider( aSelectionFromTo, aFromTo );
						setTimeSliderBaseColor();
						getEventList( scope.agent.agentId, aFromTo || calcuSliderTimeSeries( aSelectionFromTo ) );
					}

					scope.$on('jvmMemoryChartDirective.cursorChanged.forHeap', function (e, event) {
						scope.$broadcast('jvmMemoryChart.showCursorAt.forNonHeap', event.index);
						broadcastToCpuLoadChart(e, event);
						broadcastToTpsChart(e, event);
						broadcastToActiveTraceChart(e, event);
						broadcastToDataSourceChart(e, event);
					});
					scope.$on('jvmMemoryChartDirective.cursorChanged.forNonHeap', function (e, event) {
						scope.$broadcast('jvmMemoryChartDirective.showCursorAt.forHeap', event.index);
						broadcastToCpuLoadChart(e, event);
						broadcastToTpsChart(e, event);
						broadcastToActiveTraceChart(e, event);
						broadcastToDataSourceChart(e, event);
					});
					scope.$on('cpuLoadChartDirective.cursorChanged.forCpuLoad', function (e, event) {
						scope.$broadcast('jvmMemoryChartDirective.showCursorAt.forHeap', event.index);
						scope.$broadcast('jvmMemoryChartDirective.showCursorAt.forNonHeap', event.index);
						broadcastToTpsChart(e, event);
						broadcastToActiveTraceChart(e, event);
						broadcastToDataSourceChart(e, event);
					});
					scope.$on('tpsChartDirective.cursorChanged.forTps', function (e, event) {
						scope.$broadcast('jvmMemoryChartDirective.showCursorAt.forHeap', event.index);
						scope.$broadcast('jvmMemoryChartDirective.showCursorAt.forNonHeap', event.index);
						broadcastToCpuLoadChart(e, event);
						broadcastToActiveTraceChart(e, event);
						broadcastToDataSourceChart(e, event);
					});
					scope.$on('activeTraceChartDirective.cursorChanged.forActiveTrace', function (e, event) {
						scope.$broadcast('jvmMemoryChartDirective.showCursorAt.forHeap', event.index);
						scope.$broadcast('jvmMemoryChartDirective.showCursorAt.forNonHeap', event.index);
						broadcastToCpuLoadChart(e, event);
						broadcastToTpsChart(e, event);
						broadcastToDataSourceChart(e, event);
					});
					scope.$on('dsChartDirective.cursorChanged.forDataSource', function (e, event) {
						scope.$broadcast('jvmMemoryChartDirective.showCursorAt.forHeap', event.index);
						scope.$broadcast('jvmMemoryChartDirective.showCursorAt.forNonHeap', event.index);
						broadcastToCpuLoadChart(e, event);
						broadcastToTpsChart(e, event);
						broadcastToActiveTraceChart(e, event);
					});
				}
			};
		}
	]);
})(jQuery);