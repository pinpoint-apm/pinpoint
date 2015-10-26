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
			STATUS: "status",
			MESSAGE: "message",
			TIME_STAMP: "timeStamp",
			APPLICATION_NAME: "applicationName",
			ACTIVE_THREAD_COUNTS: "activeThreadCounts"
		},
		template: {
			agentChart: '<div class="agent-chart"><div></div></div>',
			chartDirective: Handlebars.compile( '<realtime-chart-directive chart-color="{{chartColor}}" xcount="{{xAxisCount}}" show-extra-info="{{showExtraInfo}}" request-label="requestLabelNames" namespace="{{namespace}}" width="{{width}}" height="{{height}}"></realtime-chart-directive>' )
		},
		css : {
			borderWidth: 4,
			height: 180,
			navbarHeight: 70,
			titleHeight: 30
		},
		sumChart: {
			width: 260,
			height: 120
		},
		otherChart: {
			width: 120,
			height: 60
		}
	});
	
	pinpointApp.controller('RealtimeChartCtrl', ['RealtimeChartCtrlConfig', '$scope', '$element', '$rootScope', '$compile', '$window', 'globalConfig', 'RealtimeWebsocketService', '$location',
	    function (cfg, $scope, $element, $rootScope, $compile, $window, globalConfig, websocketService, $location) {
			
	    	$element = $($element);
			//@TODO will move to preference-service 
			var X_AXIS_COUNT = 10;
	    	var RECEIVE_SUCCESS = 0;
	    	
			var $elSumChartWrapper = $element.find("div.agent-sum-chart");
	    	var $elAgentChartListWrapper = $element.find("div.agent-chart-list");
	    	var $elWarningMessage = $element.find(".connection-message");
	    	var $elHandleGlyphicon = $element.find(".handle .glyphicon");
	    	var aAgentChartElementList = [];
	    	var oNamespaceToIndexMap = {};
	    	var aSumChartData = [0];
	    	var bIsWas = false;
	    	var bIsFullWindow = false;
	    	var bShowRealtimeChart = true;
	    	var popupHeight = cfg.css.height;
	    	
	    	$scope.hasCriticalError = false;
	    	$scope.sumChartColor 	= ["rgba(44, 160, 44, 1)", 	"rgba(60, 129, 250, 1)", 	"rgba(248, 199, 49, 1)", 	"rgba(246, 145, 36, 1)" ];
	    	$scope.agentChartColor 	= ["rgba(44, 160, 44, .8)", "rgba(60, 129, 250, .8)", 	"rgba(248, 199, 49, .8)", 	"rgba(246, 145, 36, .8)"];
	    	$scope.requestLabelNames= [ "Fast", "Normal", "Slow", "Very Slow"];
	    	$scope.currentAgentCount = 0;
	    	$scope.currentApplicationName = "";
	    	$scope.bInitialized = false;
	    	
	    	function getInitChartData( len ) {
    	    	var a = [];
    	        for( var i = 0 ; i < $scope.sumChartColor.length ; i++ ) {
    	            a.push( d3.range(len).map(function() { return 0; }) );
    	        }
    	        return a;
    	    }
	    	function initChartDirective() {
	    		if ( hasAgentChart( "sum" ) === false ) {
		    		$elSumChartWrapper.append( $compile( cfg.template.chartDirective({
		    			"chartColor": "sumChartColor",
		    			"xAxisCount": X_AXIS_COUNT,
		    			"namespace": "sum",
		    			"showExtraInfo": "true",
		    			"height": cfg.sumChart.height,
		    			"width": cfg.sumChart.width
		    		}))($scope) );
		    		oNamespaceToIndexMap["sum"] = -1;
	    		}
	    	}
	    	function hasAgentChart( agentName ) {
	    		return angular.isDefined( oNamespaceToIndexMap[agentName] );
	    	}
	    	function addAgentChart( agentName ) {
	    		var $newAgentChart = $( cfg.template.agentChart ).append( $compile( cfg.template.chartDirective({
	    			"chartColor": "agentChartColor",
	    			"xAxisCount": X_AXIS_COUNT,
	    			"namespace": aAgentChartElementList.length,
	    			"showExtraInfo": "false",
	    			"height": cfg.otherChart.height,
	    			"width": cfg.otherChart.width 
	    		}))($scope) );
	    		$elAgentChartListWrapper.append( $newAgentChart );
	    		
	    		linkNamespaceToIndex( agentName, aAgentChartElementList.length );
	    		aAgentChartElementList.push( $newAgentChart );
	    	}
	        function initSend() {
	        	var bConnected = websocketService.open({
	        		onopen: function(event) {
	        			startReceive();
	        		},
	        		onmessage: function(data) {
		            	receive( data );
	        		},
	        		onclose: function(event) {
	        			$scope.$apply(function() {
	        				disconnectedConnection();
		            	});
	        		},
	        		ondelay: function() {
	        			websocketService.close();
	        		}
	        	});
	        	if ( bConnected ) {
	        		initChartDirective();
	        	}
	        }
	        function receive( data ) {
	        	$scope.hasCriticalError = false;
	        	if ( data[cfg.keys.APPLICATION_NAME] !== $scope.currentApplicationName ) return;
	        	
	        	var applicationData = data[cfg.keys.ACTIVE_THREAD_COUNTS];
	        	var aRequestSum = getSumOfRequestType( applicationData );
	        	addSumYValue( aRequestSum );
	        	
	        	broadcastData( applicationData, aRequestSum, data[cfg.keys.TIME_STAMP] );
	        }
	        function broadcastData( applicationData, aRequestSum, timeStamp ) {
	        	var maxY = getMaxOfYValue();
	        	var agentIndexAndCount = 0;
	        	var bAllError = true;
	        	
	        	for( var agentName in applicationData ) {
	        		checkAgentChart( agentName, agentIndexAndCount );
	        		
	        		if ( applicationData[agentName][cfg.keys.CODE] === RECEIVE_SUCCESS ) {
	        			bAllError = false;
	        			$rootScope.$broadcast('realtimeChartDirective.onData.' + oNamespaceToIndexMap[agentName], applicationData[agentName][cfg.keys.STATUS], timeStamp, maxY, bAllError );
	        		} else {
	        			$rootScope.$broadcast('realtimeChartDirective.onError.' + oNamespaceToIndexMap[agentName], applicationData[agentName][cfg.keys.MESSAGE], timeStamp, maxY );
	        		}
	        		
	        		showAgentChart( agentIndexAndCount );
	        		agentIndexAndCount++;
	        	}
        		$rootScope.$broadcast('realtimeChartDirective.onData.sum', aRequestSum, timeStamp, maxY, bAllError );
	        	
        		$scope.$apply(function() {
	        		$scope.currentAgentCount = agentIndexAndCount;
	        	});
	        }
	        function checkAgentChart( agentName, agentIndexAndCount ) {
	        	if ( hasAgentChart( agentName ) == false ) {
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
	        function setAgentName( index, name ) {
	        	aAgentChartElementList[index].find("div").html(name);
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
	        	if ( aSumChartData.legnth > X_AXIS_COUNT ) {
	        		aSumChartData.shift();
	        	}
	        }
	        function getMaxOfYValue() {
    	        return d3.max( aSumChartData, function( d ) {
	                return d;
	            });
    	    }
	        function startReceive() {
	        	websocketService.send(cfg.sendPrefix + $scope.currentApplicationName);
	        }
	        function initReceive() {
	        	if ( websocketService.isOpened() == false ) {
	        		initSend();
	        	} else {
	        		startReceive();
	        	}
        		bShowRealtimeChart = true;
	        }
	        function stopReceive() {
	        	bShowRealtimeChart = false;
        		websocketService.stopReceive(cfg.sendPrefix);
	        }
	        function stopChart() {
	        	$rootScope.$broadcast('realtimeChartDirective.clear.sum');
	        	$.each( aAgentChartElementList, function(index, el) {
	        		$rootScope.$broadcast('realtimeChartDirective.clear.' + index);
	        		el.hide();
	        	});
	        }
	        function disconnectedConnection() {
	        	$elWarningMessage.css("background-color", "rgba(200, 200, 200, 0.9)");
	        	$elWarningMessage.find("h4").css("color", "red").html("Closed connection.<br/><br/>Select node again.");
	        	$elWarningMessage.find("button").show();
	        	$scope.hasCriticalError = true;
	        }
	        function waitingConnection() {
	        	$elWarningMessage.css("background-color", "rgba(138, 171, 136, 0.5)");
	        	$elWarningMessage.find("h4").css("color", "blue").html("Waiting Connection...");
	        	$elWarningMessage.find("button").hide();
	        	$scope.hasCriticalError = true;
	        }
	        function hidePopup() {
	        	$element.animate({
	        		bottom: -popupHeight,
	        		left: 0
	        	}, 500, function() {
	        		$elHandleGlyphicon.removeClass("glyphicon-chevron-down").addClass("glyphicon-chevron-up");
	        	});
	        }
	        function showPopup() {
	        	$element.animate({
	        		bottom: 0,
	        		left: 0
	        	}, 500, function() {
	        		$elHandleGlyphicon.removeClass("glyphicon-chevron-up").addClass("glyphicon-chevron-down");
	        	});
	        }
	        function adjustWidth() {
	        	$element.innerWidth( $element.parent().width() - cfg.css.borderWidth + "px" );
	        }
	        $scope.$on('realtimeChartController.close', function () {
	        	hidePopup();
	        	var prevShowRealtimeChart = bShowRealtimeChart;
	        	$scope.closePopup();
	        	bShowRealtimeChart = prevShowRealtimeChart;
	        });
	        $scope.$on('realtimeChartController.initialize', function (event, was, applicationName) {
	        	if ( /^\/main/.test( $location.path() ) == false ) return;
	        	bIsWas = angular.isUndefined( was ) ? false : was;
	        	applicationName = angular.isUndefined( applicationName ) ? "" : applicationName;

	        	$scope.currentApplicationName = applicationName;
	        	if ( globalConfig.useRealTime === false ) return;
	        	if ( bShowRealtimeChart === false ) return;
	        	if ( bIsWas === false ) {
	        		hidePopup();
	        		return;
	        	}
	        	
	        	adjustWidth();
	        	$scope.bInitialized = true;
	        	
	        	showPopup();
	        	$scope.closePopup();
	        	$scope.currentApplicationName = applicationName;
        		waitingConnection();
        		
        		initReceive();
	        });
	        $scope.retryConnection = function() {
	        	waitingConnection();
        		initReceive();
	        };
	        $scope.resizePopup = function() {
	        	if ( bIsFullWindow ) {
	        		popupHeight = cfg.css.height;
	        		$element.css({
	        			"height": cfg.css.height + "px",
	        			"bottom": "0px"
	        		});
	        		$elAgentChartListWrapper.css("height", "150px");
	        	} else {
	        		popupHeight = $window.innerHeight - cfg.css.navbarHeight;
	        		$element.css({
	        			"height": popupHeight + "px",
	        			"bottom": "0px"
	        		});
	        		$elAgentChartListWrapper.css("height", (popupHeight - cfg.css.titleHeight) + "px");
	        	}
	        	bIsFullWindow = !bIsFullWindow;
	        }
	        $scope.toggleRealtime = function() {
	        	if ( bIsWas === false ) return;
	        	
	        	if ( bShowRealtimeChart === true ) {
	        		hidePopup();
	        		stopReceive();
	        		bShowRealtimeChart = false;
	        	} else {
	        		showPopup();
	        		waitingConnection();
	        		initReceive();
	        		bShowRealtimeChart = true;
	        	}
	        }
	        
	        $scope.closePopup = function() {
	        	stopReceive();
	        	stopChart();
	        	$scope.currentApplicationName = "";
	        	$scope.currentAgentCount = 0;
	        	$scope.hasCriticalError = false;
	        }
	        $($window).on("resize", function() {
	        	adjustWidth();
	        });
	    }
	]);
})(jQuery);