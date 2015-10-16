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
		}
	});
	
	pinpointApp.controller('RealtimeChartCtrl', ['RealtimeChartCtrlConfig', '$scope', '$element', '$rootScope', '$compile', '$window', 'globalConfig', 'RealtimeWebsocketService',
	    function (cfg, $scope, $element, $rootScope, $compile, $window, globalConfig, websocketService) {
	    	
			//@TODO will move to preference-service 
			var X_AXIS_COUNT = 10;
	    	var RECEIVE_SUCCESS = 0;
	    	
			var $elSumChartWrapper = $element.find("div.agent-sum-chart");
	    	var $elAgentChartListWrapper = $element.find("div.agent-chart-list");
	    	var $elWarningMessage = $element.find(".connection-message");
	    	var aAgentChartElementList = [];
	    	var oNamespaceToIndexMap = {};
	    	var aSumChartData = [0];
	    	var screenState = "small";
	    	
	    	$scope.hasCriticalError = false;
	    	$scope.showRealtimeChart = false;
	    	$scope.sumChartColor 	= ["rgba(44, 160, 44, 1)", 	"rgba(60, 129, 250, 1)", 	"rgba(248, 199, 49, 1)", 	"rgba(246, 145, 36, 1)" ];
	    	$scope.agentChartColor 	= ["rgba(44, 160, 44, .8)", "rgba(60, 129, 250, .8)", 	"rgba(248, 199, 49, .8)", 	"rgba(246, 145, 36, .8)"];
	    	$scope.requestLabelNames= [ "Fast", "Normal", "Slow", "Very Slow"];
	    	$scope.currentAgentCount = 0;
	    	$scope.currentApplicationName = "";
	    	
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
		    			"height": 120,
		    			"width": 260
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
	    			"height": 60,
	    			"width": 120 
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
	        				disconnectedConneciton();
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
	        	
	        	for( var agentName in applicationData ) {
	        		checkAgentChart( agentName, agentIndexAndCount );
	        		
	        		if ( applicationData[agentName][cfg.keys.CODE] === RECEIVE_SUCCESS ) {
	        			$rootScope.$broadcast('realtimeChartDirective.onData.' + oNamespaceToIndexMap[agentName], applicationData[agentName][cfg.keys.STATUS], timeStamp, maxY );
	        		} else {
	        			$rootScope.$broadcast('realtimeChartDirective.onError.' + oNamespaceToIndexMap[agentName], applicationData[agentName][cfg.keys.MESSAGE], timeStamp, maxY );
	        		}
	        		
	        		showAgentChart( agentIndexAndCount );
	        		agentIndexAndCount++;
	        	}
        		$rootScope.$broadcast('realtimeChartDirective.onData.sum', aRequestSum, timeStamp, maxY );
	        	
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
	        	$scope.$apply(function() {
	        		$scope.showRealtimeChart = true;
	        	});
	        }
	        function stopReceive() {
	        	$scope.showRealtimeChart = false;
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
	        	$scope.hasCriticalError = true;
	        }
	        function waitingConnection() {
	        	$elWarningMessage.css("background-color", "rgba(138, 171, 136, 0.5)");
	        	$elWarningMessage.find("h4").css("color", "blue").html("Waiting Connection...");
	        	$scope.hasCriticalError = true;
	        }
	        
	        $scope.$on('realtimeChartController.initialize', function (event, isWas, applicationName) {
	        	if ( globalConfig.useRealTime === false ) return;
	        	if ( isWas === false && $scope.showRealtimeChart == false ) return;
	        	
	        	if ( isWas === true ) {
	        		if ( $scope.showRealtimeChart === true ) {
	    	        	$scope.closePopup();	        			
	        		}
	        		waitingConnection();
	        		
	        		$scope.currentApplicationName = applicationName;
	        		initReceive();
	        	} else {
	        		stopReceive();
	        	}
	        	
	        });
	        $scope.resizePopup = function() {
	        	switch( screenState ) {
		        	case "full":
		        		$element.css({
		        			"height": "180px",
		        			"bottom": "184px"
		        		});
		        		$elAgentChartListWrapper.css("height", "150px");
		        		screenState = "small";
		        		break;
		        	case "small":
		        		$element.css({
		        			"height": ($window.innerHeight - 70) + "px",
		        			"bottom": ($window.innerHeight - 70 + 4) + "px"
		        		});
		        		$elAgentChartListWrapper.css("height", ($window.innerHeight - 70 - 30) + "px");
		        		screenState = "full";
		        		break;
	        	}
	        }
	        $scope.closePopup = function() {
	        	stopReceive();
	        	stopChart();
	        	$scope.currentApplicationName = "";
	        	$scope.currentAgentCount = 0;
	        	$scope.hasCriticalError = false;
	        }			
	    }
	]);
})(jQuery);