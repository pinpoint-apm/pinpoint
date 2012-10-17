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

    <link href="/common/css/bootstrap/bootstrap.css" rel="stylesheet">
    <link href="/common/css/bootstrap/bootstrap-responsive.css" rel="stylesheet"/>

    <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
    <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->

    <script type="text/javascript" src="/common/js/jquery/jquery-1.7.1.min.js"></script>
    <script type="text/javascript" src="/common/js/bootstrap.min.js"></script>
</head>
<body>
	<h4>TraceId: ${traceId}</h4>


	<c:set var="startTime" scope="page" value="0"/>

	<c:forEach items="${spanList}" var="span" varStatus="status">
		<c:set var="sp" scope="page" value="${span.span}"/>
		<c:set var="begin" scope="page" value="0"/>
		<c:set var="end" scope="page" value="0"/>
		
		
		<c:forEach items="${sp.annotations}" var="ano" varStatus="annoStatus">
			<c:if test="${ano.key eq 'CS' or ano.key eq 'SR'}">
				<c:set var="begin" scope="page" value="${ano.timestamp}"/>
				
				<c:if test="${status.first}">
					<c:set var="startTime" scope="page" value="${ano.timestamp}"/>
				</c:if>
			</c:if>
			<c:if test="${ano.key eq 'CR' or ano.key eq 'SS'}">
				<c:set var="end" scope="page" value="${ano.timestamp}"/>
			</c:if>
			${hippo:bytesToString(ano.valueTypeCode, ano.value)} 
		</c:forEach>
		
		
		<div style="width:${end - begin}px; background-color:red;margin-left:px">
		${sp.serviceName}<br/>
		${end - begin}
		</div>
		
		
		<br/>
		<br/>
	</c:forEach>














		
	<table id="businessTransactions" class="table table-bordered">
	    <thead>
	    <tr>
	    	<th>#</th>
	        <th>TIME</th>
	        <th>GAP</th>
	        <th>Application</th>
	        <th>time</th>
	        <th>endpoint</th>
	        <th>Action</th>
	        <th>Action</th>
	    </tr>
	    </thead>
	    <tbody>
	
		<c:forEach items="${spanList}" var="span" varStatus="status">
		<c:set var="sp" scope="page" value="${span.span}"/>
			<c:forEach items="${sp.annotations}" var="ano" varStatus="annoStatus">
				<tr>
					<td>${status.count}</td>
					<td>${ano.timestamp}</td>
					<td>
					<c:if test="${not annoStatus.first}">${ano.timestamp - bt}<br/>${ano.timestamp}<br/>${bt}</c:if>
					</td>
					   
					<td>${sp.serviceName}</td>
					<td>${sp.timestamp}</td>
					<td>${sp.endPoint}</td>
					<td>${ano.key}</td> 
					<td>${hippo:bytesToString(ano.valueTypeCode, ano.value)}</td> 
				</tr>
			   <c:set var="bt" scope="page" value="${ano.timestamp}"/>
			</c:forEach>
			<tr><td colspan="8">&nbsp;</td></tr>
		</c:forEach>
	
	    </tbody>
	</table>
</body>
</html>