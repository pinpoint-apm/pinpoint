<%@ page contentType="text/html; charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page isErrorPage="true"  %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="en">
<head>
    <title>PINPOINT - ERROR</title>
    <meta charset="utf-8">
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <%----%>
	<link type="text/css" rel="stylesheet" href="/components/bootstrap/css/bootstrap.css" />
    <link type="text/css" rel="stylesheet" href="/components/bootstrap/css/bootstrap-responsive.css" />
    <%-- deprecated components_v1  --%>
    <%--<link type="text/css" rel="stylesheet" href="/components_v1/pinpoint/css/pinpoint.css" />--%>

    <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
    <!--<script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>-->
    <%--<![endif]-->--%>
</head>
<body>
<div class="container">
	<div class="row">
		<div class="span12">
            <h4>
			<c:if test="${not empty errorCode}">
                errorCode:${errorCode}
            </c:if>
            </h4>
            <c:if test="${not empty message}">
                Message:${message}
            </c:if>
            <c:if test="${not empty stackTrace}">
                Caused:${stacktrace}
            </c:if>
		</div>
	</div>
</div>
</body>
</html>