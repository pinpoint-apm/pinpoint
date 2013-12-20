<%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
{
	"applicationMapData" : {
		"nodeDataArray": [
			<c:forEach items="${nodes}" var="node" varStatus="status">
			{
				"id" : ${status.count},
				"key" : ${status.count},
				<c:choose>
					<c:when test="${node.applicationName == 'USER'}">
					"text" : "USER",
					</c:when>
					<c:otherwise>
					"text" : "${node.applicationName}",
					</c:otherwise>
				</c:choose>
				<c:choose>
					<c:when test="${node.serviceType.desc == 'USER'}">
					"category" : "USER",
					</c:when>
					<c:otherwise>
					"category" : "${node.serviceType.desc}",
					</c:otherwise>
				</c:choose>
				<c:choose>
					<c:when test="${node.serviceType.desc == 'USER'}">"fig" : "Ellipse"</c:when>
					<c:when test="${node.serviceType.desc == 'TOMCAT'}">"fig" : "RoundedRectangle"</c:when>
					<c:otherwise>"fig" : "Rectangle"</c:otherwise>
				</c:choose>,
				"serviceTypeCode" : "${node.serviceType.code}",
				"terminal" : "${node.serviceType.terminal}",
				"isWas" : ${node.serviceType.was},
				"serverList" : {
					<c:if test="${node.serviceType.desc != 'UNKNOWN'}">
					<c:forEach items="${node.serverInstanceList}" var="serverInstance" varStatus="status5">
						"${serverInstance.key}" : {
							"name" : "${serverInstance.key}", 
							"status" : null,
							"instanceList" : {
								<c:forEach items="${serverInstance.value}" var="instance" varStatus="status6">
								"${instance.id}" : ${instance.json}
									<c:if test="${!status6.last}">,</c:if>
								</c:forEach>								
							}
						}
						<c:if test="${!status5.last}">,</c:if>
					</c:forEach>
					</c:if>
				}
			} <c:if test="${!status.last}">,</c:if>
			</c:forEach>
		],
		"linkDataArray": [
			<c:forEach items="${links}" var="link" varStatus="status">
			{
				"id" : "${link.from.sequence + 1}-${link.to.sequence + 1}",
				"from" : ${link.from.sequence + 1},
				"to" : ${link.to.sequence + 1},
				"sourceinfo" : ${link.from.json},
				"targetinfo" : ${link.to.json},
				"text" : ${link.histogram.totalCount},
				"error" : ${link.histogram.errorCount},
				"slow" : ${link.histogram.slowCount},
				"histogram" : ${link.histogram.json},
				"targetHosts" : {
					<c:forEach items="${link.hostList.hostList}" var="host" varStatus="status2">
						"${host.host}" : {
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