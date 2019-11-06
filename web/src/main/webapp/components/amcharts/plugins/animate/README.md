# amCharts Animate

Version: 1.1.2


## Description

Smoothly animates the `dataProvider`

It works with serial, pie, XY, funnel, and radar.

Here are some examples:

[Serial chart (line)](http://codepen.io/team/amcharts/pen/64673d1369cc47c0e6a970b071bafd03)

[Serial chart (column)](http://codepen.io/team/amcharts/pen/a5322d071a194d5975a4c68309724324)

[Pie chart](http://codepen.io/team/amcharts/pen/3ff9b206ce37111fa508156df38504bc)

[XY chart](http://codepen.io/team/amcharts/pen/9c9289a7b5f8b1d6d11441836e09cc81)

[Funnel chart](http://codepen.io/team/amcharts/pen/8fd8d025730b01939a2eb56b908488df)

[Radar chart](http://codepen.io/team/amcharts/pen/6ffb5e356b6015a6dcb6019d7b14d3f6)


## Installation

Include `animate.min.js` on your web page:

```
<script src="//www.amcharts.com/lib/3/plugins/animate/animate.min.js"></script>
```

## Usage

Rather than using `chart.validateData`, instead use `chart.animateData`:

```
chart.animateData(newData, { duration: 1000 });
```

It will now smoothly animate from the old data to the new data.

----

The first argument is the new `dataProvider` for the chart.

The second argument is an object that can contain the following options:

* `duration` is required: it is the number of milliseconds that the animation should play for.

* `complete` is optional: it is a function that is called when the animation completes.

----

The new `dataProvider` must be different from the old `dataProvider`

If you want to modify the existing `dataProvider`, you must create a deep copy:

```
// Creates a deep copy of the old dataProvider
var newDataProvider = JSON.parse(JSON.stringify(chart.dataProvider));

// Adds new data to the new dataProvider
newDataProvider.push({ category: "foo", value: 10 });

// Mutates the data in the new dataProvider
newDataProvider[0].value = 50;

// Animates with the new dataProvider
chart.animateData(newDataProvider, { duration: 1000 });
```


## License

All software included in this collection is licensed under Apache License 2.0.

This basically means you're free to use or modify it, even make your own
versions or completely different products out of them.

Please see attached file "license.txt" for the complete license or online here:

http://www.apache.org/licenses/LICENSE-2.0


## Contact us

* Email: contact@amcharts.com
* Web: http://www.amcharts.com/
* Facebook: https://www.facebook.com/amcharts
* Twitter: https://twitter.com/amcharts


## Changelog

### 1.1.2
* Fixing a bug with the minimum/maximum

### 1.1.1
* It now automatically sets the minimum/maximum on the value axes while animating

### 1.1.0
* Adding in support for XY charts

### 1.0.0
* Initial release
