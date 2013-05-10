<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="hippo" uri="http://hippo.nhncorp.com/hippo" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>PINPOINT - ${applicationName} response scatter</title>
    <meta charset="utf-8">
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link href="/common/css/hippo/hippo.css" rel="stylesheet"/>
    <link href="/common/css/hippo/scatter.css" rel="stylesheet"/>

    <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
    <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->

	<!-- commons -->    
    <script type="text/javascript" src="/common/js/jquery/jquery-1.9.1.min.js"></script>
    <script type="text/javascript" src="/common/js/jquery/jquery-ui-1.10.2.js"></script>
	<script type="text/javascript" src="/common/js/modernizr-2.6.2.min.js"></script>
    <script type="text/javascript" src="/common/js/date.js"></script>
	<script type="text/javascript" src="/common/js/hippo/scatter/underscore-min.js"></script>
	<script type="text/javascript" src="/common/js/hippo/scatter/jquery.Class.js"></script>
    <script type="text/javascript" src="/common/js/hippo/hippo.js"></script>
    
    <!-- scatter chart -->
    <script type="text/javascript" src="/common/js/hippo/chart-scatter4.js"></script>
	<script type="text/javascript" src="/common/js/hippo/scatter/jquery.dragToSelect.js"></script>
	<script type="text/javascript" src="/common/js/hippo/scatter/jquery.BigScatterChart.js"></script>
    
    <style type="text/css">
    body {
    	padding: 30px;
	}
    </style>
</head>
<body>
<h5>'${applicationName}' response scatter</h5>
<h5></h5>
<div id="scatterchart"></div>
<script type="text/javascript">
$(document).ready(function () {
	showResponseScatter("${applicationName}", ${from}, ${to}, ${period}, ${usePeriod}, "${filter}", 800, 500);
	$("H5:nth-child(2)").text(new Date(${from}).toString("yyyy-MM-dd HH:mm") + " ~ " + new Date(${to}).toString("yyyy-MM-dd HH:mm"))
});
</script>
</body>
</html>