<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
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
			"value" : ${link.histogram.totalCount},
			"error" : ${link.histogram.errorCount},
			"slow" : ${link.histogram.slowCount},
			"histogram" : ${link.histogram}
		} <c:if test="${!status.last}">,</c:if>
	</c:forEach>
	]
	}
}