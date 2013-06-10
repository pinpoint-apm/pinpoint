var oServerMap = null;
var FILTER_DELIMETER = "^";
var FILTER_ENTRY_DELIMETER = "|";

function nodeStatistics() {
	
}

function linkStatistics(
		begin,
		end,
		srcServiceType,
		srcApplicationName,
		destServiceType,
		destApplicationName) {
	
	var params = {
		"from" : begin,
		"to" : end,
		"srcServiceType" : srcServiceType,
		"srcApplicationName" : srcApplicationName,
		"destServiceType" : destServiceType,
		"destApplicationName" : destApplicationName
	}

	var showFailedRateChart = function(data) {
		$("#linkInfoDetails .linkInfoSFChart").show();
		nv.addGraph(function() {
//			var chart = nv.models.stackedAreaChart().x(function(d) {
			var chart = nv.models.multiBarChart().x(function(d) {
				return d[0];
			}).y(function(d) {
				return d[1];
			}).clipEdge(true).color(function(d) {
				if (d.key == "Success") {
					return "green";
				} else if (d.key == "Failed") {
					return "red";
				} else {
					return nv.utils.getColor(d);
				}
			}).showControls(false);//.style('expand').showControls(false);

			chart.stacked(true);
			
			chart.xAxis.tickFormat(function(d) {
				return d3.time.format('%x %H:%M')(new Date(d));
			});

			// chart.yAxis.tickFormat(d3.format(',.2f'));
			chart.yAxis.tickFormat(function(d) {
				return d;
			});

			d3.select('#linkInfoDetails .linkInfoSFChart svg')
				.datum(data)
				.transition()
				.duration(500)
				.call(chart);

			nv.utils.windowResize(chart.update);

			return chart;
		});
	};
	
	var showSummary = function(data) {
		$("#linkInfoDetails .linkInfoBarChart").show();
		nv.addGraph(function() {
			var chart = nv.models.discreteBarChart().x(function(d) {
				return d.label;
			}).y(function(d) {
				return d.value;
			}).staggerLabels(false).tooltips(false).showValues(true);
	
			chart.xAxis.tickFormat(function(d) {
				if($.isNumeric(d)) {
					return (d >= 1000) ? d / 1000 + "s" : d + "ms";
				}
				return d;
			});
			
			chart.yAxis.tickFormat(function(d) {
				return d;
			});
			
			chart.valueFormat(function(d) {
				return d;
			});
			
			d3.select('#linkInfoDetails .linkInfoBarChart svg')
					.datum(data)
					.transition()
					.duration(500)
					.call(chart);
	
			nv.utils.windowResize(chart.update);
	
			return chart;
		});
	}

	var showTimeseriesHistogram = function(data) {
		$("#linkInfoDetails .linkInfoChart").show();
		nv.addGraph(function() {
//			var chart = nv.models.stackedAreaChart().x(function(d) {
			var chart = nv.models.multiBarChart().x(function(d) {
				return d[0];
			}).y(function(d) {
				return d[1];
			}).clipEdge(true).showControls(false);
			
			chart.stacked(true);
			
			chart.xAxis.tickFormat(function(d) {
				return d3.time.format('%H:%M')(new Date(d));
			});
			
			chart.yAxis.tickFormat(function(d) {
				return d;
			});
						
			d3.select('#linkInfoDetails .linkInfoChart svg')
			.datum(data)
			.transition()
			.duration(500)
			.call(chart);
			
			nv.utils.windowResize(chart.update);
			
			return chart;
		});
	}; 
	
	$("#statisticsProgressbar").show();
	getLinkStatisticsData(params, function(query, result) {
		$("#statisticsProgressbar").hide();
		showFailedRateChart(result.timeseriesFailRate);
		showSummary(result.histogramSummary);
		showTimeseriesHistogram(result.timeseriesHistogram);
	});
}

function filteredLinkStatistics(
		applicationName,
		serviceType,
		begin,
		end,
		srcServiceType,
		srcApplicationName,
		destServiceType,
		destApplicationName,
		prevFilter) {
	
	var params = {
			"application" : applicationName,
			"serviceType" : serviceType,
			"from" : begin,
			"to" : end,
			"srcServiceType" : srcServiceType,
			"srcApplicationName" : srcApplicationName,
			"destServiceType" : destServiceType,
			"destApplicationName" : destApplicationName,
			"filter" : prevFilter
	}
	window.open("/filteredLinkStatistics.pinpoint?" + decodeURIComponent($.param(params)), "");
}

function filterPassingTransaction(
			applicationName,
			serviceType,
			begin,
			end,
			srcServiceType,
			srcApplicationName,
			destServiceType,
			destApplicationName,
			prevFilter) {
	
	if (srcServiceType == "CLIENT") {
		applicationName = srcApplicationName = destApplicationName;
	}
	
	var params = {
		"application" : applicationName,
		"serviceType" : serviceType,
		"from" : begin,
		"to" : end,
		"filter" : ((prevFilter) ? prevFilter + FILTER_DELIMETER : "")
					+ srcServiceType + FILTER_ENTRY_DELIMETER
					+ srcApplicationName + FILTER_ENTRY_DELIMETER
					+ destServiceType + FILTER_ENTRY_DELIMETER
					+ destApplicationName
	}
	window.open("/filtermap.pinpoint?" + decodeURIComponent($.param(params)), "");
}

function getLinkStatisticsData(query, callback) {
    jQuery.ajax({
    	type : 'GET',
    	url : '/linkStatistics.pinpoint',
    	cache : false,
    	dataType: 'json',
    	data : {
			from : query.from,
			to : query.to,
			srcServiceType : query.srcServiceType,
			srcApplicationName : query.srcApplicationName,
			destServiceType : query.destServiceType,
			destApplicationName : query.destApplicationName
    	},
    	success : function(result) {
    		callback(query, result);
    	},
    	error : function(xhr, status, error) {
    		console.log("ERROR", status, error);
    	}
    });
}

function getServerMapData2(query, callback) {
    jQuery.ajax({
    	type : 'GET',
    	url : '/getServerMapData2.pinpoint',
    	cache : false,
    	dataType: 'json',
    	data : {
    		application : query.applicationName,
    		serviceType : query.serviceType,
    		from : query.from,
    		to : query.to,
    		hideIndirectAccess : query.hideIndirectAccess
    	},
    	success : function(result) {
    		callback(query, result);
    	},
    	error : function(xhr, status, error) {
    		console.log("ERROR", status, error);
    	}
    });
}

function getLastServerMapData2(query, callback) {
    jQuery.ajax({
    	type : 'GET',
    	url : '/getLastServerMapData2.pinpoint',
    	cache : false,
    	dataType: 'json',
    	data : {
    		application : query.applicationName,
    		serviceType : query.serviceType,
    		period : query.period,
    		hideIndirectAccess : query.hideIndirectAccess
    	},
    	success : function(result) {
    		callback(query, result);
    	},
    	error : function(xhr, status, error) {
    		console.log("ERROR", status, error);
    	}
    });
}

function getFilteredServerMapData(query, callback) {
    jQuery.ajax({
    	type : 'GET',
    	url : '/getFilteredServerMapData.pinpoint',
    	cache : false,
    	dataType: 'json',
    	data : {
    		application : query.applicationName,
    		serviceType : query.serviceType,
    		from : query.from,
    		to : query.to,
    		filter : query.filter
    	},
    	success : function(result) {
    		callback(query, result);
    	},
    	error : function(xhr, status, error) {
    		console.log("ERROR", status, error);
    	}
    });
}

var serverMapCachedData;
var serverMapCachedQuery;

function toggleMergeUnknowns(e) {
	var target = $("#mergeUnknown");
	var selected = target.data('selected');
	
	if (selected) {
		target.data('selected', false);
		selected = false;
		target.html('<i class="icon-ok icon-white"></i> Merge unknowns</a>');
	} else {
		target.data('selected', true);
		selected = true;
		target.html('<i class="icon-ok"></i> Merge unknowns</a>');
	}
	
	if (serverMapCachedData && serverMapCachedQuery) {
		$(".nodeinfo").remove();
		$(".linkinfo").remove();
		if (oServerMap) {
			oServerMap.clear();
		}
		serverMapCallback(serverMapCachedQuery, serverMapCachedData, true);
	}
}

function toggleHideIndirectAccess(e) {
	var target = $("#hideIndirectAccess");
	var selected = target.data('selected');
	
	if (selected) {
		target.data('selected', false);
		selected = false;
		target.html('<i class="icon-ok icon-white"></i> Hide indirect access');
	} else {
		target.data('selected', true);
		selected = true;		
		target.html('<i class="icon-ok"></i> Hide indirect access');
	}

	$("#progressbar").show();
	showServerMap(Nav.getApplicationName(), Nav.getServiceType(), Nav.getQueryStartTime(), Nav.getQueryEndTime(), Nav.getQueryPeriod(), Nav.isQueryFromNow(), null, Nav.isHideIndirectAccess(), function() { $("#progressbar").hide(); });
}

var serverMapCallback = function(query, data, ignoreCache) {
	var containerId = "servermap";
	
	var cloneObject = function(obj) {
	    var newObj = (obj instanceof Array) ? [] : {};
	    for (var i in obj) {
	        if (obj[i] && typeof obj[i] == "object") {
	            newObj[i] = cloneObject(obj[i]);
	        } else {
	            newObj[i] = obj[i];
	        }
	    }
	    return newObj;
	}; 
	
	serverMapCachedQuery = cloneObject(query);
	serverMapCachedData = cloneObject(data);
	
	if (data.applicationMapData.nodeDataArray.length == 0) {
		warning("NO DATA", "");
		return;
	} else {
		clearAllWarnings();
		$("#" + containerId).show();
	}

	if ($('#mergeUnknown').data('selected')) {
	// if ($('#mergeUnknown').is(':checked')) {
		mergeUnknown(data);
	}

	replaceClientToUser(data);

	if (oServerMap == null) {
		oServerMap = new ServerMap({
	        sContainerId : containerId,
			"sImageDir" : '/images/icons/',
			"htIcons" : {
				'APACHE' : 'APACHE.png',
				'ARCUS' : 'ARCUS.png',
				'CUBRID' : 'CUBRID.png',
				'ETC' : 'ETC.png',
				'MEMCACHED' : 'MEMCACHED.png',
				'MYSQL' : 'MYSQL.png',
				'QUEUE' : 'QUEUE.png',
				'TOMCAT' : 'TOMCAT.png',
				'UNKNOWN_CLOUD' : 'UNKNOWN_CLOUD.png',
				'UNKNOWN_GROUP' : 'UNKNOWN_CLOUD.png',
				'USER' : 'USER.png'
			},
			"htLinkTheme" : {
				"default" : {
					"background" : { 0: "rgb(240, 240, 240)", 0.3: "rgb(240, 240, 240)", 1: "rgba(240, 240, 240, 1)"},
					"border" : "gray",
					"font" : "10pt calibri, helvetica, arial, sans-serif",
					"color" : "#919191",
					"align" : "center",
					"margin" : 1
				},
				"good" : {
					// "background" : { 0: "rgb(240, 1, 240)", 0.3: "rgb(240, 1, 240)", 1: "rgba(240, 1, 240, 1)"},
					"background" : { 0: "#2CA02C"},
					"border" : "green",
					"font" : "10pt calibri, helvetica, arial, sans-serif",
					"color" : "#919191",
					"align" : "center",
					"margin" : 1
				},
				"bad" : {
					// "background" : { 0: "rgb(214, 27, 28)", 0.3: "rgb(214, 27, 28)", 1: "rgba(214, 27, 28, 1)"},
					// "background" : { 0: "#D62728" },
					"background" : { 0: "rgba(200, 27, 28, 1)" },
					"border" : "#FFFFFF",
					"font" : "10pt calibri, helvetica, arial, sans-serif",
					"color" : "#FFFFFF",
					"align" : "center",
					"margin" : 1
				}
			},
			fOnNodeContextClick : function(e, data) {
				nodeContextClickHandler(e, query, data, "#" + containerId);
			},
			fOnLinkContextClick : function(e, data) {
				linkContextClickHandler(e, query, data, "#" + containerId);
			},
			fOnLinkClick : function(e, data) {
				linkClickHandler(e, query, data, "#" + containerId);
			},
			fOnNodeClick : function(e, data) {
				nodeClickHandler(e, query, data, "#" + containerId);
			}
	    });
	} else {
		oServerMap.option({
			fOnNodeContextClick : function(e, data) {
				nodeContextClickHandler(e, query, data, "#" + containerId);
			},
			fOnLinkContextClick : function(e, data) {
				linkContextClickHandler(e, query, data, "#" + containerId);
			},
			fOnLinkClick : function(e, data) {
				linkClickHandler(e, query, data, "#" + containerId);
			},
			fOnNodeClick : function(e, data) {
				nodeClickHandler(e, query, data, "#" + containerId);
			}
		});
	}
    oServerMap.load(data.applicationMapData);
};

function showServerMap(applicationName, serviceType, from, to, period, usePeriod, filterText, hideIndirectAccess, cb) {
	console.log("showServerMap", applicationName, serviceType, from, to, period, usePeriod, filterText, hideIndirectAccess, cb);
	
	emptyDetailPanel();
	$(".nodeinfo").remove();
	$(".linkinfo").remove();

	if (oServerMap) {
		oServerMap.clear();
	}
	
	var query = {
		applicationName : applicationName,
		serviceType : serviceType, 
		from : from,
		to : to, 
		period : period,
		usePeriod : usePeriod,
		filter : filterText,
		hideIndirectAccess : hideIndirectAccess
	};

	console.log("filterText", filterText);
    
    if (filterText) {
    	getFilteredServerMapData(query, function(query, result) {
	    		if (cb) {
	    			cb(query, result);
	    		}
	    		serverMapCallback(query, result);
    		});
    } else if (usePeriod) {
        getLastServerMapData2(query, function(query, result) {
    		if (cb) {
    			cb(query, result);
    		}
    		serverMapCallback(query, result);
		});
    } else {
        getServerMapData2(query, function(query, result) {
    		if (cb) {
    			cb(query, result);
    		}
    		serverMapCallback(query, result);
		});
    }
}

// TODO 임시코드로 나중에 USER와 backend를 구분할 예정.
var replaceClientToUser = function(data) {
	var nodes = data.applicationMapData.nodeDataArray;
	nodes.forEach(function(node) {
		if (node.category == "CLIENT") {
			node.category = "USER";
			node.text = "USER";
		}
	});
}

var mergeUnknown = function(data) {
	var nodes = data.applicationMapData.nodeDataArray;
	var links = data.applicationMapData.linkDataArray;
	
	var inboundCountMap = {};
	nodes.forEach(function(node) {
		if (!inboundCountMap[node.key]) {
			inboundCountMap[node.key] = {
				"sourceCount" : 0,
				"totalCallCount" : 0
			};
		}
		
		links.forEach(function(link) {
			if (link.to == node.key) {
				inboundCountMap[node.key].sourceCount++;
				inboundCountMap[node.key].totalCallCount += link.text;
			}
		});
	});
	
	var newNodeList = [];
	var newLinkList = [];
	
	var removeNodeIdSet = {};
	var removeLinkIdSet = {};
	
	nodes.forEach(function(node, nodeIndex) {
		if (node.category == "UNKNOWN_CLOUD") {
			return;
		}
		
		var newNode;
		var newLink;
		var newNodeKey = "UNKNOWN_GROUP_" + node.key;

		var unknownCount = 0;
		links.forEach(function(link, linkIndex) {
			if (link.from == node.key &&
				link.targetinfo.serviceType == "UNKNOWN_CLOUD" &&
				inboundCountMap[link.to] && inboundCountMap[link.to].sourceCount == 1) {
				unknownCount++;
			}
		});
		if (unknownCount < 2) {
			return;
		}
		
		// for each children.
		links.forEach(function(link, linkIndex) {
			if (link.targetinfo.serviceType != "UNKNOWN_CLOUD") {
				return;
			}
			if (inboundCountMap[link.to] && inboundCountMap[link.to].sourceCount > 1) {
				return;
			}

			// branch out from current node.
			if (link.from == node.key) {
				if (!newNode) {
					newNode = {
				    	"id" : newNodeKey,
				    	"key" : newNodeKey,
					    "text" : "",
					    "hosts" : [],
					    "category" : "UNKNOWN_GROUP",
					    "terminal" : "true",
					    "agents" : [],
					    "fig" : "FramedRectangle"
					}
				}
				if (!newLink) {
					newLink = {
					    	"id" : node.key + "-" + newNodeKey,
							"from" : node.key,
							"to" : newNodeKey,
							"sourceinfo" : [],
							"targetinfo" : [],
							"text" : 0,
							"error" : 0,
							"slow" : 0,
							"rawhistogram" : [],
							"histogram" : {}
					};
				}
				
				// fill the new node/link informations.
				newNode.text += link.targetinfo.applicationName + " (" + link.text + ")\n";

				newLink.text += link.text;
				newLink.error += link.error;
				newLink.slow += link.slow;
				newLink.sourceinfo.push(link.sourceinfo);
				newLink.targetinfo.push(link.targetinfo);
				newLink.rawhistogram.push(link.histogram);

				$.each(link.histogram, function(key, value) {
					if (newLink.histogram[key]) {
						newLink.histogram[key] += value;	
					} else {
						newLink.histogram[key] = value;
					}
				});
				
				removeNodeIdSet[link.to] = null;
				removeLinkIdSet[link.id] = null;
			}
		});
		
		if (newNode) {
			newNodeList.push(newNode);
		}
		
		if (newLink) {
			if ((newLink.error / newLink.text * 100) > 10) {
				newLink.category = "bad";
			} else {
				newLink.category = "default";
			}
			console.log(newLink);
			newLinkList.push(newLink);
		}
	});
	
	newNodeList.forEach(function(newNode) {
		data.applicationMapData.nodeDataArray.push(newNode);
	});
	
	newLinkList.forEach(function(newLink) {
		data.applicationMapData.linkDataArray.push(newLink);
	});
	
	$.each(removeNodeIdSet, function(key, val) {
		nodes.forEach(function(node, i) {
			if (node.id == key) {
				nodes.splice(i, 1);
			}
		});
	});
	
	$.each(removeLinkIdSet, function(key, val) {
		links.forEach(function(link, i) {
			if (link.id == key) {
				links.splice(i, 1);
			}
		});			
	});
}

var nodeContextClickHandler = function(e, query, data, containerId) {
	if ($("DIV.nodeinfo" + data.id).length > 0) {
		$("DIV.nodeinfo" + data.id).remove();
		return;
	}
	
	data.query = query;
	var htOffset = $(containerId).offset();
	var template;
	if (data.category == "CLIENT") {
		template = $('#ClientContextInfoBox');
	} else if (data.category == "UNKNOWN_GROUP") {
		template = $('#UnknownGroupContextInfoBox');
	} else {
		template = $('#ApplicationContextInfoBox');
	}
	
	var box = template
				.tmpl(data)
				.css({'top':e.pageY - htOffset.top, 'left':e.pageX - htOffset.left, 'z-index':300})
				.addClass('nodeinfo')
				.addClass('nodeinfo' + data.id);
	
	box.appendTo($(containerId).parent());
}

var linkContextClickHandler = function(e, query, data, containerId) {
	if ($("DIV.linkinfo" + data.id).length > 0) {
		$("DIV.linkinfo" + data.id).remove();
		return;
	}
	data.query = query;
	var htOffset = $(containerId).offset();
	var box = $('#LinkContextInfoBox')
				.tmpl(data)
				.css({'top':e.pageY - htOffset.top, 'left':e.pageX - htOffset.left, 'z-index':300})
				.addClass('linkinfo')
				.addClass('linkinfo' + data.id);
	box.appendTo($(containerId).parent());
}

var nodeClickHandler = function(e, query, data, containerId) {
	emptyDetailPanel();
	
	data.query = query;
	var htOffset = $(containerId).offset();
	var template;
	if (data.category == "CLIENT") {
		template = $('#ClientContextInfoBox');
	} else if (data.category == "UNKNOWN_GROUP") {
		template = $('#UnknownGroupContextInfoBox');
	} else {
		template = $('#ApplicationContextInfoBox');
	}
	
	var box = template
				.tmpl(data)
				.css({'top':e.pageY - htOffset.top, 'left':e.pageX - htOffset.left, 'z-index':300})
				.addClass('nodeinfo')
				.addClass('nodeinfo' + data.id);
	
	$('#nodeInfoDetails .info').append($('#NodeInfoBox').tmpl(data));
}

var linkClickHandler = function(e, query, data, containerId) {
	console.log("link data", data);
	
	emptyDetailPanel();
	
	// rawhistogram이 있는 녀석은 상세정보 조회 불가.
	// TODO rawhistogram말고 다른 정보로 판단하도록 수정하기.
	if (data.rawhistogram) {
		$('#linkInfoDetails .info').text("merge된 연결선은 상세정보를 조회할 수 없습니다. 맵 옵션에서 'merge unknown'을 해제하고 조회하세요.");	
		return;
	}
	
	/*
	if (data.sourceinfo.serviceType == "CLIENT") {
		$('#linkInfoDetails .info').text("CLIENT 정보는 아직 제공하지 않습니다.");	
		return;
	}
	*/
	
	data.query = query;
	$('#linkInfoDetails .info').append($('#LinkInfoBox').tmpl(data));

	linkStatistics(	query.from,
					query.to,
					data.sourceinfo.serviceTypeCode,
					data.sourceinfo.applicationName,
					data.targetinfo.serviceTypeCode,
					data.targetinfo.applicationName);

	/*
	showLinkHistogramDetailed();
	showLinkHistogramSum();
	showLinkSuccessOrFailedDetailed();
	*/
}

var emptyDetailPanel = function() {
	$('#nodeInfoDetails .info').empty();
	$('#linkInfoDetails .info').empty();
	
	$("#linkInfoDetails .linkInfoBarChart").hide();
	$("#linkInfoDetails .linkInfoChart").hide();
	$("#linkInfoDetails .linkInfoSFChart").hide();
	
	$("#linkInfoDetails .linkInfoBarChart svg").empty();
	$("#linkInfoDetails .linkInfoChart svg").empty();
	$("#linkInfoDetails .linkInfoSFChart svg").empty();
}