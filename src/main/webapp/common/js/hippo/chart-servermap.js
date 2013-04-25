var oServerMap = null;

function getServerMapData2(application, begin, end, callback) {
    var app = application.split("@");
	d3.json("/getServerMapData2.hippo?application=" + app[0] + "&serviceType=" + app[1] + "&from=" + begin + "&to=" + end, function(d) { callback(d); });
}

function getLastServerMapData2(application, period, callback) {
    var app = application.split("@");
	d3.json("/getLastServerMapData2.hippo?application=" + app[0] + "&serviceType=" + app[1] + "&period=" + period, function(d) { callback(d); });
}

function getServerMapData(application, begin, end, callback) {
    var app = application.split("@");
	d3.json("/getServerMapData.hippo?application=" + app[0] + "&from=" + begin + "&to=" + end, function(d) { callback(d); });
}

function getLastServerMapData(application, period, callback) {
    var app = application.split("@");
	d3.json("/getLastServerMapData.hippo?application=" + app[0] + "&period=" + period, function(d) { callback(d); });
}

function showServerMap(applicationName) {
	var containerId = "servermap";
	
	if (oServerMap) {
		oServerMap.clear();
	}
	
	var serverMapCallback = function(data) {
		if (data.applicationMapData.nodeDataArray.length == 0) {
			warning("NO DATA", "");
		} else {
			clearAllWarnings();
		}
		
		mergeUnknown(data);
		
		console.log(data);
		
		if (oServerMap == null) {
			oServerMap = new ServerMap({
		        sContainerId : containerId,
				fOnNodeClick : function(e, data) {
					nodeClickHandler(e, data, "#" + containerId);
				},
				fOnLinkClick : function(e, data) {
					linkClickHandler(e, data, "#" + containerId);
				}
		    });
		}
		
	    oServerMap.load(data.applicationMapData);
    };

    if (isQueryFromNow()) {
        getLastServerMapData2($("#application").val(), getQueryPeriod(), serverMapCallback);
    } else {
        getServerMapData2($("#application").val(), getQueryStartTime(), getQueryEndTime(), serverMapCallback);
    }
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

var nodeClickHandler = function(e, data, containerId) {
	if (data.category == "CLIENT") {
		if ($("DIV.nodeinfo" + data.id).length == 0) {
			var htOffset = $(containerId).offset();
			var box = $('#ClientBox')
							.tmpl(data)
							.css({'top':e.pageY - htOffset.top, 'left':e.pageX - htOffset.left, 'z-index':300})
							.attr('class', 'nodeinfo' + data.id);
			box.appendTo($(containerId).parent());
		}	
	} else if (data.category == "UNKNOWN_GROUP") {
		if ($("DIV.nodeinfo" + data.id).length == 0) {
			var htOffset = $(containerId).offset();
			var box = $('#UnknownGroupBox')
			.tmpl(data)
			.css({'top':e.pageY - htOffset.top, 'left':e.pageX - htOffset.left, 'z-index':300})
			.attr('class', 'nodeinfo' + data.id);
			box.appendTo($(containerId).parent());
		}
	} else {
		if ($("DIV.nodeinfo" + data.id).length == 0) {
			var htOffset = $(containerId).offset();
			var box = $('#ApplicationBox')
						.tmpl(data)
						.css({'top':e.pageY - htOffset.top, 'left':e.pageX - htOffset.left, 'z-index':300})
						.attr('class', 'nodeinfo' + data.id);
			box.appendTo($(containerId).parent());
		}
	}
}

var linkClickHandler = function(e, data, containerId) {
	if ($("DIV.linkinfo" + data.id).length > 0) {
		return;
	}
	
	var htOffset = $(containerId).offset();
	var box = $('#LinkInfoBox')
				.tmpl(data)
				.css({'top':e.pageY - htOffset.top, 'left':e.pageX - htOffset.left, 'z-index':300})
				.attr('class', 'linkinfo' + data.id);
	box.appendTo($(containerId).parent());
}