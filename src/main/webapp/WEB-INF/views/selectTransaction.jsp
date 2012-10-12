<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<html>
<head>
    <title>transaction detail</title>
</head>
<body>


<br/>
<h4>TraceId: ${traceId}</h4>

<br/>
<br/>

<c:forEach items="${spanList}" var="span" varStatus="status">
    <c:set var="sp" scope="page" value="${span.span}"/>
    ${status.count} : ${span.depth} : ${sp.agentID} : time: ${sp.timestamp} :${sp.spanID}, ${sp.parentSpanId}
    <br/>
    &nbsp;&nbsp; &nbsp;${sp.serviceName} : ${sp.name} : endpoint:${sp.endPoint}
    <br/>
    <c:forEach items="${sp.annotations}" var="ano">
        &nbsp;&nbsp; &nbsp; ${ano.value}: duration:${ano.duration}
        <br/>
    </c:forEach>
    <br/>

</c:forEach>

</body>
</html>