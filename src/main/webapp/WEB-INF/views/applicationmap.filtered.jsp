<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="hippo" uri="http://hippo.nhncorp.com/hippo" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="en">
<head>
    <title>Transaction details (${traceId})</title>
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
    <script type="text/javascript" src="/common/js/bootstrap.min.js"></script>
	<script type="text/javascript" src="/common/js/bootstrap-datepicker.js"></script>
	<script type="text/javascript" src="/common/js/modernizr-2.6.2.min.js"></script>
    <script type="text/javascript" src="/common/js/date.js"></script>
	<script type="text/javascript" src="/common/js/hippo/scatter/underscore-min.js"></script>
	<script type="text/javascript" src="/common/js/hippo/scatter/jquery.Class.js"></script>
    <script type="text/javascript" src="/common/js/hippo/hippo.js"></script>
    <script type="text/javascript" src="/common/js/sorttable.js"></script>
    <script type="text/javascript" src="/select2/select2.js"></script>
    
    <!-- scatter chart -->
    <script type="text/javascript" src="/common/js/hippo/chart-scatter4.js"></script>
	<script type="text/javascript" src="/common/js/hippo/scatter/jquery.dragToSelect.js"></script>
	<script type="text/javascript" src="/common/js/hippo/scatter/jquery.BigScatterChart.js"></script>
    
	<!-- server map -->    
    <script type="text/javascript" src="/common/js/hippo/chart-servermap.js"></script>
    <script type="text/javascript" src="/common/js/go.js"></script>
    <script type="text/javascript" src="/common/js/hippo/servermap/jquery.tmpl.min.js"></script>
    <script type="text/javascript" src="/common/js/hippo/servermap/Point2D.js"></script>
    <script type="text/javascript" src="/common/js/hippo/servermap/intersection.js"></script>
    <script type="text/javascript" src="/common/js/hippo/servermap/canvas.roundRect.js"></script>
    <script type="text/javascript" src="/common/js/hippo/servermap/jquery.ServerMap.js"></script>
    
    <!-- help -->
    <script type="text/javascript" src="/common/js/hippo/help.js"></script>
    <script type="text/javascript" src="/common/js/hippo/message.js"></script>
</head>
<body>
<h3>${filter}</h3>
<div id="servermap" style="width:1000px;height:700px; border:1px solid #DDDDDD; overflow:hidden;"></div>
<script type="text/javascript">
    var data = {
		"applicationMapData" : {
			"nodeDataArray": [
				<c:forEach items="${nodes}" var="node" varStatus="status">
				{
	 			   	"id" : ${status.count},
	 			   	"key" : ${status.count},
	 			    "text" : "${node}",
	 			    "hosts" : [
	 			    <c:forEach items="${node.hosts}" var="host" varStatus="status2">
	 			        "${host}"
	 					<c:if test="${!status2.last}">,</c:if>
					</c:forEach>
					],
					"category" : "${node.serviceType.desc}",
					"terminal" : "${node.serviceType.terminal}"
				} <c:if test="${!status.last}">,</c:if>
				</c:forEach>
			],
			"linkDataArray": [
				<c:forEach items="${links}" var="link" varStatus="status">
				{
					"id" : "${link.from.sequence + 1}-${link.to.sequence + 1}",
					"from" : ${link.from.sequence + 1},
					"to" : ${link.to.sequence + 1},
					"text" : ${link.histogram.totalCount},
					"error" : ${link.histogram.errorCount},
					"slow" : ${link.histogram.slowCount},
					"histogram" : ${link.histogram}
				} <c:if test="${!status.last}">,</c:if>
				</c:forEach>   	
			]
		}
	}
    
    var oServerMap;
    
    $(document).ready(function () {
    	var containerId = "servermap";

		if (data.applicationMapData.nodeDataArray.length == 0) {
			return;
		}
		
		if (oServerMap == null) {
			oServerMap = new ServerMap({
		        sContainerId : containerId,
				fOnNodeClick : function(e, data) {
					// nodeClickHandler(e, data, "#" + containerId);
				},
				fOnLinkClick : function(e, data) {
					// linkClickHandler(e, data, "#" + containerId);
				}
		    });
		}
		
	    oServerMap.load(data.applicationMapData);
	    
        $('#chartTabs a:first').tab('show');
        $('#traceTabs a:first').tab('show');
    });
</script>
</body>
</html>