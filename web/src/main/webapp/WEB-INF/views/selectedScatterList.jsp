<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>PINPOINT</title>
    <meta charset="utf-8">
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <link type="text/css" rel="stylesheet" href="/components_v1/bootstrap/css/bootstrap.css" />
    <link type="text/css" rel="stylesheet" href="/components_v1/bootstrap/css/bootstrap-responsive.css" />
    <link type="text/css" rel="stylesheet" href="/components_v1/sorttable/sorttable.css" />

    <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
    <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->

	<!-- commons -->    
    <script type="text/javascript" src="/components_v1/jquery/jquery-1.9.1.min.js"></script>
    <script type="text/javascript" src="/components_v1/sorttable/sorttable.js"></script>
	<script type="text/javascript" src="/components_v1/utils/date.format.js"></script>
    <script type="text/javascript" src="/components_v1/pinpoint/pinpoint.js"></script>
    
    <style type="text/css">
	html, body {
		height: 100%;
	}
	</style>
</head>
<body>

<div class="progress progress-info" id="readProgress">
  <div class="bar" style="width: 0%">fetched</div>
  <div id="fetchButtons">
  	<span id="fetchMore" style="cursor:pointer;">fetch more</span> / 
  	<span id="fetchAll" style="cursor:pointer;">fetch all</span>
  </div>
</div>

<table id="selectedBusinessTransactionsDetail" class="table table-bordered table-condensed table-hover sortable" style="font-size:12px;">
	<thead>
	<tr>
	    <th class="sorttable_numeric">#</th>
	    <th class="sorttable_numeric">Time</th>
	    <th>Application</th>
	    <th class="sorttable_numeric">Res. Time (ms)</th>
	    <th>Exception</th>
	    <th>AgentId</th>
	    <th>ClientIP</th>
	    <th>TraceId</th>
	</tr>
	</thead>
	<tbody>
	</tbody>
</table>
<script type="text/javascript">
var selectedRow;

function selectRow(row) {
	if (selectedRow) {
		$(selectedRow).css({'background-color':'#FFFFFF'});
	}
	selectedRow = row;
	$(row).css({'background-color':'#FFFF00'});
	$("#transactionView").hide();
	// $("#loader").show();
	return false;
}

//
// TODO prototype.
//

var fetchCount = 1;
var MAX_FETCH_BLOCK_SIZE = 5;
var lastFetchedIndex = 0;

function fetchNext() {
	console.log("fetch next");
	fetchStart();
}

function fetchAll() {
	MAX_FETCH_BLOCK_SIZE = 100000000;
	fetchStart();
}

function fetchStart() {
	var traces = parent.opener.selectdTracesBox[parent.window.name];
	if (!traces) {
		alert("Query parameter 캐시가 삭제되었기 때문에 데이터를 조회할 수 없습니다.\n\n이러한 현상은 scatter chart를 새로 조회했을 때 발생할 수 있습니다.");
		$("#loader").hide();
		return;
	}
	
	var query = [];
	var temp = {};
	for (var i = lastFetchedIndex, j = 0; i < MAX_FETCH_BLOCK_SIZE * fetchCount && i < traces.length; i++, j++) {
		if (i > 0) {
			query.push("&");
		}
		console.log(i, j, traces.length);
		query.push("I");
		query.push(j);
		query.push("=");
		query.push(traces[i].traceId);
		
		query.push("&T");
		query.push(j);
		query.push("=");
		query.push(traces[i].x)
		
		query.push("&R");
		query.push(j);
		query.push("=");
		query.push(traces[i].y)
		
		lastFetchedIndex++;
	}
	
	fetchCount++;
	
	if (i == traces.length) {
		$("#fetchButtons").hide();
	}
	
	$("#readProgress .bar").text("fetched (" + i + " / " + traces.length + ")");
	$("#readProgress .bar").css("width", i / traces.length * 100 + "%")
	
	var startTime = new Date().getTime();
	
	$.post("/transactionmetadata.pinpoint", query.join(""), function(d) {
		var fetchedTime = new Date().getTime();
		console.log("List fetch time. " + (fetchedTime - startTime) + "ms")
		writeContents(d);
		var renderTime = new Date().getTime();
		console.log("List render time. " + (renderTime - fetchedTime) + "ms")
		$("#loader").hide();
	})
	.fail(function() {
		alert("Failed to fetching the request informations.");
	});	
}

$(document).ready(function () {
	if(!parent.opener) {
		return;
	}
	
	fetchStart();
	
	$("#fetchMore").bind('click', fetchNext);
	$("#fetchAll").bind('click', fetchAll);
});

var writeContents = function(d) {
	// $("#selectedBusinessTransactionsDetail TBODY").empty();
	
	var data = d.metadata;
	
	var html = [];
	for (var i = 0; i < data.length; i++) {
		if(data[i].exception) {
			html.push("<tr class='error' onclick='selectRow(this);'>");
		} else {
			html.push("<tr onclick='selectRow(this);'>");
		}

		html.push("<td style='padding-right:5px;text-align:right'>");
		html.push(lastFetchedIndex - data.length + i + 1);
		html.push("</td>");

		html.push("<td sorttable_customkey='");
		html.push(data[i].startTime);
		html.push("'>");
		html.push(new Date(data[i].startTime).format("HH:MM:ss l"));
		html.push("</td>");
		
		html.push("<td>");
		html.push("<a href='");
		html.push("/transactionInfo.pinpoint?traceId=" + data[i].traceId + "&focusTimestamp=" + data[i].collectorAcceptTime);
		html.push("' target='transactionView'>");
		html.push(data[i].application);
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
		html.push(data[i].agentId);
		html.push("</td>");
		
		html.push("<td>");
		html.push("<a href='#' onclick=\"alert('not implemented. ip정보 조회 페이지로 연결.');\">");
		html.push(data[i].remoteAddr);
		html.push("</a>");
		html.push("</td>");

		html.push("<td>");
		html.push("<a href='");
		html.push("/transactionInfo.pinpoint?traceId=" + data[i].transactionSequence + "&focusTimestamp=" + data[i].collectorAcceptTime);
		html.push("' target='transactionView'>");
		html.push(data[i].transactionSequence);
		html.push("</a>");
		html.push("</td>");
		
		html.push("</tr>");
	}

	$("#selectedBusinessTransactionsDetail TBODY").append(html.join(''));
}
</script>
</body>
</html>