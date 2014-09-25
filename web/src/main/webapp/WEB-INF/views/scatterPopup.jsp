<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="pinpoint" uri="http://pinpoint.nhncorp.com/pinpoint" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>PINPOINT - ${applicationName} response scatter</title>
    <meta charset="utf-8">
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

	<link type="text/css" rel="stylesheet" href="/components_v1/bootstrap/css/bootstrap.css" />
    <link type="text/css" rel="stylesheet" href="/components_v1/bootstrap/css/bootstrap-responsive.css" />
    <link type="text/css" rel="stylesheet" href="/components_v1/pinpoint/css/pinpoint.css" />
    <link type="text/css" rel="stylesheet" href="/components_v1/pinpoint-scatter/css/scatter.css" />

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
    <script type="text/javascript" src="/components_v1/utils/date.js"></script>
    <script type="text/javascript" src="/components_v1/pinpoint/pinpoint.js"></script>
    
    <!-- scatter chart -->
	<script type="text/javascript" src="/components_v1/jquery-dragtoselect/jquery.dragToSelect.js"></script>
	<script type="text/javascript" src="/components_v1/pinpoint-scatter/jquery.BigScatterChart.js"></script>
    <script type="text/javascript" src="/components_v1/pinpoint/chart-scatter4.js"></script>
    
    <!-- help -->
    <script type="text/javascript" src="/components_v1/pinpoint/message.js"></script>
    
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