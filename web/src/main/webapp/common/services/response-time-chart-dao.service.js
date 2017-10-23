(function() {
	'use strict';

	pinpointApp.constant( "ResponseTimeChartDaoServiceConfig", {
		dateFormat: "YYYY-MM-DD HH:mm:ss"
	});

	pinpointApp.service( "ResponseTimeChartDaoService", [ "ResponseTimeChartDaoServiceConfig",
		function ResponseTimeChartDaoService( cfg ) {

			this.parseData = function( aChartData ) {
				var aAVGData = aChartData.charts[ "AVG" ].points;
				var refinedChartData = {
					data: [],
					empty: true,
					defaultMax: 100
				};
				for ( var i = 0 ; i < aAVGData.length ; i++ ) {
					var yVal = aAVGData[i]["avgYVal"];
					if ( yVal !== -1 ) {
						refinedChartData.empty = false;
					}
					refinedChartData.data.push({
						"avg" : getFloatValue( yVal ),
						"time": moment( aAVGData[i]["xVal"] ).format( cfg.dateFormat ),
						"title": "AVG"
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
					"valueAxes": [
						{
							"stackType": "regular",
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
							"balloonText": "[[description]] : [[value]]",
							"legendValueText": "([[description]]) [[value]]",
							"lineColor": "rgb(44, 160, 44)",
							"fillColor": "rgb(44, 160, 44)",
							"title": "AVG",
							"descriptionField": "title",
							"valueField": "avg",
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
				}
			};
			function getFloatValue( val ) {
				return angular.isNumber( val ) ? val.toFixed(2) : 0.00;
			}
			function convertWithUnits(value) {
				var units = [ "ms", "sec", "min" ];
				var result = value;
				var index = 0;
				while ( result > 1000 ) {
					index++;
					result /= 1000;
				}
				return result + units[index] + " ";
			}
		}
	]);
})();
