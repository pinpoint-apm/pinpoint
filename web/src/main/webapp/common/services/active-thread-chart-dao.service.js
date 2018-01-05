(function() {
	'use strict';

	pinpointApp.constant( "ActiveThreadChartDaoServiceConfig", {
		dateFormat: "YYYY-MM-DD HH:mm:ss"
	});

	pinpointApp.service( "ActiveThreadChartDaoService", [ "ActiveThreadChartDaoServiceConfig",
		function ActiveThreadChartDaoService( cfg ) {

			this.parseData = function( aChartData ) {
				var aX = aChartData.charts.x;
				var aActiveTraceFast = aChartData.charts.y[ "ACTIVE_TRACE_FAST" ];
				var aActiveTraceNormal = aChartData.charts.y[ "ACTIVE_TRACE_NORMAL" ];
				var aActiveTraceSlow = aChartData.charts.y[ "ACTIVE_TRACE_SLOW" ];
				var aActiveTraceVerySlow = aChartData.charts.y[ "ACTIVE_TRACE_VERY_SLOW" ];
				var xLen = aX.length;
				var fastLen = aActiveTraceFast.length;
				var normalLen = aActiveTraceNormal.length;
				var slowLen = aActiveTraceSlow.length;
				var verySlowLen = aActiveTraceVerySlow.length;
				var refinedChartData = {
					data: [],
					empty: false,
					forceMax: false,
					defaultMax: 10
				};

				if ( fastLen === 0 && normalLen === 0 && slowLen === 0 && verySlowLen === 0 ) {
					refinedChartData.empty = true;
				}
				for ( var i = 0 ; i < xLen ; i++ ) {
					var thisData = {
						"time": moment(aX[i]).format(cfg.dateFormat)
					};
					if ( fastLen > i ) {
						thisData["fast"] = getFloatValue(aActiveTraceFast[i][2]);
						thisData["fastTitle"] = aActiveTraceFast[i][4];
					}
					if ( normalLen > i ) {
						thisData["normal"] = getFloatValue(aActiveTraceNormal[i][2]);
						thisData["normalTitle"] = aActiveTraceNormal[i][4];
					}
					if ( slowLen > i ) {
						thisData["slow"] = getFloatValue(aActiveTraceSlow[i][2]);
						thisData["slowTitle"] = aActiveTraceSlow[i][4];
					}
					if ( verySlowLen > i ) {
						thisData["verySlow"] = getFloatValue(aActiveTraceVerySlow[i][2]);
						thisData["verySlowTitle"] = aActiveTraceVerySlow[i][4];
					}
					refinedChartData.data.push( thisData );
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
						"align" : "right",
						"position": "top",
						"valueWidth": 70,
						"markerSize": 10,
						"valueAlign": "left",
						"valueFunction": function(graphDataItem, valueText) {
							if ( parseInt( valueText.split(" ")[1] ) === -1 ) {
								return "";
							}
							return valueText;
						}
					},
					"usePrefixes": true,
					"dataProvider": oChartData.data,
					"valueAxes": [
						{
							"stackType": "regular",
							"gridAlpha": 0,
							"axisAlpha": 1,
							"position": "left",
							"title": "Active Thread(count)",
							"minimum" : 0
						}
					],
					"graphs": [
						{
							"balloonText": "[[description]] : [[value]]",
							"legendValueText": "([[description]]) [[value]]",
							"lineColor": "rgb(44, 160, 44)",
							"fillColor": "rgb(44, 160, 44)",
							"title": "Fast",
							"descriptionField": "fastTitle",
							"valueField": "fast",
							"fillAlphas": 0.4,
							"connect": false
						},{
							"balloonText": "[[description]] : [[value]]",
							"legendValueText": "([[description]]) [[value]]",
							"lineColor": "rgb(60, 129, 250)",
							"fillColor": "rgb(60, 129, 250)",
							"title": "Normal",
							"descriptionField": "normalTitle",
							"valueField": "normal",
							"fillAlphas": 0.4,
							"connect": false
						},{
							"balloonText": "[[description]] : [[value]]",
							"legendValueText": "([[description]]) [[value]]",
							"lineColor": "rgb(248, 199, 49)",
							"fillColor": "rgb(248, 199, 49)",
							"title": "Slow",
							"descriptionField": "slowTitle",
							"valueField": "slow",
							"fillAlphas": 0.4,
							"connect": false
						},{
							"balloonText": "[[description]] : [[value]]",
							"legendValueText": "([[description]]) [[value]]",
							"lineColor": "rgb(246, 145, 36)",
							"fillColor": "rgb(246, 145, 36)",
							"title": "Very Slow",
							"descriptionField": "verySlowTitle",
							"valueField": "verySlow",
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
				return angular.isNumber( val ) ? ( val === -1 ? null : val.toFixed(2) ) : 0.00;
			}
		}
	]);
})();
