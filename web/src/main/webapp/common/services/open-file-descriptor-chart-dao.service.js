(function() {
	'use strict';

	pinpointApp.constant( "OpenFileDescriptorDaoServiceConfig", {
		dateFormat: "YYYY-MM-DD HH:mm:ss"
	});

	pinpointApp.service( "OpenFileDescriptorDaoService", [ "OpenFileDescriptorDaoServiceConfig",
		function OpenFileDescriptorDaoService( cfg ) {

			this.parseData = function( aChartData ) {
				var aX = aChartData.charts.x;
				var pointsOpenFileDescriptor = aChartData.charts.y["OPEN_FILE_DESCRIPTOR_COUNT"];
				var xLen = aX.length;
				var len = pointsOpenFileDescriptor.length;

				var refinedChartData = {
					data: [],
					empty: false,
					forceMax: false,
					defaultMax: 100
				};
				if ( len === 0 ) {
					refinedChartData.empty = true;
				}
				for (var i = 0; i < xLen; ++i) {
					var thisData = {
						time: moment(aX[i]).format( cfg.dateFormat )
					};
					if ( len > i ) {
						thisData["count"] = pointsOpenFileDescriptor[i][2] === -1 ? null : pointsOpenFileDescriptor[i][2].toFixed(2);
					}
					refinedChartData.data.push(thisData);
				}
				return refinedChartData;
			};
			this.getChartOptions = function( oChartData ) {
				return {
					"type": "serial",
					"theme": "light",
					"autoMargins": false,
					"marginTop": 10,
					"marginLeft": 70,
					"marginRight": 70,
					"marginBottom": 40,
					"usePrefixes": true,
					"dataProvider": oChartData.data,
					"valueAxes": [
						{
							"id": "v1",
							"gridAlpha": 0,
							"axisAlpha": 1,
							"position": "left",
							"title": "File Descriptor(count)",
							"minimum" : 0,
							"labelFunction": function(value) {
								return value;
							}
						}
					],
					"graphs": [
						{
							"valueAxis": "v1",
							"balloonText": "[[value]]",
							"legendValueText": "[[value]]",
							"lineColor": "rgb(31, 119, 180)",
							"fillColor": "rgb(31, 119, 180)",
							"title": "Open File Descriptor",
							"valueField": "count",
							"fillAlphas": 0.4,
							"connect": false
						}
					],
					"categoryField": "time",
					"categoryAxis": {
						"axisColor": "#DADADA",
						"startOnAxis": true,
						"gridPosition": "start",
						"labelFunction": function (valueText) {
							return valueText.replace(/\s/, "<br>").replace(/-/g, ".").substring(2);
						}
					},
					"chartCursor": {
						"categoryBalloonAlpha": 0.7,
						"fullWidth": true,
						"cursorAlpha": 0.1
					}
				};
			};
		}
	]);
})();
