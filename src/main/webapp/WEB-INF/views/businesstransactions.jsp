<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="hippo" uri="http://hippo.nhncorp.com/hippo" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>PROBE - ${applicationName} request list</title>
    <meta charset="utf-8">
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <link href="/common/css/bootstrap/bootstrap.css" rel="stylesheet">
    <link href="/common/css/bootstrap/bootstrap-responsive.css" rel="stylesheet"/>
    <link href="/common/css/hippo/sorttable.css" rel="stylesheet"/>
    <link href="/common/css/datepicker.css" rel="stylesheet"/>
    <link href="/select2/select2-customized.css" rel="stylesheet"/>

    <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
    <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->

    <script type="text/javascript" src="/common/js/jquery/jquery-1.9.1.min.js"></script>
    <script type="text/javascript" src="/common/js/jquery/jquery-ui-1.10.2.js"></script>
    <script type="text/javascript" src="/common/js/bootstrap.min.js"></script>
    <script type="text/javascript" src="/common/js/sorttable.js"></script>
    <script type="text/javascript" src="/common/js/hippo/hippo.js"></script>
    <script type="text/javascript">
	var shownList;
	var selectedRow;
    function showRequestList(rpc, row) {
    	if (shownList) {
    		$(shownList).hide();
    	}
    	if (selectedRow) {
    		$(selectedRow).css({'background-color':'#FFFFFF'});
    	}
    	
    	shownList = "#requestList-" + rpc;
    	selectedRow = row;
    	
   		$(shownList).show();
		$(row).css({'background-color':'#FFFF00'});
   		return false;
    }
    </script>
    <style type="text/css">
    body {
    	padding: 30px;
	}
    </style>
</head>
<body>

<h4>Application : ${applicationName}</h4>
<h5>Time : <fmt:formatDate value="${from}" pattern="yyyy-MM-dd HH:mm:ss"/> ~ <fmt:formatDate value="${to}" pattern="yyyy-MM-dd HH:mm:ss"/></h5>

    <div style="position:relative;">
        <div style="margin-right:530px;overflow:scroll;height:600px;width:600px;">
		<h5>URL list</h5>
		<table class="table table-bordered table-hover sortable">
			<thead>
				<tr>
					<th>URL</th>
					<th class="sorttable_numeric">Calls</th>
					<th class="sorttable_numeric">Avg</th>
					<th class="sorttable_numeric">Min</th>
					<th class="sorttable_numeric">Max</th>
				</tr>
			</thead>
			<tbody>
				<c:forEach items="${rpcList}" var="t" varStatus="status">
				<tr style="cursor:pointer;" onclick="showRequestList(${status.count}, this);">
					<td>${t.rpc}</td>
					<td><fmt:formatNumber value="${t.calls}" type="number" /></td>
					<td><fmt:formatNumber value="${t.totalTime / t.calls}" type="number" pattern="#" /></td>
					<td><fmt:formatNumber value="${t.minTime}" type="number" /></td>
					<td><fmt:formatNumber value="${t.maxTime}" type="number" /></td>
				</tr>
				</c:forEach>
			</tbody>
		</table>
		</div>
	
		<div style="position:absolute;top:0;right:0;overflow:scroll;height:600px;width:600px;">
		<c:forEach items="${requestList}" var="t" varStatus="status">
		<div id="requestList-${status.count}" style="display:none;">
			<h5>Request list</h5>
			<table class="table table-bordered table-hover sortable">
				<thead>
					<tr>
						<th>#</th>
						<th class="sorttable_numeric">Time</th>
						<th>TraceId</th>
						<th class="sorttable_numeric">Response Time (ms)</th>
					</tr>
				</thead>
				<tbody>
				    <c:forEach items="${t.traces}" var="trace" varStatus="status2">
				    <tr>
				    	<td>${status2.count}</td>
				    	<td>${hippo:longToDateStr(trace.startTime, "HH:mm:ss SSS")}</td>
				    	<td><a href="/selectTransaction.hippo?traceId=${trace.traceId}&focusTimestamp=-1" target="_blank">${trace.traceId}</a></td>
				    	<td>${trace.executionTime}</td>
				    </tr>
					</c:forEach>		
				</tbody>
			</table>
		</div>
		</c:forEach>
		</div>
	</div>
	
</body>
</html>