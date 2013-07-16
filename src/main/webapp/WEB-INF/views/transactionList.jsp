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

     <link href="/components/bootstrap/css/bootstrap.css" rel="stylesheet">
    <link href="/components/bootstrap/css/bootstrap-responsive.css" rel="stylesheet"/>
    <link href="/components/pinpoint/css/pinpoint.css" rel="stylesheet"/>
    <link href="/components/pinpoint-scatter/css/scatter.css" rel="stylesheet"/>
    <link href="/components/sorttable/sorttable.css" rel="stylesheet"/>
    <link href="/components/select2/select2.css" rel="stylesheet"/>

    <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
    <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->

	<!-- commons -->    
    <script type="text/javascript" src="/components/jquery/jquery-1.9.1.min.js"></script>
    <script type="text/javascript" src="/components/jquery-ui/jquery-ui-1.10.2.js"></script>
	<script type="text/javascript" src="/components/jquery-class/jquery.Class.js"></script>
    <script type="text/javascript" src="/components/jquery-template/jquery.tmpl.min.js"></script>
    <script type="text/javascript" src="/components/bootstrap/js/bootstrap.min.js"></script>
	<script type="text/javascript" src="/components/bootstrap-datepicker/bootstrap-datepicker.js"></script>
	<script type="text/javascript" src="/components/modernizer/modernizr-2.6.2.min.js"></script>
	<script type="text/javascript" src="/components/underscore/underscore-min.js"></script>
    <script type="text/javascript" src="/components/select2/select2.js"></script>
    <script type="text/javascript" src="/components/sorttable/sorttable.js"></script>
    <script type="text/javascript" src="/components/utils/date.js"></script>
    <script type="text/javascript" src="/components/pinpoint/pinpoint.js"></script>
    <script type="text/javascript" src="/components/pinpoint/navigationbar.js"></script>
    
    <!-- scatter chart -->
	<script type="text/javascript" src="/components/jquery-dragtoselect/jquery.dragToSelect.js"></script>
	<script type="text/javascript" src="/components/pinpoint-scatter/jquery.BigScatterChart.js"></script>
    <script type="text/javascript" src="/components/pinpoint/chart-scatter3.js"></script>
    
    <!-- help -->
    <script type="text/javascript" src="/components/pinpoint/help.js"></script>
    <script type="text/javascript" src="/components/pinpoint/message.js"></script>
    
    <style type="text/css">
    body {
    	padding: 30px;
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
    <div class="table-left">
		<h4>Application : ${applicationName}</h4>
		<h5>Time : <fmt:formatDate value="${from}" pattern="yyyy-MM-dd HH:mm:ss"/> ~ <fmt:formatDate value="${to}" pattern="yyyy-MM-dd HH:mm:ss"/></h5>
		<h5>Total URL count : <fmt:formatNumber value="${urlCount}" type="number" /></h5>
		<h5>Total request count : <fmt:formatNumber value="${totalCount}" type="number" /></h5>
		
		<div style="max-height:300px;overflow:scroll;">
			<table class="table table-bordered table-condensed table-hover sortable">
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
						<td class="num" sorttable_customkey="${t.calls}"><fmt:formatNumber value="${t.calls}" type="number" /></td>
						<td class="num" sorttable_customkey="${t.totalTime / t.calls}"><fmt:formatNumber value="${t.totalTime / t.calls}" type="number" /></td>
						<td class="num" sorttable_customkey="${t.minTime}"><fmt:formatNumber value="${t.minTime}" type="number" /></td>
						<td class="num" sorttable_customkey="${t.maxTime}"><fmt:formatNumber value="${t.maxTime}" type="number" /></td>
					</tr>
					</c:forEach>
				</tbody>
			</table>
		</div>
	</div>
	<div class="table-right">
		<div id="scatterchart"></div>
	</div>
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
	
	drawScatter("${applicationName}", ${from.time}, ${to.time}, "scatterchart", 500, 450);
	updateScatter("", "", datamap[id]);
		return false;
}

<c:forEach items="${requestList}" var="t" varStatus="status">
datamap["${status.count}"] = [
	    <c:forEach items="${t.traces}" var="trace" varStatus="status2">
		{
   			"x" : ${trace.startTime},
   			"y" : ${trace.executionTime},
   			"traceId" : "${trace.traceId}",
   			"type" : <c:choose><c:when test="${trace.exceptionCode == 1}">"Failed"</c:when><c:otherwise>"Success"</c:otherwise></c:choose> 
   		}
	    <c:if test="${!status2.last}">,</c:if>
		</c:forEach>		
    	];
</c:forEach>
</script>
</body>
</html>