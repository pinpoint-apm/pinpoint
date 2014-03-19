<%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
{
	"lastFetchedTimestamp" : ${lastFetchedTimestamp},
	"applicationMapData" : {
		"nodeDataArray": [
			<c:forEach items="${nodes}" var="node" varStatus="status">
			${node.nodeJson} <c:if test="${!status.last}">,</c:if>
			</c:forEach>
		],
		"linkDataArray": [
			<c:forEach items="${links}" var="link" varStatus="status">
			{
				"id" : "${link.linkName}",
				"from" : "${link.from.nodeName}",
				"to" : "${link.to.nodeName}",
				"sourceInfo" : ${link.from.json},
				"targetInfo" : ${link.to.json},
                "filterApplicationName" : "${link.filterApplication.name}",
                "filterApplicationServiceTypeCode" : ${link.filterApplication.serviceTypeCode},
				"text" : ${link.histogram.totalCount},
				"error" : ${link.histogram.errorCount},
				"slow" : ${link.histogram.verySlowCount},
				"histogram" : ${link.histogram.json},
                "sourceHistogram" : {
                    <c:forEach items="${link.sourceList.callHistogramList}" var="linkAgentHistogram" varStatus="linkAgentHistogramStatus">
                        "${linkAgentHistogram.id}" : ${linkAgentHistogram.histogram.json}
                    <c:if test="${!linkAgentHistogramStatus.last}">,</c:if>
                    </c:forEach>
                },
				"targetHosts" : {
					<c:forEach items="${link.targetList.callHistogramList}" var="host" varStatus="status2">
						"${host.id}" : {
							"histogram" : ${host.histogram.json}
						}<c:if test="${!status2.last}">,</c:if>
					</c:forEach>
				},
				<c:choose>
					<c:when test="${(link.histogram.errorCount / link.histogram.totalCount * 100) > 10}">"category" : "bad"</c:when>
					<c:otherwise>"category" : "default"</c:otherwise>
				</c:choose>
			} <c:if test="${!status.last}">,</c:if>
			</c:forEach>   	
		]
	}
}