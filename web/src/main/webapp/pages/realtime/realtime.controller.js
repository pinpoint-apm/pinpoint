// @WILL_REMOVE_SOMEDAY
(function() {
	'use strict';
	pinpointApp.constant('RealtimeCtrlConfig', {
		sendPrefix: "applicationName=",
		keys: {
			CODE: "code",
			TYPE: "type",
			RESULT: "result",
			STATUS: "status",
			COMMAND: "command",
			MESSAGE: "message",
			TIME_STAMP: "timeStamp",
			PARAMETERS: "parameters",
			APPLICATION_NAME: "applicationName",
			ACTIVE_THREAD_COUNTS: "activeThreadCounts"
		},
		values: {
			PING: "PING",
			PONG: "PONG",
			REQUEST: "REQUEST",
			RESPONSE: "RESPONSE",
			ACTIVE_THREAD_COUNT: "activeThreadCount"
		},
		template: {
			agentChart: '<div class="agent-chart"><div></div><span class="glyphicon glyphicon-new-window"></span></div>',
			chartDirective: Handlebars.compile( '<realtime-chart-directive timeout-max-count="{{timeoutMaxCount}}" chart-color="{{chartColor}}" xcount="{{xAxisCount}}" show-extra-info="{{showExtraInfo}}" request-label="requestLabelNames" namespace="{{namespace}}" width="{{width}}" height="{{height}}"></realtime-chart-directive>' )
		},
		css : {
			borderWidth: 2,
			height: 180,
			navBarHeight: 42,
			titleHeight: 30
		},
		otherChart: {
			width: 120,
			height: 60
		},
		const: {
			MIN_Y: 10
		}
	});
	pinpointApp.controller( "RealtimeCtrl", [ "RealtimeCtrlConfig", "$scope", "$rootScope", "$routeParams", "$compile", "$location", "$timeout", "$rootElement", "$window", "RealtimeWebsocketService", "LocalStorageManagerService", "SystemConfigurationService", "AnalyticsService", "PreferenceService",
		function (cfg, $scope, $rootScope, $routeParams, $compile, $location, $timeout, $rootElement, $window, webSocketService, LocalStorageManagerService, SystemConfigService, AnalyticsService, PreferenceService) {
			AnalyticsService.send(AnalyticsService.CONST.REALTIME);

			$rootScope.wrapperStyle = {
				'padding-top': '0px'
			};
			var TIMEOUT_MAX_COUNT = 10;
			var X_AXIS_COUNT = 10;
			var RECEIVE_SUCCESS = 0;
			var pageSize = webSocketService.getPagingSize();

			$scope.applicationName = $routeParams.application.split("@")[0];
			$scope.serviceType = $routeParams.application.split("@")[1];
			$scope.currentPage = parseInt( $routeParams.page, 10 );
			$scope.serverTotalCount = "-";
			$scope.from = ($scope.currentPage - 1) * pageSize + 1;
			$scope.to = $scope.currentPage * pageSize;

			var $elAgentChartListWrapper, $elWarningMessage;
			var oNamespaceToIndexMap = {};
			var aAgentChartElementList = [];
			// var aChildScopeList = [];
			var aSumChartData = [0];
			var timeoutResult = null;
			var wsPongTemplate = (function() {
				var o = {};
				o[cfg.keys.TYPE] = cfg.values.PONG;
				return JSON.stringify(o);
			})();
			var wsMessageTemplate = (function() {
				var o = {};
				o[cfg.keys.TYPE] = cfg.values.REQUEST;
				o[cfg.keys.COMMAND] = cfg.values.ACTIVE_THREAD_COUNT;
				o[cfg.keys.PARAMETERS] = {};
				return o;
			})();
			$scope.agentChartColor 	= ["rgba(44, 160, 44, .8)", "rgba(60, 129, 250, .8)", 	"rgba(248, 199, 49, .8)", 	"rgba(246, 145, 36, .8)"];
			$scope.requestLabelNames= [ "1s", "3s", "5s", "Slow"];
			$(document).on("visibilitychange", function() {
				switch ( document.visibilityState ) {
					case "hidden":
						timeoutResult = $timeout(function() {
							webSocketService.close();
							timeoutResult = null;
						}, 60000);
						break;
					case "visible":
						if ( timeoutResult !== null ) {
							$timeout.cancel( timeoutResult );
						} else {
							$scope.retryConnection();
						}
						timeoutResult = null;
						break;
				}
			});

			function initElements() {
				$elAgentChartListWrapper = $rootElement.find("div.agent-chart-list");
				$elWarningMessage = $rootElement.find(".connection-message");
				$elWarningMessage.hide();
			}
			function startConnection() {
				if ( SystemConfigService.get("showActiveThread") === false ) return;

				initNamespaceToIndexMap();
				initChartDirective();
				showWaitingConnectionPopup();
				initReceive();
			}
			function initNamespaceToIndexMap() {
				oNamespaceToIndexMap = {};
			}
			function initChartDirective() {
			}
			function hasAgentChart( agentName ) {
				return angular.isDefined( oNamespaceToIndexMap[agentName] );
			}
			function showDisconnectedConnectionPopup() {
				$elWarningMessage.css("background-color", "rgba(200, 200, 200, 0.9)");
				$elWarningMessage.find("h4").css("color", "red").html("Closed connection.<br/><br/>Select node again.");
				$elWarningMessage.find("button").show();
				$elWarningMessage.show();
			}
			function showWaitingConnectionPopup() {
				$elWarningMessage.css("background-color", "rgba(138, 171, 136, 0.5)");
				$elWarningMessage.find("h4").css("color", "blue").html("Waiting Connection...");
				$elWarningMessage.find("button").hide();
				$elWarningMessage.show();
			}
			function initReceive() {
				if ( webSocketService.isOpened() === false ) {
					initSend();
				} else {
					startReceive();
				}
			}
			function initSend() {
				var bConnected = webSocketService.open({
					onopen: function(event) {
						startReceive();
					},
					onmessage: function(data) {
						receive( data );
					},
					onclose: function(event) {
						$scope.$apply(function() {
							showDisconnectedConnectionPopup();
						});
					},
					ondelay: function() {
						webSocketService.close();
					},
					retry: function() {
						$scope.retryConnection();
					}
				});
			}
			function startReceive() {
				webSocketService.send( makeRequest() );
			}
			function makeRequest() {
				wsMessageTemplate[cfg.keys.PARAMETERS][cfg.keys.APPLICATION_NAME] = $scope.applicationName;
				return JSON.stringify(wsMessageTemplate);
			}
			function receive( data ) {
				$elWarningMessage.hide();
				switch( data[cfg.keys.TYPE] ) {
					case cfg.values.PING:
						webSocketService.send( wsPongTemplate );
						break;
					case cfg.values.RESPONSE:
						var responseData = data[cfg.keys.RESULT];
						if ( responseData[cfg.keys.APPLICATION_NAME] !== $scope.applicationName ) return;

						var applicationData = responseData[cfg.keys.ACTIVE_THREAD_COUNTS];
						var aRequestSum = getSumOfRequestType( applicationData );
						setServerTotalCount( Object.keys(applicationData).length );
						addSumYValue( aRequestSum );

						broadcastData( applicationData, aRequestSum, responseData[cfg.keys.TIME_STAMP] );
						break;
				}
			}
			function getSumOfRequestType( datum ) {
				var aRequestSum = [0, 0, 0, 0];
				for( var p in datum ) {
					if ( datum[p][cfg.keys.CODE] === RECEIVE_SUCCESS ) {
						jQuery.each(datum[p][cfg.keys.STATUS], function( i, v ) {
							aRequestSum[i] += v;
						});
					}
				}
				return aRequestSum;
			}
			function addSumYValue( data ) {
				aSumChartData.push( data.reduce(function(pre, cur) {
					return pre + cur;
				}));
				if ( aSumChartData.length > X_AXIS_COUNT ) {
					aSumChartData.shift();
				}
			}
			function setServerTotalCount(count) {
				$timeout(function() {
					$scope.serverTotalCount = count;
					if ($scope.to > $scope.serverTotalCount) {
						$scope.to = $scope.serverTotalCount;
					}
				});
			}
			function broadcastData( applicationData, aRequestSum, timeStamp ) {
				var maxY = Math.max( getMaxOfYValue(), cfg.const.MIN_Y);
				var agentIndexAndCount = 0;
				var bAllError = true;

				for( var agentName in applicationData ) {
					if ( agentIndexAndCount + 1 >= $scope.from && agentIndexAndCount < $scope.to ) {
						checkAgentChart(agentName, agentIndexAndCount);

						if (applicationData[agentName][cfg.keys.CODE] === RECEIVE_SUCCESS) {
							bAllError = false;
							$scope.$broadcast('realtimeChartDirective.onData.' + oNamespaceToIndexMap[agentName], applicationData[agentName][cfg.keys.STATUS], timeStamp, maxY, bAllError);
						} else {
							$scope.$broadcast('realtimeChartDirective.onError.' + oNamespaceToIndexMap[agentName], applicationData[agentName], timeStamp, maxY);
						}

						showAgentChart(agentIndexAndCount);
					}
					agentIndexAndCount++;
				}
				checkNotUseAgentChart( agentIndexAndCount );
			}
			function getMaxOfYValue() {
				return d3.max( aSumChartData, function( d ) {
					return d;
				});
			}
			function checkAgentChart( agentName, agentIndexAndCount ) {
				if ( hasAgentChart( agentName ) === false ) {
					if ( hasNotUseChart( agentIndexAndCount ) ) {
						linkNamespaceToIndex(agentName, agentIndexAndCount);
					} else {
						addAgentChart(agentName, agentIndexAndCount);
					}
				}
				setAgentName( agentIndexAndCount, agentName );
			}
			function addAgentChart( agentName, agentIndexAndCount ) {
				var newChildScope = $scope.$new();

				var $newAgentChart = $( cfg.template.agentChart ).append( $compile( cfg.template.chartDirective({
					"width": cfg.otherChart.width,
					"height": cfg.otherChart.height,
					"namespace": aAgentChartElementList.length,
					"chartColor": "agentChartColor",
					"xAxisCount": X_AXIS_COUNT,
					"showExtraInfo": "false",
					"timeoutMaxCount": TIMEOUT_MAX_COUNT
				}))( newChildScope ));
				$elAgentChartListWrapper.append( $newAgentChart );

				linkNamespaceToIndex( agentName, aAgentChartElementList.length );
				aAgentChartElementList[agentIndexAndCount] = $newAgentChart;
			}
			function linkNamespaceToIndex( name, index ) {
				oNamespaceToIndexMap[name] = index;
			}
			function hasNotUseChart( index ) {
				return aAgentChartElementList.length > index;
			}
			function showAgentChart( index ) {
				aAgentChartElementList[index].show();
			}
			function checkNotUseAgentChart( count ) {
				for( var i = count ; i < aAgentChartElementList.length ; i++ ) {
					aAgentChartElementList[i].hide();
				}
			}
			function setAgentName( index, name ) {
				aAgentChartElementList[index].find("div").attr("data-name", name).html(name);
			}
			init();
			function init() {
				initElements();
				startConnection();
			}
			$scope.retryConnection = function() {
				showWaitingConnectionPopup();
				initReceive();
			};
			$scope.showAgentInfo = function( $event ) {
				if ( $( $event.target ).hasClass("page") ) {
					return;
				}
				if ( SystemConfigService.get("showActiveThreadDump") === true ) {
					var $target = $( $event.target );
					if ($target.hasClass("agent-chart-list")) {
						return;
					}
					var agentId = $target.parents(".agent-chart").find("div").attr("data-name");
					var openType = LocalStorageManagerService.getThreadDumpLayerOpenType();
					if (openType === null || openType === "window") {
						$window.open(
							getOpenUrl() +
							"/threadDump/" + $scope.applicationName + "@" + $scope.serviceType + "/" + agentId + "?" + Date.now().valueOf() ,
							"Thread Dump Info",
							"width=1280px,height=800px,menubar=no,toolbar=no,location=no,resizable=yes,scrollbars=no,status=no"
						);
					}
					AnalyticsService.send(AnalyticsService.CONST.MAIN, AnalyticsService.CONST.CLK_OPEN_THREAD_DUMP_LAYER);
				}
			};
			function getOpenUrl() {
				var url = $location.absUrl();
				var index = url.indexOf( $location.path() );
				return url.substring(0, index);
			}
		}
	]);
})();