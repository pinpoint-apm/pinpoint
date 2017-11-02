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
					empty: false,
					forceMax: false,
					defaultMax: 10
				};
				var maxAvg = 0;

				for( var groupIndex = 0 ; groupIndex < aChartData.length ; groupIndex++ ) {
					var oGroupData = aChartData[groupIndex];
					var targetId = oGroupData.id;
					var aAvgData = oGroupData.charts.y["ACTIVE_CONNECTION_SIZE"];
					var xLen = oGroupData.charts.x.length;
					var avgLen = aAvgData.length;

					if ( avgLen === 0 ) {
						refinedChartData.empty = true;
					}
					for( var fieldIndex = 0 ; fieldIndex < xLen ; fieldIndex++ ) {
						if ( groupIndex === 0 ) {
							refinedChartData.data[fieldIndex] = {
								"time": moment(oGroupData.charts.x[fieldIndex]).format(cfg.dateFormat)
							};
						}
						if ( avgLen > fieldIndex ) {
							var oData = aAvgData[fieldIndex];
							maxAvg = Math.max( maxAvg, oData[2] );
							refinedChartData.data[fieldIndex][prefix+targetId]  = oData[2].toFixed(1);
						} else {
							refinedChartData.data[fieldIndex][prefix+targetId]  = -1;
						}
					}
				}
				refinedChartData.defaultMax = refinedChartData.empty ? refinedChartData.defaultMax : parseInt(maxAvg) + 1;
				return refinedChartData;
			};
		}
	]);
})();
