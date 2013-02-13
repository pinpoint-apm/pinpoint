<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
{
	"metadata" : [
		<c:forEach items="${metadata}" var="v" varStatus="status">
		{
			"traceId" : "${v.traceId}",
			"startTime" : ${v.startTime},
			"elapsed" : ${v.elapsed},
			"application" : "${v.rpc}",
			"agentId" : "${v.agentId}",
			"endpoint" : "${v.endPoint}",
			"exception" : ${v.exception},
			"remoteAddr" : "${v.remoteAddr}"
		}
    	<c:if test="${!status.last}">,</c:if>
		</c:forEach>
	]
}