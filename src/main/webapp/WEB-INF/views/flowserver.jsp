<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
{
"graphdata" : {
"nodes" : [
<c:forEach items="${nodes}" var="node" varStatus="status">
    {
	    "name" : "${node}",
	    "recursiveCallCount" : "${node.recursiveCallCount}",
	    "agentIds" : [
	    <c:forEach items="${node.agentIds}" var="agentId" varStatus="status2">
	        "${agentId}"
	        <c:if test="${!status2.last}">,</c:if>
	    </c:forEach>
	    ],
	    "serviceType" : "${node.serviceType}",
	    "terminal" : "${node.serviceType.terminal}"
    } <c:if test="${!status.last}">,</c:if>
</c:forEach>
],
"links" : [
<c:forEach items="${links}" var="link" varStatus="status">
    {
		"source" : ${link.from.sequence},
		"target" : ${link.to.sequence},
		"value" : ${link.requestCount},
		"histogram" : ${link.histogram}
	} <c:if test="${!status.last}">,</c:if>
</c:forEach>
]
},

"businessTransactions" : [
<c:forEach items="${businessTransactions}" var="t" varStatus="status">
    {
	    "name" : "${t.rpc}",
	    "calls" : ${t.calls},
	    "avgTime" : ${t.totalTime / t.calls},
	    "minTime" : ${t.minTime},
	    "maxTime" : ${t.maxTime},
	    "health" : 1,
	    "traces" : [
	    <c:forEach items="${t.traces}" var="trace" varStatus="status2">
	        { "traceId" : "${trace.traceId}", "executionTime" : ${trace.executionTime}, "timestamp" : ${trace.startTime} }
	        <c:if test="${!status2.last}">,</c:if>
	    </c:forEach>
	    ]
    } <c:if test="${!status.last}">,</c:if>
</c:forEach>
],

"scatter" : [
<c:forEach items="${traces}" var="t" varStatus="status3">
    <c:forEach items="${t.traces}" var="trace" varStatus="status4">
        {
	        "traceId" : "${trace.traceId}",
	        "timestamp" : ${trace.startTime},
	        "executionTime" : ${trace.executionTime},
	        "name" : "${t.rpc}",
	        "exception" : ${trace.exception}
        } <c:if test="${!status4.last}">,</c:if>
    </c:forEach>
    <c:if test="${!status3.last}">,</c:if>
</c:forEach>
]
}