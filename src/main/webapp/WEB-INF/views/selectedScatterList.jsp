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

    <link href="/common/css/bootstrap/bootstrap.css" rel="stylesheet">
    <link href="/common/css/bootstrap/bootstrap-responsive.css" rel="stylesheet"/>
    <link href="/common/css/pinpoint/sorttable.css" rel="stylesheet"/>

    <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
    <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->

	<!-- commons -->    
    <script type="text/javascript" src="/common/js/jquery/jquery-1.9.1.min.js"></script>
    <script type="text/javascript" src="/common/js/sorttable.js"></script>
    <script type="text/javascript" src="/common/js/date.format.js"></script>
    <script type="text/javascript" src="/common/js/pinpoint/pinpoint.js"></script>
    <style type="text/css">
	html, body {
		height: 100%;
	}
	</style>
</head>
<body>
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

$(document).ready(function () {
	if(!parent.opener) {
		return;
	}
	
	var traces = parent.opener.selectdTracesBox[parent.window.name];
	if (!traces) {
		alert("Query parameter 캐시가 삭제되었기 때문에 데이터를 조회할 수 없습니다.\n\n이러한 현상은 scatter chart를 새로 조회했을 때 발생할 수 있습니다.");
		$("#loader").hide();
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
});

var writeContents = function(d) {
	$("#selectedBusinessTransactionsDetail TBODY").empty();
	
	var data = d.metadata;
	
	var html = [];
	for (var i = 0; i < data.length; i++) {
		if(data[i].exception) {
			html.push("<tr class='error' onclick='selectRow(this);'>");
		} else {
			html.push("<tr onclick='selectRow(this);'>");
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
		html.push("/transactionInfo.pinpoint?traceId=" + data[i].traceId + "&focusTimestamp=" + data[i].collectorAcceptTime);
		html.push("' target='transactionView'>");
		html.push(data[i].traceId);
		html.push("</a>");
		html.push("</td>");
		
		html.push("</tr>");
	}

	$("#selectedBusinessTransactionsDetail TBODY").append(html.join(''));
}
</script>
</body>
</html>