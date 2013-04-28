var oScatterChart;
var selectdTracesBox = {};

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
    
    window.open("/scatterpopup.hippo?" + params.join(""), params.join(""), "width=900, height=700, resizable=yes");
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

    var imageFileName = applicationName +
    				"_" +
    				new Date(from).toString("yyyyMMdd_HHmm") +
    				"~" +
    				new Date(to).toString("yyyyMMdd_HHmm") +
    				"_response_scatter.png";
    
    downloadButton.attr("download", imageFileName);
    downloadButton.unbind("click");
    downloadButton.bind("click", function() {
    	oScatterChart.saveAsPNG(downloadButton);
    });
    
    $("#scatterChartContainer SPAN").unbind("click");     
    $("#scatterChartContainer SPAN").bind("click", function() {
    	showRequests(applicationName, from, to, period, usePeriod);
    });
    
    var htDataSource = {
		fUrl : function(nCallCount) {
			if(nCallCount === 0) {
				return "/getLastScatterData.hippo";	
			} else {
				return "/getScatterData.hippo";
			}							
		},
		fData : function(nCallCount, htFetchedData) {
			var htData;
			if(nCallCount === 0 || typeof(htFetchedData) === 'undefined'){
				htData = {
					'application' : applicationName,
					'period' : period,
					'limit' : 5000
				};
			}else{
				htData = {
					'application' : applicationName,
					'from' : htFetchedData.scatter[htFetchedData.scatter.length - 1].x + 1,
					'to' : to,
					'limit' : 5000
				};
			}
			return htData;
		},
		fFetch : function(htFetchedData) {
			if (htFetchedData.scatter.length != 0) {
				return true;
			} else {
				return false;
			}

			if (htFetchedData.scatter[htFetchedData.scatter.length - 1].x < date.getTime()) {
				return true;
			}					
			return false;
		},
		htOption : {
			dataType : 'jsonp',
			jsonp : '_callback'
		}
	};
    
    drawScatter(applicationName, from, to, "scatterchart", w, h);
    
	oScatterChart.loadFromDataSource(htDataSource);
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
            var traces = this.getDataByXY(htXY.nXFrom, htXY.nXTo, htXY.nYFrom, htXY.nYTo);
            
            if (traces.length === 0) {
                return;
            }
            
            if (traces.length === 1) {
                openTrace(traces[0].traceId, traces[0].x);
                return;
            }

            var token = Math.random() * 10000 + 1;
            selectdTracesBox[token] = traces;
            
            var popupwindow = window.open("/selectedScatter.hippo", token);
        }
});
}