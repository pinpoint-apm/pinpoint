<%@ page contentType="text/html; charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="pinpoint" uri="http://pinpoint.nhncorp.com/pinpoint" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="en">
<head>
    <title>PINPOINT - ERROR</title>
    <meta charset="utf-8">
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    
	<link type="text/css" rel="stylesheet" href="/components_v1/bootstrap/css/bootstrap.css" />
    <link type="text/css" rel="stylesheet" href="/components_v1/bootstrap/css/bootstrap-responsive.css" />
    <link type="text/css" rel="stylesheet" href="/components_v1/pinpoint/css/pinpoint.css" />

    <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
    <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->

	<!-- commons -->    
    <script type="text/javascript" src="/components_v1/jquery/jquery-1.9.1.min.js"></script>
    <script type="text/javascript" src="/components_v1/bootstrap/js/bootstrap.min.js"></script>
</head>
<body>
<div class="container">
	<div class="row">
		<div class="span12">
			<c:if test="${errorCode eq 9}">
			<h4>
			Trace not found.
			</h4>
			</c:if>
		</div>
	</div>
</div>
</body>
</html>