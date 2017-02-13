<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>

  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
    <title>amCharts Data Loader Example</title>
    <script src="http://www.amcharts.com/lib/3/amcharts.js"></script>
    <script src="http://www.amcharts.com/lib/3/serial.js"></script>
    <script src="../dataloader.min.js"></script>
    <style>
    body, html {
      font-family: Verdana;
      font-size: 12px;
    }
    #chartdiv {
      width: 100%;
      height: 500px;
    }
    </style>
    <script>
    var chart = AmCharts.makeChart("chartdiv", {
     "type": "serial",
      "dataLoader": {
        "url": "data/serial.json",
        "format": "json",
        "showErrors": true,
        "noStyles": true,
        "async": true,
        "load": function( options, chart ) {
          // Here the data is already loaded and set to the chart.
          // We can iterate through it and add proper graphs
          for ( var key in chart.dataProvider[ 0 ] ) {
            if ( chart.dataProvider[ 0 ].hasOwnProperty( key ) && key != chart.categoryField ) {
              var graph = new AmCharts.AmGraph();
              graph.valueField = key;
              graph.type = "line";
              graph.title = key,
              graph.lineThickness = 2;
              chart.addGraph( graph );
            }
          }
        }
      },
     "rotate": false,
     "marginTop": 10,
     "categoryField": "year",
     "categoryAxis": {
       "gridAlpha": 0.07,
       "axisColor": "#DADADA",
       "startOnAxis": false,
       "title": "Year",
       "guides": [{
         "category": "2001",
         "lineColor": "#CC0000",
         "lineAlpha": 1,
         "dashLength": 2,
         "inside": true,
         "labelRotation": 90,
         "label": "fines for speeding increased"
       }, {
         "category": "2007",
         "lineColor": "#CC0000",
         "lineAlpha": 1,
         "dashLength": 2,
         "inside": true,
         "labelRotation": 90,
         "label": "motorcycle fee introduced"
       }]
     },
     "valueAxes": [{
       "stackType": "regular",
       "gridAlpha": 0.07,
       "title": "Traffic incidents"
     }],
     "graphs": [],
     "legend": {
       "position": "bottom",
       "valueText": "[[value]]",
       "valueWidth": 100,
       "valueAlign": "left",
       "equalWidths": false,
       "periodValueText": "total: [[value.sum]]"
     },
     "chartCursor": {
       "cursorAlpha": 0
     },
     "chartScrollbar": {
       "color": "FFFFFF"
     }

    });
    </script>
  </head>

  <body>
    <div id="chartdiv"></div>
  </body>

</html>