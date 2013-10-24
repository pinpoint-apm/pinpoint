<%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
{
<c:forEach items="${applicationAgentList}" var="agentListMap" varStatus="status">
	"${agentListMap.key}" : {
		"agentList" : [
			<c:forEach items="${agentListMap.value}" var="agent" varStatus="status2">
				${agent.json}
				<c:if test="${!status2.last}">,</c:if>
			</c:forEach>
		]
	}
	<c:if test="${!status.last}">,</c:if>
</c:forEach>
}