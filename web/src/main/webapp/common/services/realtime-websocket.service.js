(function() {
	'use strict';
	/**
	 * (en)RealtimeWebsocketService 
	 * @ko RealtimeWebsocketService
	 * @group Service
	 * @name RealtimeWebsocketService
	 * @class
	 */
	pinpointApp.constant( "RealtimeWebsocketServiceConfig", {
		wsUrl: "agent/activeThread.pinpointws",
		wsTimeout: 10000, //ms
		retryTimeout: 3000,
		maxRetryCount: 1
	});
	
	pinpointApp.service( "RealtimeWebsocketService", [ "RealtimeWebsocketServiceConfig", function(cfg) {

		var connectTime = null;
	    var lastReceiveTime = null;
    	var webSocket = null;
    	var bIsOpenConnection = false;
    	var refInterval = null;
    	var oHandlers;
		var retryCount = 0;
		var pagingSize = 30;

	    this.open = function( handlers ) {
	    	webSocket = null;
	    	oHandlers = handlers;
        	if ( angular.isDefined( WebSocket ) ) {
				connectWebSocket();
	            return true;
        	}
        	return false;
	    };
	    this.isOpened = function() {
	    	return webSocket !== null;
	    };
	    this.close = function() {
	    	if ( webSocket !== null ) {
	    		webSocket.close();
	    	}
	    	webSocket = null;
	    };
	    this.send = function( message ) {
	    	if ( webSocket !== null ) {
	    		webSocket.send( message );
	    	}
	    };
	    this.stopReceive = function( message ) {
	    	if ( webSocket !== null && bIsOpenConnection ) {
	    		webSocket.send( message );
	    	}
	    	stopTimeoutChecker();
	    };
	    this.getPagingSize = function() {
			return pagingSize;
		}
		function connectWebSocket() {
			webSocket = new WebSocket("ws://" + location.host + location.pathname + cfg.wsUrl);
			webSocket.onopen = function(event) {
				bIsOpenConnection = true;
				connectTime = lastReceiveTime = Date.now();
				startTimeoutChecker();
				oHandlers.onopen(event);
			};
			webSocket.onmessage = function(event) {
				lastReceiveTime = Date.now();
				oHandlers.onmessage(JSON.parse( event.data ));
			};
			webSocket.onclose = function(event) {
				bIsOpenConnection = false;
				webSocket = null;
				stopTimeoutChecker();
				oHandlers.onclose(event);
				checkRetry();
			};
		}
	    
	    function startTimeoutChecker() {
    		refInterval = setInterval(function() {
	    		if ( lastReceiveTime === null ) {
	    			return;
	    		}
	    		if ( Date.now() - lastReceiveTime < cfg.wsTimeout ) {
	    			return;
	    		}
	    		oHandlers.ondelay();
	    	}, 1000);
    	}
    	function stopTimeoutChecker() {
    		clearInterval( refInterval );
    	}
		function checkRetry() {
			if ( connectTime !== null && connectTime === lastReceiveTime && ( Date.now() - connectTime < cfg.retryTimeout ) ) {
				if ( retryCount < cfg.maxRetryCount ) {
					retryCount++;
					oHandlers.retry();
				}
			}
		}
	}]);
})();