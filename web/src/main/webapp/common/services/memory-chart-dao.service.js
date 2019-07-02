(function() {
	'use strict';

	pinpointApp.constant( "MemoryChartDaoServiceConfig", {
		dateFormat: "YYYY-MM-DD HH:mm:ss"
	});

	pinpointApp.service( "MemoryChartDaoService", [ "MemoryChartDaoServiceConfig",
		function MemoryChartDaoService( cfg ) {

			this.parseNonHeapData = function( aChartData ) {
				var category = [
					{ id: "JVM_MEMORY_NON_HEAP_USED", key: "Used", isFgc: false },
					{ id: "JVM_MEMORY_NON_HEAP_MAX", key: "Max", isFgc: false },
					{ id: "fgc", key: "FGC", isFgc: true }
				];
				return _parseData( category, aChartData );
			};
			this.parseHeapData = function( aChartData ) {
				var category = [
					{ id: "JVM_MEMORY_HEAP_USED", key: "Used", isFgc: false },
					{ id: "JVM_MEMORY_HEAP_MAX", key: "Max", isFgc: false },
					{ id: "fgc", key: "FGC", isFgc: true }
				];
				return _parseData( category, aChartData );
			};
			function _parseData( category, aChartData ) {
				var aX = aChartData.charts.x;
				var pointsTime = aChartData.charts.y["JVM_GC_OLD_TIME"];
				var pointsCount = aChartData.charts.y["JVM_GC_OLD_COUNT"];
				var refinedChartData = {
					data: [],
					empty: false,
					forceMax: false,
					defaultMax: 100,
					defaultMax2: 10000000
				};
				var cumulativeGcTime = 0;

				for (var i = 0; i < aX.length; ++i) {
					var thisData = {
						time: moment(aX[i]).format( cfg.dateFormat )
					};
					if ( pointsTime.length === 0 ) {
						refinedChartData.empty = true;
					} else {
						for (var j = 0; j < category.length; j++) {
							if (category[j].isFgc) {
								var gcCount = pointsCount[i][3];
								var gcTime = pointsTime[i][3];
								if (gcTime > 0) {
									cumulativeGcTime += gcTime;
								}
								if (gcCount > 0) {
									thisData[category[j].key + "Count"] = gcCount;
									thisData[category[j].key + "Time"] = cumulativeGcTime;
									cumulativeGcTime = 0;
								}
							} else {
								var yValue = aChartData.charts.y[category[j]["id"]][i][1];
								if (yValue > 0) {
									thisData[category[j].key] = yValue === -1 ? null : yValue;
								}
							}
						}
					}
					refinedChartData.data.push(thisData);
				}
				return refinedChartData;
			}
			this.getChartOptions = function( oChartData ) {
				return {
					"type": "serial",
					"theme": "light",
					"autoMargins": false,
					"marginTop": 10,
					"marginLeft": 70,
					"marginRight": 70,
					"marginBottom": 40,
					"legend": {
						"useGraphSettings": true,
						"autoMargins": false,
						"align" : "right",
						"position": "top",
						"valueWidth": 70,
						"markerSize": 10,
						"valueAlign": "left",
						"valueFunction": function(graphDataItem, valueText) {
							if ( parseInt( valueText ) === -1 ) {
								return "";
							}
							return valueText;
						}
					},
					"usePrefixes": true,
					"dataProvider": oChartData.data,
					"valueAxes": [
						{
							"id": "v1",
							"gridAlpha": 0,
							"axisAlpha": 1,
							"position": "right",
							"title": "Full GC (ms)",
							"minimum": 0
						},
						{
							"id": "v2",
							"gridAlpha": 0,
							"axisAlpha": 1,
							"position": "left",
							"title": "Memory (bytes)",
							"minimum": 0
						}
					],
					"graphs": [
						{
							"valueAxis": "v2",
							"balloonText": "[[value]]B",
							"legendValueText": "[[value]]B",
							"lineColor": "rgb(174, 199, 232)",
							"title": "Max",
							"valueField": "Max",
							"fillAlphas": 0,
							"connect": false
						},
						{
							"valueAxis": "v2",
							"balloonText": "[[value]]B",
							"legendValueText": "[[value]]B",
							"lineColor": "rgb(31, 119, 180)",
							"fillColor": "rgb(31, 119, 180)",
							"title": "Used",
							"valueField": "Used",
							"fillAlphas": 0.4,
							"connect": false
						},
						{
							"valueAxis": "v1",
							"balloonFunction": function(item, graph) {
								var data = item.serialDataItem.dataContext;
								var balloonText = data.FGCTime + "ms";
								var fgcCount = data.FGCCount;
								if (fgcCount > 1) {
									balloonText += " (" + fgcCount + ")";
								}
								return balloonText;
							},
							"legendValueText": "[[value]]ms",
							"lineColor": "#FF6600",
							"title": "Major GC",
							"valueField": "FGCTime",
							"type": "column",
							"fillAlphas": 0.3,
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
