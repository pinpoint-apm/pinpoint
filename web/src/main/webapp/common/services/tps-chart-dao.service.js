(function() {
	'use strict';

	pinpointApp.constant( "TPSChartDaoServiceConfig", {
		dateFormat: "YYYY-MM-DD HH:mm:ss"
	});

	pinpointApp.service( "TPSChartDaoService", [ "TPSChartDaoServiceConfig",
		function TPSChartDaoService( cfg ) {

			this.parseData = function( aChartData ) {
				var aSampledContinuationData = aChartData.charts["TPS_SAMPLED_CONTINUATION"].points;
				var aSampledNewData = aChartData.charts["TPS_SAMPLED_NEW"].points;
				var aUnsampledContinuationData = aChartData.charts["TPS_UNSAMPLED_CONTINUATION"].points;
				var aUnsampledNewData = aChartData.charts["TPS_UNSAMPLED_NEW"].points;
				var aTotalData = aChartData.charts["TPS_TOTAL"].points;
				var refinedChartData = {
					data: [],
					empty: true,
					defaultMax: 10
				};
				var tpsLength = aTotalData.length;

				for ( var i = 0 ; i < tpsLength ; i++ ) {
					var sampledContinuationTps = aSampledContinuationData[i]["avgYVal"];
					var sampledNewTps = aSampledNewData[i]["avgYVal"];
					var unsampledContinuationTps = aUnsampledContinuationData[i]["avgYVal"];
					var unsampledNewTps = aUnsampledNewData[i]["avgYVal"];
					var totalTps = aTotalData[i]["avgYVal"];
					if ( sampledContinuationTps !== -1 || sampledNewTps !== -1 || unsampledContinuationTps !== -1 || unsampledNewTps !== -1 || totalTps !== -1 ) {
						refinedChartData.empty = false;
					}
					refinedChartData.data.push({
						"time" : moment(aSampledContinuationData[i]["xVal"]).format( cfg.dateFormat ),
						"sampledContinuationTps": getFloatValue( sampledContinuationTps ),
						"sampledNewTps": getFloatValue( sampledNewTps ),
						"unsampledContinuationTps": getFloatValue( unsampledContinuationTps ),
						"unsampledNewTps": getFloatValue( unsampledNewTps ),
						"totalTps": getFloatValue( totalTps )
					});
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
					"legend": {
						"useGraphSettings": true,
						"autoMargins": true,
						"align": "right",
						"position": "top",
						"valueWidth": 70,
						"markerSize": 10,
						"valueAlign": "left"
					},
					"usePrefixes": true,
					"dataProvider": oChartData.data,
					"valueAxes": [{
						"stackType": "regular",
						"gridAlpha": 0,
						"axisAlpha": 1,
						"position": "left",
						"title": "Transaction(count)",
						"minimum": 0
					}],
					"graphs": [
						{
							"balloonText": "Sampled Continuation : [[value]]",
							"legendValueText": "[[value]]",
							"lineColor": "rgb(214, 141, 8)",
							"fillColor": "rgb(214, 141, 8)",
							"title": "S.C",
							"valueField": "sampledContinuationTps",
							"fillAlphas": 0.4,
							"connect": false
						}, {
							"balloonText": "Sampled New : [[value]]",
							"legendValueText": "[[value]]",
							"lineColor": "rgb(252, 178, 65)",
							"fillColor": "rgb(252, 178, 65)",
							"title": "S.N",
							"valueField": "sampledNewTps",
							"fillAlphas": 0.4,
							"connect": false
						}, {
							"balloonText": "Unsampled Continuation : [[value]]",
							"legendValueText": "[[value]]",
							"lineColor": "rgb(90, 103, 166)",
							"fillColor": "rgb(90, 103, 166)",
							"title": "U.C",
							"valueField": "unsampledContinuationTps",
							"fillAlphas": 0.4,
							"connect": false
						}, {
							"balloonText": "Unsampled New : [[value]]",
							"legendValueText": "[[value]]",
							"lineColor": "rgb(160, 153, 255)",
							"fillColor": "rgb(160, 153, 255)",
							"title": "U.N",
							"valueField": "unsampledNewTps",
							"fillAlphas": 0.4,
							"connect": false
						}, {
							"balloonText": "Total : [[value]]",
							"legendValueText": "[[value]]",
							"lineColor": "rgba(31, 119, 180, 0)",
							"fillColor": "rgba(31, 119, 180, 0)",
							"title": "Total",
							"valueField": "totalTps",
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
			function getFloatValue( val ) {
				return angular.isNumber( val ) ? val.toFixed(2) : 0.00;
			}
		}
	]);
})();
