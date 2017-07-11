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

					scope.$on( "down.select.application", function () {
						initTimeSliderUI();
						loadStatChart( UrlVoService.getQueryStartTime(), UrlVoService.getQueryEndTime() );
					});
					scope.$on( "down.changed.application", function () {
						initTimeSliderUI();
						loadStatChart( UrlVoService.getQueryStartTime(), UrlVoService.getQueryEndTime() );
					});
					scope.$on( "down.changed.period", function () {
						initTimeSliderUI();
						loadStatChart( UrlVoService.getQueryStartTime(), UrlVoService.getQueryEndTime() );
					});

					function loadStatChart(from, to) {
						var oParam = {
							to : to,
							from : from,
							sampleRate: 1,
							applicationId: UrlVoService.getApplicationName()
						};
						if ( oParam.from > 0 && oParam.to > 0 ) {
							$http.get( "getApplicationStat/cpuLoad/chart.pinpoint" +  getQueryStr(oParam) ).then(function(chartData) {
								if ( angular.isUndefined(chartData.data.exception) ) {
									scope.$broadcast("statisticChartDirective.initAndRenderWithData.jvm", makeChartData({
										id: "jvmCpuLoad",
										title: "JVM Cpu Usage",
										isAvailable: false,
										maximum: true
									}, "Cpu Usage (%)", ["JVM(avg)", "JVM(max)", "JVM(min)"], chartData.data.charts["CPU_LOAD_JVM"]), "100%", "270px");
									scope.$broadcast("statisticChartDirective.initAndRenderWithData.system", makeChartData({
										id: "systemCpuLoad",
										title: "System Cpu Usage",
										isAvailable: false,
										maximum: true
									}, "Cpu Usage (%)", ["System(avg)", "System(max)", "System(min)"], chartData.data.charts["CPU_LOAD_SYSTEM"]), "100%", "270px");
								} else {
									console.log("error");
								}
							}, function(error) {
							});
							$http.get( "getApplicationStat/memory/chart.pinpoint" +  getQueryStr(oParam) ).then(function(chartData) {
								if ( angular.isUndefined(chartData.data.exception) ) {
									scope.$broadcast("statisticChartDirective.initAndRenderWithData.heap", makeChartData({
										id: "memoryHeapLoad",
										title: "Memory Heap",
										isAvailable: false,
										maximum: false
									}, "Memory(bytes)", ["JVM(avg)", "JVM(max)", "JVM(min)"], chartData.data.charts["MEMORY_HEAP"]), "100%", "270px");
									scope.$broadcast("statisticChartDirective.initAndRenderWithData.non-heap", makeChartData({
										id: "memoryNonHeapLoad",
										title: "Memory Non Heap",
										isAvailable: false,
										maximum: false
									}, "Memory(bytes)", ["System(avg)", "System(max)", "System(min)"], chartData.data.charts["MEMORY_NON_HEAP"]), "100%", "270px");
								} else {
									console.log("error");
								}
							}, function(error) {
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
					function getQueryStr( o ) {
						return "?" + Object.keys(o).map(function(v) {
								return v + "=" + o[v];
							}).join("&");
					}

					scope.selectTime = -1;
					var timeSlider = null;
					function initTimeSliderUI() {
						var aSelectionFromTo = [];
						aSelectionFromTo[0] = UrlVoService.getQueryStartTime();
						aSelectionFromTo[1] = UrlVoService.getQueryEndTime();
						if ( scope.selectTime === -1 ) {
							scope.selectTime = UrlVoService.getQueryEndTime();
						}
						initTime( scope.selectTime );
						initTimeSlider( aSelectionFromTo );
						getTimelineList( calcuSliderTimeSeries( aSelectionFromTo ) );
					}
					function initTime( time ) {
						scope.targetPicker = moment( time ).format( "YYYY-MM-DD HH:mm:ss" );
					}
					function initTimeSlider( aSelectionFromTo ) {
						if ( timeSlider !== null ) {
							timeSlider.resetTimeSeriesAndSelectionZone( aSelectionFromTo, calcuSliderTimeSeries( aSelectionFromTo ) );
						} else {
							timeSlider = new TimeSlider( "timeSlider-for-application-statistic", {
								"width": $("#timeSlider-for-application-statistic").get(0).getBoundingClientRect().width,
								"height": 90,
								"handleSrc": "images/handle.png",
								"timeSeries": calcuSliderTimeSeries( aSelectionFromTo ),
								"handleTimeSeries": aSelectionFromTo,
								"selectTime": aSelectionFromTo[1],
								"timelineData": {}
							}).addEvent("clickEvent", function( aEvent ) {
							}).addEvent("selectTime", function( time ) {
								scope.selectTime = time;
								initTime( time );
							}).addEvent("changeSelectionZone", function( aTime ) {
								loadStatChart( aTime[0], aTime[1] );
							}).addEvent("changeSliderTimeSeries", function( aEvents ) {});
						}
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
				}
			};
		}
	]);
})(jQuery);