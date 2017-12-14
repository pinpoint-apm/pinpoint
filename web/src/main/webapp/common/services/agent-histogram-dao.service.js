(function() {
	'use strict';

	pinpointApp.constant( "agentHistogramDaoServiceConfig", {
		url: "getResponseTimeHistogramDataV2.pinpoint"
	});

	pinpointApp.service( "AgentHistogramDaoService", [ "agentHistogramDaoServiceConfig", "$http", "ServerMapDaoService",
		function AgentHistogramDaoService( cfg, $http, ServerMapDaoService ) {

			this.loadAgentHistogram = function( node, navBarVoService, callback, from, to ) {
				if ( node.agentHistogram && node.agentTimeSeriesHistogram ) { // filteredMap page
					callback("success", {
						agentHistogram : node.agentHistogram,
						agentTimeSeriesHistogram : node.agentTimeSeriesHistogram,
						serverList: node.serverList
					});
				} else { // main page
					$http.post(cfg.url + makeParam(node, navBarVoService, from, to), ServerMapDaoService.getFromToData(node.key)).then(function (result) {
						callback("success", result.data);
					}, function (error) {
						callback("error", error);
					});
				}
			};
			function makeParam(node, navBarVoService, from, to) {
				return [
					"?",
					"applicationName=", node.applicationName,
					"&serviceTypeCode=", node.serviceTypeCode,
					"&from=", from || navBarVoService.getQueryStartTime(),
					"&to=", to || navBarVoService.getQueryEndTime()
				].join("");
			}
		}
	]);
})();
