<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="pinpoint" uri="http://pinpoint.nhncorp.com/pinpoint" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>PINPOINT - ${applicationName} request list</title>
    <meta charset="utf-8">
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

	<link type="text/css" rel="stylesheet" href="/components_v1/bootstrap/css/bootstrap.css" />
    <link type="text/css" rel="stylesheet" href="/components_v1/bootstrap/css/bootstrap-responsive.css" />
    <link type="text/css" rel="stylesheet" href="/components_v1/pinpoint/css/pinpoint.css" />
    <link type="text/css" rel="stylesheet" href="/components_v1/pinpoint-scatter/css/scatter.css" />
    <link type="text/css" rel="stylesheet" href="/components_v1/sorttable/sorttable.css" />
    <link type="text/css" rel="stylesheet" href="/components_v1/select2/select2.css" />

    <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
    <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->

	<!-- commons -->    
    <script type="text/javascript" src="/components_v1/jquery/jquery-1.9.1.min.js"></script>
    <script type="text/javascript" src="/components_v1/jquery-ui/jquery-ui-1.10.2.js"></script>
	<script type="text/javascript" src="/components_v1/jquery-class/jquery.Class.js"></script>
    <script type="text/javascript" src="/components_v1/jquery-template/jquery.tmpl.min.js"></script>
    <script type="text/javascript" src="/components_v1/bootstrap/js/bootstrap.min.js"></script>
	<script type="text/javascript" src="/components_v1/bootstrap-datepicker/bootstrap-datepicker.js"></script>
	<script type="text/javascript" src="/components_v1/modernizer/modernizr-2.6.2.min.js"></script>
	<script type="text/javascript" src="/components_v1/underscore/underscore-min.js"></script>
    <script type="text/javascript" src="/components_v1/select2/select2.js"></script>
    <script type="text/javascript" src="/components_v1/sorttable/sorttable.js"></script>
    <script type="text/javascript" src="/components_v1/utils/date.js"></script>
    <script type="text/javascript" src="/components_v1/pinpoint/pinpoint.js"></script>
    <script type="text/javascript" src="/components_v1/pinpoint/navigationbar.js"></script>
    
    <!-- scatter chart -->
	<script type="text/javascript" src="/components_v1/jquery-dragtoselect/jquery.dragToSelect.js"></script>
	<script type="text/javascript" src="/components_v1/pinpoint-scatter/jquery.BigScatterChart.js"></script>
    <script type="text/javascript" src="/components_v1/pinpoint/chart-scatter3.js"></script>
    
    <!-- help -->
    <script type="text/javascript" src="/components_v1/pinpoint/message.js"></script>
    
    <style type="text/css">
    body {
    	padding: 30px;
	}
	
	.number {
		text-align: right;
	}
	
	@media(min-width:1100px){
	  .table-container {
	  	position:relative;
	  }
	  	
	  .table-left {
	  	margin-right:530px;
	  }
	
	  .table-right {
	  	position:absolute;
	  	top:0;
	  	right:0;
	  	width:510px;
	  }
	}
	@media(max-width:1099px){
	  .table-right {
	    padding-left:20px;
	    width:60px;
	    max-width:60px;
	  }
	}
    </style>
</head>
<body>

<div class="table-container">

	<h4>Application : ${applicationName}</h4>
	<h5>Time : <fmt:formatDate value="${from}" pattern="yyyy-MM-dd HH:mm:ss"/> ~ <fmt:formatDate value="${to}" pattern="yyyy-MM-dd HH:mm:ss"/></h5>
	<h5>Total URL count : <fmt:formatNumber value="${urlCount}" type="number" /></h5>
	<h5>Total request count : <fmt:formatNumber value="${totalCount}" type="number" /></h5>

	<div style="width:800px;" class="progress progress-info" id="readProgress">
	  <div class="bar" style="width: 100%">fetched</div>
	  <!--
	  여기도 시간으로 나눠서 조회할 수 있도록 변경하기. 
	  <div id="fetchButtons">
	  	<span id="fetchMore" style="cursor:pointer;">fetch more</span> / 
	  	<span id="fetchAll" style="cursor:pointer;">fetch all</span>
	  </div>
	  -->
	</div>

	<div style="width:800px;max-height:300px;overflow:scroll;">
		<table class="table table-bordered table-condensed table-hover sortable">
			<thead>
				<tr>
					<th>URL</th>
					<th class="sorttable_numeric">Calls</th>
					<th class="sorttable_numeric">Error</th>
					<th class="sorttable_numeric">Avg(ms)</th>
					<th class="sorttable_numeric">Min(ms)</th>
					<th class="sorttable_numeric">Max(ms)</th>
				</tr>
			</thead>
			<tbody>
				<c:forEach items="${rpcList}" var="t" varStatus="status">
				<tr style="cursor:pointer;" onclick="showRequestList(${status.count}, this);">
					<td>${t.rpc}</td>
					<td style="text-align:right;" sorttable_customkey="${t.calls}"><fmt:formatNumber value="${t.calls}" type="number" /></td>
					<td style="text-align:right;" sorttable_customkey="${t.error}"><fmt:formatNumber value="${t.error}" type="number" /></td>
					<td style="text-align:right;" sorttable_customkey="${t.totalTime / t.calls}"><fmt:formatNumber value="${t.totalTime / t.calls}" type="number" pattern="#,###" /></td>
					<td style="text-align:right;" sorttable_customkey="${t.minTime}"><fmt:formatNumber value="${t.minTime}" type="number" /></td>
					<td style="text-align:right;" sorttable_customkey="${t.maxTime}"><fmt:formatNumber value="${t.maxTime}" type="number" /></td>
				</tr>
				</c:forEach>
			</tbody>
		</table>
	</div>
	<div id="scatterchart"></div>
</div>

<script type="text/javascript">
var datamap = {};
var selectedRow;

function showRequestList(id, row) {
	if (selectedRow) {
		$(selectedRow).css({'background-color':'#FFFFFF'});
	}
	selectedRow = row;
	$(row).css({'background-color':'#FFFF00'});
	
	drawScatter("${applicationName}", ${from.time}, ${to.time}, "scatterchart", 800, 400);
	updateScatter("", "", datamap[id]);
	return false;
}

$(document).ready(function () {
	drawScatter("${applicationName}", ${from.time}, ${to.time}, "scatterchart", 800, 400);
	$.each(datamap, function(i, e) {
		updateScatter("", "", e);
	});
});

<c:forEach items="${requestList}" var="t" varStatus="status">
datamap["${status.count}"] = [
	<c:forEach items="${t.traces}" var="trace" varStatus="status2">
	{
	"x" : ${trace.startTime},
	"y" : ${trace.executionTime},
	"traceId" : "${trace.transactionId}",
	"type" : <c:choose><c:when test="${trace.exceptionCode == 1}">"Failed"</c:when><c:otherwise>"Success"</c:otherwise></c:choose> 
	}
	<c:if test="${!status2.last}">,</c:if>
	</c:forEach>		
];
</c:forEach>
</script>
</body>
</html>