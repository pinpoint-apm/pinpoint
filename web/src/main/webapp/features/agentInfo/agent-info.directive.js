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
	
	pinpointApp.directive('agentInfoDirective', [ 'agentInfoConfig', '$timeout', 'AlertsService', 'ProgressBarService', 'AgentDaoService', 'AgentAjaxService', 'TooltipService', "AnalyticsService", 'helpContentService',
	    function ( cfg, $timeout, AlertsService, ProgressBarService, AgentDaoService, agentAjaxService, tooltipService, analyticsService, helpContentService ) {
	        return {
	            restrict: 'EA',
	            replace: true,
	            templateUrl: 'features/agentInfo/agentInfo.html?v' + G_BUILD_TIME,
	            link: function postLink(scope, element, attrs) {

	                scope.agentInfoTemplate = 'features/agentInfo/agentInfoReady.html?v=' + G_BUILD_TIME;
					scope.showEventInfo = false;
					scope.showDetail = false;
					scope.selectTime = -1;
					var oNavbarVoService, timeSlider = null, bInitTooltip = false;
	                var oAlertService = new AlertsService();
	                var oProgressBarService = new ProgressBarService();
					var $targetPicker = null;

					var initTimePicker = function( time ) {
						if ( $targetPicker === null ) {
							$targetPicker = $("#target-picker");
							$targetPicker.datetimepicker({
								dateFormat: "yy-mm-dd",
								timeFormat: "HH:mm:ss",
								controlType: "select",
								showButtonPanel: true,
								onSelect: function () {},
								onClose: function (selectedTime) {
									var time = moment( selectedTime, "YYYY-MM-DD HH:mm:ss").valueOf();
									if ( scope.selectTime !== time ) {
										timeSlider.setSelectTime( time );
									}
								}
							});
							$("#ui-datepicker-div").addClass("inspector-datepicker");
						}
						setPickerTime( time );
					};
					var setPickerTime = function( time ) {
						$targetPicker.datetimepicker( 'setDate', new Date( time ) );
					};
					var initTimeSlider = function( aSelectionFromTo, aFromTo ) {
						if ( timeSlider !== null ) {
							timeSlider.emptyData();
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
								setPickerTime( time );
							}).addEvent("changeSelectionZone", function( aTime ) {
								loadChartData( scope.agent.agentId, aTime, getPeriod(aTime[0], aTime[1] ), function() {
								});
							}).addEvent("changeSliderTimeSeries", function( aEvents ) {
							});
						}
					};
					var getPeriod = function( from, to ) {
						return (to - from) / 1000 / 60;
					};
					var setTimeSliderBaseColor = function() {
						timeSlider.setDefaultStateLineColor( TimeSlider.EventColor[ scope.agent.status.state.code == 100 ? "10100" : "10200"] );
					};

					var loadChartData = function( agentId, aFromTo, period, callback ) {
						oProgressBarService.startLoading();
						oProgressBarService.setLoading(40);
						agentAjaxService.getAgentStateForChart({
							"agentId": agentId,
							"from": aFromTo[0],
							"to": aFromTo[1],
							"sampleRate": AgentDaoService.getSampleRate(period)
						}, function (result) {
							if ( result.errorCode || result.status ) {
								oProgressBarService.stopLoading();
								oAlertService.showError('There is some error.');
								return;
							}
							scope.agentStat = result;
							if (angular.isDefined(result.type) && result.type) {
								scope.agent['jvmGcType'] =  result.type;
							}
							oProgressBarService.setLoading(80);
							showCharts(result);
							$timeout(function () {
								oProgressBarService.setLoading(100);
								oProgressBarService.stopLoading();
							}, 700);
							callback();
						});
					};
					var loadAgentInfo = function( time ) {
						oProgressBarService.startLoading();
						oProgressBarService.setLoading(40);
						agentAjaxService.getAgentInfo({
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
					};
					var loadEventInfo = function( oEvent ) {
						agentAjaxService.getEvent({
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
					};
					var calcuSliderTimeSeries = function( aFromTo ) {
						var from = aFromTo[0], to = aFromTo[1];
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
	                };

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
					var getEventList = function( agentId, aFromTo ) {
						agentAjaxService.getEventList({
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
					};

	                var broadcastToCpuLoadChart = function(e, event) {
	                	if (scope.cpuLoadChart.isAvailable) {
	                        scope.$broadcast('cpuLoadChartDirective.showCursorAt.forCpuLoad', event.index);
	                	}
	                };
	                var broadcastToTpsChart = function(e, event) {
	                    if (scope.tpsChart.isAvailable) {
	                        scope.$broadcast('tpsChartDirective.showCursorAt.forTps', event.index);
	                    }
	                };
					scope.toggleHelp = function() {
						$("._wrongApp").popover({
							"title": "<span class='label label-info'>" + oNavbarVoService.getApplicationName() + "</span> <span class='glyphicon glyphicon-resize-horizontal'></span> <span class='label label-info'>" + scope.agent.applicationName + "</span>",
							"content": helpContentService.inspector.wrongApp
								.replace(/\{\{application1\}\}/g, oNavbarVoService.getApplicationName() )
								.replace(/\{\{application2\}\}/g, scope.agent.applicationName )
								.replace(/\{\{agentId\}\}/g, scope.agent.agentId ),
							"html": true
						}).popover("toggle");
					};
					scope.isSameApplication = function() {
						return scope.agent.applicationName === oNavbarVoService.getApplicationName();
					};

					scope.formatDate = function( time ) {
						return moment(time).format('YYYY.MM.DD HH:mm:ss');
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
						getEventList( scope.agent.agentId, aRange );
					};

					scope.toggleShowDetail = function() {
						analyticsService.send( analyticsService.CONST.INSPECTOR, analyticsService.CONST.CLK_SHOW_SERVER_TYPE_DETAIL );
						scope.showDetail = !scope.showDetail;
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
						oNavbarVoService = navbarVoService;
						scope.agentInfoTemplate = 'features/agentInfo/agentInfoMain.html?v=' + G_BUILD_TIME;
						scope.agent = agent;
						scope.chartGroup = null;
						scope.currentServiceInfo = initServiceInfo(agent);

						var aFromTo, period, aSelectionFromTo = [];
						if ( timeSlider === null ) {
							aSelectionFromTo[0] = oNavbarVoService.getQueryStartTime();
							aSelectionFromTo[1] = oNavbarVoService.getQueryEndTime();
							period = oNavbarVoService.getPeriod();
						} else {
							aSelectionFromTo = timeSlider.getSelectionTimeSeries();
							aFromTo = timeSlider.getSliderTimeSeries();
							period = getPeriod(aSelectionFromTo[0], aSelectionFromTo[1]);
						}
						if ( scope.selectTime === -1 ) {
							scope.selectTime = oNavbarVoService.getQueryEndTime();
						} else {
							if ( scope.selectTime !== oNavbarVoService.getQueryEndTime() ) {
								loadAgentInfo( scope.selectTime );
							}
						}
						$timeout(function () {
							loadChartData(agent.agentId, aSelectionFromTo, period, function() {
								initTimePicker( scope.selectTime );
								initTooltip();
								initTimeSlider( aSelectionFromTo, aFromTo );
								setTimeSliderBaseColor();
								getEventList( agent.agentId, aFromTo || calcuSliderTimeSeries( aSelectionFromTo ) );
							});
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