# amCharts Data Loader

Version: 1.0.16


## Description

By default all amCharts libraries accept data in JSON format. It needs to be 
there when the web page loads, defined in-line or loaded via custom code.

This plugin introduces are native wrapper that enables automatic loading of data
from external data data sources in CSV and JSON formats.

Most of the times you will just need to provide a URL of the external data 
source - static file or dynamically generated - and it will do the rest.


## Important notice

Due to security measures implemented in most of the browsers, the external data 
loader will work only when the page with the chart or map is loaded via web 
server.

So, any of the examples loaded locally (file:///) will not work.

The page needs to be loaded via web server (http://) in order to work properly.

Loading data from another domain than the web page is loaded is possible but is 
a subject for `Access-Control-Allow-Origin` policies defined by the web server 
you are loading data from.

For more about loading data across domains use the following thread:
http://stackoverflow.com/questions/1653308/access-control-allow-origin-multiple-origin-domains


## Usage

### 1) Include the minified version of file of this plugin. I.e.:

```
<script src="amcharts/plugins/dataloader/dataloader.min.js" type="text/javascript"></script>
```

(this needs to go after all the other amCharts includes)

### 2) Add data source properties to your chart configuration.

Regular (Serial, Pie, etc.) charts:

```
AmCharts.makeChart( "chartdiv", {
  ...,
  "dataLoader": {
    "url": "data.json",
    "format": "json"
  }
} );
```

Stock chart:

```
AmCharts.makeChart( "chartdiv", {
  ...,
  "dataSets": [{
    ...,
    "dataLoader": {
      "url": "data.csv",
      "format": "csv",
      "delimiter": ",",       // column separator
      "useColumnNames": true, // use first row for column names
      "skip": 1               // skip header row
    }
  }]
} );
```

That's it. The plugin will make sure the files are loaded and dataProvider is 
populated with their content *before* the chart is built.

Some formats, like CSV, will require additional parameters needed to parse the 
data, such as "separator".

If the "format" is omitted, the plugin will assume JSON.

### Using in object-based chart setup

If you’re still using object-based chart setup, assign dataLoader-related config object to chart object’s `dataLoader` property:

```
var chart = new AmCharts.AmSerialChart();
...
chart["dataLoader"] = {
  "url": "data.csv",
  "format": "csv",
  "delimiter": ",",
  "useColumnNames": true,
  "skip": 1
};
```


## Complete list of available dataLoader settings

Property | Default | Description
-------- | ------- | -----------
async | true | If set to false (not recommended) everything will wait until data is fully loaded
complete | | Callback function to execute when loader is done
delimiter | , | [CSV only] a delimiter for columns (use \t for tab delimiters)
emptyAs | undefined | [CSV only] replace empty columns with whatever is set here
error | | Callback function to execute if file load fails
init | | Callback function to execute when Data Loader is initialized, before any loading starts
format | json | Type of data: json, csv
headers | | An array of objects with two properties (key and value) to attach to HTTP request
load | | Callback function to execute when file is successfully loaded (might be invoked multiple times)
noStyles | false | If set to true no styles will be applied to "Data loading" curtain
numberFields | | [CSV only] An array of fields in data to treat as numbers
postProcess | | If set to function reference, that function will be called to "post-process" loaded data before passing it on to chart. The handler function will receive two parameters: loaded data, Data Loader options
progress | | Set this to function reference to track progress of the load. The function will be passed in three parameters: global progress, individual file progress, file URL.
showErrors | true | Show loading errors in a chart curtain
showCurtain | true| Show curtain over the chart area when loading data
reload | 0 | Reload data every X seconds
reverse | false | [CSV only] add data points in revers order
skip | 0 | [CSV only] skip X first rows in data (includes first row if useColumnNames is used)
skipEmpty | true | [CSV only] Ignore empty lines in data
timestamp | false | Add current timestamp to data URLs (to avoid caching)
useColumnNames | false | [CSV only] Use first row in data as column names when parsing


## Using in JavaScript Stock Chart

In JavaScript Stock Chart it works exactly the same as in other chart types, 
with the exception that `dataLoader` is set as a property to the data set 
definition. I.e.:

```
var chart = AmCharts.makeChart("chartdiv", {
  "type": "stock",
  ...
  "dataSets": [{
    "title": "MSFT",
      "fieldMappings": [{
        "fromField": "Open",
        "toField": "open"
      }, {
        "fromField": "High",
        "toField": "high"
      }, {
        "fromField": "Low",
        "toField": "low"
      }, {
        "fromField": "Close",
        "toField": "close"
      }, {
        "fromField": "Volume",
        "toField": "volume"
      }],
      "compared": false,
      "categoryField": "Date",
      "dataLoader": {
        "url": "data/MSFT.csv",
        "format": "csv",
        "showCurtain": true,
        "showErrors": true,
        "async": true,
        "reverse": true,
        "delimiter": ",",
        "useColumnNames": true
      }
    }
  }]
});
```

### Can I also load event data the same way?

Sure. You just add a `eventDataLoader` object to your data set. All the same 
settings apply.


## Adding custom headers to HTTP requests

If you want to add additional headers to your data load HTTP requests, use
"headers" array. Each header is an object with two keys: "key" and "value":

```
"dataLoader": {
  "url": "data/serial.json",
  "format": "json",
  "headers": [{
    "key": "x-access-token",
    "value": "123456789"
  }]
}
```


## Manually triggering a reload of all data

Once chart is initialized, you can trigger the reload of all data manually by
calling `chart.dataLoader.loadData()` function. (replace "chart" with the actual
variable that holds reference to your chart object)

## Using callback functions

Data Loader can call your own function when certain event happens, like data
loading is complete, error occurs, etc.

To set custom event handlers, use these config options:

* "complete"
* "init"
* "load"
* "error"
* "progress"

Example:

```
AmCharts.makeChart( "chartdiv", {
  ...,
  "dataSets": [{
    ...,
    "dataLoader": {
      "url": "data.json",
      "init": function ( options, chart ) {
        console.log( 'Loading started' );
      },
      "load": function ( options, chart ) {
        console.log( 'Loaded file: ' + options.url );
      },
      "complete": function ( chart ) {
        console.log( 'Woohoo! Finished loading' );
      },
      "error": function ( options, chart ) {
        console.log( 'Ummm something went wrong loading this file: ' + options.url );
      },
      "progress": function( totalPercent, filePercent, url ) {
        console.log( 'Total percent loaded: ' + Math.round( totalPercent ) );
      }
    }
  }]
} );
```

## Using Data Loader's standalone functions

Data Loader's load and parsing functions are available for external standalone use.

The three available functions are as follows:

Function | Parameters | Description
-------- | ---------- | -----------
AmCharts.loadFile() | url, options, callback | Loads the file and passes it into callback function (unparsed)
AmCharts.parseCSV() | data, options | Parses data in string CSV format and returns JavaScript Array
AmCharts.parseJSON() | data | Parses data in string JSON format and returns JavaScript Array

The options passed into standalone functions are the same as discussed in [Complete list of available dataLoader settings](#complete-list-of-available-dataloader-settings) chapter.

### JSON Example

```
AmCharts.loadFile(dataset_url, {}, function(data) {
  var chartData = AmCharts.parseJSON(data);
  console.log(chartData); // this will output an array
});
```

### CSV Example

```
AmCharts.loadFile(dataset_url, {}, function(data) {
  var chartData = AmCharts.parseCSV(data, {
    "delimiter": ",",
    "useColumnNames": true
  });
  console.log(chartData); // this will output an array
});
```

## Translating into other languages

Depending on configuration options the plugin will display a small number of 
text prompts, like 'Data loading...'.

Plugin will try matching chart's `language` property and display text prompts in 
a corresponding language. For that the plugin needs to have the translations.

Some of the plugin translations are in **lang** subdirectory. Simply include the 
one you need.

If there is no translation to your language readily available, just grab en.js, 
copy it and translate.

The structure is simple:

```
'The phrase in English': 'Translation'
```

The phrase in English must be left intact.

When you're done, you can include your language as a JavaScript file.

P.S. send us your translation so we can include it for the benefits of other 
users. Thanks!


## Requirements

This plugin requires at least 3.13 version of JavaScript Charts, JavaScript
Stock Chart or JavaScript Maps.


## Demos

They're all in subdirectory /examples.


## Extending this plugin

You're encouraged to modify, extend and make derivative plugins out of this
plugin.

You can modify files, included in this archive or, better yet, fork this project
on GitHub:

https://github.com/amcharts/dataloader

We're curious types. Please let us know (contact@amcharts.com) if you do create
something new out of this plugin.


## License

This plugin is licensed under Apache License 2.0.

This basically means you're free to use or modify this plugin, even make your
own versions or completely different products out of it.

Please see attached file "license.txt" for the complete license or online here:

http://www.apache.org/licenses/LICENSE-2.0


## Contact us

* Email:contact@amcharts.com
* Web: http://www.amcharts.com/
* Facebook: https://www.facebook.com/amcharts
* Twitter: https://twitter.com/amcharts


## Changelog

### 1.0.16
* Added "numberFields" config array

### 1.0.15
* Added "emptyAs" config property. Empty CSV values will be set to this (default `undefined`)

### 1.0.14
* Added "init" event handler, which is called **before** loading starts

### 1.0.13
* Added "progress" handler, which can be used to monitor data load progress

### 1.0.12
* Better default options handling in external calls to AmCharts.loadFile
* Fixed the latest version of Stock Chart not resetting to default pre-defined period
* New example: Using Data Loader functions externally (map_json_external_function.html)

### 1.0.11
* New translation: Added French translation. Thanks Remy!
* Tweaks to allow better animation after data load on Pie chart

### 1.0.10
* Fixed error related to headers not being set when using standalone data load functions

### 1.0.9
* Plugin will now ignore empty CSV lines by default (configurable with `skipEmpty` property)

### 1.0.8
* Added `headers` config variable which allows adding custom headers to HTTP requests

### 1.0.7
* Fixed an issue with the Pie chart when it is being loaded in inactive tab

### 1.0.6
* Added support for Gauge chart (loads `arrows` array)

### 1.0.5
* Fixed JS error if periodSelector was not defined in chart config
* Now all callback functions (complete, error, load) receive additional parameter: chart
* postProcess function will now have "this" context set to Data Loader object as well as receive chart reference as third paramater

### 1.0.4
* Added `chart.dataLoader.loadData()` function which can be used to manually trigger all data reload

### 1.0.3
* Fixed the bug where defaults were not being applied properly
* Fixed the bug with translations not being applied properly
* Cleaned up the code (to pass JSHint validation)

### 1.0.2
* Fixed the issue with modified Array prototypes

### 1.0.1
* Added `complete`, `load` and `error` properties that can be set with function handlers to be invoked on load completion, successful file load or failed load respectively
* Fixed language container initialization bug
* Fixed bug that was causing parse errors not be displayed

### 1.0
* Added GANTT chart support

### 0.9.2
* Added global data load methods that can be used to load and parse data by code outside plugin
* Trim CSV column names
* Translation added: Lithuanian

### 0.9.1
* Fix chart animations not playing after asynchronous load

### 0.9
* Initial release