(function($) {
	'use strict';

	/**
	 * (en) Agent 모든 Ajax 요청을 대리함.
	 * @ko Agent 모든 Ajax 요청을 대리함.
	 * @group Service
	 * @name AgentAjaxService
	 * @class
	 */
	pinpointApp.constant('AgentAjaxServiceConfig', {
		"agentList": "/getAgentList.pinpoint",				// agentId, timestamp ( or agentId, from, to )
		"agentInfo": "/getAgentInfo.pinpoint", 				// agentId, timestamp
		"agetEvent": "/getAgentEvent.pinpoint", 				// agentId, eventTimestamp, eventTypeCode
		"agentStatus": "/getAgentStatus.pinpoint", 			// agentId, timestamp
		"agentEventList": "/getAgentEvents.pinpoint", 		// agentId, from, to
		"agentStateForChart": "/getAgentStat.pinpoint"		//
	});

	pinpointApp.service('AgentAjaxService', [ 'AgentAjaxServiceConfig', '$http', function ($config, $http) {
		this.getAgentList = function(data, callback) {
			retrieve($config.agentList, data, callback);
		};
		this.getAgentStateForChart = function( data, callback ) {
			retrieve($config.agentStateForChart, data, callback);
		};
		this.getAgentInfo = function( data, callback ) {
			retrieve($config.agentInfo, data, callback);
		};
		this.getEventList = function( data, callback ) {
			data.exclude = "10199";
			retrieve($config.agentEventList, data, callback);
		};
		this.getEvent = function( data, callback ) {
			retrieve($config.agetEvent, data, callback);
		};

		function retrieve(url, data, callback) {
			$http.get(url +  getQueryStr( data ) ).then(function(result) {
				callback(result.data);
			}, function(error) {
				callback(error);
			});
		}
		function getQueryStr( o ) {
			var query = "?";
			for( var p in o ) {
				query += ( query == "?" ? "" : "&" ) + p + "=" + o[p];
			}
			return query;
		}
	}]);
})(jQuery);