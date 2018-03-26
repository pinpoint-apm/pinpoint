(function() {
	'use strict';

	pinpointApp.constant( "ResponseTimeChartDaoServiceConfig", {
		dateFormat: "YYYY-MM-DD HH:mm:ss"
	});

	pinpointApp.service( "ResponseTimeChartDaoService", [ "ResponseTimeChartDaoServiceConfig",
		function ResponseTimeChartDaoService( cfg ) {

			this.parseData = function( aChartData ) {
				var aX = aChartData.charts.x;
				var aAVGData = aChartData.charts.y[ "AVG" ];
				var aMaxData = aChartData.charts.y[ "MAX" ];
				var xLen = aX.length;
				var avgLen = aAVGData.length;
				var refinedChartData = {
					data: [],
					empty: avgLen === 0 ? true : false,
					forceMax: false,
					defaultMax: 100
				};

				for ( var i = 0 ; i < xLen ; i++ ) {
					var thisData = {
						"time": moment( aX[i] ).format( cfg.dateFormat )
					};
					if ( avgLen > i ) {
						thisData["avg"] = getFloatValue( aAVGData[i][2] );
						thisData["max"] = getFloatValue( aMaxData[i][1] );
						thisData["title"] = "AVG";
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
						"align": "right",
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
							"gridAlpha": 0,
							"axisAlpha": 1,
							"position": "left",
							"title": "Response Time(ms)",
							"minimum": 0,
							"labelFunction": function (value) {
								return convertWithUnits(value);
							}
						}
					],
					"graphs": [
						{
							"balloonText": "AVG : [[value]]",
							"legendValueText": "[[value]]",
							"lineColor": "rgb(44, 160, 44)",
							"fillColor": "rgb(44, 160, 44)",
							"title": "AVG",
							"valueField": "avg",
							"fillAlphas": 0.4,
							"connect": false
						},
						{
							"valueAxis": "v1",
							"balloonText": "MAX : [[value]]",
							"legendValueText": "[[value]]",
							"lineColor": "rgb(246, 145, 36)",
							"title": "MAX",
							"valueField": "max",
							"fillAlphas": 0,
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
				}
			};
			function getFloatValue( val ) {
				return angular.isNumber( val ) ? ( val === -1 ? null : val.toFixed(2) ) : 0.00;
			}
			function convertWithUnits(value) {
				var units = [ "ms", "sec", "min" ];
				var result = value;
				var index = 0;
				while ( result >= 1000 ) {
					index++;
					result /= 1000;
				}
				return result + units[index] + " ";
			}
		}
	]);
})();
