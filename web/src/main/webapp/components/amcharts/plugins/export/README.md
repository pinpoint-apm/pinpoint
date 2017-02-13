# amCharts Export

Version: 1.4.56


## Description

This plugin adds export capabilities to all amCharts products - charts and maps.

It allows annotating and exporting chart or related data to various bitmap,
vector, document or data formats, such as PNG, JPG, PDF, SVG, JSON, XLSX and
many more.


## Important notice

Please note that due to security measures implemented in modern browsers, some
or all export options might not work if the web page is loaded locally (via
file:///) or contain images loaded from different host than the web page itself.


## Usage

### 1) Include the minified version of file of this plugin as well as the
bundled CSS file. I.e.:

```
<script src="amcharts/plugins/export/export.min.js"></script>
<link  type="text/css" href="amcharts/plugins/export/export.css" rel="stylesheet">
```

Or if you'd rather use amCharts CDN:

```
<script src="//cdn.amcharts.com/lib/3/plugins/export/export.min.js"></script>
<link  type="text/css" href="//cdn.amcharts.com/lib/3/plugins/export/export.css" rel="stylesheet">
```

(this needs to go after all the other amCharts includes)

### 2) Enable `export` with default options:

```
AmCharts.makeChart( "chartdiv", {
  ...,
  "export": {
    "enabled": true
  }
} );
```

### ... OR set your own custom options:

```
AmCharts.makeChart( "chartdiv", {
  ...,
  "export": {
    "enabled": true,
    "menu": [ {
      "class": "export-main",
      "menu": [ {
        "label": "Download",
        "menu": [ "PNG", "JPG", "CSV" ]
      }, {
        "label": "Annotate",
        "action": "draw",
        "menu": [ {
          "class": "export-drawing",
          "menu": [ "PNG", "JPG" ]
        } ]
      } ]
    } ]
  }
} );
```


## Loading external libraries needed for operation of this plugin

The plugin relies on a number of different libraries, to export images, draw
annotations or generate download files.

Those libraries need to be loaded for the plugin to work properly.

There are two ways to load them. Choose the one that is right:

### 1) Automatic (preferred)

All libraries required for plugin operation are included withing plugins */libs*
subdirectory.

The plugin will automatically try to look in chart's [`path`](http://docs.amcharts.com/3/javascriptcharts/AmSerialChart#path)
property. If your plugin files are located within plugins folder under amcharts
(as is the case with the default distributions), you don't need to do anything -
the libraries will load on-demand.

If you are using relative url, note that it is relative to the web page you are
displaying your chart on, not the export.js library.

In case you've moved the libs folder you need to tell the plugin where it is
`"libs": { "path": "../libs/" }`

### 2) Manual

You can also load all those JavaScript libraries by `<script>` tags. Since
loading of libraries is on by default you will need to turn it off by setting
`"libs": { "autoLoad": false }`

Here is a full list of the files that need to be loaded for each operation:

File | Located in | Required for
---- | ---------- | ------------
fabric.min.js | libs/fabric.js/ | Any export operation
FileSaver.js | libs/FileSaver.js/ | Used to offer download files
pdfmake.min.js | libs/pdfmake/ | Export to PDF format
vfs_fonts.js | libs/pdfmake/ | Export to PDF format
jszip.js | libs/jszip/ | Export to XLSX format
xlsx.js | libs/xlsx/ | Export to XLSX format


## Complete list of available export settings

Property | Default | Description
-------- | ------- | -----------
backgroundColor | #FFFFFF | RGB code of the color for the background of the exported image
enabled | true | Enables or disables export functionality
divId | | ID or a reference to div object in case you want the menu in a separate container.
fabric | {} | Overwrites the default drawing settings (fabricJS library)
fallback | {} | Holds the messages to guide the user to copy the generated output; `false` will disable the fallback feature
fileName | amCharts | A file name to use for generated export files (an extension will be appended to it based on the export format)
legend | {} | Places the legend in case it is within an external container ([skip to chapter](#adding-external-legend))
libs | | 3rd party required library settings (see the above section)
menu | [] | A list of menu or submenu items (see the next chapter for details)
pdfMake | {} | Overwrites the default settings for PDF export (pdfMake library)
position | top-right | A position of export icon. Possible values: "top-left", "top-right" (default), "bottom-left", "bottom-right"
removeImages | true | If true export checks for and removes "tainted" images that area lodead from different domains
delay | | General setting to delay the capturing of the chart ([skip to chapter](#delay-the-capturing-before-export))
exportFields | [] | If set, only fields in this array will be exported ( data export only )
exportTitles | false | Exchanges the data field names with it's dedicated title ( data export only )
columnNames | {} | An object of key/value pairs to use as column names when exporting to data formats. `exportTitles` needs to be set for this to work as well.
exportSelection | false | Exports the current data selection only ( data export only )
dataDateFormat | | Format to convert date strings to date objects, uses by default charts dataDateFormat ( data export only )
dateFormat | YYYY-MM-DD | Formats the category field in given date format ( data export only )
keyListener | false | If true it observes the pressed keys to undo/redo the annotations
fileListener | false | If true it observes the drag and drop feature and loads the dropped image file into the annotation
drawing | {} | Object which holds all possible settings for the annotation mode ([skip to chapter](#annotation-settings))
overflow | true | Flag to overwrite the css attribute 'overflow' of the chart container to avoid cropping the menu on small container
border | {} | An object of key/value pairs to define the overlaying border
processData | | A function which can be used to change the dataProvider when exporting to CSV, XLSX, or JSON
pageOrigin | true | A flag to show / hide the origin of the generated PDF ( pdf export only )
forceRemoveImages | false | If true export removes all images
debug | false | A flag which enables the plugin to write console logs (currently used by the dependency handler only).


## Configuring export menu

Plugin includes a way to completely control what is displayed on export menu.
You can set up menus, sub-menus, down to any level. You can even add custom
items there that execute your arbitrary code on click. It's so configurable
it makes us sick with power ;)

The top-level menu is configured via `menu` property under `export`. It should
always be an array, even if you have a single item in it.

The array items could be either objects or format codes. Objects will allow you
to specify labels, action, icon, child items and even custom code to be executed
on click.

Simple format codes will assume you need an export to that format.

### Simple menu setup

Here's a sample of the simple menu setup that allows export to PNG, JPG and CSV:

```
"export": {
  "enabled": true,
  "menu": [ {
    "class": "export-main",
    "menu": [ "PNG", "JPG", "CSV" ]
  } ]
}
```

The above will display a menu out of three options when you hover on export
icon:

* PNG
* JPG
* CSV

When clicked the plugin will trigger export to a respective format.

If that is all you need, you're all set.

Please note that we have wrapped out menu into another menu item, so that only
the icon is displayed until we roll over the icon. This means that technically
we have a two-level hierarchical menu.

If we opmitted that first step, the chart would simply display a format list
right there on the chart.

### Advanced menu setup

However, you can do so much more with the menu.

Let's add more formats and organize image and data formats into separate
submenus.

To add a submenu to a menu item, simply add a `menu` array as its own property:

```
"export": {
  "enabled": true,
  "menu": [ {
    "class": "export-main",
    "menu": [ {
      "label": "Download as image",
      "menu": [ "PNG", "JPG", "SVG" ]
    }, {
      "label": "Download data",
      "menu": [ "CSV", "XLSX" ]
    } ]
  } ]
}
```

Now we have a hierarchical menu with the following topology:

* Download as image
  * PNG
  * JPG
  * SVG
* Download data
  * CSV
  * XLSX

We can mix "string" and "object" formats the way we see fit, i.e.:

```
"export": {
  "menu": [
    "PNG",
    { "label": "JPEG",
      "format": "JPG" },
    "SVG"
  ]
}
```

The magic does not end here, though.

### Adding custom click events to menu items

Just like we set `label` and `format` properties for menu item, we can set
`click` as well.

This needs to be a function reference. I.e.:

```
"export": {
  "menu": [
    "PNG",
    { "label": "JPEG",
      "click": function () {
        alert( "Clicked JPEG. Wow cool!" );
      } },
    "SVG"
  ]
}
```

### Adding external legend

In case you have an external legend you need to define the position where it should get placed in your export.
By default it obtains the dimensions from the container but you can optionally overwrite those settings as shown below.

```
"export": {
  "legend": {
    "position": "top",  // or "right", "bottom" and "left" are possible
    "width": 200,       // optional
    "height": 200       // optional
  }
}
```

### Changing the dataProvider when exporting

If you want to change the dataProvider when exporting to CSV, XLSX, or JSON, you can use the `processData` option:

```
"export": {
  "processData": function (data, cfg) {
    return data.slice(1, -1);
  }
}
```

### Adding overlaying border

In case you need a more visible separation of your chart for further processing you can add an overlaying border.

```
"export": {
  "border": {
    "stroke": "#000000",  // HEX-CODE to define the border color
    "strokeWidth": 1,     // number which represents the width in pixel
    "strokeOpacity": 1    // number which controls the opacity from 0 - 1
  }
}
```

### Menu item reviver

By passing the `menuReviver` callback function you can modify the resulting menu
item or relative container, before it gets appended to the list (`ul`). The
function takes two arguments and it needs to return a valid DOM element.

```
"export": {
  "menuReviver": function(item,li) {
    li.setAttribute("class","something special");
    return li;
  }
}
```

#### Format specific options that you can override using Menu item reviver

Some formats, such as CSV, have specific parameters that are used when exporting to this format. For example, default column separator for CSV is a comma. But what if you would like to be that a tab? You could use `menuReviver` for that like this:

```
"export": {
  "enabled": true,
  "menuReviver": function(cfg,li) {
    if ( cfg.format == "CSV" ) {
      cfg.delimiter = "\t";
    }
    return li;
  }
}
```

Below you will find a list of parameters that you can override for each format:

**JPG**

Property | Default | Available values | Description
--------- | ------- | ---------------- | -----------
quality |1 | 0-1 | A quality of the resulting JPG image
multiplier | 1 | number | Set this to non-1 number to resize the resulting image by

**PNG**

Property | Default | Available values | Description
--------- | ------- | ---------------- | -----------
quality | 1 | 0-1 | A quality of the resulting JPG image
multiplier | 1 | number | Set this to non-1 number to resize the resulting image by

**PDF**

Property | Default | Available values | Description
--------- | ------- | ---------------- | -----------
multiplier | 2 | number | Set this to non-1 number to resize the resulting image by

**PRINT**

Property | Default | Available values | Description
--------- | ------- | ---------------- | -----------
delay | 1 | number | Delay by number of seconds before triggering print
lossless | false | true/false | Enable or disable image optimization when printing

**CSV**

Property | Default | Available values | Description
--------- | ------- | ---------------- | -----------
delimiter | "," | string | A string to use as a column delimiter
quotes | true | true/false | Set whether to enclose strings in doublequotes
escape | true | true/false | Set whether to escape strings
withHeader | true | true/false | Add header row with column names

**XLSX**

Property | Default | Available values | Description
--------- | ------- | ---------------- | -----------
dateFormat | "dateObject" | "dateObject"\|"string" | Whether to export dates as dates recognisable by Excel or formatted as strings
withHeader | true | true/false | Add header row with column names
stringify | false | true/false | Convert all cell content to strings


### Menu walker

In case you don't like our structure, go ahead and write your own recursive
function to create the menu by the given list configured through `menu`.

```
"export": {
  "menuWalker": function(list,container) {
    // some magic to generate the nested lists using the given list
  }
}
```

### Printing the chart

Adding menu item to print the chart or map is as easy as adding export ones. You
just use "PRINT" as `format`. I.e.:

```
"export": {
  "menu": [
    "PNG",
    "SVG",
    "PRINT"
  ]
}
```

Or if you want to change the label:

```
"export": {
  "menu": [
    "PNG",
    "SVG",
    {
      "format": "PRINT",
      "label": "Print Chart"
    }
  ]
}
```

### Annotating the chart before export

OK, this one is so cool, you'll need a class 700 insulation jacket.

By default each menu item triggers some kind of export. You can trigger an
"annotation" mode instead by including `"action": "draw"` instead.

```
"export": {
  "menu": [ {
    "class": "export-main",
    "menu": [ {
      "label": "Download",
      "menu": [ "PNG", "JPG", "CSV", "XLSX" ]
    }, {
      "label": "Annotate",
      "action": "draw"
    } ]
  } ]
}
```

Now, when you click on the "Annotate" item in the menu, the chart will turn into
an image editor which you can actual draw on and the menu gets replaced by the
default annotation menu.

If you don't like the detault annotation menu, you can define your own:

```
"export": {
  "menu": [ {
    "class": "export-main",
    "menu": [ {
      "label": "Download",
      "menu": [ "PNG", "JPG", "CSV", "XLSX" ]
    }, {
      "label": "Annotate",
      "action": "draw",
      "menu": [ {
        "class": "export-drawing",
        "menu": [ "JPG", "PNG", "SVG", "PDF" ]
      } ]
    } ]
  } ]
}
```

Now, when you turn on the annotation mode, your own submenu will display,
allowing to export the image into either PNG, JPG, SVG or PDF.

And that's not even the end of it. You can add menu items to cancel, undo, redo
and still be able to reuse the choices by using the actions `draw.modes`,
`draw.widths`, `draw.colors` or `draw.shapes`.

```
"export": {
  "menu": [ {
    "class": "export-main",
    "menu": [ {
      "label": "Download",
      "menu": [ "PNG", "JPG", "CSV", "XLSX" ]
    }, {
      "label": "Annotate",
      "action": "draw",
      "menu": [ {
        "class": "export-drawing",
        "menu": [ {
            label: "Size ...",
            action: "draw.widths",
            widths: [ 5, 20, 30 ] // replaces the default choice
        }, {
          "label": "Edit",
          "menu": [ "UNDO", "REDO", "CANCEL" ]
        }, {
          "label": "Save",
          "format": "PNG"
        } ]
      } ]
    } ]
  } ]
}
```

### Annotation settings

Since 1.2.1 it's also possible to set some of the annotation options without the
need to re-define the whole menu structure. You can easily adjust the choice of
modes, colors, widths or shapes, and set the defaults when entering the
annotation mode.

Following setup shows you all available settings. If you don't have the
`drawing` property at all, it will falls back to the defaults.

```
"export": {
  "drawing": {
    "enabled": true, // Flag for `action: "draw"` menu items to toggle it's visibility
    "shapes": [ "test.svg" ], // Choice of shapes offered in the menu (shapes are being loaded from the shapes folder)

    "width": 2, // Width of the pencil and line when entering the annotation mode
    "widths": [ 2, 10, 20 ], // Choice of widths offered in the menu

    "color": "#000000", // Color of the pencil, line, text and shapes when entering the annotation mode
    "colors": [ "#000000", "#FF0000" ] // Choice of colors offered in the menu

    "opacity": 1, // Opacity of the pencil, line, text and shapes when entering the annotation mode
    "opacities": [ 1, 0.8, 0.6, 0.4, 0.2 ] // Choice of opacity offered in the menu

    "menu": [ ... ], // Shown menu when entering the annotation mode

    "mode": "pencil", // Drawing mode when entering the annotation mode "pencil", "line" and "arrow" are available
    "modes": [ "pencil" , "line", "arrow" ], // Choice of modes offered in the menu
    "arrow": "end", // position of the arrow on drawn lines; "start","middle" and "end" are available

    "autoClose": true // Flag to automatically close the annotation mode after download
  }
}
```

If you need to filter the drawn elements, you can pass the `reviver` method in
your global configuration, or pass it to the `capture` method if you export
manually. For example, to hide all free labels you can simply do something like
the following:

```
"export": {
  "menu": ["PNG"],
  "reviver": function(obj) {
    if ( obj.className == "amcharts-label" ) {
      obj.opacity = 0;
    }
  }
}
```

### Delay the capturing before export

In some cases you may want to delay the capturing of the current chart snapshot
to highlight the current value. For this you can simply define the 'delay'
property in your menu item:

```
"export": {
  "delay": 3,

  // or specifically on individual menu items

  "menu": [{
    "label": "PNG",
    "format": "PNG",
    "delay": 3
  }]
}
```

### Events

Since version 1.1.7 the plugin introduces some events you can use. For example
the `afterCapture` event allows you to add some texts or images which can't be
seen on the regular chart but only the generated export. Use it to watermark
your exported images.

```
"export": {
  "afterCapture": function(menuConfig) {
      var text = new fabric.Text("This is shown on exported image only", {
        top: 50,
        left: 100,
        family: this.setup.chart.fontFamily,
        fontSize: this.setup.chart.fontSize * 2
      });
      this.setup.fabric.add(text);
  },

  // or specifically on individual menu items

  "menu": [{
    "label": "PNG",
    "format": "PNG",  
    "afterCapture": function(menuConfig) {
        var text = new fabric.Text("This is shown on exported image only", {
          top: 50,
          left: 100,
          family: this.setup.chart.fontFamily,
          fontSize: this.setup.chart.fontSize * 2
        });
        this.setup.fabric.add(text);
    }
  }],
```

Since version 1.4.29 we have added the `onReady` callback to get your stuff done
right after the plugin or specific dependency is ready to use. Ensure to check the
`timedout` flag to be sure the dependency got fully loaded.

```
"export": {
  "onReady": function( type, timedout ) {

    // Plugin ready for data exports
    if ( type == "data" ) {
      this.toCSV( {}, function( data ) {
        // Exported to CSV
      } );

    // Plugin ready for image exports
    } else if ( type == "fabric" && !timedout ) {
      this.capture( {}, function() {
        this.toPNG( {}, function( data ) {
          // Exported to PNG
        } );
      } );
    }
  }
}
```

### A list of the events

Name | Arguments | Description
---- | --------- | -----------
beforeCapture | [menu item setup](#a-list-of-menu-item-properties) | Called before the SVG element gets converted
afterCapture | [menu item setup](#a-list-of-menu-item-properties) | Called right before the passed callback of the capture method


### A list of menu item properties

Property | Description
-------- | -----------
action | Set to "draw" if you want the item to trigger annotation mode
class | Class name applied to the tag
click | Function handler invoked upon click on menu item
format | A format to export chart/map to upon click (see below for a list of available formats)
icon | Icon file (will use chart's [path](http://docs.amcharts.com/3/javascriptcharts/AmSerialChart#path) if the URL is not full)
label | Text label to be displayed
menu | An array of submenu items
title | A title attribute of the link
backgroundColor | The background color of the canvas
fileName | A file name to use for generated export files (an extension will be appended to it based on the export format)
extension | File extension for the generated export file (uses format default if not defined)
mimeType | Internet media type to generate the export file (usses format default if not defined)
pageSize | A string or { width: number, height: number } ([details](#exporting-to-pdf))
pageOrientation | By default we use portrait, you can change it to landscape if you wish ([details](#exporting-to-pdf))
pageMargins | [left, top, right, bottom] or [horizontal, vertical] or just a number for equal margins ([details](#exporting-to-pdf))
content | Array of elements which represents the content ([details](#exporting-to-pdf))
multiplier | Scale factor for the generated image
lossless | Flag to print the actual vector graphic instead of buffered bitmap (print option only, experimental)
delay | A numeric value to delay the capturing in seconds ([details](#delay-the-capturing-before-export))
exportFields | [] | If set, only fields in this array will be exported ( data export only )
exportTitles | Exchanges the data field names with it's dedicated title ( data export only )
columnNames | An object of key/value pairs to use as column names when exporting ( data export only )
exportSelection | Exports the current data selection only ( data export only )
dataDateFormat | Format to convert date strings to date objects, uses by default charts dataDateFormat ( data export only )
dateFormat | Formats the category field in given date format ( data export only )
border | An object of key/value pairs to define the overlaying border
pageOrigin | A flag to show / hide the origin of the generated PDF ( pdf export only )
compress | A flag to compress the generated output ( svg only )

Available `format` values:

* JPG
* PNG
* SVG
* CSV
* JSON
* PDF
* XLSX
* PRINT

### Exporting to PDF

When exporting to PDF, you can set and modify the content of the resulting
document. I.e. add additional text and/or modify image size, etc.

To do that, you can use menu item's `content` property.

Each item in `content` represents either a text line (string) or an exported
image.

To add a text line, simply use a string. It can even be a JavaScript variable or
a function that returns a string.

To include exported image, use `image: "reference"`.

Additionally, you can add `fit` property which is an array of pixel dimensions,
you want the image to be scaled to fit into.

Here's an example of such export menu item:

```
{
  "format": "PDF",
  "content": [ "Saved from:", window.location.href, {
    "image": "reference",
    "fit": [ 523.28, 769.89 ] // fit image to A4
  } ]
}
```

Property | Description
-------- | -----------
pageSize | a string or { width: number, height: number }
pageOrientation | by default we use portrait, you can change it to landscape if you wish
pageMargins | [left, top, right, bottom] or [horizontal, vertical] or just a number for equal margins
content | array of elements which represents the content ([full description](https://github.com/bpampuch/pdfmake/))

Pagesize | Dimensions in pixel
-------- | -----------
4A0 | [4767.87, 6740.79]
2A0 | [3370.39, 4767.87]
A0 | [2383.94, 3370.39]
A1 | [1683.78, 2383.94]
A2 | [1190.55, 1683.78]
A3 | [841.89, 1190.55]
A4 | [595.28, 841.89]
A5 | [419.53, 595.28]
A6 | [297.64, 419.53]
A7 | [209.76, 297.64]
A8 | [147.40, 209.76]
A9 | [104.88, 147.40]
A10 | [73.70, 104.88]
B0 | [2834.65, 4008.19]
B1 | [2004.09, 2834.65]
B2 | [1417.32, 2004.09]
B3 | [1000.63, 1417.32]
B4 | [708.66, 1000.63]
B5 | [498.90, 708.66]
B6 | [354.33, 498.90]
B7 | [249.45, 354.33]
B8 | [175.75, 249.45]
B9 | [124.72, 175.75]
B10 | [87.87, 124.72]
C0 | [2599.37, 3676.54]
C1 | [1836.85, 2599.37]
C2 | [1298.27, 1836.85]
C3 | [918.43, 1298.27]
C4 | [649.13, 918.43]
C5 | [459.21, 649.13]
C6 | [323.15, 459.21]
C7 | [229.61, 323.15]
C8 | [161.57, 229.61]
C9 | [113.39, 161.57]
C10 | [79.37, 113.39]
RA0 | [2437.80, 3458.27]
RA1 | [1729.13, 2437.80]
RA2 | [1218.90, 1729.13]
RA3 | [864.57, 1218.90]
RA4 | [609.45, 864.57]
SRA0 | [2551.18, 3628.35]
SRA1 | [1814.17, 2551.18]
SRA2 | [1275.59, 1814.17]
SRA3 | [907.09, 1275.59]
SRA4 | [637.80, 907.09]
EXECUTIVE | [521.86, 756.00]
FOLIO | [612.00, 936.00]
LEGAL | [612.00, 1008.00]
LETTER | [612.00, 792.00]
TABLOID | [792.00, 1224.00]

## Styling the export menu

The plugin comes with a default CSS file `export.css`. You just need to include
it on your page.

Feel free to override any styles defined in it, create your own version and
modify as you see fit.

If you choose to modify it, we suggest creating a copy so it does not get
overwritten when you update amCharts or plugin.


## Plugin API

We explained how you can define custom functions to be executed on click on
export menu items.

Those functions can tap into plugin's methods to augment it with some custom
functionality.

Here's an example:

```
"export": {
  menu: [ {
    label: "JPG",
    click: function() {
      this.capture({},function() {
        this.toJPG( {}, function( data ) {
          this.download( data, "image/jpg", "amCharts.jpg" );
        });
      });
    }
  } ]
}
```

The above will use plugin's internal `capture` method to capture it's current
state and `toJPG()` method to export the chart to JPEG format.

Yes, you're right, it's the exact equivalent of just including "JPG" string. The
code is here for the explanatory purposes.

Here's a full list of API functions available for your consumption:

Function | Parameters | Description
-------- | ---------- | -----------
toJPG | (object) options, (function) callback | Prepares a JPEG representation of the chart and passes the binary data to the callback function
toPNG | (object) options, (function) callback | Prepares a PNG representation of the chart and passes the binary data to the callback function
toSVG | (object) options, (function) callback | Prepares a SVG representation of the chart and passes the binary data to the callback function
toPDF | (object) options, (function) callback | Prepares a PDF representation of the chart and passes the binary data to the callback function
toJSON | (object) options, (function) callback | Prepares a JSON and passes the plain data to the callback function
toCSV | (object) options, (function) callback | Prepares a CSV and passes the plain data to the callback function
toXLSX | (object) options, (function) callback | Prepares a XLSX representation of the chart and passes the binary data to the callback function
toBlob | (object) options, (function) callback | Prepares a BLOB and passes the instance to the callback function
toCanvas | (object) options, (function) callback | Prepares a Canvas and passes the element to the callback function
toArray | (object) options, (function) callback | Prepares an Array and passes the data to the callback function
toImage | (object) options, (function) callback | Generates an image element which holds the output in an embedded base64 data url

## Annotation API

Since version 1.4.27 we've introduced the functionality to manage the annotations on the fly. The setter returns an array of objects, where each element represents an annotation.
On the other hand the setter processes the given annotations within options (options.data). Both methods support the reviver callback which allows you to modify the annotations if needed.

Function | Parameters | Description
-------- | ---------- | -----------
getAnnotations | (object) options, (function) callback | Returns an array of objects where each element represents an annotation.
setAnnotations | (object) options, (function) callback | Draws the given annotations (options.data).

Here's an example how to insert annotations, please ensure your chart is in annotation mode:

```
chart.export.setAnnotations({

  // Array of annotations, accepts this simple handwritten format or the detailed output of the getter
  data: [{
    top: 200,
    left: 200,
    text: "Test annotation",
    type: "text"
  }],

  // Allows you to modify the annotation before it's being added into the canvas.
  reviver: function(obj,index) {
    obj.fill = "#FF00FF";
  }


},function() {
  // Callback when finished 
});
```

## Fallback for IE9

Unfortunately, Internet Explorer 9 has restrictions in place that prevent the
download of locally-generated files. In this case the plugin will place the
generated image along download instructions directly over the chart area.

To avoid having a bigger payload by including senseless polyfills to your site,
you may need to add following metatag in your `<head>` of your HTML document.

```
<meta http-equiv="X-UA-Compatible" content="IE=edge" />
```

This feature will kick in by default. If you want to disable it simply pass
`false` to the `fallback` parameter.

```
"export": {
  fallback: false
}
```

In case you want to change our default messages you can modify it like
following.

```
"export": {
  fallback: {
    text: "CTRL + C to copy the data into the clipboard.",
    image: "Rightclick -> Save picture as... to save the image."
  }
}
```

## Requirements

This plugin requires at least 3.13 version of JavaScript Charts, JavaScript
Stock Chart or JavaScript Maps.

The export will also need relatively recent browsers.

IE10 and up are supported.

Partial support for IE9; Fallback mechanism.

IE8 and older are not supported I'm afraid. Hey, it's time to upgrade!


## Demos

They're all in subdirectory /examples.


## Extending this plugin

You're encouraged to modify, extend and make derivative plugins out of this
plugin.

You can modify files, included in this archive or, better yet, fork this project
on GitHub:

https://github.com/amcharts/export

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

### 1.4.55
* Added: Dependency handler on export methods, holds the actual call until the namespace shows up in current scope (uses libs.loadTimeout).

### 1.4.55
* Added: Radial gradient issue on pie caused by the vertical gradient fix (v1.4.44)

### 1.4.54
* Fixed: Default text size on width changes

### 1.4.53
* Fixed: IOS print issue, captured the whole page instead of the single page

### 1.4.52
* Fixed: Blank export issue especifically in chrome on IOS devices.
* Updated: FileSaver.js due the chrome export issue on IOS

### 1.4.51
* Fixed: Render issue on maps due half pixel position correction; simplified the positioning.

### 1.4.50
* Fixed: Half pixel positioning issue which caused the blurriness within the canvas and exported image.

### 1.4.49
* Fixed: Custom data export issue through toXSLX and toJSON which results in an empty data sheet.

### 1.4.48
* Fixed: toXLSX issue handling objects/array in data points.

### 1.4.47
* Fixed: `afterCapture` issue which removed injected elements.

### 1.4.46
* Fixed: toSVG clipPath issue which exported multiple times the same clipPath

### 1.4.45
* Fixed: Object selection issue on charts with heavy amount of datapoints
* Fixed: Line/Arrow drawing issue on vertical movement
* Changed: Pressing escape while an object has been selected will deselect first instead quiting the annotation mode

### 1.4.44
* Fixed: Vertical gradient issue

### 1.4.43
* Fixed: Shown label in hidden valueAxis

### 1.4.42
* Fixed: `exportFields` issue in combination with `columnNames` or `exportTitles`
* Added: `dataFieldsTitlesMap` into `processData` context to be able to trace back the translated keys against the data fields

### 1.4.41
* Added: quote, escape option to `toArray` method
* Fixed: toArray method to respect `exportFields` order
* Fixed: toCSV, toXLSX to respect `exportFields` order

### 1.4.40
* Fixed: Infinite loop in Angular2 Zones
* Fixed: `compress` option being obtained from the global config

### 1.4.39
* Added: New menu option `compress` to compress the generated output (svg only).
* Fixed: Strikethrough issue in SVG output.

### 1.4.38
* Fixed: Support for external stock chart legends, overlapping issue with free licensed version of amcharts

### 1.4.37
* Fixed: Gradient issue which left the chart elements hidden after the export process
* Fixed: Typo in examples dropdown
* Added: Support for external stock chart legends

### 1.4.36
* Fixed: exportFields order being considered
* Fixed: Keep scroll position after printing
* Fixed: Namespace key issue with minified resource versions

### 1.4.35
* Fixed: Menu handling issue on touch devices, uses css classname to toggle menu items (updated CSS file, on devices only where the "Touch" object is within window scope)

### 1.4.34
* Fixed: Data shifting issue in data exports with compared graphs (stock only)
* Fixed: Shallow copy of compared graphs in data exports (stock only)

### 1.4.33
* Fixed: fill/stroke polyfilling issue on svg elements with color validation/preparation for fabric 

### 1.4.32
* Fixed: Issue polyfilling the color attributes with "rgba" color codes

### 1.4.31
* Changed: Included independent IE detection to handle specific IE10, IE11 svg image in canvas issue

### 1.4.30
* Fixed: Pattern loading, positioning issue, supports x,y offset now

### 1.4.29
* Added: `libs.loadTimeout` dependency namespace timeout used within onReady handler
* Added: `fabric.loadTimeout` loading image timeout to avoid blocking the export process
* Added: [onReady](#events) ready callback handler to get notified when the export or specific dependency is ready to use
* Fixed: fill/stroke issue on SVG elements which caused a crash within the export process
* Fixed: Image loader which freezed occasionally and caused an unexpected behaviour

### 1.4.28
* Fixed: Positioning / handling issue on multiline labels (injected modifed fabricJS snippet to handle it)
* Fixed: Cursor issue on regular exports which flashed the crosshair cursor for a moment

### 1.4.27
* Added: [Annotations API](#annotations-api) to get or set annotations within drawing mode.

### 1.4.26
* Fixed: IE10 SVG image handing issue, caused by an internal bug of IE10 (removes SVGs automatically to avoid triggering the security policy)

### 1.4.25
* Fixed: `export.config.advanced.js` sample config issue with drawing callbacks
* Fixed: `delay` property reset issue, did not get considered after first usage
* Fixed: `drawing.enabled` propery issue after first usage, stayed on true
* Changed: Updated fabric.js source to `1.6.2`
* Added: Advanced sample using the advanced config

### 1.4.24
* Fixed: Issue with external legends in maps
* Fixed: Resource dependency issue of xlsx with jszip
* Fixed: Issue with `forceRemoveImages` in local enviroment (includes all ":\" and "file://" sources)
* Changed: Resource loading order according to it's priority
* Changed: Loading minified resource versions by default to improve the payload significantly

### 1.4.23
* Fixed: Issue with `forceRemoveImages` in local enviroment

### 1.4.22
* Fixed: Local time offset issue on XLSX exports
* Added: `forceRemoveImages` to remove images regardless if they are tainted or not
* Added: Used config ([processData](#changing-the-dataprovider-when-exporting)) as additional given parameter.

### 1.4.21
* Fixed: Issue with file:// image origin, forced removal as it does not fit to the CORS policy and blocks the image export.

### 1.4.20
* Fixed: Issue with disappearing images in PDFs caused by exceeding boundary box for images

### 1.4.19
* Fixed: Issue with radial gradient

### 1.4.18
* Fixed: Issue with the legend positioning on the left side

### 1.4.17
* Fixed: `clip-path` issue on XY serial charts which exposed the drawn line beyond the plotarea.

### 1.4.16
* Added: ([processData](#changing-the-dataprovider-when-exporting)) config-property to change the dataProvider when exporting.

### 1.4.15
* Fixed: Menu items obtain the global `multiplier` setting to scale the output image.

### 1.4.14
* Added: ([border](#adding-overlaying-border)) config-property to add an overlaying border on the output image.

### 1.4.13
* Fixed: Issue on balloons showing it's content as HTML added "textContent" as alternative getter

### 1.4.12
* Fixed: clipPath issue on SVG export (workaround until fabricJS handles that by themselves)

### 1.4.11
* Fixed: Depth issue on value labels on columns

### 1.4.10
* Fixed: potential vulnerability with anonymous function declaration

### 1.4.9
* Added: Radial gradient support on pie charts (new feature in amCharts v3.18.0)

### 1.4.8
* Fixed: Clippath positioning issue
* Fixed: Issue removing tainted images
* Fixed: Hashbanged url interpretation issue in IE (related to reusable svg nodes)

### 1.4.7
* Fixed: Zeroes were being exported to data formats as empty strings rather than numbers

### 1.4.6
* Fixed: Loading issue with patterns in firefox

### 1.4.5
* Fixed: Issue with the "canvas-container" on chart revalidations

### 1.4.4
* Added: Balloon text orientation
* Fixed: Issue with multiline label positioning

### 1.4.3
* Added: `exportFields` option which is an array of fields to export in data formats (if you want to export just some fields as opposed to all fields)

### 1.4.2
* Added: `overflow` flag to overwrite the css attribute 'overflow' of the chart container

### 1.4.1
* Fixed: cropped bullets on XY charts

### 1.4.0
* Fixed: beforeCapture issue on SVG document changes
* Added: Namespace check within globals for required third party software

### 1.3.9
* Fixed: Base tag gradient drawing issue (includes embedded hotfix for fabricjs commit #c9745ff)

### 1.3.8
* Fixed: Base tag clip path issue which draw the lines outside the plotarea

### 1.3.7
* Added: `columnNames` property, which allows overriding column names when expoerting chart data

### 1.3.6
* Fixed: Checking parseDates for category values for date interpretation

### 1.3.5
* Fixed: Scrollbar issue hiding the unselected scrollbar background area

### 1.3.4
* Fixed: Absolute legend positioning issue.

### 1.3.3
* Added: English as default language when define language does not exist

### 1.3.2
* Added: ([drawing.autoClose](#annotation-settings)) new flag to automatically close the annotation mode after download
* Fixed: Internal pdfMake issue which prevented to generate PDFs in IE10, uses custom build until officially fixed

### 1.3.1
* Added: Timestamp date fields get converted as dates
* Fixed: XLSX respects given dateFormat
* Changed: JSON exports date fields as date objects by default

### 1.3.0
* Fixed: Issue hiding drawing container on "drawing.done"
* Fixed: Legend positioning issue with charts created in a hidden container

### 1.2.9
* Fixed: Issue with missing `export.css` which showed the canvas
* Fixed: Issue with empty menu items; adds list only with childNodes > 1
* Fixed: Issue with hidden bullets; caused by wrong clip-paths
* Added: Polish language file ( thanks to piernik )

### 1.2.8
* Fixed: Issue in `gatherClassName` checking element type

### 1.2.7
* Fixed: Generates true JPG file instead PNG with JPG extension
* Fixed: Drag&Drop feature does not activate automatically the annotation mode

### 1.2.6
* Added: Native EXCEL date cell type for date fields, forced by default
* Fixed: Issue in `getChartData` which ignored given data array ( affected API usage only )

### 1.2.5
* Added: Illustrator support; `reviver` method to `toSVG`; converts by default RGBA to HEX colors codes and places it's dedicated opacity property
* Fixed: Multiline text positioning / line heights

### 1.2.4
* Added: `exportSelection` exports the current data selection
* Added: `dataDateFormat` converts the date-strings to date objects with given format
* Added: `dateFormat` converts the date in given format
* Added: `processData` to format date fields and translate fields
* Changed: `gatherChartData` collects data, fields and titles only and uses `processData` to format

### 1.2.3
* Fixed: Positioning issue on multiline labels

### 1.2.2
* Fixed: Issue with object changes which overwrite undo/redo object states
* Fixed: Issue with default fontSize

### 1.2.1
* Added: Possibility to add text, lines, shapes ([details](#annotation-settings))
* Added: Possibility to change drawing mode, color, opacity and size
* Added: Possibility to select,move,scale drawn items
* Added: Possibility to define dedicated drawing menu `drawing.menu` individual menu items get prioritised
* Added: Dropbox feature which allows to drag images into the chart `fileListener: true`
* Added: Keylistener which allows to undo/redo/remove the drawn steps `keyListener: true`
* Added: Isolated plugin to be able to initiate manually regardless of the chart setup
* Fixed: Conflict with prototypeJS which caused tainted return value from `toArray`

### 1.2.0
* Fixed: Issue with deepMerge which did not allow to modfiy the pdfMake default settings
* Fixed: Menu issue which did not allow to modify the pdfMake settings
* Fixed: Undo issue which needed double attempts in the beginning
* Added: Drag/Scale feature in annotation mode; toggles automatically between drawing/dragging while hovering over the elements

### 1.1.9
* Added: `exportTitles` available in general or individual setup which exchanges the data field names with it's dedicated title
* Fix: Interpolates missing data fields across data provider

### 1.1.8
* Added: Temporary workaround to bypass FileSaver check; issue prevented to open blob urls in safari browser

### 1.1.7
* Added: beforeCapture to be able to indicate the export process in some way
* Added: afterCapture to be able to modify the fabric instance if needed
* Added: SVG element as second argument within the "reviver" callback
* Added: Multiple arguments supported in "handleCallback" method

### 1.1.6
* Fix: Pattern render issue in IE;
* Added: Multiline support (workaround until fabricJS supports tspan)
* Added: General delay property to delay the capturing of the chart ([details](#delay-the-capturing-before-export))

### 1.1.5
* Fix: Tainted check issue which failed if location.origin wasn't available
* Fix: Capture image check, triggers callback only if all images have been loaded
* Added: Multi language support; embedded english by default; overtakes chart language
* Added: Delay feature, which allows to delay the capturing ([details](#delay-the-capturing-before-export))

### 1.1.4
* Fix: Did not collect clip-path and pattern from legend
* Fix: External legend did not respect given width when positioned on left side
* Fix: Improved tainted image detection

### 1.1.3
* Added: Added reviver in capturing method to filter the drawn chart elements

### 1.1.2
* Added: Generalized fallback; does a lookup on the Blob constructor
* Fix: Wait for lazy images, triggers capture callback only when all images have been fully loaded
* Discovered: [Safari 5 issue](https://github.com/kangax/fabric.js/issues/2241) please adapt fabric.js manually to solve it

### 1.1.1
* Fix: CSV export issue on date based charts
* Fix: Enhanced migration script to obtain more settings

### 1.1.0
* Fix: Print issue on safari which captured the actual page instead of the export
* Added: IE9 download fallback for `text/plain` and `image/*` mime types (CSS has been modified)
* Added: `toImage` method; returns `img` element with embedded base64 imagery
* Added: `getBase64` option in `toSVG`
* Added: `toImage` usage in `toPRINT` to be able to choose the image type + settings.
* Added: `lossless` option in `toPRINT` (experimental)

### 1.0.9
* Added: IE9 base64 export
* Added: Third party updates + minified versions

### 1.0.8
* Fix: IE8 issue which prevents the chart from initiating

### 1.0.7
* Fix: issue on toCSV handling the header (first row)

### 1.0.6
* Fix: issue on revalidation the chart/map
* Added: [path](http://docs.amcharts.com/3/javascriptcharts/AmSerialChart#path) to load the libaries by default

### 1.0.5
* Added: divId to be able to place the menu within an external container
* Added: menuWalker to replace the whole menu generation
* Added: menuReviver to adapt menu items before being appended to the list
* Added: libs.async to load dependencies asynchronous (default true)

### 1.0.4
* Considering classNamePrefix (dont't forget to adapt export.css)
* Added: safety delay on print restore to ensure capturing the canvas

### 1.0.3
* Fix: flagged relative image paths as tainted

### 1.0.2
* Fix: compabitily on array method extension such as PrototypeJS

### 1.0.1
* Added: libs.reload: false, script tag crawling to avoid multiple insertions
* Fix: IE10 bug on print
* Fix: migration bug, replaces menu instead of appending

### 1.0
* Initial release