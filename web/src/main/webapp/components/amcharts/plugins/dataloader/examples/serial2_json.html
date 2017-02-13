<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>

  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
    <title>amCharts Data Loader Example</title>
    <script src="http://www.amcharts.com/lib/3/amcharts.js"></script>
    <script src="http://www.amcharts.com/lib/3/serial.js"></script>
    <script src="http://www.amcharts.com/lib/3/themes/dark.js"></script>
    <script src="../dataloader.min.js"></script>
    <style>
    body, html {
      font-family: Verdana;
      font-size: 12px;
      background-color:#282828;
    }
    #chartdiv {
      width: 100%;
      height: 500px;
    }
    </style>
    <script>
      var chart = AmCharts.makeChart("chartdiv", {
        "type": "serial",
        "theme": "dark",
        "dataLoader": {
          "url": "data/serial2.json",
          "showErrors": true,
          "complete": function ( chart ) {
            console.log( "Loading complete" );
          },
          "load": function ( options, chart ) {
            console.log( "File loaded: ", options.url );
          },
          "error": function ( options, chart ) {
            console.log( "Error occured loading file: ", options.url );
          }
        },
        "categoryField": "year",
        "startDuration": 1,
        "rotate": true,
        "categoryAxis": {
          "gridPosition": "start"
        },
        "valueAxes": [{
          "position": "top",
          "title": "Million USD",
          "minorGridEnabled": true
        }],
        "graphs": [{
          "type": "column",
          "title": "Income",
          "valueField": "income",
          "fillAlphas":1,
          "balloonText": "<span style='font-size:13px;'>[[title]] in [[category]]:<b>[[value]]</b></span>"
        }, {
          "type": "line",
          "title": "Expenses",
          "valueField": "expenses",
          "lineThickness": 2,
          "bullet": "round",
          "balloonText": "<span style='font-size:13px;'>[[title]] in [[category]]:<b>[[value]]</b></span>"
        }],
        "legend": {
          "useGraphSettings": true
        },
        "creditsPosition": "top-right",
        "responsive": {
          "enabled": true
        }
      });

      function reloadData() {
        chart.dataLoader.loadData();
      }
    </script>
  </head>

  <body>
    <div id="chartdiv"></div>
    <input type="button" value="Trigger data reload" onclick="reloadData();" />
  </body>

</html>