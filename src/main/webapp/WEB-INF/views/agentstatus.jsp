<%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
{
<c:forEach items="${statusMap}" var="agentStatus" varStatus="status">
"${agentStatus.key}" : [ ${agentStatus.value.exists},
	<c:choose>
	<c:when test="${agentStatus.value.exists}">
	${agentStatus.value.checkTime - agentStatus.value.agentInfo.startTime},
	${agentStatus.value.agentInfo.startTime}
	</c:when>
	<c:otherwise>
	-1, -1
	</c:otherwise>
	</c:choose>
]<c:if test="${!status.last}">,</c:if>
</c:forEach>
}