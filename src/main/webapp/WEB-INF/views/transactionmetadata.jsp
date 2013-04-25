<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
{
	"metadata" : [
		<c:forEach items="${metadata}" var="span" varStatus="status">
		{
			"traceId" : "${span.traceId}",
			"collectorAcceptTime" : "${span.collectorAcceptTime}",
			"startTime" : ${span.startTime},
			"elapsed" : ${span.elapsed},
			"application" : "${span.rpc}",
			"agentId" : "${span.agentId}",
			"endpoint" : "${span.endPoint}",
			"exception" : ${span.exception},
			"remoteAddr" : "${span.remoteAddr}"
		}
    	<c:if test="${!status.last}">,</c:if>
		</c:forEach>
	]
}