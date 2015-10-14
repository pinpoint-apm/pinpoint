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
	});
	
	pinpointApp.service('RealtimeWebsocketService', [ 'RealtimeWebsocketServiceConfig', function(cfg) {

	    var self = this;
	    var lastReceiveTime = null;
    	var websocket = null;
    	var refInterval = null;
    	var oHandlers;

	    this.open = function( handlers ) {
	    	websocket = null;
	    	oHandlers = handlers;
        	if ( angular.isDefined( WebSocket ) ) {
	    		websocket = new WebSocket("ws://" + location.host + cfg.wsUrl);
	    		websocket.onopen = function(event) {
	    			lastReceiveTime = Date.now();
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
	            };
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
	    }
	    
	    function startTimeoutChecker() {
    		refInterval = setInterval(function() {
	    		if ( lastReceiveTime == null ) {
	    			return;
	    		}
	    		if ( Date.now() - lastReceiveTime < cfg.wsTimeout ) {
	    			return;
	    		}
	    		oHandlers.ondelay();
	    	}, 1000);
    	}
    	function stopTimeoutChecker() {
    		lastReceiveTime = null;
    		clearInterval( refInterval );
    	}
	    	
	}]);
})();