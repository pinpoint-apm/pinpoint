var oServerMap = null;

function showServerMap(applicationName) {
	var containerId = "servermap";
	
	var serverMapCallback = function(data) {
		if (data.applicationMapData.nodeDataArray.length == 0) {
			warning("NO DATA", "");
		} else {
			clearAllWarnings();
		}
		
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
    	$("#" + containerId).show();
    };

    if (isQueryFromNow()) {
        getLastServerMapData2($("#application").val(), getQueryPeriod(), serverMapCallback);
    } else {
        getServerMapData2($("#application").val(), getQueryStartTime(), getQueryEndTime(), serverMapCallback);
    }
}

var nodeClickHandler = function(e, data, containerId) {
	if (data.serviceType == "CLIENT") {
		if ($("DIV.nodeinfo" + data.id).length == 0) {
			var htOffset = $(containerId).offset();
			var box = $('#ClientBox')
							.tmpl(data)
							.css({'top':e.pageY - htOffset.top, 'left':e.pageX - htOffset.left})
							.attr('class', 'nodeinfo' + data.id);
			box.appendTo(containerId);
		}	
	} else {
		if ($("DIV.nodeinfo" + data.id).length == 0) {
			var htOffset = $(containerId).offset();
			var box = $('#ServerBox')
						.tmpl(data)
						.css({'top':e.pageY - htOffset.top, 'left':e.pageX - htOffset.left})
						.attr('class', 'nodeinfo' + data.id);
			box.appendTo(containerId);
		}
	}
}

var linkClickHandler = function(e, data, containerId) {
	if ($("DIV.linkinfo" + data.id).length > 0) {
		return;
	}
	
	var htOffset = $(containerId).offset();
	var box = $('#EdgeBox')
				.tmpl(data)
				.css({'top':e.pageY - htOffset.top, 'left':e.pageX - htOffset.left})
				.attr('class', 'linkinfo' + data.id);
	box.appendTo(containerId);
}