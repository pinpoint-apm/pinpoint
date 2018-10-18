(function( $ ) {
	pinpointApp.constant( "agentInfoDirectiveConfig", {
		ID: "AGENT_INFO_DRTV_"
	});

	pinpointApp.directive( "agentInfoDirective", [ "agentInfoDirectiveConfig", "$sce", "$timeout", "CommonUtilService", "UrlVoService", "AlertsService", "ProgressBarService", "AgentDaoService", "ResponseTimeChartDaoService", "ActiveThreadChartDaoService", "TPSChartDaoService", "CPULoadChartDaoService", "MemoryChartDaoService", "OpenFileDescriptorDaoService", "DirectBufferDaoService","AgentAjaxService", "TooltipService", "AnalyticsService", "helpContentService",
		function ( cfg, $sce, $timeout, CommonUtilService, UrlVoService, AlertsService, ProgressBarService, AgentDaoService, ResponseTimeChartDaoService, ActiveThreadChartDaoService, TPSChartDaoService, CPULoadChartDaoService, MemoryChartDaoService, OpenFileDescriptorDaoService, DirectBufferDaoService, AgentAjaxService, TooltipService, AnalyticsService, helpContentService ) {
			return {
				restrict: 'EA',
				replace: true,
				templateUrl: 'features/agentInfo/agentInfoMain.html?v' + G_BUILD_TIME,
				link: function postLink(scope, element, attrs) {
					cfg.ID += CommonUtilService.getRandomNum();
					scope.agent = {};
					scope.showEventInfo = false;
					scope.showDetail = false;
					scope.selectTime = -1;
					scope.selectedDSIndex = 0;
					var timeSlider = null, bInitTooltip = false;
					var oAlertService = new AlertsService();

					function initTime( time ) {
						scope.targetPicker = moment( time ).format( "YYYY-MM-DD HH:mm:ss" );
					}
					function initTimeSlider( aSelectionFromTo, aFromTo, selectedTime ) {
						if ( timeSlider !== null ) {
							timeSlider.resetTimeSeriesAndSelectionZone( aSelectionFromTo, aFromTo ? aFromTo : calcuSliderTimeSeries( aSelectionFromTo ), selectedTime );
							getTimelineList( scope.agent.agentId, aFromTo || calcuSliderTimeSeries( aSelectionFromTo ) );
						} else {
							timeSlider = new TimeSlider("timeSlider-for-agent-info", {
								"width": $("#timeSlider-for-agent-info").get(0).getBoundingClientRect().width,
								"height": 90,
								"handleSrc": "images/handle.png",
								"timeSeries": aFromTo ? aFromTo : calcuSliderTimeSeries(aSelectionFromTo),
								"handleTimeSeries": aSelectionFromTo,
								"selectTime": selectedTime || aSelectionFromTo[1],
								"timelineData": {}
							}).addEvent("clickEvent", function (aEvent) {// [x, y, obj]
								loadEventInfo(aEvent[2]);
							}).addEvent("selectTime", function (time) {
								scope.selectTime = time;
								loadAgentInfo(time);
								initTime(time);
								sendUpTimeSliderTimeInfo(timeSlider.getSliderTimeSeries(), timeSlider.getSelectionTimeSeries(), time);
							}).addEvent("changeSelectionZone", function (aTime) {
								loadChartData(scope.agent.agentId, aTime, getPeriod(aTime[0], aTime[1]), function () {
								});
								sendUpTimeSliderTimeInfo(timeSlider.getSliderTimeSeries(), aTime, timeSlider.getSelectTime());
							}).addEvent("changeSliderTimeSeries", function (aEvents) {
							});
							getTimelineList( scope.agent.agentId, aFromTo || calcuSliderTimeSeries( aSelectionFromTo ) );
						}
					}
					function sendUpTimeSliderTimeInfo( sliderTimeSeries, sliderSelectionTimeSeries, sliderSelectedTime ) {
						scope.$emit("up.changed.timeSliderOption", sliderTimeSeries, sliderSelectionTimeSeries, sliderSelectedTime );
					}
					function getPeriod( from, to ) {
						return (to - from) / 1000 / 60;
					}
					function loadChartData( agentId, aFromTo, period) {//, callback ) {
						var oParam = {
							"agentId": agentId,
							"from": aFromTo[0],
							"to": aFromTo[1],
							"sampleRate": AgentDaoService.getSampleRate( period )
						};

						// showLoading();
						AgentAjaxService.getJVMChartData( oParam, function (result) {
							if ( angular.isDefined(result.type) ) {
								scope.agent["jvmGcType"] = result.type;
							}
							showJvmChart(result);
						});
						AgentAjaxService.getCpuLoadChartData( oParam, function (result) {
							showCpuLoadChart(result);
						});
						AgentAjaxService.getTPSChartData( oParam, function (result) {
							showTpsChart(result);
						});
						AgentAjaxService.getActiveTraceChartData( oParam, function (result) {
							showActiveTraceChart(result);
						});
						AgentAjaxService.getResponseTimeChartData( oParam, function (result) {
							showResponseTimeChart(result);
						});
						AgentAjaxService.getDataSourceChartData( oParam, function (result) {
							dataSourceChartData = result;
							showDataSourceChart();
						});
						AgentAjaxService.getOpenFileDescriptorChartData( oParam, function (result) {
							showOpenFileDescriptorChart(result);
						});
						AgentAjaxService.getDirectBufferChartData( oParam, function (result) {
							var refinedChartData = DirectBufferDaoService.parseData( result );
							showDirectBufferCountChart(refinedChartData);
							showDirectBufferMemoryChart(refinedChartData);
							showMappedBufferCountChart(refinedChartData);
							showMappedBufferMemoryChart(refinedChartData);
						});
					}
					function loadAgentInfo( time ) {
						AgentAjaxService.getAgentInfo({
							"agentId": scope.agent.agentId,
							"timestamp": time
						}, function( result ) {
							if ( result === '' ) {
								result = {
									"agentId": scope.agent.agentId,
									"agentVersion": "",
									"applicationName": "",
									"hostName": "",
									"initialStartTimestamp": 0,
									"ip": "",
									"jvmInfo": {
										"gcTypeName": "",
										"jvmVersion": ""
									},
									"pid": "",
									"ports": "",
									"status": {
										"agentId": "",
										"state": {
											"desc": ""
										}
									},
									"jvmGcType": "",
									"vmVersion": ""
								};
							}
							var jvmGcType = scope.agent.jvmGcType;
							scope.agent = result;
							scope.agent.jvmGcType = jvmGcType;
							scope.currentServiceInfo = initServiceInfo(result);
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
							["heap", "permGen", "cpuUsage", "tps", "activeThread", "responseTime", "dataSource", "openFileDescriptor", "directBufferCount", "directBufferMemory", "mappedBufferCount", "mappedBufferMemory"].forEach(function(value) {
								TooltipService.init( value );
							});
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
					// function showLoading() {
					// 	scope.$broadcast("agentInspectorChartDirective.showLoading.agent-heap");
					// 	scope.$broadcast("agentInspectorChartDirective.showLoading.agent-non-heap");
					// 	scope.$broadcast("agentInspectorChartDirective.showLoading.agent-cpu-load");
					// 	scope.$broadcast("agentInspectorChartDirective.showLoading.agent-tps");
					// 	scope.$broadcast("agentInspectorChartDirective.showLoading.agent-active-thread");
					// 	scope.$broadcast("agentInspectorChartDirective.showLoading.agent-response-time");
					// 	scope.$broadcast("dsChartDirective.showLoading.agent-data-source");
					// }
					function showJvmChart( chartData ) {
						var refinedChartData = MemoryChartDaoService.parseHeapData( chartData );
						scope.$broadcast(
							"agentInspectorChartDirective.initAndRenderWithData.agent-heap",
							refinedChartData,
							MemoryChartDaoService.getChartOptions( refinedChartData ),
							"100%",
							"270px"
						);

						var refinedChartData2 = MemoryChartDaoService.parseNonHeapData( chartData );
						scope.$broadcast(
							"agentInspectorChartDirective.initAndRenderWithData.agent-non-heap",
							refinedChartData2,
							MemoryChartDaoService.getChartOptions( refinedChartData2 ),
							"100%",
							"270px"
						);

					}
					function showCpuLoadChart( chartData ) {
						var refinedChartData = CPULoadChartDaoService.parseData( chartData );
						scope.$broadcast(
							"agentInspectorChartDirective.initAndRenderWithData.agent-cpu-load",
							refinedChartData,
							CPULoadChartDaoService.getChartOptions( refinedChartData ),
							"100%",
							"270px"
						);
					}
					function showTpsChart( chartData ) {
						var refinedChartData = TPSChartDaoService.parseData( chartData );
						scope.$broadcast(
							"agentInspectorChartDirective.initAndRenderWithData.agent-tps",
							refinedChartData,
							TPSChartDaoService.getChartOptions( refinedChartData ),
							"100%",
							"270px"
						);
					}
					function showActiveTraceChart( chartData ) {
						var refinedChartData = ActiveThreadChartDaoService.parseData( chartData );
						scope.$broadcast(
							"agentInspectorChartDirective.initAndRenderWithData.agent-active-thread",
							refinedChartData,
							ActiveThreadChartDaoService.getChartOptions( refinedChartData ),
							"100%",
							"270px"
						);
					}
					function showResponseTimeChart( chartData ) {
						var refinedChartData = ResponseTimeChartDaoService.parseData( chartData );
						scope.$broadcast(
							"agentInspectorChartDirective.initAndRenderWithData.agent-response-time",
							refinedChartData,
							ResponseTimeChartDaoService.getChartOptions( refinedChartData ),
							"100%",
							"270px"
						);
					}
					function showOpenFileDescriptorChart( chartData ) {
						var refinedChartData = OpenFileDescriptorDaoService.parseData( chartData );
						scope.$broadcast(
							"agentInspectorChartDirective.initAndRenderWithData.agent-open-file-descriptor",
							refinedChartData,
							OpenFileDescriptorDaoService.getChartOptions( refinedChartData ),
							"100%",
							"270px"
						);
					}
					function showDirectBufferCountChart( refinedChartData ) {
						scope.$broadcast(
							"agentInspectorChartDirective.initAndRenderWithData.agent-direct-buffer-count",
							refinedChartData,
							DirectBufferDaoService.getDirectBufferCountChartOptions( refinedChartData ),
							"100%",
							"270px"
						);
					}
					function showDirectBufferMemoryChart( refinedChartData ) {
						scope.$broadcast(
							"agentInspectorChartDirective.initAndRenderWithData.agent-direct-buffer-memory",
							refinedChartData,
							DirectBufferDaoService.getDirectBufferMemoryChartOptions( refinedChartData ),
							"100%",
							"270px"
						);
					}
					function showMappedBufferCountChart( refinedChartData ) {
						scope.$broadcast(
							"agentInspectorChartDirective.initAndRenderWithData.agent-mapped-buffer-count",
							refinedChartData,
							DirectBufferDaoService.getMappedBufferCountChartOptions( refinedChartData ),
							"100%",
							"270px"
						);
					}
					function showMappedBufferMemoryChart( refinedChartData ) {
						scope.$broadcast(
							"agentInspectorChartDirective.initAndRenderWithData.agent-mapped-buffer-memory",
							refinedChartData,
							DirectBufferDaoService.getMappedBufferMemoryChartOptions( refinedChartData ),
							"100%",
							"270px"
						);
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
							scope.$broadcast( "dsChartDirective.toggleGraph.agent-data-source", $event.target.value, $event.target.checked );
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
						scope.$broadcast( "dsChartDirective.toggleGraphAll.agent-data-source" );
					};
					scope.hasDataSource = function() {
						return dataSourceChartData.length !== 0 && dataSourceChartData[0].charts.y["ACTIVE_CONNECTION_SIZE"].length !== 0;
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
						scope.dataSourceChartKeys = dataSourceChartData.map(function(obj, index) {
							var key = dataSourceIdPrefix + obj.id;
							scope.dataSourceChartCheckedKeys[key] = index < 30 ? true : false;
							return {
								display: obj["databaseName"] ? obj["databaseName"] : obj["id"],
								value: key
							};
						});
						scope.$broadcast( "dsChartDirective.initAndRenderWithData.agent-data-source", AgentDaoService.parseDataSourceChartDataForAmcharts( dataSourceChartData, dataSourceIdPrefix ), scope.dataSourceChartCheckedKeys, "100%", "270px");
					}
					function showDataSourceDetailInfo( targetId, index ) {
						var id = parseInt( targetId.split("_")[1] );
						for( var i = 0 ; i < dataSourceChartData.length ; i++ ) {
							var oTarget = dataSourceChartData[i];
							if ( oTarget.id == id ) {
								setDataSourceDetail(
									oTarget.charts.y["ACTIVE_CONNECTION_SIZE"][index][2],
									oTarget.charts.y["ACTIVE_CONNECTION_SIZE"][index][1],
									oTarget.charts.y["MAX_CONNECTION_SIZE"][index][1],
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
								sendUpTimeSliderTimeInfo(timeSlider.getSliderTimeSeries(), timeSlider.getSelectionTimeSeries(), scope.selectTime);
							}
						});
					}
					function removePopover() {
						$("._wrongApp").popover("destroy");
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
						return moment(time).format("YYYY.MM.DD HH:mm:ss");
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
						removePopover();
						if( cfg.ID === invokerId ) return;
						if ( CommonUtilService.isEmpty( agent.agentId ) ) {
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
						scope.agent = agent;
						scope.chartGroup = null;
						scope.currentServiceInfo = initServiceInfo(agent);

						var aFromTo, period, aSelectionFromTo = [], selectedTime;
						if ( bInvokedByTop ) {
							aSelectionFromTo[0] = UrlVoService.getQueryStartTime();
							aSelectionFromTo[1] = UrlVoService.getQueryEndTime();
							period = UrlVoService.getPeriod();
						} else {
							if ( sliderTimeSeriesOption === undefined || sliderTimeSeriesOption === null ) {
								if ( timeSlider === null ) {
									aSelectionFromTo[0] = UrlVoService.getQueryStartTime();
									aSelectionFromTo[1] = UrlVoService.getQueryEndTime();
								} else {
									aSelectionFromTo = timeSlider.getSelectionTimeSeries();
									aFromTo = timeSlider.getSliderTimeSeries();
								}
								period = UrlVoService.getPeriod();
							} else {
								aSelectionFromTo = sliderTimeSeriesOption["selectionTimeSeries"];
								aFromTo = sliderTimeSeriesOption["timeSeries"];
								selectedTime = sliderTimeSeriesOption["selectedTime"];
								scope.selectTime = selectedTime;
							}
						}
						if ( scope.selectTime === -1 || bInvokedByTop ) {
							scope.selectTime = UrlVoService.getQueryEndTime();
						} else {
							if ( scope.selectTime !== UrlVoService.getQueryEndTime() ) {
								loadAgentInfo( scope.selectTime );
							}
						}
						initTimeSliderUI( aSelectionFromTo, aFromTo, selectedTime || scope.selectTime );
						loadChartData(agent["agentId"], aSelectionFromTo, period );
						initTooltip();
					});
					function initTimeSliderUI( aSelectionFromTo, aFromTo, selectedTime ) {
						initTime( selectedTime );
						initTimeSlider( aSelectionFromTo, aFromTo, selectedTime );
						// getTimelineList( scope.agent.agentId, aFromTo || calcuSliderTimeSeries( aSelectionFromTo ) );
					}
					scope.$on("agentInspectorChartDirective.cursorChanged", function (e, sourceTarget, event) {
						if ( typeof event.index === "undefined" ) {
							timeSlider.hideFocus();
						} else {
							timeSlider.showFocus( moment(event.target.chart.dataProvider[event.index].time).valueOf() );
						}
						scope.$broadcast( "agentInspectorChartDirective.showCursorAt", sourceTarget, event.index );
						scope.$broadcast( "dsChartDirective.showCursorAt.agent-data-source", event.index);
					});
					scope.$on("dsChartDirective.cursorChanged.agent-data-source", function (e, targetId, index) {
						showDataSourceDetailInfo( targetId, index );
						scope.$broadcast( "agentInspectorChartDirective.showCursorAt", "agent-data-source", index );
					});
				}
			};
		}
	]);
})(jQuery);