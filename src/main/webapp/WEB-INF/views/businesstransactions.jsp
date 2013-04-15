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
    <link href="/common/css/hippo/hippo.css" rel="stylesheet"/>
    <link href="/common/css/hippo/sorttable.css" rel="stylesheet"/>
    <link href="/common/css/hippo/scatter.css" rel="stylesheet"/>
    <link href="/common/css/datepicker.css" rel="stylesheet"/>
    <link href="/select2/select2-customized.css" rel="stylesheet"/>

    <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
    <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->

	<!-- commons -->    
    <script type="text/javascript" src="/common/js/jquery/jquery-1.9.1.min.js"></script>
    <script type="text/javascript" src="/common/js/jquery/jquery-ui-1.10.2.js"></script>
    
    <script type="text/javascript" src="/select2/select2.js"></script>
    <script type="text/javascript" src="/common/js/bootstrap.min.js"></script>
	<script type="text/javascript" src="/common/js/modernizr-2.6.2.min.js"></script>
	<script type="text/javascript" src="/common/js/bootstrap-datepicker.js"></script>
    <script type="text/javascript" src="/common/js/sorttable.js"></script>
    <script type="text/javascript" src="/common/js/date.format.js"></script>
    <script type="text/javascript" src="/common/js/hippo/hippo.js"></script>

	<script type="text/javascript" src="/common/js/d3.js"></script>
    
    <!-- scatter chart -->
    <script type="text/javascript" src="/common/js/hippo/chart-scatter3.js"></script>
	<script type="text/javascript" src="/common/js/hippo/scatter/underscore-min.js"></script>
    <script type="text/javascript" src="/common/js/hippo/scatter/date.js"></script>
	<script type="text/javascript" src="/common/js/hippo/scatter/jquery.Class.js"></script>
	<script type="text/javascript" src="/common/js/hippo/scatter/jquery.dragToSelect.js"></script>
	<script type="text/javascript" src="/common/js/hippo/scatter/jquery.BigScatterChart.js"></script>
    
	<!-- server map -->    
    <script type="text/javascript" src="/common/js/hippo/chart-springy.js"></script>
    <script type="text/javascript" src="/common/js/hippo/servermap/jquery.tmpl.min.js"></script>
    <script type="text/javascript" src="/common/js/hippo/servermap/Point2D.js"></script>
    <script type="text/javascript" src="/common/js/hippo/servermap/intersection.js"></script>
    <script type="text/javascript" src="/common/js/hippo/servermap/springy.js"></script>
    <script type="text/javascript" src="/common/js/hippo/servermap/canvas.roundRect.js"></script>
    <script type="text/javascript" src="/common/js/hippo/servermap/hippoServerMap.js"></script>
    
    <!-- requests list -->
    <script type="text/javascript" src="/common/js/hippo/chart-transactions.js"></script>
    
    <!-- help -->
    <script type="text/javascript" src="/common/js/hippo/help.js"></script>
    <script type="text/javascript" src="/common/js/hippo/message.js"></script>
	
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
<div class="container">
	<div class="row">
		<div class="span8">
			<h4>Application : ${applicationName}</h4>
			<h5>Time : <fmt:formatDate value="${from}" pattern="yyyy-MM-dd HH:mm:ss"/> ~ <fmt:formatDate value="${to}" pattern="yyyy-MM-dd HH:mm:ss"/></h5>
			<h5>Total URL count : <fmt:formatNumber value="${urlCount}" type="number" /></h5>
			<h5>Total request count : <fmt:formatNumber value="${totalCount}" type="number" /></h5>
		</div>
		<div class="span4">
			<div id="scatterchart"></div>
		</div>
	</div>
	<div class="row">
		<div class="span12" style="max-height:500px;overflow:scroll;">
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
	</div>
	<div class="row">
		<div class="span12">
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
</div>

<!-- MODAL -->
<div class="modal hide fade" id="traceIdSelectModal" style="width:1200px; margin-left:-600px">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal">×</button>
        <h3>Selected Traces</h3>
    </div>
    <div class="modal-body">
		<table id="selectedBusinessTransactionsDetail" class="table table-bordered table-hover sortable">
			<thead>
			<tr>
			    <th class="sorttable_numeric">#</th>
			    <th class="sorttable_numeric">Time</th>
			    <th>TraceId</th>
			    <th class="sorttable_numeric">Res. Time (ms)</th>
			    <th>Exception</th>
			    <th>Application</th>
			    <th>AgentId</th>
			    <th>ClientIP</th>
			</tr>
			</thead>
			<tbody>
			</tbody>
		</table>
    </div>
    <div class="modal-footer">
        <a href="#" class="btn" data-dismiss="modal">Close</a>
    </div>
</div>
<!-- END OF MODAL -->

<script type="text/javascript">
var data = {
	"scatter" : [
	<c:forEach items="${scatterList}" var="t" varStatus="status">
	    <c:forEach items="${t.traces}" var="trace" varStatus="status2">
		{
			"x" : ${trace.startTime},
			"y" : ${trace.executionTime},
			"traceId" : "${trace.traceId}",
			"type" : <c:choose><c:when test="${trace.exceptionCode == 1}">"Failed"</c:when><c:otherwise>"Success"</c:otherwise></c:choose> 
		}
	    <c:if test="${!status2.last}">,</c:if>
		</c:forEach>
	    <c:if test="${!status.last}">,</c:if>
	</c:forEach>
	]
}
drawScatter("${applicationName}", ${from.time}, ${to.time}, "scatterchart", 400, 250);
updateScatter("", "", data.scatter);
</script>
</body>
</html>