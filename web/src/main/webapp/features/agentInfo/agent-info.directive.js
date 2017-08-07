(function( $ ) {
	pinpointApp.constant( "agentInfoDirectiveConfig", {
		ID: "AGENT_INFO_DRTV_"
	});

	pinpointApp.directive( "agentInfoDirective", [ "agentInfoDirectiveConfig", "$sce", "$timeout", "SystemConfigurationService", "CommonUtilService", "UrlVoService", "AlertsService", "ProgressBarService", "AgentDaoService", "AgentAjaxService", "TooltipService", "AnalyticsService", "helpContentService",
		function ( cfg, $sce, $timeout, SystemConfigService, CommonUtilService, UrlVoService, AlertsService, ProgressBarService, AgentDaoService, AgentAjaxService, TooltipService, AnalyticsService, helpContentService ) {
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
					scope.selectedDSIndex = 0;
					scope.enableDataSourceChart = SystemConfigService.get("showInspectorDataSource") === true;
					var timeSlider = null, bInitTooltip = false;
					var oAlertService = new AlertsService();
					var oProgressBarService = new ProgressBarService();


					function initTime( time ) {
						scope.targetPicker = moment( time ).format( "YYYY-MM-DD HH:mm:ss" );
					}
					function initTimeSlider( aSelectionFromTo, aFromTo, selectedTime ) {
						if ( timeSlider !== null ) {
							timeSlider.resetTimeSeriesAndSelectionZone( aSelectionFromTo, aFromTo ? aFromTo : calcuSliderTimeSeries( aSelectionFromTo ), selectedTime );
						} else {
							timeSlider = new TimeSlider( "timeSlider-for-agent-info", {
								"width": $("#timeSlider-for-agent-info").get(0).getBoundingClientRect().width,
								"height": 90,
								"handleSrc": "images/handle.png",
								"timeSeries": aFromTo ? aFromTo : calcuSliderTimeSeries( aSelectionFromTo ),
								"handleTimeSeries": aSelectionFromTo,
								"selectTime": aSelectionFromTo[1],
								"timelineData": {}
							}).addEvent("clickEvent", function( aEvent ) {// [x, y, obj]
								loadEventInfo(aEvent[2]);
							}).addEvent("selectTime", function( time ) {
								scope.selectTime = time;
								loadAgentInfo( time );
								initTime( time );
								sendUpTimeSliderTimeInfo( timeSlider.getSliderTimeSeries(), timeSlider.getSelectionTimeSeries(), time );
							}).addEvent("changeSelectionZone", function( aTime ) {
								loadChartData( scope.agent.agentId, aTime, getPeriod(aTime[0], aTime[1] ), function() {});
								sendUpTimeSliderTimeInfo( timeSlider.getSliderTimeSeries(), aTime, timeSlider.getSelectTime() );
							}).addEvent("changeSliderTimeSeries", function( aEvents ) {});
						}
					}
					function sendUpTimeSliderTimeInfo( sliderTimeSeries, sliderSelectionTimeSeries, sliderSelectedTime ) {
						scope.$emit("up.changed.timeSliderOption", sliderTimeSeries, sliderSelectionTimeSeries, sliderSelectedTime );
					}
					function getPeriod( from, to ) {
						return (to - from) / 1000 / 60;
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
						AgentAjaxService.getResponseTimeChartData( oParam, function (result) {
							if ( checkResponse( result ) ) {
								showResponseTimeChart(result);
							}
						});
						AgentAjaxService.getDataSourceChartData( oParam, function (result) {
							if ( checkResponse( result ) ) {
								dataSourceChartData = result;
								showDataSourceChart();
							}
						});
						function checkResponse( result ) {
							responseCount++;
							oProgressBarService.setLoading(20 + (responseCount * 16) );
							if ( responseCount >= 6 ) {
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
						AgentAjaxService.getEventList({
							"agentId": scope.agent.agentId,
							"from": oEvent.startTimestamp,
							"to": oEvent.endTimestamp
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
							TooltipService.init( "responseTime" );
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
					function showResponseTimeChart( chartData ) {
						var responseTime = { id: "responseTime", title: "Response Time", isAvailable: false};
						scope.responseTimeChart = responseTime;

						scope.$broadcast( "responseTimeChartDirective.initAndRenderWithData.forResponseTime", AgentDaoService.parseResponseTimeChartDataForAmcharts(responseTime, chartData), '100%', '270px');
					}

					var dataSourceChartData = [];
					var dataSourceIdPrefix = "source_";
					scope.dataSourceChartKeys = [];
					scope.dataSourceChartCheckedKeys = [];
					scope.checkChecked = function( key ) {
						return scope.dataSourceChartCheckedKeys[key];
					};
					scope.changeDataSource = function( $event ) {
						var tagName = $event.target.tagName.toUpperCase();
						if ( tagName === "INPUT" ) {
							scope.$broadcast( "dsChartDirective.toggleGraph.forDataSource", $event.target.value, $event.target.checked );
						}
					};
					scope.selectAllDataSource = function() {
						for (var p in scope.dataSourceChartCheckedKeys) {
							scope.dataSourceChartCheckedKeys[p] = true;
						}
						scope.dataSourceChartKeys = scope.dataSourceChartKeys.map(function(v) {
							return {
								display: v.display,
								value: v.value
							};
						});
						scope.$broadcast( "dsChartDirective.toggleGraphAll.forDataSource" );
					};
					scope.hasDataSource = function() {
						return dataSourceChartData.length === 0 || dataSourceChartData[0].id === -1 ? false : true;
					};
					scope.emptyDataSource = function() {
						return dataSourceChartData.length === 0 ? false : dataSourceChartData[0].id === -1 ? true : false;
					};
					function setDataSourceDetail( activeAvg, activeMax, totalMax, id, type, databaseName, jdbcUrl ) {
						var bInit = arguments.length === 0 ? true : false;
						element.find(".ds-active-avg").html( bInit ? "-" : activeAvg );
						element.find(".ds-active-max").html( bInit ? "-" : activeMax );
						element.find(".ds-total-max").html( bInit ? "-" : totalMax );
						element.find(".ds-id").html( bInit ? "-" : id );
						element.find(".ds-type").html( bInit ? "-" : type );
						element.find(".ds-database-name").html( bInit ? "-" : databaseName );
						element.find(".ds-jdbc-url").html( bInit ? "-" : jdbcUrl );
					}
					function showDataSourceChart() {
						scope.dataSourceChartCheckedKeys = {};
						var description = { id: "dataSource", title: "Data Source", isAvailable: false };
						scope.dataSourceChartKeys = dataSourceChartData.map(function(obj, index) {
							var key = dataSourceIdPrefix + obj.id;
							scope.dataSourceChartCheckedKeys[key] = index < 30 ? true : false;
							return {
								display: obj.databaseName ? obj.databaseName : obj.id,
								value: key
							};
						});
						scope.dataSourceChartDescription = description;

						scope.$broadcast( "dsChartDirective.initAndRenderWithData.forDataSource", AgentDaoService.parseDataSourceChartDataForAmcharts(description, dataSourceChartData, dataSourceIdPrefix), scope.dataSourceChartCheckedKeys, '100%', '270px');
					}
					function showDataSourceDetailInfo( targetId, index ) {
						var id = parseInt( targetId.split("_")[1] );
						for( var i = 0 ; i < dataSourceChartData.length ; i++ ) {
							var oTarget = dataSourceChartData[i];
							if ( oTarget.id == id ) {
								setDataSourceDetail(
									oTarget.charts["ACTIVE_CONNECTION_SIZE"].points[index].avgYVal,
									oTarget.charts["ACTIVE_CONNECTION_SIZE"].points[index].maxYVal,
									oTarget.charts["MAX_CONNECTION_SIZE"].points[index].maxYVal,
									oTarget.id,
									oTarget.serviceType,
									oTarget.databaseName,
									oTarget.jdbcUrl
								);
							}
						}
					}
					function getTimelineList( agentId, aFromTo ) {
						AgentAjaxService.getAgentTimeline({
							"agentId": agentId,
							"from": aFromTo[0],
							"to": aFromTo[1]
						}, function( result ) {
							if ( result.errorCode || result.status ) {
								oAlertService.showError('There is some error.');
							} else {
								timeSlider.addData(result);
							}
						});
					}
					function broadcastToCpuLoadChart(e, event) {
						if (scope.cpuLoadChart.isAvailable) {
							scope.$broadcast("cpuLoadChartDirective.showCursorAt.forCpuLoad", event.index);
						}
					}
					function broadcastToTpsChart(e, event) {
						if (scope.tpsChart.isAvailable) {
							scope.$broadcast("tpsChartDirective.showCursorAt.forTps", event.index);
						}
					}
					function broadcastToActiveTraceChart(e, event) {
						if (scope.activeTraceChart.isAvailable) {
							scope.$broadcast("activeTraceChartDirective.showCursorAt.forActiveTrace", event.index);
						}
					}
					function broadcastToResponseTimeChart(e, event) {
						if (scope.responseTimeChart.isAvailable) {
							scope.$broadcast("responseTimeChartDirective.showCursorAt.forResponseTime", event.index);
						}
					}
					function broadcastToDataSourceChart(e, event) {
						if (scope.dataSourceChartDescription.isAvailable) {
							scope.$broadcast("dsChartDirective.showCursorAt.forDataSource", event.index);
						}
					}
					scope.toggleSourceSelectLayer = function() {
						element.find("#data-source-chart .type-select-layer").toggle();
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
						getTimelineList( scope.agent.agentId, timeSlider.getSliderTimeSeries() );
					};
					scope.moveNext = function() {
						timeSlider.moveNext();
						getTimelineList( scope.agent.agentId, timeSlider.getSliderTimeSeries() );
					};
					scope.moveHead = function() {
						timeSlider.moveHead();
						getTimelineList( scope.agent.agentId, timeSlider.getSliderTimeSeries() );
					};
					scope.zoomInTimeSlider = function() {
						timeSlider.zoomIn();
						getTimelineList( scope.agent.agentId, timeSlider.getSliderTimeSeries() );
					};
					scope.zoomOutTimeSlider = function() {
						timeSlider.zoomOut();
						getTimelineList( scope.agent.agentId, timeSlider.getSliderTimeSeries() );
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
					scope.replaceNewLineToBR = function( msg ) {
						if ( msg ) {
							return $sce.trustAsHtml(msg.replace(/\n/g, "<br>"));
						} else {
							return '';
						}
					};
					scope.$on( "down.changed.agent", function ( event, invokerId, agent, bInvokedByTop, sliderTimeSeriesOption ) {
						if( cfg.ID === invokerId ) return;
						if ( CommonUtilService.isEmpty( agent.agentId ) ) {
							scope.hasAgentData = false;
							return;
						}
						// init data-source data
						dataSourceChartData = [];
						scope.dataSourceChartKeys = [];
						scope.dataSourceChartCheckedKeys = [];
						setDataSourceDetail();
						element.find(".type-select-layer").hide();
						element.find(".ds-detail").hide();
						scope.showEventInfo = false;
						scope.hasAgentData = true;
						scope.agent = agent;
						scope.chartGroup = null;
						scope.currentServiceInfo = initServiceInfo(agent);

						var aFromTo, period, aSelectionFromTo = [], selectedTime;
						if ( timeSlider === null || bInvokedByTop ) {
							aSelectionFromTo[0] = UrlVoService.getQueryStartTime();
							aSelectionFromTo[1] = UrlVoService.getQueryEndTime();
							period = UrlVoService.getPeriod();
						} else {
							if ( sliderTimeSeriesOption === undefined || sliderTimeSeriesOption === null ) {
								aSelectionFromTo = timeSlider.getSelectionTimeSeries();
								aFromTo = timeSlider.getSliderTimeSeries();
								// period = getPeriod(aSelectionFromTo[0], aSelectionFromTo[1]);
								period = UrlVoService.getPeriod();
							} else {
								aSelectionFromTo = sliderTimeSeriesOption["selectionTimeSeries"];
								aFromTo = sliderTimeSeriesOption["timeSeries"];
								selectedTime = sliderTimeSeriesOption["selectedTime"];
							}
						}
						if ( scope.selectTime === -1 || bInvokedByTop ) {
							scope.selectTime = UrlVoService.getQueryEndTime();
						} else {
							if ( scope.selectTime !== UrlVoService.getQueryEndTime() ) {
								loadAgentInfo( scope.selectTime );
							}
						}
						loadChartData(agent.agentId, aSelectionFromTo, period, function() {
							initTimeSliderUI( aSelectionFromTo, aFromTo, selectedTime );
						});

						initTooltip();

					});
					function initTimeSliderUI( aSelectionFromTo, aFromTo, selectedTime ) {
						initTime( selectedTime || scope.selectTime );
						initTimeSlider( aSelectionFromTo, aFromTo, selectedTime );
						getTimelineList( scope.agent.agentId, aFromTo || calcuSliderTimeSeries( aSelectionFromTo ) );
					}

					scope.$on("jvmMemoryChartDirective.cursorChanged.forHeap", function (e, event) {
						scope.$broadcast('jvmMemoryChart.showCursorAt.forNonHeap', event.index);
						broadcastToCpuLoadChart(e, event);
						broadcastToTpsChart(e, event);
						broadcastToActiveTraceChart(e, event);
						broadcastToResponseTimeChart(e, event);
						broadcastToDataSourceChart(e, event);
					});
					scope.$on("jvmMemoryChartDirective.cursorChanged.forNonHeap", function (e, event) {
						scope.$broadcast("jvmMemoryChartDirective.showCursorAt.forHeap", event.index);
						broadcastToCpuLoadChart(e, event);
						broadcastToTpsChart(e, event);
						broadcastToActiveTraceChart(e, event);
						broadcastToResponseTimeChart(e, event);
						broadcastToDataSourceChart(e, event);
					});
					scope.$on("cpuLoadChartDirective.cursorChanged.forCpuLoad", function (e, event) {
						scope.$broadcast("jvmMemoryChartDirective.showCursorAt.forHeap", event.index);
						scope.$broadcast("jvmMemoryChartDirective.showCursorAt.forNonHeap", event.index);
						broadcastToTpsChart(e, event);
						broadcastToActiveTraceChart(e, event);
						broadcastToResponseTimeChart(e, event);
						broadcastToDataSourceChart(e, event);
					});
					scope.$on("tpsChartDirective.cursorChanged.forTps", function (e, event) {
						scope.$broadcast("jvmMemoryChartDirective.showCursorAt.forHeap", event.index);
						scope.$broadcast("jvmMemoryChartDirective.showCursorAt.forNonHeap", event.index);
						broadcastToCpuLoadChart(e, event);
						broadcastToActiveTraceChart(e, event);
						broadcastToResponseTimeChart(e, event);
						broadcastToDataSourceChart(e, event);
					});
					scope.$on("activeTraceChartDirective.cursorChanged.forActiveTrace", function (e, event) {
						scope.$broadcast("jvmMemoryChartDirective.showCursorAt.forHeap", event.index);
						scope.$broadcast("jvmMemoryChartDirective.showCursorAt.forNonHeap", event.index);
						broadcastToCpuLoadChart(e, event);
						broadcastToTpsChart(e, event);
						broadcastToResponseTimeChart(e, event);
						broadcastToDataSourceChart(e, event);
					});
					scope.$on("responseTimeChartDirective.cursorChanged.forResponseTime", function (e, event) {
						scope.$broadcast("jvmMemoryChartDirective.showCursorAt.forHeap", event.index);
						scope.$broadcast("jvmMemoryChartDirective.showCursorAt.forNonHeap", event.index);
						broadcastToCpuLoadChart(e, event);
						broadcastToTpsChart(e, event);
						broadcastToActiveTraceChart(e, event);
						broadcastToDataSourceChart(e, event);
					});
					scope.$on("dsChartDirective.cursorChanged.forDataSource", function (e, targetId, index) {
						var o = { "index": index };
						showDataSourceDetailInfo( targetId, index );
						scope.$broadcast("jvmMemoryChartDirective.showCursorAt.forHeap", index);
						scope.$broadcast("jvmMemoryChartDirective.showCursorAt.forNonHeap", index);
						broadcastToCpuLoadChart(e, o);
						broadcastToTpsChart(e, o);
						broadcastToActiveTraceChart(e, o);
					});
				}
			};
		}
	]);
})(jQuery);