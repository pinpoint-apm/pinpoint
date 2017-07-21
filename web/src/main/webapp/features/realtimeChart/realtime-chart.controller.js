(function($) {
	'use strict';
	/**
	 * (en)RealtimeChartCtrl 
	 * @ko RealtimeChartCtrl
	 * @group Controller
	 * @name RealtimeChartCtrl
	 * @class
	 */
	pinpointApp.constant('RealtimeChartCtrlConfig', {
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
		sumChart: {
			width: 260,
			height: 120
		},
		otherChart: {
			width: 120,
			height: 60
		},
		const: {
			MIN_Y: 10
		}
	});
	
	pinpointApp.controller( "RealtimeChartCtrl", [ "RealtimeChartCtrlConfig", "$scope", "$element", "$location", "$rootScope", "$compile", "$timeout", "$window", "SystemConfigurationService", "LocalStorageManagerService", "UrlVoService", "RealtimeWebsocketService", "AnalyticsService", "TooltipService",
	    function (cfg, $scope, $element, $location, $rootScope, $compile, $timeout, $window, SystemConfigService, LocalStorageManagerService, UrlVoService, webSocketService, AnalyticsService, tooltipService) {
	    	$element = $($element);
			//@TODO will move to preference-service 
	    	var TIMEOUT_MAX_COUNT = 10;
			var X_AXIS_COUNT = 10;
	    	var RECEIVE_SUCCESS = 0;

			var $elSumChartWrapper, $elTitle, $elSumChartCount, $elAgentChartListWrapper, $elWarningMessage, $elPin;
	    	var preUrlParam = "";
			var currentApplicationName = "";
			var currentServiceType = "";
	    	var aAgentChartElementList = [];
			var aChildScopeList = [];
	    	var oNamespaceToIndexMap = {};
	    	var aSumChartData = [0];
			var bIsFirstInit = true;
	    	var bIsPinned = true;
	    	var bIsWas = false;
	    	var bShowRealtimeChart = true;
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
			var timeoutResult = null;
			tooltipService.init( "realtime" );

			$scope.maxPageSize = webSocketService.getPagingSize();
			$scope.pagingValue = [];
	    	$scope.sumChartColor 	= ["rgba(44, 160, 44, 1)", 	"rgba(60, 129, 250, 1)", 	"rgba(248, 199, 49, 1)", 	"rgba(246, 145, 36, 1)" ];
	    	$scope.agentChartColor 	= ["rgba(44, 160, 44, .8)", "rgba(60, 129, 250, .8)", 	"rgba(248, 199, 49, .8)", 	"rgba(246, 145, 36, .8)"];
	    	$scope.requestLabelNames= [ "1s", "3s", "5s", "Slow"];
	    	$scope.serverTotalCount = 0;
			$scope.showServerPaging = false;
	    	$scope.bInitialized = false;

			$(document).on("visibilitychange", function() {
				if ( UrlVoService.isRealtime() === false ) return;

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
			initElements();
			function initElements() {
				$elSumChartWrapper = $element.find("div.agent-sum-chart");
				$elTitle = $element.find("div.agent-sum-chart div:first-child span:first-child");
				$elSumChartCount = $element.find("div.agent-sum-chart div:first-child span:last-child");
				$elAgentChartListWrapper = $element.find("div.agent-chart-list");
				$elWarningMessage = $element.find(".connection-message");
				$elPin = $element.find(".glyphicon-pushpin");
				$elWarningMessage.hide();
				$elTitle.html("");
				$elSumChartCount.html("0");
			}

	    	function initChartDirective() {
	    		if ( hasAgentChart( "sum" ) === false ) {
	    			var newChildScope = $scope.$new();
		    		$elSumChartWrapper.append( $compile( cfg.template.chartDirective({
		    			"width": cfg.sumChart.width,
		    			"height": cfg.sumChart.height,
		    			"namespace": "sum",
		    			"chartColor": "sumChartColor",
		    			"xAxisCount": X_AXIS_COUNT,
		    			"showExtraInfo": "true",
		    			"timeoutMaxCount": TIMEOUT_MAX_COUNT
					}))( newChildScope ));
					aChildScopeList.push( newChildScope );
		    		oNamespaceToIndexMap["sum"] = -1;
	    		}
	    	}
	    	function initNamespaceToIndexMap() {
	    		if ( angular.isDefined( oNamespaceToIndexMap["sum"] ) ) {
	    			oNamespaceToIndexMap = {};
		    		oNamespaceToIndexMap["sum"] = -1;
	    		} else {
	    			oNamespaceToIndexMap = {};
	    		}
	    	}
	    	function hasAgentChart( agentName ) {
	    		return angular.isDefined( oNamespaceToIndexMap[agentName] );
	    	}
	    	function addAgentChart( agentName ) {
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
				aChildScopeList.push( $scope.$new() );
	    		$elAgentChartListWrapper.append( $newAgentChart );
	    		
	    		linkNamespaceToIndex( agentName, aAgentChartElementList.length );
	    		aAgentChartElementList.push( $newAgentChart );
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
	        	// if ( bConnected ) {
	        	// 	initChartDirective();
	        	// }
	        }
	        function receive( data ) {
				$elWarningMessage.hide();
	        	switch( data[cfg.keys.TYPE] ) {
	        		case cfg.values.PING:
	        			webSocketService.send( wsPongTemplate );
	        			break;
	        		case cfg.values.RESPONSE:
		        		var responseData = data[cfg.keys.RESULT];
						if ( responseData[cfg.keys.APPLICATION_NAME] !== currentApplicationName ) return;
			        	
			        	var applicationData = responseData[cfg.keys.ACTIVE_THREAD_COUNTS];
			        	var aRequestSum = getSumOfRequestType( applicationData );
						setServerTotalCount( Object.keys(applicationData).length );
			        	addSumYValue( aRequestSum );
			        	
			        	broadcastData( applicationData, aRequestSum, responseData[cfg.keys.TIME_STAMP] );

	        			break;
	        	}
	        }
	        function broadcastData( applicationData, aRequestSum, timeStamp ) {
	        	var maxY = Math.max( getMaxOfYValue(), cfg.const.MIN_Y);
	        	var agentIndexAndCount = 0;
	        	var bAllError = true;

	        	for( var agentName in applicationData ) {
	        		checkAgentChart( agentName, agentIndexAndCount );
	        		
	        		if ( applicationData[agentName][cfg.keys.CODE] === RECEIVE_SUCCESS ) {
	        			bAllError = false;
	        			$scope.$broadcast('realtimeChartDirective.onData.' + oNamespaceToIndexMap[agentName], applicationData[agentName][cfg.keys.STATUS], timeStamp, maxY, bAllError );
	        		} else {
	        			$scope.$broadcast('realtimeChartDirective.onError.' + oNamespaceToIndexMap[agentName], applicationData[agentName], timeStamp, maxY );
	        		}
	        		
	        		showAgentChart( agentIndexAndCount );
	        		agentIndexAndCount++;
	        		if ( agentIndexAndCount >= $scope.maxPageSize ) {
	        			break;
					}
	        	}
	        	checkNotUseAgentChart( agentIndexAndCount );
        		$scope.$broadcast('realtimeChartDirective.onData.sum', aRequestSum, timeStamp, maxY, bAllError );
				$elSumChartCount.html(agentIndexAndCount);
	        }
	        function makeRequest( applicationName ) {
	        	wsMessageTemplate[cfg.keys.PARAMETERS][cfg.keys.APPLICATION_NAME] = applicationName;
	        	return JSON.stringify(wsMessageTemplate);
	        }
	        function checkAgentChart( agentName, agentIndexAndCount ) {
	        	if ( hasAgentChart( agentName ) === false ) {
        			if ( hasNotUseChart( agentIndexAndCount ) ) {
        				linkNamespaceToIndex(agentName, agentIndexAndCount);
        			} else {
	        			addAgentChart(agentName);
	        		}
        		}
        		setAgentName( agentIndexAndCount, agentName );
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
	        function getMaxOfYValue() {
    	        return d3.max( aSumChartData, function( d ) {
	                return d;
	            });
    	    }
	        function startReceive() {
	        	webSocketService.send( makeRequest( currentApplicationName ) );
	        }
	        function initReceive() {
	        	if ( webSocketService.isOpened() === false ) {
	        		initSend();
	        	} else {
	        		startReceive();
	        	}
        		bShowRealtimeChart = true;
	        }
	        function stopReceive() {
	        	bShowRealtimeChart = false;
        		webSocketService.stopReceive( makeRequest("") );
	        }
	        function stopChart() {
	        	$rootScope.$broadcast("realtimeChartDirective.clear.sum");
	        	$.each( aAgentChartElementList, function(index, el) {
	        		$rootScope.$broadcast("realtimeChartDirective.clear." + index);
	        		el.hide();
	        	});
				$.each( aChildScopeList, function(index, childScope) {
					childScope.$destroy();
				});
				aChildScopeList.length = 0;
				$timeout(function() {
					$elSumChartWrapper.find("svg").remove();
					$.each( aAgentChartElementList, function( index, el ) {
						el.remove();
					});
					aAgentChartElementList.length = 0;
				});
				oNamespaceToIndexMap = {};
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
	        function hidePopup() {
				hideSub();
	        	$element.css("top", "initial").animate({
					left: 0,
	        		bottom: -parseInt(LocalStorageManagerService.getRealtimeLayerHeight() || cfg.css.height)
	        	}, 500, function() {
	        	});
	        	setServerTotalCount(0);
	        }
	        function showPopup() {
				var savedHeight = LocalStorageManagerService.getRealtimeLayerHeight() || cfg.css.height;
				$element.css("height", savedHeight);
	        	$element.animate({
	        		bottom: 0,
	        		left: 0
	        	}, 500, function() {
					$elAgentChartListWrapper.css("height", savedHeight - cfg.css.titleHeight );
	        	});
	        }
	        function adjustWidth() {
	        	$element.css("top", "initial").innerWidth( $element.parent().width() - cfg.css.borderWidth + "px" );
	        }
	        function setPinColor() {
	        	$elPin.css("color", bIsPinned ? "red": "");
	        }
	        function arrayIndexFill( length ) {
				var arr = [];
				for( var i = 2 ; i <= length ; i++ ) {
					arr.push(i);
				}
				return arr;
			}
	        function setServerTotalCount(count) {
				$scope.serverTotalCount = count;
				if ( count <= $scope.maxPageSize  ) {
					$scope.showServerPaging = false;
					$scope.pagingValue = [];
				} else {
					$scope.showServerPaging = true;
					var pageSize = count / $scope.maxPageSize  + ( count % $scope.maxPageSize  > 0 ? 1 : 0 );
					$scope.pagingValue = arrayIndexFill(pageSize);
				}
			}
			$scope.openRATChart = function( page ) {
				$window.open(
					getOpenUrl() +
					"/realtime/" + currentApplicationName + "@" + currentServiceType + "/" + page,
					"RealTime Active Thread Chart Paging View [" + page + "]",
					"width=1280px,height=800px,menubar=no,toolbar=no,location=no,resizable=yes,scrollbars=no,status=no"
				);
			};
	        $scope.$on( "realtimeChartController.close", function () {
	        	hidePopup();
	        	var prevShowRealtimeChart = bShowRealtimeChart;
	        	resetStatus();
	        	bShowRealtimeChart = prevShowRealtimeChart;
	        	setPinColor();
	        });
	        $scope.$on( "realtimeChartController.initialize", function (event, was, applicationName, serviceType, urlParam ) {
	        	hideSub();
	        	if ( bIsPinned === true && preUrlParam === urlParam ) return;
	        	if ( UrlVoService.isRealtime() === false ) return;
	        	bIsWas = angular.isUndefined( was ) ? false : was;
	        	applicationName = angular.isUndefined( applicationName ) ? "" : applicationName;
	        	serviceType = angular.isUndefined( serviceType ) ? "" : serviceType;

	        	preUrlParam = urlParam;

				if ( bIsFirstInit === true ) {
					initElements();
					bIsFirstInit = false;
				}
	        	if ( SystemConfigService.get("showActiveThread") === false ) return;
	        	if ( bShowRealtimeChart === false ) return;
	        	if ( bIsWas === false ) {
	        		hidePopup();
	        		return;
	        	}
	        	initNamespaceToIndexMap();
				initChartDirective();
	        	adjustWidth();
	        	$scope.bInitialized = true;

				// resetStatus();
				currentApplicationName = applicationName;
				currentServiceType = serviceType;
				$elTitle.html( currentApplicationName );
	        	showPopup();
        		showWaitingConnectionPopup();
        		
        		initReceive();
        		setPinColor();
	        });
	        $scope.retryConnection = function() {
	        	showWaitingConnectionPopup();
        		initReceive();
	        };
	        $scope.pin = function() {
	        	bIsPinned = !bIsPinned;
				AnalyticsService.send( AnalyticsService.CONST.MAIN, bIsPinned ? AnalyticsService.CONST.CLK_REALTIME_CHART_PIN_ON : AnalyticsService.CONST.CLK_REALTIME_CHART_PIN_OFF );
	        	setPinColor();
	        };
	        $scope.showAgentInfo = function( $event ) {
	        	if ( $( $event.target ).hasClass("paging") || $( $event.target ).parent().hasClass("paging") ) {
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
							"/threadDump/" + currentApplicationName + "@" + currentServiceType + "/" + agentId + "?" + Date.now().valueOf() ,
							"Thread Dump Info",
							"width=1280px,height=800px,menubar=no,toolbar=no,location=no,resizable=yes,scrollbars=no,status=no"
						);
					} else {
						$rootScope.$broadcast("thread-dump-info-layer.open", currentApplicationName, agentId);
					}
					AnalyticsService.send(AnalyticsService.CONST.MAIN, AnalyticsService.CONST.CLK_OPEN_THREAD_DUMP_LAYER);
				}
			};
	        function getOpenUrl() {
	        	var url = $location.absUrl();
	        	var index = url.indexOf( $location.path() );
	        	return url.substring(0, index);
			}
			function hideSub() {
				$rootScope.$broadcast( "thread-dump-info-layer.close" );
			}
	        function resetStatus() {
	        	stopReceive();
	        	stopChart();
				$elWarningMessage.hide();
				currentApplicationName = "";
				currentServiceType = "";
				$elTitle.html( currentApplicationName );
				$elSumChartCount.html("0");
	        }
	        $($window).on("resize", function() {
	        	adjustWidth();
	        	var newHeight = $window.innerHeight - cfg.css.navBarHeight;
				$element.resizable("option", "maxHeight", newHeight);
				if ( parseInt($element.css("height")) > newHeight && newHeight > cfg.css.height ) {
					$element.css("height", newHeight);
					$elAgentChartListWrapper.css("height", newHeight - cfg.css.titleHeight );
					LocalStorageManagerService.setRealtimeLayerHeight( newHeight );
				}
	        });
			$element.resizable({
	        	minHeight: cfg.css.height,
				maxHeight: $window.innerHeight - cfg.css.navBarHeight,
				handles: "n",
				resize: function( event, ui ) {
					$elAgentChartListWrapper.css("height", ui.size.height - cfg.css.titleHeight );
					LocalStorageManagerService.setRealtimeLayerHeight( ui.size.height );
				}
			});
		}
	]);
})(jQuery);