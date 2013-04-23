var oScatterChart;
var selectdTracesBox = {};

function getScatterData(application, from, to, period, callback) {
	console.log("Get scatter data. appName=" + application + ", from=" + from + ", to=" + to + ", period=" + period);
	var app = application.split("@");
	d3.json("/getScatterData.hippo?application=" + app[0] + "&from=" + from + "&to=" + to + "&limit=5000", function(d) { callback(d, app[0], from, to, period); });
}

function getLastScatterData(application, from, to, period, callback) {
	console.log("get last scatter data. appName=" + application + ", from=" + from + ", to=" + to + ", period=" + period);
	var app = application.split("@");
	d3.json("/getLastScatterData.hippo?application=" + app[0] + "&period=" + period + "&limit=5000", function(d) { callback(d, app[0], from, to, period); });
}

function getRealtimeScatterData(application, from, to, period, callback) {
	console.log("get realtime scatter data. appName=" + application + ", from=" + from + ", to=" + to + ", period=" + period);
	var app = application.split("@");
	d3.json("/getRealtimeScatterData.hippo?application=" + app[0] + "&from=" + from + "&limit=5000", function(d) { callback(d, app[0], from, to, period); });
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
        
        window.open("/scatterpopup.hippo?" + params.join(""), params.join(""), "width=900, height=600, resizable=yes");
}

function showResponseScatter(applicationName, from, to, period, usePeriod, w, h) {
        console.log("ShowReponseScatter. appName=" + applicationName + ", from=" + from + ", to=" + to + ", period=" + period);

        if (oScatterChart) {
                oScatterChart.clear();
        }
        
        delete selectdTracesBox;
        selectdTracesBox = {};
        
        $("#scatterChartContainer H5").text("'" + applicationName + "' response scatter")
        
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
	console.log("ShowReponseScatter callback. appName=" + applicationName + ", from=" + from + ", to=" + to + ", period=" + period);

	// 처음 조회된 데이터를 그려준다.
    updateScatter(from, to, data.scatter, "#scatter");
    
    if (data.scatter.length == 0) {
    	console.log("Stop query scatter. no data.");
        return;
    }
    
    // 데이터 조회가 추가로 필요한지 확인한다.
    var lastTimeStamp = data.scatter[data.scatter.length - 1].x;

    if (lastTimeStamp >= to) {
    	console.log("Stop query scatter. lastTimeStamp=" + lastTimeStamp + ", to=" + to);
        return;
    }
    
    var queryNext = true;
    
    var fetch = function() {
    	console.log("fetch scatter data");
    	clearInterval(scatterFetchTimer);
                
		if(!queryNext || lastTimeStamp >= to) {
			scatter.hideProgressbar();
	        console.log("fetching scatter data finished.");
	        return;
		}
                
        try {
            console.log("fetching scatter data.");
                
            getScatterData($("#application").val(), lastTimeStamp + 1, to, period, function(data2, from, to, period) {
	            console.log("fetched " + data2.scatter.length);
	            
	            if (data2.scatter.length == 0) {
	                queryNext = false;
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
        
        var popupwindow = window.open("/selectedScatter.html", token);
}

/*
var selectDotCallbackDeprecated = function(traces) {
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
*/

/*
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
                        updateScatter(getQueryStartTime(), getQueryEndTime(), data.scatter, "#scatter");
                        from = data.queryTo + 1;
                });
                }, 3000);
        } else {
                clearInterval(timer);
                timer = null;
                console.log("[auto-refresh] stopped.")
        }
});
*/

function updateScatter(start, end, scatter_data, targetId, limit) {
        if (scatter_data.length == 0) {
                return;
        }
        oScatterChart.addBubbleAndDraw(scatter_data);
        // oScatterChart.addBubbleAndMoveAndDraw(data, date.getTime() + 3600000);
}

function drawScatter(title, start, end, targetId, w, h) {
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
                nWidth : w ? w : 500,
                nHeight : h ? h : 400,
                // nXMin: date.getTime() - 86400000, nXMax: date.getTime(),
                nXMin: start, nXMax: end,
                nYMin: 0, nYMax: 10000,
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