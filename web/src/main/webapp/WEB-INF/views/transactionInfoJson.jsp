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
"${pinpoint:escapeJson(record.title)}",
"${pinpoint:escapeJson(pinpoint:escapeHtml(record.arguments))}",
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
            ${node.nodeJson} <c:if test="${!status.last}">,</c:if>
			</c:forEach>
		],
		"linkDataArray": [
			<c:forEach items="${links}" var="link" varStatus="status">
             ${link.json} <c:if test="${!status.last}">,</c:if>
			</c:forEach>   	
		]
	}
}