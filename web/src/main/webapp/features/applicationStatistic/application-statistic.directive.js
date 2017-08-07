(function( $ ) {
	pinpointApp.constant( "applicationStatisticDirectiveConfig", {
		ID: "APPLICATION_STATISTIC_DRTV_"
	});

	pinpointApp.directive( "applicationStatisticDirective", [ "applicationStatisticDirectiveConfig", "$sce", "$http", "$timeout", "SystemConfigurationService", "CommonUtilService", "UrlVoService", "AlertsService", "ProgressBarService", "AgentDaoService", "AgentAjaxService", "TooltipService", "AnalyticsService", "helpContentService",
		function ( cfg, $sce, $http, $timeout, SystemConfigService, CommonUtilService, UrlVoService, AlertsService, ProgressBarService, AgentDaoService, AgentAjaxService, TooltipService, AnalyticsService, helpContentService ) {
			return {
				restrict: 'EA',
				replace: true,
				templateUrl: 'features/applicationStatistic/applicationStatistic.html?v' + G_BUILD_TIME,
				link: function postLink(scope, element, attrs) {
					cfg.ID += CommonUtilService.getRandomNum();

					scope.$on( "down.select.application", function (event, invokeId, sliderTimeSeriesOption) {
						initTimeSliderUI(sliderTimeSeriesOption);
						if ( sliderTimeSeriesOption === undefined || sliderTimeSeriesOption === null ) {
							loadStatChart(UrlVoService.getQueryStartTime(), UrlVoService.getQueryEndTime());
						} else {
							loadStatChart(sliderTimeSeriesOption["selectionTimeSeries"][0], sliderTimeSeriesOption["selectionTimeSeries"][1]);
						}
					});
					scope.$on( "down.changed.application", function () {
						initTimeSliderUI();
						loadStatChart( UrlVoService.getQueryStartTime(), UrlVoService.getQueryEndTime() );
					});
					scope.$on( "down.changed.period", function () {
						initTimeSliderUI();
						loadStatChart( UrlVoService.getQueryStartTime(), UrlVoService.getQueryEndTime() );
					});

					function initTooltip() {
						TooltipService.init( "statHeap" );
						TooltipService.init( "statPermGen" );
						TooltipService.init( "statJVMCpu" );
						TooltipService.init( "statSystemCpu" );
						TooltipService.init( "statTPS" );
					}
					function loadStatChart(from, to) {
						var oParam = {
							to : to,
							from : from,
							sampleRate: 1,
							applicationId: UrlVoService.getApplicationName()
						};
						if ( oParam.from > 0 && oParam.to > 0 ) {
							AgentAjaxService.getStatCpuLoad( oParam, function(chartData) {
								if ( angular.isUndefined(chartData.exception) ) {
									scope.$broadcast("statisticChartDirective.initAndRenderWithData.jvm", makeChartData({
										id: "jvmCpuLoad",
										title: "JVM Cpu Usage",
										isAvailable: false,
										maximum: true
									}, "Cpu Usage (%)", ["Avg", "Max", "Min"], chartData.charts["CPU_LOAD_JVM"]), "100%", "270px");
									scope.$broadcast("statisticChartDirective.initAndRenderWithData.system", makeChartData({
										id: "systemCpuLoad",
										title: "System Cpu Usage",
										isAvailable: false,
										maximum: true
									}, "Cpu Usage (%)", ["Avg", "Max", "Min"], chartData.charts["CPU_LOAD_SYSTEM"]), "100%", "270px");
								} else {
									console.log("error");
								}
							});
							AgentAjaxService.getStatMemory( oParam, function(chartData) {
								if ( angular.isUndefined(chartData.exception) ) {
									scope.$broadcast("statisticChartDirective.initAndRenderWithData.heap", makeChartData({
										id: "memoryHeapLoad",
										title: "Memory Heap",
										isAvailable: false,
										maximum: false
									}, "Memory(bytes)", ["Avg", "Max", "Min"], chartData.charts["MEMORY_HEAP"]), "100%", "270px");
									scope.$broadcast("statisticChartDirective.initAndRenderWithData.non-heap", makeChartData({
										id: "memoryNonHeapLoad",
										title: "Memory Non Heap",
										isAvailable: false,
										maximum: false
									}, "Memory(bytes)", ["Avg", "Max", "Min"], chartData.charts["MEMORY_NON_HEAP"]), "100%", "270px");
								} else {
									console.log("error");
								}
							});
							AgentAjaxService.getStatTPS( oParam, function(chartData) {
								if ( angular.isUndefined(chartData.exception) ) {
									scope.$broadcast("statisticChartDirective.initAndRenderWithData.tps", makeChartData({
										id: "transactionPerSecond",
										title: "Transaction Per Second",
										isAvailable: false,
										maximum: false
									}, "Transactions(count)", ["Avg", "Max", "Min"], chartData.charts["TRANSACTION_COUNT"]), "100%", "270px");
								} else {
									console.log("error");
								}
							});
						}
					}
					function makeChartData(chartProperty, chartTitle, legendTitles, chartData) {
						var returnData = {
							data: [],
							title: legendTitles,
							field: ["avg", "max", "min", "maxAgent", "minAgent"],
							chartTitle: chartTitle,
							maximum: chartProperty.maximum
						};
						if (chartData) {
							chartProperty.isAvailable = true;
						} else {
							return returnData;
						}
						var pointsData = chartData.points;
						var length = pointsData.length;
						for (var i = 0; i < length; ++i) {
							if ( pointsData[i]['yValForAvg'] === -1 || pointsData[i]['yValForMin'] === -1 || pointsData[i]['yValForMax'] === -1 ) {
								returnData.data.push({
									"time": moment(pointsData[i]['xVal']).format("YYYY-MM-DD HH:mm:ss")
								});
							} else {
								returnData.data.push({
									"time": moment(pointsData[i]['xVal']).format("YYYY-MM-DD HH:mm:ss"),
									"avg": pointsData[i]['yValForAvg'].toFixed(2),
									"min": pointsData[i]['yValForMin'].toFixed(2),
									"max": pointsData[i]['yValForMax'].toFixed(2),
									"minAgent": pointsData[i]['agentIdForMin'],
									"maxAgent": pointsData[i]['agentIdForMax']
								});
							}
						}
						return returnData;
					}

					scope.selectTime = -1;
					var timeSlider = null;
					function initTimeSliderUI( sliderTimeSeriesOption ) {
						if ( sliderTimeSeriesOption === undefined || sliderTimeSeriesOption === null ) {
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
					}
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
						scope.$broadcast("statisticChartDirective.showCursorAt", event["index"], namespace);
					});
					initTooltip();
				}
			};
		}
	]);
})(jQuery);