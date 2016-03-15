(function() {
	'use strict';
	/**
	 * (en)RealtimeWebsocketService 
	 * @ko RealtimeWebsocketService
	 * @group Service
	 * @name RealtimeWebsocketService
	 * @class
	 */
	pinpointApp.constant('RealtimeWebsocketServiceConfig', {
		wsUrl: "/agent/activeThread.pinpointws",
		wsTimeout: 10000, //ms
		retryTimeout: 3000,
		maxRetryCount: 1
	});
	
	pinpointApp.service('RealtimeWebsocketService', [ 'RealtimeWebsocketServiceConfig', function(cfg) {

		var connectTime = null;
	    var lastReceiveTime = null;
    	var websocket = null;
    	var refInterval = null;
    	var oHandlers;
		var retryCount = 0;

	    this.open = function( handlers ) {
	    	websocket = null;
	    	oHandlers = handlers;
        	if ( angular.isDefined( WebSocket ) ) {
				connectWebsocket();
	            return true;
        	}
        	return false;
	    };
	    this.isOpened = function() {
	    	return websocket !== null;
	    };
	    this.close = function() {
	    	if ( websocket !== null ) {
	    		websocket.close();
	    	}
	    	websocket = null;
	    };
	    this.send = function( message ) {
	    	if ( websocket !== null ) {
	    		websocket.send( message );
	    	}
	    };
	    this.stopReceive = function( message ) {
	    	if ( websocket !== null ) {
	    		websocket.send( message );
	    	}
	    	stopTimeoutChecker();
	    };
		function connectWebsocket() {
			websocket = new WebSocket("ws://" + location.host + cfg.wsUrl);
			websocket.onopen = function(event) {
				connectTime = lastReceiveTime = Date.now();
				startTimeoutChecker();
				oHandlers.onopen(event);
			};
			websocket.onmessage = function(event) {
				lastReceiveTime = Date.now();
				oHandlers.onmessage(JSON.parse( event.data ));
			};
			websocket.onclose = function(event) {
				console.log( "onClose websocket", event);
				websocket = null;
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
					console.log("retry websocket connection");
					retryCount++;
					oHandlers.retry();
				}
			}
		}
	}]);
})();