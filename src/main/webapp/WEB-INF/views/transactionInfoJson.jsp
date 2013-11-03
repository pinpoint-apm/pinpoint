<%@ page language="java" contentType="application/javascript; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="pinpoint" uri="http://pinpoint.nhncorp.com/pinpoint" %>
{
	"applicationName" : "${applicationName}",
	"transactionId" : "${traceId.formatString}",
	"agentId" : "${recordSet.agentId}",
	"applicationId" : "${recordSet.applicationId}",
	"callStackStart" : ${callstackStart},
	"callStackEnd" : ${callstackEnd},
    "completeState" : "${completeState}",
	"callStackIndex" : {
		"depth":0,
		"begin":1,
		"end":2,
		"excludeFromTimeline":3,
		"applicationName":4,
		"tab":5,
		"id":6,
		"parentId":7,
		"isMethod":8,
		"hasChild":9,
		"title":10,
		"arguments":11,
		"executeTime":12,
		"gap":13,
		"elapsedTime":14,
		"barWidth":15,                 
		"simpleClassName":16,
		"apiType":17,
		"agent":18,
		"isFocused":19,
		"hasException":20
	},
	"callStack" : [
<c:forEach items="${callstack}" var="record" varStatus="status">[
"${span.depth}",
${record.begin},
${record.begin + record.elapsed},
${record.excludeFromTimeline},
"${record.applicationName}",
${record.tab},
"${record.id}",
"<c:if test="${record.parentId > 0}">${record.parentId}</c:if>",
${record.method},
${record.hasChild},
"${record.title}",
"${record.arguments}",
"<c:if test="${record.method}">${pinpoint:longToDateStr(record.begin, "HH:mm:ss SSS")}</c:if>",
"<c:if test="${record.method}"><fmt:formatNumber value="${record.gap}" type="number" /></c:if>",
"<c:if test="${record.method}"><fmt:formatNumber type="number" value="${record.elapsed}"/></c:if>",
"<c:if test="${record.method}"><fmt:formatNumber value="${((end - begin) * barRatio) + 0.9}" type="number" pattern="#"/></c:if>",                 
"${record.simpleClassName}",
"${record.apiType}",
"${record.agent}",
${record.focused},
${record.hasException}
]<c:if test="${!status.last}">,</c:if></c:forEach>
],
	"applicationMapData" : {
		"nodeDataArray": [
			<c:forEach items="${nodes}" var="node" varStatus="status">
			{
				"id" : ${status.count},
				"key" : ${status.count},
				<c:choose>
					<c:when test="${node.applicationName == 'CLIENT'}">
					"text" : "USER",
					</c:when>
					<c:otherwise>
					"text" : "${node.applicationName}",
					</c:otherwise>
				</c:choose>
				<c:choose>
					<c:when test="${node.serviceType.desc == 'CLIENT'}">
					"category" : "USER",
					</c:when>
					<c:otherwise>
					"category" : "${node.serviceType.desc}",
					</c:otherwise>
				</c:choose>
				<c:choose>
					<c:when test="${node.serviceType.desc == 'CLIENT'}">"fig" : "Ellipse"</c:when>
					<c:when test="${node.serviceType.desc == 'TOMCAT'}">"fig" : "RoundedRectangle"</c:when>
					<c:otherwise>"fig" : "Rectangle"</c:otherwise>
				</c:choose>,
				"serviceTypeCode" : "${node.serviceType.code}",
				"terminal" : "${node.serviceType.terminal}",
				"isWas" : ${node.serviceType.was},
				"serverList" : {
					<c:forEach items="${node.serverInstanceList}" var="serverInstance" varStatus="status5">
						"${serverInstance.key}" : {
							"name" : "${serverInstance.key}", 
							"status" : null,
							"instanceList" : {
								<c:forEach items="${serverInstance.value}" var="instance" varStatus="status6">
								"${instance.key}" : ${instance.value.json}
									<c:if test="${!status6.last}">,</c:if>
								</c:forEach>								
							}
						}
						<c:if test="${!status5.last}">,</c:if>
					</c:forEach>
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
					<c:forEach items="${link.hostList}" var="host" varStatus="status2">
						"${host.value.host}" : {
							"histogram" : ${host.value.histogram.json}
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