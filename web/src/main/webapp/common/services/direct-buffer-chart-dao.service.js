(function() {
	'use strict';

	pinpointApp.constant( "DirectBufferDaoServiceConfig", {
		dateFormat: "YYYY-MM-DD HH:mm:ss"
	});

	pinpointApp.service( "DirectBufferDaoService", [ "DirectBufferDaoServiceConfig",
		function DirectBufferDaoService( cfg ) {

			this.parseData = function( aChartData ) {
				var aX = aChartData.charts.x;
				var pointsDirectCount = aChartData.charts.y["DIRECT_COUNT"];
				var pointsMappedCount = aChartData.charts.y["MAPPED_COUNT"];
				var pointsDirectMemoryUsed = aChartData.charts.y["DIRECT_MEMORY_USED"];
				var pointsMappedMemoryUsed = aChartData.charts.y["MAPPED_MEMORY_USED"];
				var xLen = aX.length;
				var directCountLen = pointsDirectCount.length;
				var mappedCountLen = pointsMappedCount.length;
				var directMemoryUsedLen = pointsDirectMemoryUsed.length;
				var mappedMemoryUsedLen = pointsMappedMemoryUsed.length;

				var refinedChartData = {
					data: [],
					empty: false,
					forceMax: false,
					defaultMax: 100
				};
				if ( directCountLen === 0 && mappedCountLen === 0 ) {
					refinedChartData.empty = true;
				}
				for (var i = 0; i < xLen; ++i) {
					var thisData = {
						time: moment(aX[i]).format( cfg.dateFormat )
					};
					if ( directCountLen > i ) {
						thisData["directCount"] = pointsDirectCount[i][2] === -1 ? null : pointsDirectCount[i][2].toFixed(2);
					}
					if ( mappedCountLen > i ) {
						thisData["mappedCount"] = pointsMappedCount[i][2] === -1 ? null : pointsMappedCount[i][2].toFixed(2);
					}
					if ( directMemoryUsedLen > i ) {
						thisData["directMemoryUsed"] = pointsDirectMemoryUsed[i][2] === -1 ? null : pointsDirectMemoryUsed[i][2].toFixed(2);
					}
					if ( mappedMemoryUsedLen > i ) {
						thisData["mappedMemoryUsed"] = pointsMappedMemoryUsed[i][2] === -1 ? null : pointsMappedMemoryUsed[i][2].toFixed(2);
					}
					refinedChartData.data.push(thisData);
				}
				return refinedChartData;
			};
			this.getDirectBufferCountChartOptions = function( oChartData ) {
				return this.getCommonChartOptions( oChartData, "Buffer (counts)", {
					"valueAxis": "v1",
					"balloonText": "Count : [[value]]",
					"legendValueText": "[[value]]",
					"lineColor": "rgb(31, 119, 180)",
					"title": "Direct",
					"valueField": "directCount",
					"fillAlphas": 0,
					"connect": false
				});
			};
			this.getDirectBufferMemoryChartOptions = function( oChartData ) {
				return this.getCommonChartOptions( oChartData, "Memory (bytes)", {
					"valueAxis": "v1",
					"balloonText": "Memory : [[value]]",
					"legendValueText": "[[value]]",
					"lineColor": "rgb(31, 119, 180)",
					"title": "Direct",
					"valueField": "directMemoryUsed",
					"fillAlphas": 0,
					"connect": false
				});
			};
			this.getMappedBufferCountChartOptions = function( oChartData ) {
				return this.getCommonChartOptions( oChartData, "Buffer (counts)", {
					"valueAxis": "v1",
					"balloonText": "Count : [[value]]",
					"legendValueText": "[[value]]",
					"lineColor": "rgb(31, 119, 180)",
					"title": "Direct",
					"valueField": "mappedCount",
					"fillAlphas": 0,
					"connect": false
				});
			};
			this.getMappedBufferMemoryChartOptions = function( oChartData ) {
				return this.getCommonChartOptions( oChartData, "Memory (bytes)", {
					"valueAxis": "v1",
					"balloonText": "Memory : [[value]]",
					"legendValueText": "[[value]]",
					"lineColor": "rgb(31, 119, 180)",
					"title": "Direct",
					"valueField": "mappedMemoryUsed",
					"fillAlphas": 0,
					"connect": false
				});
			};
			this.getCommonChartOptions = function( oChartData, title, graph ) {
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
							"title": title,
							"minimum" : 0
						}
					],
					"graphs": [ graph ],
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
