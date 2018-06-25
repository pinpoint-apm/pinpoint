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
				var dsLen = aChartData.length;
				var refinedChartData = {
					data: [],
					empty: false,
					forceMax: false,
					defaultMax: 10
				};
				var maxAvg = 0;

				for( var i = 0 ; i < dsLen ; i++ ) {
					var oGroupData = aChartData[i];
					var targetId = oGroupData.id;
					var aAvgData = oGroupData.charts.y["ACTIVE_CONNECTION_SIZE"];
					var xLen = oGroupData.charts.x.length;
					var avgLen = aAvgData.length;

					if ( avgLen === 0 ) {
						refinedChartData.empty = true;
					}
					for( var j = 0 ; j < xLen ; j++ ) {
						if ( i === 0 ) {
							refinedChartData.data[j] = {
								"time": moment(oGroupData.charts.x[j]).format(cfg.dateFormat)
							};
						}
						if ( avgLen > j ) {
							var oData = aAvgData[j];
							maxAvg = Math.max( maxAvg, oData[2] );
							refinedChartData.data[j][prefix+targetId]  = oData[2];
						}
					}
				}
				refinedChartData.defaultMax = refinedChartData.empty ? 10 : Math.max( 10, parseInt(maxAvg) + 5 );
				return refinedChartData;
			};
		}
	]);
})();
