(function( $ ) {
	pinpointApp.constant( "applicationStatisticDirectiveConfig", {
		ID: "APPLICATION_STATISTIC_DRTV_"
	});

	pinpointApp.directive( "applicationStatisticDirective", [ "applicationStatisticDirectiveConfig", "$sce", "$http", "$timeout", "CommonUtilService", "UrlVoService", "AlertsService", "ProgressBarService", "AgentDaoService", "AgentAjaxService", "TooltipService", "AnalyticsService", "helpContentService",
		function ( cfg, $sce, $http, $timeout, CommonUtilService, UrlVoService, AlertsService, ProgressBarService, AgentDaoService, AgentAjaxService, TooltipService, AnalyticsService, helpContentService ) {
			return {
				restrict: 'EA',
				replace: true,
				templateUrl: 'features/applicationStatistic/applicationStatistic.html?v' + G_BUILD_TIME,
				link: function postLink(scope, element, attrs) {
					cfg.ID += CommonUtilService.getRandomNum();
					scope.dataSourceData;
					scope.dataSourceSelectedIndex = 0;
					scope.noDataCollected = helpContentService.inspector.noDataCollected;
					var bEmptyDataSource = false;

					scope.$on( "down.select.application", function (event, invokeId, sliderTimeSeriesOption) {
						removePopover();
						initTimeSliderUI(sliderTimeSeriesOption);
						if ( sliderTimeSeriesOption === undefined || sliderTimeSeriesOption === null ) {
							loadStatChart(UrlVoService.getQueryStartTime(), UrlVoService.getQueryEndTime());
						} else {
							loadStatChart(sliderTimeSeriesOption["selectionTimeSeries"][0], sliderTimeSeriesOption["selectionTimeSeries"][1]);
						}
					});
					scope.$on( "down.changed.application", function () {
						removePopover();
						initTimeSliderUI();
						loadStatChart( UrlVoService.getQueryStartTime(), UrlVoService.getQueryEndTime() );
					});
					scope.$on( "down.changed.period", function () {
						removePopover();
						initTimeSliderUI();
						loadStatChart( UrlVoService.getQueryStartTime(), UrlVoService.getQueryEndTime() );
					});
					function removePopover() {
						$("._wrongApp").popover("destroy");
					}

					function initTooltip() {
						[ "statHeap", "statPermGen", "statJVMCpu", "statSystemCpu", "statTPS", "statActiveThread", "statResponseTime", "statDataSource", "statOpenFileDescriptor", "statDirectBufferCount", "statDirectBufferMemory", "statMappedBufferCount", "statMappedBufferMemory" ].forEach(function( name ) {
							TooltipService.init( name );
						});
					}
					function loadStatChart(from, to) {
						var oParam = {
							to : to,
							from : from,
							sampleRate: 1,
							applicationId: UrlVoService.getApplicationName()
						};
						if ( oParam.from > 0 && oParam.to > 0 ) {
							AgentAjaxService.getStatMemory( oParam, function(chartData) {
								if ( angular.isUndefined(chartData.exception) ) {
									scope.$broadcast("statisticChartDirective.initAndRenderWithData.application-heap", makeChartData({
										title: "Memory Heap",
										fixMax: false,
										defaultMax: 100000,
										yAxisTitle: "Memory(bytes)",
										labelFunc: function(value) {
											return convertWithUnits(value, ["", "K", "M", "G"]);
										}
									}, chartData.charts.x, chartData.charts.y["MEMORY_HEAP"]), "100%", "270px");
									scope.$broadcast("statisticChartDirective.initAndRenderWithData.application-non-heap", makeChartData({
										title: "Memory Non Heap",
										fixMax: false,
										defaultMax: 100000,
										yAxisTitle: "Memory(bytes)",
										labelFunc: function(value) {
											return convertWithUnits(value, ["", "K", "M", "G"]);
										}
									}, chartData.charts.x, chartData.charts.y["MEMORY_NON_HEAP"]), "100%", "270px");
								} else {
									console.log("error");
								}
							});
							AgentAjaxService.getStatCpuLoad( oParam, function(chartData) {
								if ( angular.isUndefined(chartData.exception) ) {
									scope.$broadcast("statisticChartDirective.initAndRenderWithData.application-jvm", makeChartData({
										title: "JVM Cpu Usage",
										fixMax: true,
										appendUnit: "%",
										defaultMax: 100,
										yAxisTitle: "Cpu Usage (%)",
										labelFunc: function(value) {
											return value + "%";
										}
									}, chartData.charts.x, chartData.charts.y["CPU_LOAD_JVM"]), "100%", "270px");
									scope.$broadcast("statisticChartDirective.initAndRenderWithData.application-system", makeChartData({
										title: "System Cpu Usage",
										fixMax: true,
										appendUnit: "%",
										defaultMax: 100,
										yAxisTitle: "Cpu Usage (%)",
										labelFunc: function(value) {
											return value + "%";
										}
									}, chartData.charts.x, chartData.charts.y["CPU_LOAD_SYSTEM"]), "100%", "270px");
								} else {
								}
							});
							AgentAjaxService.getStatTPS( oParam, function(chartData) {
								if ( angular.isUndefined(chartData.exception) ) {
									scope.$broadcast("statisticChartDirective.initAndRenderWithData.application-tps", makeChartData({
										title: "Transaction Per Second",
										fixMax: false,
										defaultMax: 10,
										yAxisTitle: "Transaction(count)",
										labelFunc: function(value) {
											return convertWithUnits(value, ["", "K", "M", "G"]);
										}
									}, chartData.charts.x, chartData.charts.y["TRANSACTION_COUNT"]), "100%", "270px");
								} else {
									console.log("error");
								}
							});
							AgentAjaxService.getStatActiveThread( oParam, function(chartData) {
								if ( angular.isUndefined(chartData.exception) ) {
									scope.$broadcast("statisticChartDirective.initAndRenderWithData.application-active-thread", makeChartData({
										title: "Active Thread",
										fixMax: false,
										defaultMax: 10,
										yAxisTitle: "Active Thread(count)",
										labelFunc: function(value) {
											return convertWithUnits(value, ["", "K", "M", "G"]);
										}
									}, chartData.charts.x, chartData.charts.y["ACTIVE_TRACE_COUNT"]), "100%", "270px");
								} else {
									console.log("error");
								}
							});
							AgentAjaxService.getStatResponseTime( oParam, function(chartData) {
								if ( angular.isUndefined(chartData.exception) ) {
									scope.$broadcast("statisticChartDirective.initAndRenderWithData.application-response-time", makeChartData({
										title: "Response Time",
										fixMax: false,
										defaultMax: 100,
										yAxisTitle: "Response Time(ms)",
										labelFunc: function(value) {
											return convertWithUnits(value, ["ms", "sec", "min"]);
										}
									}, chartData.charts.x, chartData.charts.y["RESPONSE_TIME"]), "100%", "270px");
								} else {
									console.log("error");
								}
							});
							AgentAjaxService.getStatDataSource( oParam, function(chartData) {
								if ( angular.isUndefined(chartData.exception) ) {
									scope.dataSourceData = chartData;
									bEmptyDataSource = false;
									broadcastToDataSource(chartData[scope.dataSourceSelectedIndex].charts.x, chartData[scope.dataSourceSelectedIndex].charts.y["ACTIVE_CONNECTION_SIZE"]);
								} else {
									console.log("error");
								}
							});
							AgentAjaxService.getStatOpenFileDescriptor( oParam, function(chartData) {
								if ( angular.isUndefined(chartData.exception) ) {
									scope.$broadcast("statisticChartDirective.initAndRenderWithData.application-open-file-descriptor", makeChartData({
										title: "Open File Descriptor",
										fixMax: false,
										defaultMax: 100,
										yAxisTitle: "File Descriptor(count)",
										labelFunc: function(value) {
											return value;
										}
									}, chartData.charts.x, chartData.charts.y["OPEN_FILE_DESCRIPTOR_COUNT"]), "100%", "270px");
								} else {
									console.log("error");
								}
							});
							AgentAjaxService.getStatDirectBuffer( oParam, function(chartData) {
								if ( angular.isUndefined(chartData.exception) ) {
									scope.$broadcast("statisticChartDirective.initAndRenderWithData.application-direct-buffer-count", makeChartData({
										title: "Direct Buffer Count",
										fixMax: false,
										defaultMax: 100,
										yAxisTitle: "Buffer (count)",
										labelFunc: function(value) {
											return value;
										}
									}, chartData.charts.x, chartData.charts.y["DIRECT_COUNT"]), "100%", "270px");
									scope.$broadcast("statisticChartDirective.initAndRenderWithData.application-mapped-buffer-count", makeChartData({
										title: "Mapped Buffer Count",
										fixMax: false,
										defaultMax: 100,
										yAxisTitle: "Buffer (count)",
										labelFunc: function(value) {
											return value;
										}
									}, chartData.charts.x, chartData.charts.y["MAPPED_COUNT"]), "100%", "270px");
									scope.$broadcast("statisticChartDirective.initAndRenderWithData.application-direct-buffer-memory", makeChartData({
										title: "Direct Buffer memory",
										fixMax: false,
										defaultMax: 100,
										yAxisTitle: "Memory (bytes)",
										labelFunc: function(value) {
											return convertWithUnits(value, ["", "K", "M", "G"]);
										}
									}, chartData.charts.x, chartData.charts.y["DIRECT_MEMORY_USED"]), "100%", "270px");
									scope.$broadcast("statisticChartDirective.initAndRenderWithData.application-mapped-buffer-memory", makeChartData({
										title: "Mapped Buffer Memory",
										fixMax: false,
										defaultMax: 100,
										yAxisTitle: "Memory (bytes)",
										labelFunc: function(value) {
											return convertWithUnits(value, ["", "K", "M", "G"]);
										}
									}, chartData.charts.x, chartData.charts.y["MAPPED_MEMORY_USED"]), "100%", "270px");
								} else {
									console.log("error");
								}
							});
						}
					}
					function broadcastToDataSource( xData, yData ) {
						var oMakeChartData = makeChartData({
							title: "Data Source",
							fixMax: false,
							defaultMax: 10,
							yAxisTitle: "Connection(count)",
							labelFunc: function(value, valueStr) {
								return valueStr;
							}
						}, xData, yData);
						bEmptyDataSource = oMakeChartData.empty;
						scope.$broadcast("statisticChartDirective.initAndRenderWithData.application-data-source", oMakeChartData, "100%", "270px");
					}
					function convertWithUnits(value, units) {
						var result = value;
						var index = 0;
						while ( result >= 1000 ) {
							index++;
							result /= 1000;
						}
						return result + units[index] + " ";
					}
					function makeChartData(chartProperty, aX, aY) {
						var xLen = aX.length;
						var yLen = aY.length;
						var returnData = {
							data: [],
							empty: yLen === 0 ? true : false,
							field: ["avg", "max", "min", "maxAgent", "minAgent"],
							fixMax: chartProperty.fixMax,
							category: ["Avg", "Max", "Min"],
							labelFunc: chartProperty.labelFunc,
							yAxisTitle: chartProperty.yAxisTitle,
							defaultMax: chartProperty.defaultMax,
							appendUnit: chartProperty.appendUnit || ""
						};

						for (var i = 0; i < xLen; ++i) {
							var thisData = {
								"time": moment(aX[i]).format("YYYY-MM-DD HH:mm:ss")
							};
							if ( yLen > i ) {
								thisData["avg"] = aY[i][4] === -1 ? null : aY[i][4].toFixed(2);
								thisData["min"] = aY[i][0] === -1 ? null : aY[i][0].toFixed(2);
								thisData["max"] = aY[i][2] === -1 ? null : aY[i][2].toFixed(2);
								thisData["minAgent"] = aY[i][1];
								thisData["maxAgent"] = aY[i][3];
							}
							returnData.data.push( thisData );
						}
						return returnData;
					}

					scope.selectTime = -1;
					var timeSlider = null;
					function initTimeSliderUI( sliderTimeSeriesOption ) {
						if ( angular.isUndefined( sliderTimeSeriesOption ) || sliderTimeSeriesOption === null ) {
							var aSelectionFromTo = [];
							aSelectionFromTo[0] = UrlVoService.getQueryStartTime();
							aSelectionFromTo[1] = UrlVoService.getQueryEndTime();
							if (scope.selectTime === -1) {
								scope.selectTime = UrlVoService.getQueryEndTime();
							}
							initTime(scope.selectTime);
							initTimeSlider(aSelectionFromTo);
							getTimelineList(calcuSliderTimeSeries(aSelectionFromTo));
						} else {
							scope.selectTime = sliderTimeSeriesOption["selectedTime"];
							initTime(scope.selectTime);
							initTimeSlider( sliderTimeSeriesOption["selectionTimeSeries"], sliderTimeSeriesOption["timeSeries"], sliderTimeSeriesOption["selectedTime"] );
							getTimelineList(sliderTimeSeriesOption["timeSeries"]);
						}
					}
					function initTime( time ) {
						scope.targetPicker = moment( time ).format( "YYYY-MM-DD HH:mm:ss" );
					}
					function initTimeSlider( aSelectionFromTo, aFromTo, selectedTime ) {
						if ( timeSlider !== null ) {
							if ( aFromTo && selectedTime ) {
								timeSlider.resetTimeSeriesAndSelectionZone(aSelectionFromTo, aFromTo, selectedTime);
							} else {
								timeSlider.resetTimeSeriesAndSelectionZone(aSelectionFromTo, calcuSliderTimeSeries( aSelectionFromTo ) );
							}
						} else {
							timeSlider = new TimeSlider( "timeSlider-for-application-statistic", {
								"width": $("#timeSlider-for-application-statistic").get(0).getBoundingClientRect().width,
								"height": 90,
								"handleSrc": "images/handle.png",
								"timeSeries": aFromTo || calcuSliderTimeSeries( aSelectionFromTo ),
								"handleTimeSeries": aSelectionFromTo,
								"selectTime": selectedTime || aSelectionFromTo[1],
								"timelineData": {}
							}).addEvent("clickEvent", function( aEvent ) {
							}).addEvent("selectTime", function( time ) {
								scope.selectTime = time;
								initTime( time );
								sendUpTimeSliderTimeInfo( timeSlider.getSliderTimeSeries(), timeSlider.getSelectionTimeSeries(), time );
								scope.$apply();
							}).addEvent("changeSelectionZone", function( aTime ) {
								loadStatChart( aTime[0], aTime[1] );
								sendUpTimeSliderTimeInfo( timeSlider.getSliderTimeSeries(), aTime, timeSlider.getSelectTime() );
							}).addEvent("changeSliderTimeSeries", function( aEvents ) {});
						}
					}
					function sendUpTimeSliderTimeInfo( sliderTimeSeries, sliderSelectionTimeSeries, sliderSelectedTime ) {
						scope.$emit("up.changed.timeSliderOption", sliderTimeSeries, sliderSelectionTimeSeries, sliderSelectedTime );
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
					function getTimelineList( aFromTo ) {
						timeSlider.addData({
							"agentStatusTimeline":{
								"timelineSegments":[
									{"startTimestamp":aFromTo[0],"endTimestamp":aFromTo[1],"value":"EMPTY"}
								],
								"includeWarning":false
							},
							"agentEventTimeline":{"timelineSegments":[]}
						});
						sendUpTimeSliderTimeInfo( timeSlider.getSliderTimeSeries(), timeSlider.getSelectionTimeSeries(), scope.selectTime );
					}
					scope.selectDataSource = function(event) {
						var target = event.target;
						while ( target.nodeName.toUpperCase() !== "TR" ) {
							target = target.parentNode;
							if ( target.nodeName.toUpperCase() === "BODY" ) return;
						}
						scope.dataSourceSelectedIndex = parseInt(target.getAttribute("data-source"));
						broadcastToDataSource(scope.dataSourceData[scope.dataSourceSelectedIndex].charts.x, scope.dataSourceData[scope.dataSourceSelectedIndex].charts.y["ACTIVE_CONNECTION_SIZE"]);
					};
					scope.movePrev2 = function() {
						timeSlider.movePrev();
						getTimelineList( timeSlider.getSliderTimeSeries() );
					};
					scope.moveNext2 = function() {
						timeSlider.moveNext();
						getTimelineList( timeSlider.getSliderTimeSeries() );
					};
					scope.moveHead2 = function() {
						timeSlider.moveHead();
						getTimelineList( timeSlider.getSliderTimeSeries() );
					};
					scope.zoomInTimeSlider2 = function() {
						timeSlider.zoomIn();
						getTimelineList( timeSlider.getSliderTimeSeries() );
					};
					scope.zoomOutTimeSlider2 = function() {
						timeSlider.zoomOut();
						getTimelineList( timeSlider.getSliderTimeSeries() );
					};
					scope.$on("statisticChartDirective.cursorChanged", function (e, event, namespace) {
						if ( typeof event.index === "undefined" ) {
							timeSlider.hideFocus();
						} else {
							timeSlider.showFocus( moment(event.target.chart.dataProvider[event.index].time).valueOf() );
						}
						scope.$broadcast("statisticChartDirective.showCursorAt", event["index"], namespace);
					});
					scope.emptyDataSource = function() {
						return bEmptyDataSource;
					};
					initTooltip();
				}
			};
		}
	]);
})(jQuery);