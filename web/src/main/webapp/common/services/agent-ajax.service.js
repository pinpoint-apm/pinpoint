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
		"agentList"			: "/getAgentList.pinpoint",							// agentId, timestamp ( or agentId, from, to )
		"agentInfo"			: "/getAgentInfo.pinpoint", 						// agentId, timestamp
		"agentEvent"		: "/getAgentEvent.pinpoint", 						// agentId, eventTimestamp, eventTypeCode
		"agentStatus"		: "/getAgentStatus.pinpoint", 						// agentId, timestamp
		"agentEventList"	: "/getAgentEvents.pinpoint", 						// agentId, from, to
		"jvmChart"			: "/getAgentStat/jvmGc/chart.pinpoint",
		"cpuLoadChart"		: "/getAgentStat/cpuLoad/chart.pinpoint",
		"tpsChart"			: "/getAgentStat/transaction/chart.pinpoint",
		"activeTraceChart"	: "/getAgentStat/activeTrace/chart.pinpoint",
		"dataSourceChart"	: "/getAgentStat/dataSource/chartList.pinpoint",
		"agentStateForChart": "/getAgentStat.pinpoint"
	});

	pinpointApp.service('AgentAjaxService', [ 'AgentAjaxServiceConfig', '$http', function ($config, $http) {
		this.getAgentList = function(data, callback) {
			retrieve($config.agentList, data, callback);
		};
		this.getAgentStateForChart = function( data, callback ) {
			retrieve($config.agentStateForChart, data, callback);
		};
		this.getJVMChartData = function( data, callback ) {
			retrieve($config.jvmChart, data, callback);
		};
		this.getCpuLoadChartData = function( data, callback ) {
			retrieve($config.cpuLoadChart, data, callback);
		};
		this.getTPSChartData = function( data, callback ) {
			retrieve($config.tpsChart, data, callback);
		};
		this.getActiveTraceChartData = function( data, callback ) {
			retrieve($config.activeTraceChart, data, callback);
		};
		this.getDataSourceChartData = function( data, callback ) {
			retrieve($config.dataSourceChart, data, callback);
		};
		this.getAgentInfo = function( data, callback ) {
			retrieve($config.agentInfo, data, callback);
		};
		this.getEventList = function( data, callback ) {
			data.exclude = "10199";
			retrieve($config.agentEventList, data, callback);
		};
		this.getEvent = function( data, callback ) {
			retrieve($config.agentEvent, data, callback);
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