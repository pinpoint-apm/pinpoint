(function() {
	'use strict';

	pinpointApp.constant( "ActiveThreadChartDaoServiceConfig", {
		dateFormat: "YYYY-MM-DD HH:mm:ss"
	});

	pinpointApp.service( "ActiveThreadChartDaoService", [ "ActiveThreadChartDaoServiceConfig",
		function ActiveThreadChartDaoService( cfg ) {

			this.parseData = function( aChartData ) {
				var aActiveTraceFastData = aChartData.charts[ "ACTIVE_TRACE_FAST" ].points;
				var aActiveTraceNormal = aChartData.charts[ "ACTIVE_TRACE_NORMAL" ].points;
				var aActiveTraceSlow = aChartData.charts[ "ACTIVE_TRACE_SLOW" ].points;
				var aActiveTraceVerySlow = aChartData.charts[ "ACTIVE_TRACE_VERY_SLOW" ].points;
				var refinedChartData = {
					data: [],
					empty: true,
					defaultMax: 10
				};

				for ( var i = 0 ; i < aActiveTraceFastData.length ; i++ ) {
					var obj = {
						"time": moment(aActiveTraceFastData[i]["xVal"]).format(cfg.dateFormat)
					};

					var fast = aActiveTraceFastData[i]["avgYVal"];
					var normal = aActiveTraceNormal[i]["avgYVal"];
					var slow =  aActiveTraceSlow[i]["avgYVal"];
					var verySlow =  aActiveTraceVerySlow[i]["avgYVal"];
					if ( fast !== -1 || normal !== -1 || slow !== -1 || verySlow !== -1 ) {
						refinedChartData.empty = false;
					}
					obj.fast = getFloatValue( fast );
					obj.fastTitle = aActiveTraceFastData[i].title;
					obj.normal = getFloatValue( normal );
					obj.normalTitle = aActiveTraceNormal[i].title;
					obj.slow = getFloatValue( slow );
					obj.slowTitle = aActiveTraceSlow[i].title;
					obj.verySlow = getFloatValue( verySlow );
					obj.verySlowTitle = aActiveTraceVerySlow[i].title;
					refinedChartData.data.push( obj );
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
				return angular.isNumber( val ) ? val.toFixed(2) : 0.00;
			}
		}
	]);
})();
