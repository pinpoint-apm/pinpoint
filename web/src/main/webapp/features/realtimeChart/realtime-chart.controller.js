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
		agentChartTemplate: '<div class="agent-chart"><div></div></div>'
	});
	
	pinpointApp.controller('RealtimeChartCtrl', ['RealtimeChartCtrlConfig', '$scope', '$element', '$rootScope', '$compile', 'globalConfig',
	    function (cfg, $scope, $element, $rootScope, $compile, globalConfig) {
	    	
	    	$scope.useRealTime = globalConfig.useRealTime || true;
	    	$scope.showRealtime = false;
	    	$scope.currentApplicationName = "";
	    	$scope.agentCount = 0;
	    	$scope.sumChartColor = ["rgba(44, 160, 44, 1)", "rgba(60, 129, 250, 1)", "rgba(248, 199, 49, 1)", "rgba(246, 145, 36, 1)" ];
	    	$scope.agentChartColor = ["rgba(44, 160, 44, 0.5)", "rgba(60, 129, 250, 0.5)", "rgba(248, 199, 49, 0.5)", "rgba(246, 145, 36, 0.5)" ];
	    	var DEFAULT_Y_MAX = 50;
	    	var aSumChartData = getInitChartData(10);
	    	var $elementSumChartWrapper = $element.find("div.agent-sum-chart");
	    	var $elementAgentChartList = $element.find("div.agent-chart-list");
	    	var aAgentChartList = [];
	    	var oAgentChartNamespace = {};
	    		    	
	    	var wsOpened = false;
	    	var wsocket = null;
	    	var RECEIVE_SUCCESS = 0;
	    	
	        
	    	function getInitChartData( len ) {
    	    	var a = [];
    	        for( var i = 0 ; i < $scope.sumChartColor.length ; i++ ) {
    	            a.push( d3.range(len).map(function() { return 0; }) );
    	        }
    	        return a;
    	    }
	    	function initChartDirective() {
	    		var el = $compile('<realtime-chart-directive chart-color="sumChartColor" use-label="true" namespace="sum" width="260" height="120"></realtime-chart-directive>')($scope);
	    		$elementSumChartWrapper.append( el );
	    	}
	    	function addAgentChart( agentName ) {
	    		var $newAgentChart = $( cfg.agentChartTemplate ).find("div").html(agentName).end();
	    		var el = $compile('<realtime-chart-directive chart-color="agentChartColor" use-label="false" namespace="' + aAgentChartList.length + '" width="120" height="60"></realtime-chart-directive>')($scope);
	    		
	    		$newAgentChart.append( el );
	    		$elementAgentChartList.append( $newAgentChart );
	    		
	    		aAgentChartList.push( $newAgentChart );
	    		oAgentChartNamespace[agentName] = aAgentChartList.length - 1;
	    	}
	        function initWS() {
	        	wsocket = null;
	    		wsocket = new WebSocket("ws://" + location.host + cfg.wsUrl);
	    		wsocket.onopen = function(event) {
	            	wsOpened = true;
	            	console.log( "ws open : ", event );
	            	send();
	            };
	            wsocket.onmessage = function(event) {
	            	var data = JSON.parse( event.data );
	            	receive( data );
	            };
	            wsocket.onclose = function(event) {
	            	// retry connection
	            	wsOpened = false;
	            	wsocket = null;
	            	console.log( "ws close :", event );
	            };                    	
	            
	            initChartDirective();
	        }
	        function receive( data ) {
	        	if ( angular.isUndefined( data[$scope.currentApplicationName] ) ) return;
	        	
	        	var applicationData = data[$scope.currentApplicationName];
	        	//@Test-Code
//	        	for( var i = 0 ; i < 7 ; i++ ) {
//	        		applicationData["Naver-agent-" + i] = {
//	        			code: 0,
//	        			message: "OK",
//	        			status: [0, 0, 0, 0],
//	        			timeStamp: Date.now()
//	        		};
//	        	}
	        	var agentCount = 0;
	        	var aRequestSum = [0, 0, 0, 0];
	        	var timeStamp;

	        	for( var p in applicationData ) {
	        		if ( applicationData[p].code === RECEIVE_SUCCESS ) {
	        			var aRequestCount = applicationData[p].status;
	        			for( var i = 0 ; i < aRequestCount.length ; i++ ) {
	        				aRequestSum[i] += aRequestCount[i];
	        			}
	        			timeStamp = applicationData[p].timeStamp;
	        		}
	        	}
	        	for (var i = 0 ; i < aSumChartData.length ; i++ ) {
	        		aSumChartData[i].push( aRequestSum[i] );
	        		aSumChartData[i].shift();
	        	}
	        	var sumOfMaxY = sumOfMax( aSumChartData );

	        	for( var p in applicationData ) {
	        		if ( aAgentChartList.length <= agentCount ) {
	        			addAgentChart(p);
	        		}
	        		aAgentChartList[agentCount].show();
	        		
	        		if ( applicationData[p].code === RECEIVE_SUCCESS ) {
	        			$rootScope.$broadcast('realtimeChartDirective.onData.' + oAgentChartNamespace[p], applicationData[p].status, applicationData[p].timeStamp, sumOfMaxY );
	        		} else {
	        			//show message 
	        		}
	        		agentCount++;
	        	}
	        	
        		$rootScope.$broadcast('realtimeChartDirective.onData.sum', aRequestSum, timeStamp, sumOfMaxY );
	        	$scope.$apply(function() {
	        		$scope.agentCount = agentCount;
	        	});
	        }
	        function sumOfMax(datum) {
    	        var sum = 0;
    	        for (var i = 0 ; i < datum.length ; i++ ) {
    	            sum += Math.ceil( d3.max( datum[i], function( d ) {
    	                return d;
    	            }) );
    	        }
    	        return sum === 0 ? DEFAULT_Y_MAX : sum;
    	    }
	        function send() {
	        	wsocket.send("applicationName=" + $scope.currentApplicationName);
	        }
	        function startWS( applicationName ) {
	        	$scope.currentApplicationName = applicationName;
	        	if ( wsOpened === false || wsocket == null) {
	        		initWS();
	        	} else {
	        		send();
	        	}
	        	$scope.$apply(function() {
	        		$scope.showRealtime = true;
	        	});
	        }
	        function stopWS() {
	        	$scope.showRealtime = false;
	        	wsocket.send("applicationName=");
	        }
	        function stopChart() {
	        	$rootScope.$broadcast('realtimeChartDirective.clear.sum');
	        	$.each( aAgentChartList, function(index, el) {
	        		$rootScope.$broadcast('realtimeChartDirective.clear.' + index);
	        		el.hide();
	        	});

	        }
	        
	        $scope.$on('realtimeChartController.initialize', function (event, isWas, applicationName) {
	        	if ( $scope.useRealTime === false ) return;
	        	if ( isWas === true ) {
	        		startWS( applicationName );
	        	} else {
	        		stopWS();
	        	}
	        	
	        });
	        $scope.closePopup = function() {
	        	stopWS();
	        	stopChart();
	        	$scope.currentApplicationName = "";
	        	$scope.agentCount = 0;
	        }			
	    }
	]);
})(jQuery);