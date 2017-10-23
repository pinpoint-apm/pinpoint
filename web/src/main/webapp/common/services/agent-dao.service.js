(function() {
	'use strict';

	pinpointApp.constant( "agentDaoServiceConfig", {
		dateFormat: "YYYY-MM-DD HH:mm:ss"
	});

	pinpointApp.service( "AgentDaoService", [ "agentDaoServiceConfig",
		function AgentDaoService( cfg ) {
			this.getSampleRate = function (period) {
				var MAX_POINTS = 100;
				var points = period / 5;
				var rate = Math.floor(points / MAX_POINTS);
				return points <= MAX_POINTS ? 1 : rate;
			};
			this.parseDataSourceChartDataForAmcharts = function ( aChartData, prefix ) {
				var refinedChartData = {
					data: [],
					empty: true,
					defaultMax: 10
				};
				var maxAvg = 0;
				for( var groupIndex = 0 ; groupIndex < aChartData.length ; groupIndex++ ) {
					var oGroupData = aChartData[groupIndex];
					var targetId = oGroupData.id;
					var aAvgData = oGroupData.charts["ACTIVE_CONNECTION_SIZE"].points;

					for( var fieldIndex = 0 ; fieldIndex < aAvgData.length ; fieldIndex++ ) {
						var oData = aAvgData[fieldIndex];
						if ( groupIndex === 0 ) {
							refinedChartData.data[fieldIndex] = {
								"time": moment(oData["xVal"]).format(cfg.dateFormat)
							};
						}
						maxAvg = Math.max( maxAvg, oData["avgYVal"] );
						if ( oData["avgYVal"] !== -1 ) {
							refinedChartData.empty = false;
						}
						refinedChartData.data[fieldIndex][prefix+targetId]  = oData["avgYVal"].toFixed(1);
					}
				}
				refinedChartData.defaultMax = refinedChartData.empty ? refinedChartData.defaultMax : parseInt(maxAvg) + 1;
				return refinedChartData;
			};
		}
	]);
})();
