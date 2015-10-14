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
		wsUrl: "/agent/activeThread.pinpointws",
		agentChartTemplate: '<div class="agent-chart"><div></div></div>',
		chartDirectiveTemplate: Handlebars.compile( '<realtime-chart-directive chart-color="{{chartColor}}" xcount="{{xAxisCount}}" show-extra-info="{{showExtraInfo}}" request-label="requestLabelNames" namespace="{{namespace}}" width="{{width}}" height="{{height}}"></realtime-chart-directive>' )
	});
	
	pinpointApp.controller('RealtimeChartCtrl', ['RealtimeChartCtrlConfig', '$scope', '$element', '$rootScope', '$compile', '$window', 'globalConfig',
	    function (cfg, $scope, $element, $rootScope, $compile, $window, globalConfig) {
	    	
			var X_AXIS_COUNT = 10;
	    	var RECEIVE_SUCCESS = 0;
	    	
			var $elSumChartWrapper = $element.find("div.agent-sum-chart");
	    	var $elAgentChartListWrapper = $element.find("div.agent-chart-list");
	    	var bWebsocketOpened = false;
	    	var websocket = null;
	    	var aSumChartData = [0];
	    	var aAgentChartElementList = [];
	    	var oNamespaceToIndexMap = {};
	    	var screenState = "small";
	    	
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
	    		$elSumChartWrapper.append( $compile( cfg.chartDirectiveTemplate({
	    			"chartColor": "sumChartColor",
	    			"xAxisCount": X_AXIS_COUNT,
	    			"namespace": "sum",
	    			"showExtraInfo": "true",
	    			"height": 120,
	    			"width": 260
	    		}))($scope) );
	    	}
	    	function hasAgentChart( agentName ) {
	    		return angular.isDefined( oNamespaceToIndexMap[agentName] );
	    	}
	    	function addAgentChart( agentName ) {
	    		var $newAgentChart = $( cfg.agentChartTemplate ).append( $compile( cfg.chartDirectiveTemplate({
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
	        function initWS() {
	        	websocket = null;
	        	if ( angular.isDefined( WebSocket ) ) {
		    		websocket = new WebSocket("ws://" + location.host + cfg.wsUrl);
		    		websocket.onopen = function(event) {
		    			console.log( "onOpen websocket", event);
		            	bWebsocketOpened = true;
		            	send();
		            };
		            websocket.onmessage = function(event) {
		            	receive( JSON.parse( event.data ) );
		            };
		            websocket.onclose = function(event) {
		            	console.log( "onClose websocket", event);
		            	bWebsocketOpened = false;
		            	websocket = null;
//		            	@TODO
//		            	if ( $scope.showRealtimeChart === true ) {
//		            		//reinit
//		            	}
		            };                    	
		            initChartDirective();
	        	}
	        }
	        function receive( data ) {
	        	if ( angular.isUndefined( data[$scope.currentApplicationName] ) ) return;
	        	
	        	var applicationData = data[$scope.currentApplicationName];
	        	var aRequestSum = getSumOfRequestType( applicationData );
	        	addSumYValue( aRequestSum );
	        	
	        	broadcastData( applicationData, aRequestSum );
	        	
	        }
	        function broadcastData( applicationData, aRequestSum ) {
	        	var maxY = getMaxOfYValue();
	        	var agentIndexAndCount = 0;
	        	var timeStamp;
	        	
	        	for( var agentName in applicationData ) {
	        		checkAgentChart( agentName, agentIndexAndCount );
	        		
	        		timeStamp = applicationData[agentName].timeStamp;	        		
	        		if ( applicationData[agentName].code === RECEIVE_SUCCESS ) {
	        			$rootScope.$broadcast('realtimeChartDirective.onData.' + oNamespaceToIndexMap[agentName], applicationData[agentName].status, timeStamp, maxY );
	        		} else {
	        			$rootScope.$broadcast('realtimeChartDirective.onError.' + oNamespaceToIndexMap[agentName], applicationData[agentName].message, timeStamp, maxY );
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
	        		if ( datum[p].code === RECEIVE_SUCCESS ) {
	        			jQuery.each(datum[p].status, function( i, v ) {
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
	        function send() {
	        	websocket.send("applicationName=" + $scope.currentApplicationName);
	        }
	        function startWS( applicationName ) {
	        	$scope.currentApplicationName = applicationName;
	        	if ( bWebsocketOpened === false || websocket == null) {
	        		initWS();
	        	} else {
	        		send();
	        	}
	        	$scope.$apply(function() {
	        		$scope.showRealtimeChart = true;
	        	});
	        }
	        function stopWS() {
	        	$scope.showRealtimeChart = false;
	        	websocket.send("applicationName=");
	        }
	        function stopChart() {
	        	$rootScope.$broadcast('realtimeChartDirective.clear.sum');
	        	$.each( aAgentChartElementList, function(index, el) {
	        		$rootScope.$broadcast('realtimeChartDirective.clear.' + index);
	        		el.hide();
	        	});

	        }
	        
	        $scope.$on('realtimeChartController.initialize', function (event, isWas, applicationName) {
	        	if ( globalConfig.useRealTime === false ) return;
	        	if ( isWas === false && $scope.showRealtimeChart == false ) return;
	        	
	        	if ( isWas === true ) {
	        		if ( $scope.showRealtimeChart === true ) {
	    	        	$scope.closePopup();	        			
	        		} 
	        		startWS( applicationName );
	        	} else {
	        		stopWS();
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
	        	stopWS();
	        	stopChart();
	        	$scope.currentApplicationName = "";
	        	$scope.currentAgentCount = 0;
	        }			
	    }
	]);
})(jQuery);