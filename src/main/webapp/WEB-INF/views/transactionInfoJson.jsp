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
		"service":4,
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
"${record.service}",
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
<c:choose><c:when test="${record.title == 'Exception'}">false, true</c:when><c:when test="${record.focused}">true,false</c:when><c:otherwise>false,false</c:otherwise></c:choose>
]<c:if test="${!status.last}">,</c:if></c:forEach>
],
	"applicationMapData" : {
		"nodeDataArray": [
			<c:forEach items="${nodes}" var="node" varStatus="status">
			{
				"id" : ${status.count},
				"key" : ${status.count},
				<c:choose>
					<c:when test="${node.serviceType.desc == 'USER'}">
					"text" : "USER",
					</c:when>
					<c:otherwise>
					"text" : "${node}",
					</c:otherwise>
				</c:choose>
				"hosts" : [
					<c:forEach items="${node.hosts}" var="host" varStatus="status2">
					"${host}"
					<c:if test="${!status2.last}">,</c:if>
					</c:forEach>
				],
				"category" : "${node.serviceType.desc}",
				"terminal" : "${node.serviceType.terminal}"
			}
			<c:if test="${!status.last}">,</c:if>
			</c:forEach>
		],
		"linkDataArray": [
			<c:forEach items="${links}" var="link" varStatus="status">
			{
				"id" : "${link.from.sequence + 1}-${link.to.sequence + 1}",
				"from" : ${link.from.sequence + 1},
				"to" : ${link.to.sequence + 1},
				"text" : ${link.histogram.totalCount},
				"error" : ${link.histogram.errorCount},
				"slow" : ${link.histogram.slowCount},
				"histogram" : ${link.histogram}
			}
			<c:if test="${!status.last}">,</c:if>
			</c:forEach>   	
		]
    }
}