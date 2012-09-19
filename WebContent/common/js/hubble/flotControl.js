//################################################################################
// graphing library specific code area
// to change to another graphing library, change following codes.
// following code is based on flot(http://code.google.com/p/flot/)
// @ see : changes in 'jquery.flot_noconflict.js' at 2709 line  :  $J.plot.formatDate UTC time to local timezone by minseok
//################################################################################
// coded by minseok 2010.10.

var COLOR_TOOLTIP_DATA_TEXT_SINGLE = 'rgba(0,0,0,0.75)'; // black
var COLOR_TOOLTIP_DATA_TEXT_MERGED = '#FFFFFF'; // white
var COLOR_TOOLTIP_DATA = "rgba(248,248,248,0.7)"; // tooltip background

// var COLOR_TOOLTIP_TIME_BG = "rgba(107,142,35,0.0)"; // timestamp background
var COLOR_TOOLTIP_TIME_BG = "rgba(0,0,0,0.45)"; // timestamp background
var COLOR_TOOLTIP_TIME_TEXT = "rgba(255,255,255,1)"; // timestamp text

var COLOR_TOOLTIP_TIME_BG_MAX = "rgba(250,250,250,0.7)"; // max tooltip
// background

var COLOR_SINGLE_ZERODATA = "#C0c0c0"; // Gray
var COLOR_SINGLE_NONZERO_MAX = "rgba(128,0,128,1)"; // red
var COLOR_SINGLE_NONZERO_AVG = "rgba(100,149,237,1)"; // cornflowerblue
var SNZ_MAX_ALPHA_BOTTOM = 0.2;
var SNZ_MAX_ALPHA_TOP = 0.4;
var SNZ_AVG_ALPHA_BOTTOM = 0.3;
var SNZ_AVG_ALPHA_TOP = 0.6;

var COLOR_SINGLE_NONZERO = [ COLOR_SINGLE_NONZERO_AVG, COLOR_SINGLE_NONZERO_MAX ];// blue(average)
// blue(max)
var WARN_WORDS = [ 'err', 'error', 'fail', 'timeout', 'retrans', 'drop',
		'loss', 'abort', 'lost', 'overflow', 'blocked', 'rsts', 'rto' ];
var WARN_WORDS_COLOR = "rgba(220,20,60, 1)"; // crimson
var WARN_ALPHA_BOTTOM = 0.4; // alpha value for bottom of chart with failures
var WARN_ALPHA_TOP = 0.8;

var THRESHOLD_OVER_CPU = [ 50, 70 ]
var THRESHOLD_OVER_CPU_COLOR = [ "rgba(255,69,0,1)", "rgba(255,0,0,1)" ];// pink,
// red
var THRS_CPU_ALPHA_TOP = 0.8;
var THRS_CPU_ALPHA_BOTTOM = 0.4;

var THRESHOLD_OVER_QOS = [ 10000, 100000 ] // microsec
var THRESHOLD_OVER_QOS_COLOR = [ "rgba(255,99,71,1)", "rgba(255,0,0,1)" ];// pink,
// red

var THRESHOLD_OVER_MEM_USED = [ 50, 70 ] // % of 'used. if 'used' is over 50,
// 'used'color turns pink
var THRESHOLD_OVER_MEM_USED_COLOR = [ "rgba(255,69,0,1)", "rgba(255,0,0,1)" ];// pink,
// red

var DATACOUNT_TOSHOW_DATAPOINTS_INGRAPH = 20;
var LINECHART_LINEWIDTH = 0.1;

var flotControl = {

	updateTooltipTimeout : null,
	removeTooltipTimeout : null,
	removeMaxtipTimeout : null,

	// color is 'colors''s first item. see 'flotControl.getNewFlotOption()'
	setFlotOptionToDataForMaxLine : function(dataset) {
		return flotControl.setFlotOptionToData(dataset, 1,
				COLOR_SINGLE_NONZERO_MAX, SNZ_MAX_ALPHA_BOTTOM,
				SNZ_MAX_ALPHA_TOP, LINECHART_LINEWIDTH)
	},
	setFlotOptionToDataForAvgLine : function(dataset) {
		return flotControl.setFlotOptionToData(dataset, 1,
				COLOR_SINGLE_NONZERO_AVG, SNZ_AVG_ALPHA_BOTTOM,
				SNZ_AVG_ALPHA_TOP, LINECHART_LINEWIDTH)
	},

	setFlotOptionToDataForLine : function(dataset, color, fillColorAFrom,
			fillColorATo) {
		return flotControl.setFlotOptionToData(dataset, 0.6, color,
				fillColorAFrom, fillColorATo, LINECHART_LINEWIDTH)
	},

	// series(i)
	// fillColorTo: rgba
	setFlotOptionToData : function(dataset, fillParam, color, fillColorAFrom,
			fillColorATo, lineWidthParam) {
		fillColorFrom = jQuery.color.parse(color).scale('a', fillColorAFrom)
				.toString();
		fillColorTo = jQuery.color.parse(color).scale('a', fillColorATo)
				.toString();
		dataset['lines'] = {
			show : true,
			fill : fillParam,
			steps : false,
			shadow : true,
			fillColor : {
				colors : [ fillColorFrom, fillColorTo ]
			},
			lineWidth : lineWidthParam
		};
		return dataset;
	},
	getNewFlotOption : function(factorCateName) {
		var newflotOption = {
			// we don't use builtin legend function.
			legend : {
				show : false,
				noColumns : 3
			},

			series : {
				lines : {// for default line option, may be not used in
					// conditions.
					show : true,
					steps : false,
					fill : 0.6,
					shadow : true,
					lineWidth : LINECHART_LINEWIDTH
				},

				shadowSize : 0.7,
				points : {
					show : true,
					radius : 1.5, // 2.5,
					lineWidth : 1, // 1, // in pixels
					fill : 1, // true,
					fillColor : "rgba(100,149,237,0.3)" // harded coded to
				// "black" in
				// drawPointHighlight()(
				// jquery.flot_noconflict.js
				// 2014 lines)
				}

			},
			xaxis : {
				mode : "time"
			},
			yaxis : {
				min : 0
			},
			crosshair : {
				mode : "x",
				color : "rgba(0,0,0,0.5)",
				lineWidth : 0.2
			},
			selection : {
				mode : "x"
			},
			grid : {
				// color: "#999",
				hoverable : true,
				aboveData : false,
				clickable : true,
				autoHighlight : true,
				mouseActiveRadius : 5,
				borderWidth : 0.2, // in pixels
				labelMargin : 3, // in pixels
				borderColor : null, // set if different from the grid color
				markings : null, // array of ranges or fn: axes -> array of
				// ranges
				markingsColor : "#f4f4f4",
				markingsLineWidth : 2,
				tickColor : "rgba(0,0,0,0.07)", // color used for the ticks
				backgroundColor : "rgba(0,0,0,0.03)" // null for transparent,
			// else color
			}
		};

		return flotControl.setFlotOptionByFactor(factorCateName, newflotOption);
	},
	getNewFlotOptionForPrefixAggregate : function(factorCateName) {
		var newflotOption = {
			// we don't use builtin legend function.
			legend : {
				show : false,
				noColumns : 3
			},
			series : {
				bars : {
					show : true,
					lineWidth : 0, // in pixels
					barWidth : 0.7, // in units of the x axis
					fill : true,
					fillColor : {
						colors : [ "rgba(0,0,0,0.3)", "rgba(0,0,0,0.1)" ]
					},
					align : "center", // or "center"
					horizontal : false
				// when horizontal, left is now top
				}

			},
			// dummy, del, get, hit, set,( exists)
			colors : [ "rgba(0,0,0,0.2)", "rgba(0,0,0,0.2)", "rgba(0,0,0,0.2)",
					"rgba(0,0,0,0.2)", "rgba(0,0,0,0.2)", "rgba(0,0,0,0.2)" ],
			xaxis : {
				ticks : [ [ 0, '' ], [ 1, "Get" ], [ 2, "Set" ],
						[ 3, "Delete" ], [ 4, "Hit" ] ]
			},
			yaxis : {
				min : 0
			},

			grid : {
				show : true,
				aboveData : false,
				color : "#545454", // primary color used for outline and labels
				// tickColor: "rgba(0,0,0,0.15)", // color used for the ticks
				labelMargin : 3, // in pixels
				borderWidth : 0.2, // in pixels
				borderColor : null, // set if different from the grid color
				markings : null, // array of ranges or fn: axes -> array of
				// ranges
				markingsColor : "#f4f4f4",
				markingsLineWidth : 2,
				// interactive stuff
				clickable : false,
				hoverable : false,
				autoHighlight : true, // highlight in case mouse is near
				tickColor : "rgba(0,0,0,0.06)", // color used for the ticks
				backgroundColor : "rgba(0,0,0,0.05)" // null for transparent,
			// else color
			}
		};

		return flotControl.setFlotOptionByFactor(factorCateName, newflotOption);
	},

	// not used
	getNewFlotOptionForPie : function() {
		var newflotOption = {
			series : {
				// the color theme used for graphs
				// colors: ["#edc240", "#afd8f8", "#cb4b4b", "#4da74d",
				// "#9440ed"],
				pie : {
					// colors: ["#edc240", "#afd8f8", "#cb4b4b", "#4da74d",
					// "#9440ed"],
					show : true,
					radius : 1,
					innerRadius : 0.5,
					label : {
						show : true,
						radius : 3 / 4,
						formatter : function(label, series) {
							return '<div style="font-size:11px;font-family:Arial,Helvetica;text-align:center;padding:2px;color:black">'
									+ label
									+ '<br/>'
									+ Math.round(series.percent) + '%</div>';
						},
						threshold : 0.1
					}
				}
			},
			legend : {
				show : false
			},
			grid : {
				hoverable : false,
				clickable : true
			}
		}

		return newflotOption;
	},
	// used commonMainflow.js
	KEYWORDS_TO_TRIM_FIELDSET_NAME_IN_ALL : 'all_,prefix_consolidated,memcached_prefix_,memcached_prefix-,memcached_stat_,memcached_stat-',
	// used in memcachedView.html to trim the name of stat list
	KEYWORDS_TO_TRIM_KEY_IN_MEMCACHEDVIEW : "prefix_consolidated-,prefix_consolidated_,memcached_prefix_,memcached_stats_,memcached-,memcached_,prefix-,percent-,.rrd",
	KEYWORDS_TO_TRIM_KEY_IN_QOSVIEW : "prefix_consolidated-,prefix_consolidated_,memcached_prefix_,memcached-,memcached_,prefix-,percent-,_consolidated,.rrd",
	// fine setting by factor
	// labelPrefix2Trim is separated by 'comma' , no white space betwen
	// comma(,), order is very important.
	// see also, flotControl.setGraphColorAndCheckDataForSingleLineChart()
	setFlotOptionByFactor : function(factorCateName, option) {

		option.legend.show = false;
		option.xaxis.min = null;
		option.xaxis.max = null;
		option.yaxis.max = null;

		// general yaxis format
		option.yaxis.tickFormatter = function(v, axis) {
			if (v >= 1000) {
				return flotControl.dataUnitFormatter(v, 0, false); // 0 digit
				// below
				// zero
			} else if (v <= 1) {
				return flotControl.dataUnitFormatter(v, 2, false); // 2 digit
				// below
				// zero
			} else {
				return v;
			}
		}

		option.series['tooltipDataFormatter'] = function(value) {
			if (value == null)
				return "nodata";
			return flotControl.dataUnitFormatter(value, 2, false); // 3 digit
			// below
			// zero
		}

		option.legend.labelFormatter = function(datalabel, series,
				urgentPrefixToTrim) {
			prefixesToTrim = series.labelPrefix2Trim;
			if (urgentPrefixToTrim != undefined && urgentPrefixToTrim != '')
				prefixesToTrim = urgentPrefixToTrim + "," + prefixesToTrim;

			result = flotControl.makeLabeltextFromDataLabel(datalabel,
					prefixesToTrim);
			return result;
		}
		option.series['showTooltipOnlyWhenDataOverZero'] = false;

		if (factorCateName == 'cpu') {
			// option.series.stack = true;
			option.series.stack = null; // for disable
			// option.yaxis.max=100;
			option.yaxis.tickFormatter = function(v, axis) {
				return v.toFixed(axis.tickDecimals) + "%";
			}
			option.series['tooltipDataFormatter'] = function(value) {
				if (value == null)
					return "nodata";
				return Math.round(value * 100) / 100 + "%";
			}
			option.series['labelPrefix2Trim'] = "consolidated,cpu-,cpu_";
			option.series['thresholdSetting'] = function(label, option) {
				option.series['thresholdOverValue'] = THRESHOLD_OVER_CPU;
				option.series['thresholdOverColor'] = THRESHOLD_OVER_CPU_COLOR;
			}
			option.series['showTooltipOnlyWhenDataOverZero'] = false;

		} else if (factorCateName == 'disk') {
			option.series['labelPrefix2Trim'] = "consolidated,disk_";
			option.series['showTooltipOnlyWhenDataOverZero'] = false;
		} else if (factorCateName == 'interface') {
			option.series['labelPrefix2Trim'] = "consolidated-,interface_";
			option.series['showTooltipOnlyWhenDataOverZero'] = false;

		} else if (factorCateName == 'irq') {
			option.series['labelPrefix2Trim'] = "consolidated,irq-";
			option.series['showTooltipOnlyWhenDataOverZero'] = false;

		} else if (factorCateName == 'memcached') {
			option.series['labelPrefix2Trim'] = "memcached_,prefix-";
			option.series['showTooltipOnlyWhenDataOverZero'] = false;

		} else if (factorCateName == 'memcached_prefix') {
			option.series['labelPrefix2Trim'] = "prefix_consolidated,memcached_prefix_key_value,memcached_prefix_B_plus_tree,memcached_prefix_list,memcached_prefix_set,bop,sop,lop,cmd,bp_,list_,set_,memcached,percent";
			option.series['showTooltipOnlyWhenDataOverZero'] = false;

		} else if (factorCateName == 'memcached_stat') {
			// option.series['labelPrefix2Trim'] =
			// "prefix_consolidated,memcached_stats_key_value,memcached_stats_B_plus_tree,memcached_stats_list,memcached_stats_set,bop,sop,lop,cmd,bp_,list_,set_,memcached_,percent";
			option.series['labelPrefix2Trim'] = "prefix_consolidated,memcached_stats_key_value,memcached_stats_B_plus_tree,memcached_stats_list,memcached_stats_set,cmd,bp_,list_,set_,memcached_,percent";
			option.series['showTooltipOnlyWhenDataOverZero'] = false;

		} else if (factorCateName == 'memory') {
			// option.series.stack = true;
			option.series.stack = null; // for disable
			// option.series.lines.fill=false;
			// general yaxis format
			option.yaxis.tickFormatter = function(v, axis) {
				return flotControl.dataUnitFormatter(v, 0, true); // 2 digit
				// below
				// zero
			}
			option.series['tooltipDataFormatter'] = function(value) {
				if (value == null)
					return "nodata";
				return flotControl.dataUnitFormatter(value, 2, true);
			}
			option.series['labelPrefix2Trim'] = "consolidated,memory-,memory_";
			option.series['showTooltipOnlyWhenDataOverZero'] = false;
		} else if (factorCateName == 'swap') {
			option.series['labelPrefix2Trim'] = "consolidated,swap-,swap_";
			option.series['showTooltipOnlyWhenDataOverZero'] = false;
		} else if (factorCateName == 'tcpconns') {
			option.series['labelPrefix2Trim'] = "consolidated,tcp_connections-";
			option.series['showTooltipOnlyWhenDataOverZero'] = false;
		} else if (factorCateName == 'vmem') {
			// option.series.stack = true;
			option.series.stack = null; // for disable
			option.series['labelPrefix2Trim'] = "consolidated,vmem,vmpage_number_,vmpage_action_,vmpage";
			option.series['showTooltipOnlyWhenDataOverZero'] = false;
		} else if (factorCateName == 'processes') {
			option.series['labelPrefix2Trim'] = "processes_ps_state_consolidated_,ps_state-";

		} else if (factorCateName == 'protocols') {
			option.series['labelPrefix2Trim'] = "consolidated,protocols,protocol_counter-,TcpExt,Tcp,Udp";

		} else if (factorCateName == 'QoS') {
			// option.series['labelPrefix2Trim'] =
			// "20_,consolidated-,qos_consolidated";
			option.series['labelPrefix2Trim'] = "20_,consolidated-,consolidated_";
			// general yaxis format
			option.yaxis.tickFormatter = function(v, axis) {
				return flotControl.dataUnitFormatterMicroSec(v, 0);
			}
			option.series['tooltipDataFormatter'] = function(value) {
				if (value == null)
					return "nodata";
				return flotControl.dataUnitFormatterMicroSec(value, 2); // 3
				// digit
				// below
				// zero
			}
			option.series['thresholdSetting'] = function(label, option) {
				option.series['thresholdOverValue'] = THRESHOLD_OVER_QOS;
				option.series['thresholdOverColor'] = THRESHOLD_OVER_QOS_COLOR;
			}
		}

		return option;
	},
	setLegendContainer : function(legendContainer, flotOption) {
		flotOption.legend.container = legendContainer;
		return flotOption;
	},
	// find common prefix in labels in dataset, to make compace label.
	// if no common things, return empty string
	determinSameStartsWithString : function(datasets) {
		if (datasets.length == 1)
			return '';
		var sampleStr = datasets[0].label;
		while (sampleStr.length > 0) {
			if (flotControl
					.determinSameStartsWithStringSub(datasets, sampleStr))
				return sampleStr;
			sampleStr = sampleStr.substr(0, sampleStr.length - 1);
		}
		return sampleStr;
	},
	determinSameStartsWithStringSub : function(datasets, sampleStr) {

		for ( var i = 0; i < datasets.length; i++) {
			// if(!datasets[i].label.startsWith(sampleStr))
			if (!datasets[i].label.match(eval("/^" + sampleStr + "/")))
				return false;
		}
		return true;
	},
	// find common prefix with rrdFileName(no ext.) in labels in dataset, to
	// make compace label.
	// if all labels starts with rrdFileName(no ext.), returns rrdFileName(no
	// ext.)
	// if no common things, return empty string
	getFileNamePrefixToTrim : function(datasets, rrdFileName) {
		rrdFileName = replaceEscapeChars(rrdFileName);
		var extIndex = rrdFileName.indexOf(".rrd");
		var rrdFileNameNoExt = rrdFileName
		if (extIndex > 0)
			rrdFileNameNoExt = rrdFileName.substr(0, extIndex + 1);// include
		// dot.
		var urgentPrefixToTrim = rrdFileNameNoExt;
		for ( var i = 0; i < datasets.length; i++) {
			var dataLabel = datasets[i].label;
			// if(!dataLabel.startsWith(rrdFileNameNoExt)){
			if (!dataLabel.match(eval("/^" + rrdFileNameNoExt + "/"))) {
				urgentPrefixToTrim = '';
				break;
			}
		}
		return urgentPrefixToTrim;
	},

	// insert legend and checkbox.
	// legends provided by plot library erases legends itself when unchecked.
	// so, coded new customized legends which i can control.
	drawLegend : function(graphUid, flotOption, rrdFileName) {

		try {
			var legendId = makeLegendId(graphUid);
			if (isElementExist(legendId)) {
				clearChildElement(legendContainer);
			}
			var legendContainer = jQuery("#" + legendId);
			var plot = gGraphPlotMap.get(graphUid);
			var datasets = plot.getData();// get colored data.
			var fragments = [], rowStarted = false
			// remove common prefix in labels in dataset, to make compace label.
			var urgentPrefixToTrim = flotControl
					.determinSameStartsWithString(datasets);

			for ( var i = 0; i < datasets.length; i++) {
				var data = datasets[i];
				if (i % flotOption.legend.noColumns == 0) {
					if (rowStarted)
						fragments.push('</tr>');
					fragments.push('<tr>');
					rowStarted = true;
				}

				var dataLabel = data.label;
				var labelToDisplay = flotOption.legend.labelFormatter(
						dataLabel, flotOption.series, urgentPrefixToTrim);
				fragments
						.push('<td class="legendColorBox" style="border:0px;padding:2px;background-color:'
								+ data.color
								+ '">'
								+ '<input type="checkbox" checked="checked" id="'
								+ dataLabel
								+ '" name="'
								+ dataLabel
								+ '" value="'
								+ graphUid
								+ '"/>'
								+ '</td>'
								+ '<td class="legendLabel">'
								+ labelToDisplay
								+ '</td>');

			}
			;
			if (rowStarted)
				fragments.push('</tr>');

			if (fragments.length == 0)
				return;
			var table = '<table style="font-size:smaller;color:'
					+ flotOption.grid.color + '">' + fragments.join("")
					+ '</table>';
			legendContainer.append(table);
		} finally {
			plot = null;
			datasets = null;
		}

	},
	// draw normal graph with all data
	plotGraphs : function(datasets, graphUid, option) {

		try {
			flotControl.addLoadingProgressAnimationAndShow(graphUid);

			// filter out wrong formatted dataset.
			// if failed on server, dataset contains server error message.
			filteredDatasets = Array();
			for ( var i = 0; i < datasets.length; i++) {
				var message = datasets[i].operationMessage + ""; // to
				// string.
				if (message != ''
						&& gfnStartsWith(message, commonMainflow.ERROR_PREFIX))
					continue;
				filteredDatasets.push(datasets[i]);
			}

			var plotDiv = jQuery("#" + graphUid);

			plotDiv.show(); // because excanvas's hidden problem.
			var plotObj = jQuery.plot(plotDiv, filteredDatasets, option);
			gGraphPlotMap.put(graphUid, plotObj);
			// flotControl.putTogGraphPlotMap(graphUid, plotObj);
			gGraphFlotOptionMap.put(graphUid, option);

			if (flotControl.isGraphZoomed(option))
				flotControl.displayZoomLabel(graphUid);
			if (isBarGraphUid(graphUid))
				flotControl.showDataLabelInBarGraph(graphUid);

			try {
				height = eval(plotDiv.css("height").replace("px", ""));
				if (height <= 100) {
					height = height * 0.82;
					plotDiv.css("height", height);
				}
			} catch (ex) {
				if (DEBUG)
					console.log(ex, graphUid);
			}
		} catch (e) {
			if (DEBUG)
				console.log(e, graphUid);
		} finally {
			flotControl.progressAnimationHideOne(graphUid);
		}

	},
	putTogGraphPlotMap : function(graphUid, plotObj) {
		gGraphPlotMap.put(graphUid, plotObj);
		gGraphPlotMap.put(graphUid + "_offset", plotObj.offset());

	},
	getPlotOffset : function(graphUid) {
		return gGraphPlotMap.get(graphUid + "_offset");

	},
	// draw graph with selected data
	plotAccordingToChoices : function(datasets, graphDivId, flotOption) {

		var legendDiv = jQuery("#" + makeLegendId(graphDivId));
		var seldata = flotControl.plotAccordingToChoices_getseldata(datasets,
				legendDiv);

		if (seldata.length > 0) {
			flotControl.plotGraphs(seldata, graphDivId, flotOption);
		}
		return seldata;
	},
	getSelectedLabelListInGraph : function(graphUid) {
		var labelList = new Array();
		var legendDiv = jQuery("#" + makeLegendId(graphUid));
		legendDiv.find("input:checked").each(function() {
			var labelname = jQuery(this).attr("name");
			labelList.push(labelname)
		});
		if (DEBUG)
			console.log("getSelectedLabelListInGraph:", graphUid, labelList);
		return labelList;
	},
	plotAccordingToChoices_getseldata : function(dataset, choiceContainer) {
		var seldata = [];
		choiceContainer.find("input:checked").each(function() {
			var labelname = jQuery(this).attr("name");
			if (!labelname)
				return;
			jQuery.each(dataset, function(key, val) {
				if (dataset[key].label == labelname)
					seldata.push(dataset[key]);
			});
		});
		return seldata;
	},

	displayZoomLabel : function(graphUid) {
		// displaying reset zoom button
		var graphDiv = jQuery("#" + graphUid);
		graphDiv.append(commonMainflow.makeResetZoomHtml(graphUid));
	},

	// #########################################################################
	// event related functions
	// #########################################################################

	// -------------------------------------------------------------------------
	// / zoom event
	// //////////////////////////////////////////////////////////////
	// -------------------------------------------------------------------------

	gZoomEventRages : null,
	gZoomActionTimeout : null,
	zoomAction_allGraph : function() {
		flotControl.gZoomActionTimeout = null;

		// filter visible graphs under visible category
		var targetGraphs = flotControl.getVisibleFactorHostObjs().find(
				"div[id$='_graphUid']");
		if (targetGraphs == undefined || targetGraphs.length <= 0)
			return;

		for ( var i = 0; i < targetGraphs.length; i++) {
			var graphUid = targetGraphs[i].id;
			// zoom only visible graphs.
			// if(!flotControl.isGraphVisible(graphUid))
			// continue;

			if (!isLineGraphUid(graphUid)) { // if not line graph, don't show
				// tooltip.
				continue;
			}

			flotControl.zoomEventAction(graphUid, gZoomEventRages)
		}
	},

	// check whether zoomed area has data
	// return number of data has no data.
	getDataCountInZoomedArea : function(flotOptionlocal, dataset) {
		var data = dataset[0].data;

		var startXIndex = flotOptionlocal.xaxis.min;
		var endXIndex = flotOptionlocal.xaxis.max;

		var counts = 0;
		for ( var i = 0; i < data.length; i++) {
			xValue = data[i][0];
			if (startXIndex != null && xValue < startXIndex) {
				continue;
			}
			if (endXIndex != null && endXIndex < xValue) {// skip to check ex)
				// zoomed.
				continue;
			}

			yValue = data[i][1];
			if (yValue == 'null') {
				continue;
			} else {
				counts++;
			}
		}
		return counts;
	},
	setToshowDatapoints : function(flotOption, dataset) {
		var dataCounts = flotControl.getDataCountInZoomedArea(flotOption,
				dataset);
		flotOption.series.points.show = false;
		if (dataCounts < DATACOUNT_TOSHOW_DATAPOINTS_INGRAPH)
			flotOption.series.points.show = true;
		return flotOption;

	},

	zoomEventAction : function(graphUid, ranges) {

		// var graphUid = event.target.id;
		// do the zooming
		flotOptionlocal = gGraphFlotOptionMap.get(graphUid);

		// clamp the zooming data to prevent eternal zoom
		if (ranges.xaxis.to - ranges.xaxis.from < 0.00001)
			ranges.xaxis.to = ranges.xaxis.from + 0.00001;

		// saving to flotOption
		flotOptionlocal.xaxis.min = ranges.xaxis.from;
		flotOptionlocal.xaxis.max = ranges.xaxis.to;

		// zoom flag.
		flotOptionlocal.series['isZoomed'] = true;

		var dataset = gGraphDataMap.get(graphUid);
		// if zoomed area has no data, stop zoom.
		var dataCounts = flotControl.getDataCountInZoomedArea(flotOptionlocal,
				dataset);
		if (dataCounts == 0) {
			if (DEBUG)
				console
						.log("zoomEventAction, zoomed area has no data, stop zooming, no more zoom available.")
			UIControl
					.notifyMsg("<label style='color:red;font-size:9px;font-weight:bold;'>no more zoom available</label>");
			return;
		}
		flotOptionlocal.series.points.show = false;
		if (dataCounts < DATACOUNT_TOSHOW_DATAPOINTS_INGRAPH)
			flotOptionlocal.series.points.show = true;

		// if cpu, then recheck threshhold value.
		// set color by all zero data and threshhold
		dataset = flotControl.setGraphColorAndCheckDataForSingleLineChart(
				graphUid, flotOptionlocal, dataset);
		flotControl.plotGraphs(dataset, graphUid, flotOptionlocal);

	},
	gResetZoomActionTimeout : null,
	resetZoomLabelClickEventAction_allgraphs : function() {
		if (DEBUG)
			console.log("resetZoomLabelClickEventAction_allgraphs starts")
		flotControl.gResetZoomActionTimeout = null;
		var targetGraphs = jQuery("div[id$='_resetZoom']");
		if (targetGraphs == undefined || targetGraphs.length <= 0) {
			if (DEBUG)
				console.log("resetZoomLabelClickEventAction_allgraphs 0")
			return;
		}

		if (DEBUG)
			console.log("resetZoomLabelClickEventAction_allgraphs",
					targetGraphs.length)
		for ( var i = 0; i < targetGraphs.length; i++) {
			flotControl.resetZoomLabelClickEventAction(targetGraphs[i].id)
		}

	},

	resetZoomLabelClickEventAction : function(zoomUid) {
		if (DEBUG)
			console.log("resetZoomLabelClickEventAction", zoomUid)

		var graphUid = getGraphUidFromResetZoomId(zoomUid);
		var flotOptionlocal = gGraphFlotOptionMap.get(graphUid);
		if (!flotOptionlocal)
			return;
		// reset flot option

		// resetting only zoomed information.
		flotOptionlocal.legend.show = false;
		flotOptionlocal.xaxis.min = null;
		flotOptionlocal.xaxis.max = null;
		flotOptionlocal.series['isZoomed'] = null;
		// redrawing the graph
		var dataset = gGraphDataMap.get(graphUid);
		// if cpu, then recheck threshhold value.
		// set color by all zero data and threshhold
		dataset = flotControl.setGraphColorAndCheckDataForSingleLineChart(
				graphUid, flotOptionlocal, dataset);

		// set datapoints if number of data is under some limits.
		flotOptionlocal = flotControl.setToshowDatapoints(flotOptionlocal,
				dataset);

		flotControl.plotGraphs(dataset, graphUid, flotOptionlocal);
		removeElement(zoomUid);
	},

	isGraphZoomed : function(option) {
		if (option.series.isZoomed == undefined
				|| option.series.isZoomed == false)
			return false;
		else
			return true;
	},
	actionLegendClickEvent : function(event) {

		var clickedGraphUid = event.target.value; // graphUid
		flotOptionlocal = gGraphFlotOptionMap.get(clickedGraphUid);
		// container should be null , because flot redraw if legend is clicked ,
		// then legend disappered.
		flotOptionlocal.legend.show = false;
		var dataset = gGraphDataMap.get(clickedGraphUid);
		flotControl.plotAccordingToChoices(gGraphDataMap.get(clickedGraphUid),
				clickedGraphUid, flotOptionlocal);

	},
	actionTitleClickToggleSizeEvent : function(event) {
		var eventTargetId = event.target.id;
		var graphUid = getGraphUidFromGraphTitleId(eventTargetId);
		var zoomId = makeResetZoomId(graphUid);
		removeElement(zoomId);
		var isCurrentGraphMagnified = event.target.className == "graph_title_m" ? true
				: false;
		var boolTobeMagnify = !isCurrentGraphMagnified;
		var title = event.target.innerHTML;

		// redrawing title and graph body only(without legend)

		commonMainflow.appendGraphTitleDiv(title, graphUid, boolTobeMagnify);
		commonMainflow.appendGraphCanvasDiv(graphUid, boolTobeMagnify);
		flotOptionlocal = gGraphFlotOptionMap.get(graphUid);

		var dataset = gGraphDataMap.get(graphUid);
		flotControl.plotGraphs(dataset, graphUid, flotOptionlocal);

	},

	// -------------------------------------------------------------------------
	// / graph visibility //////////////////////////////////////////////////////
	// -------------------------------------------------------------------------

	gVisibleFactorObjs : null,
	getVisibleFactorObjs : function() {
		if (flotControl.gVisibleFactorObjs == null) {
			flotControl.gVisibleFactorObjs = jQuery(
					"div[id^='factorGraphDiv_']").not(
					jQuery("div[style^='display: none;']"));
		}
		return flotControl.gVisibleFactorObjs;
	},

	gVisibleFactorHostObjs : null,
	// graph_top_div-> factorGraphDivGroupClassfactorGraphDiv_... ->
	// factorGraphDiv_disk -> graph_fieldset_host_id ->
	// hostFieldset_factorGraphDiv_disk_tm07_perf_nhnsystem_com
	getVisibleFactorHostObjs : function() {
		if (flotControl.gVisibleFactorHostObjs == null) {
			var objs = flotControl.getVisibleFactorObjs().children();
			objs = objs.children("div[id^='hostFieldset_']");
			objs = objs.not(jQuery("div[style^='display: none;']"))
			// objs =
			// jQuery("div[id^='hostFieldset_']").not(jQuery("div[style^='display:
			// none;']"));
			flotControl.gVisibleFactorHostObjs = objs;

		}
		return flotControl.gVisibleFactorHostObjs;
	},

	isGraphVisibleByUser : function(plotYTop, plotHeight) {
		scrollYStart = 0;
		scrollYEnd = 0;

		var documentObj = jQuery(document);
		if (navigator.appName == "Netscape") {
			scrollYStart = documentObj.scrollTop();
			scrollYEnd = scrollYStart + documentObj.height();
		}
		if (navigator.appName.indexOf("Microsoft") != -1) {
			scrollYStart = documentObj.scrollTop();
			scrollYEnd = scrollYStart + document.body.offsetHeight;
		}

		result = false;
		// graph top
		if (plotYTop > scrollYStart && plotYTop < scrollYEnd)
			result = true;

		// check graph bottom
		plotBottomY = plotYTop + plotHeight
		if (plotBottomY > scrollYStart && plotBottomY < scrollYEnd)
			result = true;

		return result;

	},

	getVisibleGraphList : function() {
		var targetGraphs = flotControl.getVisibleFactorObjs().find(
				"div[id$='_graphUid']");
		if (targetGraphs == undefined)
			return new Array();
		else
			return targetGraphs;
	},
	isGraphVisible : function(graphUid) {
		// get plot
		var plotObj = gGraphPlotMap.get(graphUid);
		if (plotObj == null)
			return true;

		var plotOffset = plotObj.offset();
		// var plotOffset = flotControl.getPlotOffset(graphUid);
		plotTop = plotOffset.top;
		plotHeight = plotObj.height();

		// check if graph is on screen(visible)
		if (flotControl.isGraphVisibleByUser(plotTop, plotHeight))
			return true;
		else
			return false;

	},

	// -------------------------------------------------------------------------
	// / tooltips //////////////////////////////////////////////////////////////
	// -------------------------------------------------------------------------
	gTooltipMousePosXAxis : 0, // current mouse position's xvalue
	gTooltipOriginalEventGraphUid : null, // graph id which 'plothover' mouse
	// event occured .
	showTooltip_allGraph : function() {
		mousePosXAxisValue = flotControl.gTooltipMousePosXAxis

		// filter visible graphs under visible category
		var targetGraphs = flotControl.getVisibleFactorHostObjs().find(
				"div[id$='_graphUid']");

		for ( var i = 0; i < targetGraphs.length; i++) {
			var graphUid = targetGraphs[i].id;
			if (!isLineGraphUid(graphUid)) { // if not line graph, don't show
				// tooltip.
				continue;
			}
			if (flotControl.gTooltipOriginalEventGraphUid == graphUid)
				flotControl.showTooltip_OneGraph(mousePosXAxisValue, graphUid,
						true)
			else
				flotControl.showTooltip_OneGraph(mousePosXAxisValue, graphUid,
						false)
		}
		flotControl.updateTooltipTimeout = null;
	},

	showTooltip_OneGraph : function(mousePosXAxisValue, graphUid,
			boolShowDateTooltip) {

		// get plot
		var plotObj = gGraphPlotMap.get(graphUid);
		var plotOffset = plotObj.offset();
		// var plotOffset = flotControl.getPlotOffset(graphUid);
		plotTop = plotOffset.top;
		plotHeight = plotObj.height();
		// check if graph is on screen(visible)
		if (!flotControl.isGraphVisibleByUser(plotTop, plotHeight)) {
			// if(DEBUG) console.log("not visible",graphUid)
			return;
		}

		plotLeft = plotOffset.left;
		plotWidth = plotObj.width();
		plotXaxis = plotObj.getAxes().xaxis;
		plotYaxis = plotObj.getAxes().yaxis;
		plotOption = plotObj.getOptions();

		var mergedGraph = isMergedGraphUid(graphUid);

		if (mergedGraph) {
			pixelX = flotControl.getPixelXUsingXdataRatioAndMousePosition(
					plotTop, plotLeft, plotWidth, plotHeight, plotXaxis,
					mousePosXAxisValue); // following a mouse position
			pixelX += 20;
		} else {
			// fixed x tooltip position
			pixelX = plotLeft;
			// pixelX = plotLeft + plotWidth - 40;
		}

		plotObj.unhighlight();// remove all highlight
		// display yaxis(data) tooltip #######################################
		// find y value
		var dataset = plotObj.getData();
		var tooltipcount = 0;
		var maxLabelLength = 0;
		var labelPixelXGap = 0;
		var showTooltipOnlyWhenDataOverZero = plotOption.series.showTooltipOnlyWhenDataOverZero;
		textColor = COLOR_TOOLTIP_DATA_TEXT_SINGLE
		if (mergedGraph) {
			// textColor = reverseColor(tooltipColor);
			// textColor = COLOR_TOOLTIP_DATA_TEXT_MERGED;
			textColor = COLOR_TOOLTIP_DATA_TEXT_SINGLE;
		}
		// remove common prefix in labels in dataset, to make compace label.
		var perfixToTrimFirstPriority = flotControl
				.determinSameStartsWithString(dataset);
		var rrdFileName = dataset[0].fileName;
		if (perfixToTrimFirstPriority == '')
			perfixToTrimFirstPriority = flotControl.getFileNamePrefixToTrim(
					dataset, rrdFileName);
		var pixelX = null;
		var pixelY = null;
		for (seriesIndex = 0; seriesIndex < dataset.length; seriesIndex++) {

			var series = dataset[seriesIndex];
			var xDataIndex = flotControl.getTooltipXDataIndexCloser(series,
					mousePosXAxisValue);
			var yValue = series.data[xDataIndex][1];

			if (mergedGraph) {

				if (showTooltipOnlyWhenDataOverZero && yValue <= 0) { // if
					// below
					// zero
					// then
					// don't
					// display.
					continue;
				}

				var label = plotOption.legend.labelFormatter(series.label,
						plotOption.series, perfixToTrimFirstPriority);
				contents = flotControl.tooltipDataFormatting(plotOption,
						yValue, label); // series.label
				if (contents.length > maxLabelLength)
					maxLabelLength = contents.length;

				// pixelY= plotTop+plotHeight +20+(tooltipcount%20+1)*20; //
				// absolute position
				// pixelY = plotTop + plotHeight - (tooltipcount % 20 + 1) * 20;
				// // absolute position
				var xValue = series.data[xDataIndex][0];// current x axis value
				pixelX = flotControl.getPixelXUsingXdataRatio(plotLeft,
						plotWidth, plotXaxis, xValue) + 5; // following line.
				pixelYNew = flotControl.getPixelYUsingYdataRatio(plotTop,
						plotHeight, plotYaxis, yValue) - 16; // following
				// line.

				tooltipColor = series.color;
				if (tooltipcount != 0 && tooltipcount % 20 == 0) {
					labelPixelXGap += maxLabelLength * 6;
					maxLabelLength = 0; // align tooltips by 10 items.
				}

				var MIN_LABEL_GAP = 15;
				if (pixelY != null) {
					var gap = Math.abs(pixelY - pixelYNew);
					if (gap > 0 && gap < MIN_LABEL_GAP) { // if too close to
						// previous label,
						// adjust
						if (pixelY >= pixelYNew)
							pixelYNew -= MIN_LABEL_GAP - gap;
						else
							pixelYNew += MIN_LABEL_GAP - gap;
					}
				}
				pixelY = pixelYNew;

				tooltipcount++;

			} else {
				contents = flotControl
						.tooltipDataFormatting(plotOption, yValue); // series.label

				var xValue = series.data[xDataIndex][0];// current x axis value
				pixelX = flotControl.getPixelXUsingXdataRatio(plotLeft,
						plotWidth, plotXaxis, xValue) + 5; // following line.
				pixelYNew = flotControl.getPixelYUsingYdataRatio(plotTop,
						plotHeight, plotYaxis, yValue) - 16; // following
				// line.

				var MIN_LABEL_GAP = 15;
				if (pixelY != null) {
					var gap = Math.abs(pixelY - pixelYNew);
					if (gap > 0 && gap < MIN_LABEL_GAP) { // if too close to
						// previous label,
						// adjust
						if (pixelY >= pixelYNew)
							pixelYNew -= MIN_LABEL_GAP - gap;
						else
							pixelYNew += MIN_LABEL_GAP - gap;
					}
				}
				pixelY = pixelYNew;
				tooltipColor = COLOR_TOOLTIP_DATA;
			}

			flotControl.showTooltip(makeTooltipId(graphUid + seriesIndex),
					pixelX + labelPixelXGap, pixelY, contents, textColor,
					tooltipColor, "graph_tooltip_class_data");
			plotObj.highlight(seriesIndex, xDataIndex);
			// if(DEBUG) console.log("seriesIndex, xDataIndex, xValue, yValue,
			// mousePosXAxisValue: ", seriesIndex, xDataIndex, xValue, yValue,
			// mousePosXAxisValue)
		}

		// display date tooltip
		if (!boolShowDateTooltip)
			return;

		// display x axis tip #################################################
		var xValue = series.data[xDataIndex][0];// current x axis value
		var d = new Date(xValue);
		// dataStr = tempDate.format('Y/m/d H:i:s');
		dateStr = d.getFullYear() + "/" + (d.getMonth() + 1) + "/"
				+ d.getDate() + " " + d.getHours() + ":" + d.getMinutes() + ":"
				+ d.getSeconds();

		if (mergedGraph) {
			datePixelX = pixelX - 60; // crossbar follows the middle of
			// timestamp
			datePixelY = plotTop + plotHeight + 12;

		} else {
			datePixelX = pixelX - 60; // crossbar follows the middle of
			// timestamp
			datePixelYNew = plotTop + plotHeight + 17;
			datePixelY = datePixelYNew;
		}
		flotControl.showTooltip(makeTooltipId(graphUid + seriesIndex),
				datePixelX, datePixelY, dateStr, COLOR_TOOLTIP_TIME_TEXT,
				COLOR_TOOLTIP_TIME_BG, "graph_tooltip_class_time");
	},
	// choosing closer value from mouse position.
	getTooltipXDataIndexCloser : function(series, mousePosXAxisValue) {

		var tempXDataIndex = 0;
		var seriesData = series.data;
		var length = seriesData.length;
		for (tempXDataIndex = 0; tempXDataIndex < length; tempXDataIndex++) {
			if (seriesData[tempXDataIndex][0] > mousePosXAxisValue)
				break;
		}

		var xDataIndex = tempXDataIndex;
		if (xDataIndex == length)
			xDataIndex = tempXDataIndex - 1;

		// find x point closer to mouse position.
		var p1 = seriesData[xDataIndex - 1];
		var p2 = seriesData[xDataIndex];

		if (p1 == null) {
			;
		} else if (p2 == null) {
			xDataIndex--;
		} else {
			xDataIndex = (mousePosXAxisValue - p1[0]) / (p2[0] - p1[0]) < 0.5 ? xDataIndex - 1
					: xDataIndex;// choosing closer value.
		}
		return xDataIndex;

	},

	showTooltip2 : function(toolTipId, x, y, contents, textColor, bgcolor,
			tooltipClass) {
		removeElement(toolTipId);
		// see flot.css
		// jQuery('<label id="'+toolTipId+'" class="'+tooltipClass+'"><span
		// style="color:'+textColor+';">' + contents + '</span></label>').css( {
		jQuery("body").append(
				'<label id="' + toolTipId + '" class="' + tooltipClass
						+ '" style="top:' + y + ';left:' + x
						+ ';background-color:' + bgcolor + ';color:'
						+ textColor + '" >' + contents + '</label>');
	},

	showTooltip : function(toolTipId, x, y, contents, textColor, bgcolor,
			tooltipClass) {
		removeElement(toolTipId);
		// see flot.css
		// jQuery('<label id="'+toolTipId+'" class="'+tooltipClass+'"><span
		// style="color:'+textColor+';">' + contents + '</span></label>').css( {
		jQuery(
				'<label id="' + toolTipId + '" class="' + tooltipClass
						+ '" style="color:' + textColor + '" >' + contents
						+ '</label>').css({
			top : y,
			left : x,
			'font-size' : '11px',
			'font-family' : 'Arial, Helvetica',
			'padding' : '1',
			'margin' : '2',
			'background-color' : bgcolor
		}).appendTo("body");// .fadeIn(0);
	},
	removeAllTooltip : function() {
		flotControl.removeTooltipTimeout = null;
		jQuery("label[id$=tooltip]").remove();

	},

	getPixelYUsingYdataRatio : function(plotTop, plotHeight, plotYaxis, ydata) {

		ymin = plotYaxis.min
		ymax = plotYaxis.max

		yAxisGapMax = ymax - ymin;
		yAxisGapCurrent = ydata - ymin;

		ydataPixel = plotHeight * (yAxisGapCurrent / yAxisGapMax)
		posYPixel = plotTop + plotHeight - ydataPixel
		return posYPixel;

	},

	getPixelXUsingXdataRatio : function(plotLeft, plotWidth, plotXaxis, xdata) {

		xmin = plotXaxis.min
		xmax = plotXaxis.max

		xAxisGapMax = xmax - xmin;
		xAxisGapCurrent = xdata - xmin;

		xdataPixel = plotWidth * (xAxisGapCurrent / xAxisGapMax)
		return plotLeft + xdataPixel;

	},

	getPixelXUsingXdataRatioAndMousePosition : function(plotTop, plotLeft,
			plotWidth, plotHeight, plotXaxis, mousePosXAxisValue) {

		xmin = plotXaxis.min
		xmax = plotXaxis.max
		xAxisGapMax = xmax - xmin;
		xAxisGapCurrent = mousePosXAxisValue - xmin;
		xdataPixel = plotWidth * (xAxisGapCurrent / xAxisGapMax)

		return plotLeft + xdataPixel;

	},
	tooltipDataFormatting : function(plotOption, value, label) {

		try {
			if (label == null || label == undefined)
				return plotOption.series.tooltipDataFormatter(value);
			else
				return label + "("
						+ plotOption.series.tooltipDataFormatter(value) + ")";
		} catch (e) {
			return value;
		}

	},

	// make title for graph
	// datalabel is rrdfilename without .rrd extension
	// trim prefix from datalabel only if prefix is defined in flot
	// option.labelPrefix2Trim
	// ex) ops.write => write
	makeGraphTitleFromDataLabel : function(datalabel, rrdFileName,
			labelPrefix2Trim) {

		var result = commonUtils.trimTextMiddlefix(datalabel, labelPrefix2Trim);

		// replace '_' to space.
		result = replaceUnderScore(result)
		result = replaceDash(result)
		// remove 'all' prefix
		result = commonUtils.trimTextPrefix(result, 'all');

		if (gfnEndsWith(rrdFileName, ".rrdc"))
			result += "(custom)"
		return result;
	},
	makeLabeltextFromDataLabel : function(datalabel, labelPrefix2Trim) {
		// if(DEBUG) console.log("makeLabeltextFromDataLabel ", datalabel,
		// labelPrefix2Trim)
		return commonUtils.trimTextPrefix(datalabel, labelPrefix2Trim);
	},

	// boolBytes: true(1024) false(1000)
	dataUnitFormatter : function(data, belowZeroDigit, boolBytes) {
		postfix = "";
		var unit = boolBytes ? 1024 : 1000;

		if (data / (unit * unit * unit) >= 1) {
			data = data / (unit * unit * unit);
			postfix = " G";
		} else if (data / (unit * unit) >= 1) {
			data = data / (unit * unit);
			postfix = " M";
		} else if (data / unit >= 1) {
			data = data / unit;
			postfix = " K";
		}

		return data.toFixed(belowZeroDigit) + postfix; // 3 digit below zero
	},
	// 
	dataUnitFormatterMicroSec : function(data, belowZeroDigit) {
		postfix = "";
		var unit = 1000;

		if (data / (unit * unit) >= 1) {
			data = data / (unit * unit);
			postfix = " sec";
		} else if (data / (unit) >= 1) {
			data = data / (unit);
			postfix = ' ms';
		} else {
			postfix = ' µs';
		}

		return data.toFixed(belowZeroDigit) + postfix; // 3 digit below zero
	},

	// get max data from array
	// dataset[0] = [timestamp,value]'s array.
	// result: resultMap['xValue']=timestamp;
	// resultMap['yValue']=max;
	getMaxValueInDataset : function(dataset) {
		var resultMap = new Object();
		var maxDataIndex = null;
		var maxValue = null;
		var maxTimestamp = null;
		var data = dataset.data;
		var zoommin = dataset.xaxis.min;
		var zoommax = dataset.xaxis.max;
		for ( var i = 0; i < data.length; i++) {
			timestamp = data[i][0];
			if (timestamp < zoommin)
				continue;
			if (timestamp > zoommax)
				break;

			value = data[i][1];
			if (value == null)
				continue;

			if (maxValue == null || value > maxValue) {
				maxDataIndex = i;
				maxTimestamp = data[i][0];
				maxValue = value;
			}
		}
		resultMap['maxDataIndex'] = maxDataIndex;
		resultMap['maxTimestamp'] = maxTimestamp;
		resultMap['maxValue'] = maxValue;

		return resultMap;
	},

	displayMinMaxCurrentGraph : function(graphUid) {
		if (!isLineGraphUid(graphUid)) // if not line graph, don't show .
			return;

		var mergedGraph = isMergedGraphUid(graphUid);
		// in merged graph, it is hard to find max point.

		// get plot
		var plotObj = gGraphPlotMap.get(graphUid);
		var dataset = plotObj.getData();
		// if (dataset == undefined || dataset == null || dataset.length < 0 ||
		// dataset.length > 1)
		if (dataset == undefined || dataset == null || dataset.length < 0)
			return;

		var resultMap = flotControl.getMaxValueInDataset(dataset[0]);

		var dataIndex = resultMap['maxDataIndex'];
		var xValue = resultMap['maxTimestamp'];
		var yValue = resultMap['maxValue'];
		if (dataIndex == null || xValue == null || yValue == null)
			return;

		var plotOption = plotObj.getOptions();
		// remove common prefix in labels in dataset, to make compace label.
		var perfixToTrimFirstPriority = flotControl
				.determinSameStartsWithString(dataset);
		var rrdFileName = dataset[0].fileName;
		if (perfixToTrimFirstPriority == '')
			perfixToTrimFirstPriority = flotControl.getFileNamePrefixToTrim(
					dataset, rrdFileName)

		var label = plotOption.legend.labelFormatter(dataset.label, dataset,
				perfixToTrimFirstPriority);
		contents = flotControl.tooltipDataFormatting(plotOption, yValue, label)
				+ ' max'; // series.label
		var plotOffset = plotObj.offset();
		// var plotOffset = flotControl.getPlotOffset(graphUid);
		var plotXaxis = plotObj.getAxes().xaxis;
		var plotYaxis = plotObj.getAxes().yaxis;
		// show highlight
		plotObj.highlight(0, dataIndex);

		// // show data tip
		// var pixelX = flotControl.getPixelXUsingXdataRatio(plotOffset.left,
		// plotObj.width(), plotXaxis, xValue); // following line.
		// var pixelY = flotControl.getPixelYUsingYdataRatio(plotOffset.top,
		// plotObj.height(), plotYaxis, yValue); // following line.
		// var tooltipX = pixelX - 10;
		// var tooltipY = pixelY - 15;
		var tooltipX = plotOffset.left;
		var tooltipY = plotOffset.top; // + plotObj.height() + 27;
		flotControl.showTooltip(graphUid + "_maxtip", tooltipX, tooltipY,
				contents, COLOR_TOOLTIP_DATA_TEXT_SINGLE,
				COLOR_TOOLTIP_TIME_BG_MAX, 'graph_tooltip_class_max_data');

		// show date tip
		// var datePixelX = plotOffset.left;
		var datePixelX = plotOffset.left + plotWidth - 120;
		var datePixelY = plotOffset.top; // + plotObj.height() + 27;

		var d = new Date(xValue);
		// dateStr= d.getFullYear()+"/"+(d.getMonth() + 1) +"/"+d.getDate()
		dateStr = d.getFullYear() + "/" + (d.getMonth() + 1) + "/"
				+ d.getDate() + " " + d.getHours() + ":" + d.getMinutes() + ":"
				+ d.getSeconds() + " max";
		// flotControl.showTooltip(makeTooltipId(graphUid + "_maxtip_date"),
		// datePixelX, datePixelY,
		// dateStr, COLOR_TOOLTIP_TIME_TEXT, COLOR_TOOLTIP_TIME_BG_MAX,
		// "graph_tooltip_class_max_time");
	},
	// -------------------------------------------------------------------------
	// / progress Animation //////////////////////////////////////////////////
	// -------------------------------------------------------------------------
	gProgressAnimationShowAllTimeout : null,
	gProgressAnimationHideAllTimeout : null,
	progressAnimationShowAll : function() {
		if (DEBUG)
			console.log("progressAnimationShowAll ")
		flotControl.gProgressAnimationShowAllTimeout = null;
		flotControl.progressAnimationShowOrHideAll(true);
	},
	progressAnimationHideAll : function() {

		if (DEBUG)
			console.log("progressAnimationHideAll ")
		flotControl.gProgressAnimationHideAllTimeout = null;
		flotControl.progressAnimationShowOrHideAll(false);
	},
	progressAnimationShowOrHideAll : function(flag) {

		var targetObjs = flotControl.getVisibleFactorHostObjs().find(
				"img[id$='_progressUid']");
		// var targetObjs =
		// flotControl.getVisibleFactorObjs().find("img[id$='_progressUid']");

		if (targetObjs == undefined || targetObjs.length <= 0) {
			if (DEBUG)
				console.log("progressAnimationShowOrHideAll " + flag + " 0")
			return;
		}
		if (flag == null || flag == undefined)
			flag = false;

		if (DEBUG)
			console.log("#progressAnimationShowOrHideAll " + flag
					+ targetObjs.length)

		if (flag) {
			targetObjs.show();
		} else {
			targetObjs.hide();
		}
	},

	// adds progress Animatin to graphUid's title and show.
	// not used.
	addLoadingProgressAnimationAndShow : function(graphUid) {
		var graphTitleObj = jQuery("#" + makeGraphTitleId(graphUid));
		var progressUid = makeProgressUid(graphUid);
		var progressObj = jQuery('#' + progressUid);
		if (progressObj.length == 0) {
			var progressAnimationStr = commonMainflow
					.makeProgressAnimationHtml(graphUid);
			jQuery(progressAnimationStr).appendTo(graphTitleObj).show();
		} else {
			progressObj.show();
		}

	},
	progressAnimationHideOne : function(graphUid) {
		var progressUid = makeProgressUid(graphUid);
		var obj = jQuery('#' + progressUid);
		obj.hide();
	},
	removeAllMaxtip : function() {
		flotControl.removeMaxtipTimeout = null;
		jQuery("label[id$=_maxtip]").remove();

	},
	getDataIndexValue : function(series, data) {
		var seriesData = series.data;
		// find the nearest points, x-wise
		var dataIndex = 0;
		for (dataIndex = 0; dataIndex < seriesData.length; dataIndex++) {
			if (seriesData[dataIndex][1] == data)
				break;
		}
		if (dataIndex == seriesData.length)
			--dataIndex;
		return dataIndex;
	},

	// -------------------------------------------------------------------------
	// / dashboard graph selection checkbox
	// //////////////////////////////////////////////////////////////
	// -------------------------------------------------------------------------

	gBoolSelectDashboardCharts : null,

	showCheckBoxForDashboard_allGraph : function(show) {
		if (show)
			flotControl.gBoolSelectDashboardCharts = true;
		else
			flotControl.gBoolSelectDashboardCharts = false;

		var targetObjs = jQuery("div[id^='factorGraphDiv_']").find(
				".selectGraphDiv");
		if (targetObjs == undefined || targetObjs.length <= 0)
			return;

		if (show) {
			targetObjs.css("display", "");// show
			// mark checked.
			var targetChkboxIds = flotControl
					.selectDashboardCharts_getSelCheckBoxIdLists();
			for ( var i = 0; i < targetChkboxIds.length; i++) {
				flotControl
						.selectDashboardCharts_toggleMarkOneGraph(targetChkboxIds[i]);
			}
		} else {
			targetObjs.css("display", "none");// show
			flotControl.selectDashboardCharts_clearSelectedMarkAllGraph();
		}
	},
	selectDashboardCharts_getSelCheckBoxIdLists : function() {
		// filter visible graphs under visible category
		var targetObjs = flotControl.getVisibleFactorObjs().find(
				".selectGraphDiv").find("input:checked");
		if (targetObjs == undefined)
			return new Array();
		var selChkboxList = new Array();
		for ( var i = 0; i < targetObjs.length; i++) {
			selChkboxList.push(targetObjs[i].id);
		}
		return selChkboxList;
	},
	selectDashboardCharts_clearSelectedMarkAllGraph : function() {
		var targetGraphs = flotControl
				.getVisibleFactorObjs()
				.find(
						".graph_graph_dashboard_selected, .graph_title_dashboard_selected");
		if (targetGraphs == undefined || targetGraphs.length <= 0)
			return;
		targetGraphs.removeClass("graph_graph_dashboard_selected");
		targetGraphs.removeClass("graph_title_dashboard_selected");
	},
	selectDashboardCharts_toggleMarkOneGraph : function(chkBoxId) {
		if (chkBoxId == undefined)
			return;
		var graphUid = trimPostfix(chkBoxId, '_selectCheckbox');
		var titleId = makeGraphTitleId(graphUid)
		// var checkBox = jQuery("#"+eventUid+":checked");
		var checkBox = jQuery("#" + chkBoxId);
		if (checkBox.attr("checked")) {
			// make title bolder
			jQuery("#" + graphUid).addClass("graph_graph_dashboard_selected");
			jQuery("#" + titleId).addClass("graph_title_dashboard_selected");
		} else {
			// clear
			jQuery("#" + graphUid)
					.removeClass("graph_graph_dashboard_selected");
			jQuery("#" + titleId).removeClass("graph_title_dashboard_selected");
		}

		return;

	},

	// -------------------------------------------------------------------------
	// / datalabel in bar graph. /////////////////////////////////////////////
	// -------------------------------------------------------------------------

	showDataLabelInBarGraph : function(graphUid) {
		// get plot
		var plotObj = gGraphPlotMap.get(graphUid);
		var plotOffset = plotObj.offset();
		plotTop = plotOffset.top;
		plotHeight = plotObj.height();
		// check if graph is on screen(visible)
		// if(!flotControl.isGraphVisibleByUser(plotTop, plotHeight))
		// return;

		plotLeft = plotOffset.left;
		plotWidth = plotObj.width();
		plotXaxis = plotObj.getAxes().xaxis;
		plotYaxis = plotObj.getAxes().yaxis;
		plotOption = plotObj.getOptions();

		var dataset = plotObj.getData();
		var maxLabelLength = 0;
		var labelPixelXGap = 0;
		var showTooltipOnlyWhenDataOverZero = plotOption.series.showTooltipOnlyWhenDataOverZero;
		textColor = COLOR_TOOLTIP_DATA_TEXT_SINGLE

		var perfixToTrimFirstPriority = flotControl
				.determinSameStartsWithString(dataset);
		var rrdFileName = dataset[0].fileName;
		if (perfixToTrimFirstPriority == '')
			perfixToTrimFirstPriority = flotControl.getFileNamePrefixToTrim(
					dataset, rrdFileName);
		var plotDiv = jQuery("#" + graphUid);

		for (seriesIndex = 0; seriesIndex < dataset.length; seriesIndex++) {

			var series = dataset[seriesIndex];
			if (series.label == 'dummy' || series.label == '') // dummy.
				continue;
			var seriesData = series.data;
			var dataIndex = 0;
			for (dataIndex = 0; dataIndex < seriesData.length; dataIndex++) {
				var xValue = seriesData[dataIndex][0];
				var yValue = seriesData[dataIndex][1];
				// var label = plotOption.legend.labelFormatter( series.label,
				// plotOption.series, perfixToTrimFirstPriority );
				contents = flotControl
						.tooltipDataFormatting(plotOption, yValue); // series.label
				pixelX = flotControl.getPixelXUsingXdataRatio(plotLeft,
						plotWidth, plotXaxis, xValue)// flowing data point.
				canvasPixelX = pixelX - plotLeft + 15;
				pixelY = flotControl.getPixelYUsingYdataRatio(plotTop,
						plotHeight, plotYaxis, yValue); // following line.
				canvasPixelY = pixelY - plotTop - 5;
				// tooltipColor= series.color;
				tooltipColor = COLOR_TOOLTIP_DATA;
				flotControl.showDataLabel(plotDiv, makeDataLabelId(graphUid
						+ seriesIndex), canvasPixelX, canvasPixelY, contents,
						textColor, tooltipColor, "graph_tooltip_class_data");
			}
		}
	},

	showDataLabel : function(plotDiv, labelId, x, y, contents, textColor,
			bgcolor, tooltipClass) {
		removeElement(labelId);

		plotDiv.append('<div style="position:absolute;left:' + x + 'px;top:'
				+ y + 'px;color:' + textColor + ';font-size:smaller">'
				+ contents + '</div>');
	},

	// set color by all zero data and threshhold
	setGraphColorAndCheckDataForSingleLineChart : function(graphUid,
			flotOptionlocal, dataset) {
		// change graph color to
		var graphMetaDataObj = gGraphMetaDataMap
				.get(getOrigGraphUidFromFinalGraphUid(graphUid));
		if (graphMetaDataObj == undefined)
			graphMetaDataObj = gGraphMetaDataMap.get(graphUid);

		var factorCateName = graphMetaDataObj['factorCateName'];
		var factorDirName = graphMetaDataObj['factorDirName'];

		var doCheckThreshhod = true;

		if (factorCateName == 'cpu'
				&& gfnStartsWith(factorDirName, "all_sum_of_")) {
			doCheckThreshhod = false;
		} else if (factorCateName == 'memcached_stat'
				&& gfnStartsWith(factorDirName,
						"all_memcached_memory_usage_of_")) {
			flotOptionlocal.series['thresholdSetting'] = function(label, option) {
				option.series['thresholdOverValue'] = THRESHOLD_OVER_MEM_USED;
				option.series['thresholdOverColor'] = THRESHOLD_OVER_MEM_USED_COLOR;
			}

			flotOptionlocal.yaxis.tickFormatter = function(v, axis) {
				return v.toFixed(axis.tickDecimals) + "%";
			}
			flotOptionlocal.series['tooltipDataFormatter'] = function(value) {
				if (value == null)
					return "nodata";
				return Math.round(value * 100) / 100 + "%";
			}
			flotOptionlocal.yaxis.max = 100;
			doCheckThreshhod = true;
		}

		return flotControl.setGraphColorAndCheckDataForSingleLineChart_sub(
				graphUid, flotOptionlocal, dataset, doCheckThreshhod);

	},
	// set color by all zero data and threshhold
	setGraphColorAndCheckDataForSingleLineChart_sub : function(graphUid,
			flotOptionlocal, dataset, doCheckThreshhod) {

		var thresholdover = null;
		var thresholdcolor = null;
		if (doCheckThreshhod) {
			try {
				flotOptionlocal.series.thresholdSetting('', flotOptionlocal);
				thresholdover = flotOptionlocal.series.thresholdOverValue;
				thresholdcolor = flotOptionlocal.series.thresholdOverColor;
			} catch (e) {
			}
		}
		var rrdFetchLevel = UIControl.getSelectedRrdFetchLevel();
		var useThisColor = null;
		if (rrdFetchLevel == 'MAX') {
			useThisColor = COLOR_SINGLE_NONZERO_MAX;
		} else if (rrdFetchLevel == 'AVERAGE') {
			useThisColor = COLOR_SINGLE_NONZERO_AVG;
		}

		for ( var index = 0; index < dataset.length; index++) {
			// check color by data.
			dataset = flotControl.checkAllData(flotOptionlocal, dataset, index,
					thresholdover, thresholdcolor, doCheckThreshhod,
					useThisColor);

			if (dataset[index].color == COLOR_SINGLE_ZERODATA)
				continue;
			// check color by labelname.
			lowercaseLavel = dataset[index].label.toLowerCase();
			for (i = 0; i < WARN_WORDS.length; i++) {
				if (lowercaseLavel.indexOf(WARN_WORDS[i]) >= 0) {
					dataset[index].color = WARN_WORDS_COLOR;
					flotControl
							.setFlotOptionToDataForLine(dataset[index],
									WARN_WORDS_COLOR, WARN_ALPHA_BOTTOM,
									WARN_ALPHA_TOP);
					break;
				}
			}
		}
		return dataset;

	},

	// if cpu and sum data , does't check threshhod.(this is filtered prior this
	// call.)
	checkAllData : function(flotOptionlocal, dataset, index,
			thresholdoverArray, thresholdcolorArray, doCheckThreshhod,
			useThisColor) {

		var data = dataset[index].data;
		var doThresholdChecking = false;
		if (thresholdoverArray != null && thresholdoverArray.length > 0)
			doThresholdChecking = true;

		var startXIndex = flotOptionlocal.xaxis.min;
		var endXIndex = flotOptionlocal.xaxis.max;

		var colorDecisionFlag = 0;// 0(allzero-> default), 1(normal) ,
		// 2(overthreshhod)
		var currentThresholdColorIndex = null;
		for ( var dataIndex = 0; dataIndex < data.length; dataIndex++) {
			xValue = data[dataIndex][0];
			if (startXIndex != null && xValue < startXIndex)
				continue;
			if (endXIndex != null && endXIndex < xValue) // skip to check
			// zoomed data
			{
				continue;
			}

			yValue = data[dataIndex][1];
			if (yValue == 'null')
				continue;

			if (doThresholdChecking) {
				tempThresholdColorIndex = flotControl.checkThresholdColor(
						yValue, thresholdoverArray, currentThresholdColorIndex);
				if (tempThresholdColorIndex != null) {
					currentThresholdColorIndex = tempThresholdColorIndex
					colorDecisionFlag = 2;
				}
			}
			if (colorDecisionFlag != 2 && yValue > 0) { // if thresholdIndex
				// found, then skip.
				colorDecisionFlag = 1;
			}
		}

		if (colorDecisionFlag == 0) {
			dataset[index].color = COLOR_SINGLE_ZERODATA;
		} else if (colorDecisionFlag == 2
				&& currentThresholdColorIndex < thresholdcolorArray.length) {
			color = thresholdcolorArray[currentThresholdColorIndex];
			dataset[index].color = color;
			flotControl.setFlotOptionToDataForLine(dataset[index], color,
					THRS_CPU_ALPHA_BOTTOM, THRS_CPU_ALPHA_TOP);
		} else {
			if (useThisColor != null) {
				dataset[index].color = useThisColor;
			} else {
				dataset[index].color = COLOR_SINGLE_NONZERO[dataset.length
						- index - 1];
			}
		}

		return dataset;
	},
	// return null if below threshold
	// return null if found thresholdColorIndex is lower then
	// currentThresholdColorIndex
	checkThresholdColor : function(yValue, thresholdoverSplited,
			currentThresholdColorIndex) {
		var tempThresholdColorIndex = null;
		for ( var i = 0; i < thresholdoverSplited.length; i++) {
			if (yValue >= thresholdoverSplited[i]) {
				if (currentThresholdColorIndex == null
						|| i > currentThresholdColorIndex)
					tempThresholdColorIndex = i;
			}
		}
		return tempThresholdColorIndex;
	}
};
