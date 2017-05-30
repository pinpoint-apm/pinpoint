(function() {
	'use strict';
	pinpointApp.constant( "StatisticCtrlConfig", {
		ID: "STAT_CTRL_",
		PAGE_NAME: "statistic",
		SLASH: "/"
	});
	pinpointApp.controller("StatisticCtrl", [ "StatisticCtrlConfig", "$scope", "$routeParams", "locationService", "$http", "CommonUtilService", "AgentDaoService", "UrlVoService",
		function ( cfg, $scope, $routeParams, locationService, $http, CommonUtilService, AgentDaoService, UrlVoService) {
			cfg.ID +=  CommonUtilService.getRandomNum();
			// AnalyticsService.send(AnalyticsService.CONST.INSPECTOR_PAGE);

			$scope.$on( "up.changed.application", function ( event, invokerId, newAppName ) {
				UrlVoService.setApplication( newAppName );
				changeLocation(function() {
					$scope.$broadcast( "down.changed.application", invokerId );
					loadStatChart();
				});
			});
			$scope.$on( "up.changed.period", function ( event, invokerId ) {
				changeLocation(function() {
					$scope.$broadcast( "down.changed.period", true, invokerId );
					loadStatChart();
				});

			});

			function loadStatChart() {
				var oParam = {
					to : UrlVoService.getQueryEndTime(),
					from : UrlVoService.getQueryStartTime(),
					sampleRate: 1,
					applicationId: UrlVoService.getApplicationName()
				};
				if ( oParam.from > 0 && oParam.to > 0 ) {
					$http.get( "getApplicationStat/cpuLoad/chart.pinpoint" +  getQueryStr(oParam) ).then(function(chartData) {
						if ( angular.isUndefined(chartData.data.exception) ) {
							$scope.$broadcast("statisticChartDirective.initAndRenderWithData.jvm", makeChartData({
								id: "jvmCpuLoad",
								title: "JVM Cpu Usage",
								isAvailable: false
							}, "Cpu Usage (%)", ["JVM(avg)", "JVM(max)", "JVM(min)"], chartData.data.charts["CPU_LOAD_JVM"]), "100%", "270px");
							$scope.$broadcast("statisticChartDirective.initAndRenderWithData.system", makeChartData({
								id: "systemCpuLoad",
								title: "System Cpu Usage",
								isAvailable: false
							}, "Cpu Usage (%)", ["System(avg)", "System(max)", "System(min)"], chartData.data.charts["CPU_LOAD_SYSTEM"]), "100%", "270px");
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
					chartTitle: chartTitle
				};
				if (chartData) {
					chartProperty.isAvailable = true;
				} else {
					return returnData;
				}
				var pointsData = chartData.points;
				var length = pointsData.length;
				for (var i = 0; i < length; ++i) {
					returnData.data.push({
						"time": moment(pointsData[i]['xVal']).format("YYYY-MM-DD HH:mm:ss"),
						"avg": pointsData[i]['yValForAvg'].toFixed(2),
						"min": pointsData[i]['yValForMin'].toFixed(2),
						"max": pointsData[i]['yValForMax'].toFixed(2),
						"minAgent": pointsData[i]['agentIdForMin'],
						"maxAgent": pointsData[i]['agentIdForMax']
					});
				}
				return returnData;
			}
			function changeLocation( callback ) {
				var newPath = getLocation();
				if ( locationService.path() !== newPath ) {
					locationService.skipReload().path( newPath ).replace();
					if (!$scope.$$phase) {
						$scope.$apply();
					}
					callback();
				}
			}
			function getLocation() {
				var url = [
					"",
					cfg.PAGE_NAME,
					UrlVoService.getApplication()
				].join( cfg.SLASH );

				if ( UrlVoService.getReadablePeriod() ) {
					url += cfg.SLASH + UrlVoService.getReadablePeriod() + cfg.SLASH + UrlVoService.getQueryEndDateTime();
				}
				return url;
			}
			function getQueryStr( o ) {
				return "?" + Object.keys(o).map(function(v) {
					return v + "=" + o[v];
				}).join("&");
			}
			$scope.$broadcast("down.initialize", cfg.ID );
			loadStatChart();
		}
	]);
})();