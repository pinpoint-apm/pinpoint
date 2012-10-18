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
    <script type="text/javascript">
    function showDetail(id) {
    	$("#spanDetail" + id).css("display", "");
    	$("#spanDetail" + id).css("top", event.pageY);
    	$("#spanDetail" + id).css("left", event.pageX);
    }
    
    function hideDetail(id) {
    	$("#spanDetail" + id).css("display", "none");
    }
    </script>
</head>
<body>
	<h4>TraceId: ${traceId}</h4>


<div id="timeline" style="margin-left:100px;margin-top:50px;background-color:#E8E8E8;width:1000px;">
	<c:set var="startTime" scope="page" value="0"/>
	<c:set var="endTime" scope="page" value="0"/>
	<c:forEach items="${spanList}" var="span" varStatus="status">
		<c:set var="sp" scope="page" value="${span.span}"/>
		<c:set var="begin" scope="page" value="0"/>
		<c:set var="end" scope="page" value="0"/>
		
		<div id="spanDetail${status.count}" style="display:none; position:absolute; left:0; top:0;width:500px;background-color:#E8CA68;padding:10px;">
		<ul>
			<li>AgentId = ${sp.agentId}</li>
			<li>UUID = ${hippo:longLongToUUID(sp.mostTraceId, sp.leastTraceId)}</li>
			<li>spanId = ${sp.spanId}</li>
			<li>parentSpanId = ${sp.parentSpanId}</li>
			<li>service = ${sp.serviceName}</li>
			<li>name = ${sp.name}</li>
			<li>timestamp = ${hippo:longToDateStr(sp.timestamp)}</li>
			<li>endpoint = ${sp.endPoint}</li>
			<li>terminal = ${sp.terminal}</li>
		
			<c:forEach items="${sp.annotations}" var="ano" varStatus="annoStatus">
				<c:if test="${ano.key eq 'CS' or ano.key eq 'SR'}">
					<c:set var="begin" scope="page" value="${ano.timestamp}"/>
					<li>${ano.key} = ${ano.duration}</li>				
					<c:if test="${status.first}">
						<c:set var="startTime" scope="page" value="${ano.timestamp}"/>
					</c:if>
				</c:if>
				<c:if test="${ano.key eq 'CR' or ano.key eq 'SS'}">
					<c:set var="end" scope="page" value="${ano.timestamp}"/>
					<li>${ano.key} = ${ano.duration}</li>				
					<c:if test="${status.first}">
						<c:set var="endTime" scope="page" value="${ano.timestamp}"/>
					</c:if>
				</c:if>
				<c:if test="${ano.key != 'CR' and ano.key != 'SS' and ano.key != 'CS' and ano.key != 'SR'}">
				<li>${ano.key} = ${hippo:bytesToString(ano.valueTypeCode, ano.value)}</li>
				</c:if>
			</c:forEach>
		</ul>
		</div>
		
		<c:if test="${status.first}">
			<c:set var="barRatio" scope="page" value="${1000 / (end - begin)}"/>
		</c:if>
		
		<div style="width:${(end - begin) * barRatio}px; background-color:#69B2E9;margin-left:${(begin - startTime) * barRatio}px;margin-top:3px;" onmouseover="showDetail(${status.count})" onmouseout="hideDetail(${status.count})">
			<div style="width:200px;">${sp.serviceName} (${end - begin}ms)</div>
		</div>
	</c:forEach>
</div>

	<script type="text/javascript">
	//$("#timeline").css("width", ${endTime - startTime});
	</script>


struct Annotation {
  1: i64 timestamp,
  2: optional i64 duration,
  3: string key,
  4: i32 valueTypeCode,
  5: optional binary value,
}

struct Span {
  1: string agentID
  2: i64 timestamp,
  3: i64 mostTraceID
  4: i64 leastTraceID
  5: string name,
  6: string serviceName
  7: i64 spanID,
  8: optional i64 parentSpanId,
  9: list<Annotation> annotations,
  10: optional i32 flag = 0
  11: string endPoint
  12: bool terminal
}




	<br/>
	<br/>
	<br/>
	<br/>

		
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