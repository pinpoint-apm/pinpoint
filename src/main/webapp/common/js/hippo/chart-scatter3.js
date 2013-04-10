function showResponseScatter(applicationName) {
	if (oScatterChart) {
		oScatterChart.clear();
	}
	$("#scattercharttitle").text("'" + applicationName + "' response scatter")
	$("#scattercharttitle").show();
	drawScatter(applicationName, getQueryStartTime(), getQueryEndTime(), "scatterchart");
    if (isQueryFromNow()) {
		getLastScatterData(applicationName, getQueryPeriod(), scatterFetchDataCallback);
    } else {
        getScatterData(applicationName, getQueryStartTime(), getQueryEndTime(), scatterFetchDataCallback);
    }
}

var scatterFetchDataCallback = function(data) {
	// 처음 조회된 데이터를 그려준다.
    updateScatter(getQueryStartTime(), getQueryEndTime(), data.scatter2, "#scatter");
    
    if (data.scatter2.length == 0) {
    	return;
    }
    
    // 데이터 조회가 추가로 필요한지 확인한다.
    var lastTimeStamp = data.scatter2[data.scatter2.length - 1].x;
    
    if (lastTimeStamp >= getQueryEndTime()) {
    	return;
    }
    
    var queryNext = true;
    
    var fetch = function() {
		console.log("fetch scatter data");
		clearInterval(scatterFetchTimer);
		
		if(!queryNext || lastTimeStamp >= getQueryEndTime()) {
	        scatter.hideProgressbar();
			console.log("fetching scatter data finished.");
			return;
		}
		
    	try {
    	//	scatter.showProgressbar(lastTimeStamp + 1, getQueryEndTime());
    		console.log("fetching scatter data.");
    		
        	getScatterData($("#application").val(), lastTimeStamp + 1, getQueryEndTime(), function(data2) {
        		console.log("fetched " + data2.scatter2.length);
        		// scatter.hideProgressbar();
    	        if (data2.scatter2.length == 0) {
    	        	queryNext = false;
    	        	return;
    	        }
		        updateScatter(getQueryStartTime(), getQueryEndTime(), data2.scatter2, "#scatter");
        		lastTimeStamp = data2.scatter2[data2.scatter2.length - 1].x;
        		scatterFetchTimer = setInterval(fetch, 200);
        	});
    	} catch(e) {
    		console.log(e);
    	}
    }
    var scatterFetchTimer = setInterval(fetch, 200);
};

var selectDotCallback = function(traces) {
	if (traces.length === 0) {
		return;
	}
	
	if (traces.length === 1) {
		openTrace(traces[0].traceId, traces[0].x);
		return;
	}
	
	var query = [];
	var temp = {};
	for (var i = 0; i < traces.length; i++) {
		if (i > 0) {
			query.push("&");
		}
		query.push("tr");
		query.push(i);
		query.push("=");
		query.push(traces[i].traceId);
		
		query.push("&ti");
		query.push(i);
		query.push("=");
		query.push(traces[i].x)
		
		query.push("&re");
		query.push(i);
		query.push("=");
		query.push(traces[i].y)
	}
	
	$.post("/requestmetadata.hippo", query.join(""), function(d) {
		$("#selectedBusinessTransactionsDetail TBODY").empty();
		
		var data = jQuery.parseJSON(d).metadata;
		
		var html = [];
		for (var i = 0; i < data.length; i++) {
				
			if(data[i].exception) {
				html.push("<tr class='error'>");
			} else {
				html.push("<tr>");
			}
	
			html.push("<td style='padding-right:5px;text-align:right'>");
			html.push(i + 1);
			html.push("</td>");
	
			html.push("<td sorttable_customkey='");
			html.push(data[i].startTime);
			html.push("'>");
			html.push(new Date(data[i].startTime).format("HH:MM:ss l"));
			html.push("</td>");
			
			html.push("<td>");
			html.push("<a href='#' onclick='openTrace(\"");
			html.push(data[i].traceId);
			html.push("\", \"");
			html.push(data[i].collectorAcceptTime);
			html.push("\");'>");
			html.push(data[i].traceId);
			html.push("</a>");
			html.push("</td>");
	
			html.push("<td style='padding-right:30px;text-align:right'>");
			html.push(formatNumber(data[i].elapsed));
			html.push("</td>");
	
			html.push("<td>");
			if (data[i].exception) {
				html.push(data[i].exception);
			}
			html.push("</td>");
			
			html.push("<td>");
			html.push(data[i].application);
			html.push("</td>");
			
			html.push("<td>");
			html.push(data[i].agentId);
			html.push("</td>");
			
			html.push("<td>");
			html.push("<a href='#' onclick=\"alert('not implemented. ip정보 조회 페이지로 연결.');\">");
			html.push(data[i].remoteAddr);
			html.push("</a>");
			html.push("</td>");
	
			html.push("</tr>");
		}
	
		$("#selectedBusinessTransactionsDetail TBODY").append(html.join(''));
		$('#traceIdSelectModal').modal({});
	})
	.fail(function() {
		alert("Failed to fetching the request informations.");
	});
}

var timer;
$("#auto_refresh").bind("change", function(){
	if (this.checked) {
		if (timer != null) return;

		console.log("[auto-refresh] started.")
		
		clearInterval(timer);
		setQueryDateToNow();
	    updateCharts();
	    
	    var from = getQueryEndTime();
	    
		timer = setInterval(function() {
			setQueryDateToNow();
			console.log("[auto-refresh] fetching data from=" + from);
			
	        getRealtimeScatterData(from, function(data) {
		        updateScatter(getQueryStartTime(), getQueryEndTime(), data.scatter2, "#scatter");
	        	from = data.queryTo + 1;
	        });
		}, 3000);
	} else {
		clearInterval(timer);
		timer = null;
		console.log("[auto-refresh] stopped.")
	}
});

var oScatterChart;

function updateScatter(start, end, scatter_data, targetId, limit) {
	if (scatter_data.length == 0) {
		return;
	}
	oScatterChart.addBubbleAndDraw(scatter_data);
	// oScatterChart.addBubbleAndMoveAndDraw(data, date.getTime() + 3600000);
}

function drawScatter(title, start, end, targetId) {
	if(!Modernizr.canvas) {
		alert("Can't draw scatter. Not supported browser.");
	}
	
	var date = new Date();

	if (oScatterChart != null) {
		oScatterChart.updateXYAxis(start, end, 0, 10000);
		oScatterChart.clear();
		return;
	}
	
	oScatterChart = new BigScatterChart({
		sContainerId : targetId,
		nWidth : 500,
		nHeight : 400,
		// nXMin: date.getTime() - 86400000, nXMax: date.getTime(),
		nXMin: start, nXMax: end,
		nYMin: 0, nYMax: 10000,
		nZMin: 0, nZMax: 5,
		nBubbleSize: 3,
		htTypeAndColor : {
			// type name : color
			'Success' : '#2ca02c', 
			// 'Warning' : '#f5d025',
			'Failed' : '#d62728'
		},
		fOnSelect : function(htPosition, htXY){
			console.log('fOnSelect', htPosition, htXY);
			console.time('fOnSelect');
			var aData = this.getDataByXY(htXY.nXFrom, htXY.nXTo, htXY.nYFrom, htXY.nYTo);
			console.timeEnd('fOnSelect');
			console.log('adata length', aData.length);
			selectDotCallback(aData);
		}
	});
	// oScatterChart.setBubbles([]);
	// oScatterChart.redrawBubbles();
}