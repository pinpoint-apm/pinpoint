<%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
{
	"metadata" : [
		<c:forEach items="${metadata}" var="span" varStatus="status">
		{
			"traceId" : "${span.transactionId}",
			"collectorAcceptTime" : ${span.collectorAcceptTime},
			"startTime" : ${span.startTime},
			"elapsed" : ${span.elapsed},
			"application" : "${span.rpc}",
			"agentId" : "${span.agentId}",
			"endpoint" : "${span.endPoint}",
			"exception" : ${span.errCode},
			"remoteAddr" : "${span.remoteAddr}"
		}
    	<c:if test="${!status.last}">,</c:if>
		</c:forEach>
	]
}