var oScatterChart;
var selectdTracesBox = {};

function getScatterData(application, from, to, period, callback) {
	console.log("   fetch scatter data2. appName=" + application + ", from=" + from + " (" + new Date(from) + ") , to=" + to + " (" + new Date(to) + ") , period=" + period);
	var app = application.split("@");
    jQuery.ajax({
    	type : 'GET',
    	url : '/getScatterData.pinpoint',
    	cache : false,
    	dataType: 'json',
    	data : {
    		application : app[0],
    		from : from,
    		to : to,
    		limit : 5000
    	},
    	success : function(d) {
    		callback(d, app[0], from, to, period);
    	},
    	error : function(xhr, status, error) {
    		
    	}
    });
}

function getLastScatterData(application, from, to, period, callback) {
	console.log("   fetch scatter data1. appName=" + application + ", from=" + from + " (" + new Date(from) + ") , to=" + to + " (" + new Date(to) + ") , period=" + period);
	var app = application.split("@");
    jQuery.ajax({
    	type : 'GET',
    	url : '/getLastScatterData.pinpoint',
    	cache : false,
    	dataType: 'json',
    	data : {
    		application : app[0],
    		period : period,
    		limit : 5000
    	},
    	success : function(d) {
    		callback(d, app[0], from, to, period);
    	},
    	error : function(xhr, status, error) {
    		
    	}
    });
}

function getRealtimeScatterData(application, from, to, period, callback) {
	console.log("   fetch realtime scatter data. appName=" + application + ", from=" + from + ", to=" + to + ", period=" + period);
	var app = application.split("@");
    jQuery.ajax({
    	type : 'GET',
    	url : '/getRealtimeScatterData.pinpoint',
    	cache : false,
    	dataType: 'json',
    	data : {
    		application : app[0],
    		from : from,
    		limit : 5000
    	},
    	success : function(d) {
    		callback(d, app[0], from, to, period);
    	},
    	error : function(xhr, status, error) {
    		
    	}
    });
}

function expandScatter(e) {
        var params = [];
        params.push("application=");
        params.push(e.data("applicationName"));
        params.push("&from=");
        params.push(e.data("from"));
        params.push("&to=");
        params.push(e.data("to"));
        params.push("&period=");
        params.push(e.data("period"));
        params.push("&usePeriod=");
        params.push(e.data("usePeriod"));
        
        window.open("/scatterpopup.pinpoint?" + params.join(""), params.join(""), "width=900, height=600, resizable=yes");
}

function showResponseScatter(applicationName, from, to, period, usePeriod, w, h) {
    console.log("ShowReponseScatter. appName=" + applicationName + ", from=" + from + ", to=" + to + ", period=" + period);

    if (oScatterChart) {
    	oScatterChart.clear();
    }
    
    delete selectdTracesBox;
    selectdTracesBox = {};
    
    $("#scatterChartContainer H5").text(applicationName);
    
    var fullscreenButton = $("#scatterChartContainer I.icon-fullscreen"); 
    fullscreenButton.data("applicationName", applicationName);
    fullscreenButton.data("from", from);
    fullscreenButton.data("to", to);
    fullscreenButton.data("period", period);
    fullscreenButton.data("usePeriod", usePeriod);
    
    var downloadButton = $("#scatterChartContainer A");
    downloadButton.attr("download", applicationName + ".png");
    downloadButton.unbind("click");
    downloadButton.bind("click", function() {
    	oScatterChart.saveAsPNG(downloadButton);
    });
    
    $("#scatterChartContainer SPAN").unbind("click");     
    $("#scatterChartContainer SPAN").bind("click", function() {
    	showRequests(applicationName, from, to, period, usePeriod);
    });
    
    drawScatter(applicationName, from, to, "scatterchart", w, h);
    
    if (usePeriod) {
        getLastScatterData(applicationName, from, to, period, scatterFetchDataCallback);
    } else {
        getScatterData(applicationName, from, to, period, scatterFetchDataCallback);
    }
}

var scatterFetchDataCallback = function(data, applicationName, from, to, period) {
	// 처음 조회된 데이터를 그려준다.
    updateScatter(from, to, data.scatter, "#scatter");
    
    if (data.scatter.length == 0) {
    	console.log("   stop query scatter. no data.");
        return;
    }
    
    // 데이터 조회가 추가로 필요한지 확인한다.
    var lastTimeStamp = data.scatter[0].x;

    if (lastTimeStamp >= to) {
    	console.log("   stop query scatter. lastTimeStamp=" + lastTimeStamp + ", to=" + to);
        return;
    }
    
    var queryNext = true;
    
    var fetch = function() {
    	clearInterval(scatterFetchTimer);
                
		if(!queryNext || lastTimeStamp >= to) {
			scatter.hideProgressbar();
	        console.log("fetch scatter data finished.");
	        return;
		}
                
        try {
            getScatterData(applicationName, lastTimeStamp + 1, to, period, function(data2, from, to, period) {
	            if (data2.scatter.length == 0) {
	                queryNext = false;
	                console.log("   fetch scatter data finished. there's no data.");
	                return;
	            }
	            updateScatter(from, to, data2.scatter, "#scatter");
	            lastTimeStamp = data2.scatter[data2.scatter.length - 1].x;
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

    var token = Math.random() * 10000 + 1;
    selectdTracesBox[token] = traces;
    
    var popupwindow = window.open("/selectedScatter.pinpoint", token);
}

function updateScatter(start, end, scatter_data, targetId, limit) {
    if (scatter_data.length == 0) {
    	return;
    }
	console.log("   updating scatter chart. datasize=" + scatter_data.length);
    oScatterChart.addBubbleAndDraw(scatter_data);
    // oScatterChart.addBubbleAndMoveAndDraw(data, date.getTime() + 3600000);
}

function drawScatter(title, start, end, targetId, w, h) {
        if(!Modernizr.canvas) {
                alert("Can't draw scatter. Not supported browser.");
        }
        
        var yAxisMAX = 10000;
        var date = new Date();

        if (oScatterChart != null) {
                oScatterChart.updateXYAxis(start, end, 0, yAxisMAX);
                oScatterChart.clear();
                return;
        }
        
        oScatterChart = new BigScatterChart({
                sContainerId : targetId,
                nWidth : w ? w : 500,
                nHeight : h ? h : 400,
                // nXMin: date.getTime() - 86400000, nXMax: date.getTime(),
                nXMin: start, nXMax: end,
                nYMin: 0, nYMax: yAxisMAX,
                nZMin: 0, nZMax: 5,
                nBubbleSize: 3,
        		sXLabel : '(time)',
    			sYLabel : '(ms)',
    			sTitle : title,
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