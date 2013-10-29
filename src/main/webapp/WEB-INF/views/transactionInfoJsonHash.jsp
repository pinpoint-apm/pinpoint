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
	
	"callStack" : [
		<c:set var="startTime" scope="page" value="${callstackStart}"/>
		<c:set var="endTime" scope="page" value="${callstackEnd}"/>
		<c:set var="seq" scope="page" value="0"/>

		<c:forEach items="${callstack}" var="record" varStatus="status">
		{
			<c:set var="depth" scope="page" value="${span.depth}"/>
			<c:if test="${record.method}">
				<c:set var="begin" scope="page" value="${record.begin}"/>
				<c:set var="end" scope="page" value="${record.begin + record.elapsed}"/>
			</c:if>
			<c:if test="${status.first}">
				<c:set var="barRatio" scope="page" value="${100 / (end - begin)}"/>
			</c:if>
		
			<c:choose>
				<c:when test="${record.parentId > 0}">
					<c:set var="pid" value="${record.parentId}" />
				</c:when>
				<c:otherwise>
					<c:set var="pid" value="" />
				</c:otherwise>                
			</c:choose>
			
			"depth" : "${span.depth}",
			"begin" : ${record.begin},
			"end" : ${record.begin + record.elapsed},
			"excludeFromTimeline" : ${record.excludeFromTimeline},
			"service" : "${record.service}",
			
			"tab":${record.tab},
			"id":"${record.id}",
			"parentId":"${pid}",
			"isMethod":${record.method},
			"hasChild":${record.hasChild},
			"title":"${record.title}",
			"arguments":"${record.arguments}",
			"executeTime": "<c:if test="${record.method}">${pinpoint:longToDateStr(record.begin, "HH:mm:ss SSS")}</c:if>",
			"gap":"<c:if test="${record.method}"><fmt:formatNumber value="${record.gap}" type="number" /></c:if>",
			"elapsedTime":"<c:if test="${record.method}"><fmt:formatNumber type="number" value="${record.elapsed}"/></c:if>",
			"barWidth":"<c:if test="${record.method}"><fmt:formatNumber value="${((end - begin) * barRatio) + 0.9}" type="number" pattern="#"/></c:if>",                 
			"simpleClassName":"${record.simpleClassName}",
			"apiType":"${record.apiType}",
			"agent":"${record.agent}",
                 	
			<c:choose>
				<c:when test="${record.title == 'Exception'}">
					"isFocused":false,
					"hasException":true
				</c:when>
				<c:when test="${record.focused}">
					"isFocused":true,
					"hasException":false
				</c:when>
				<c:otherwise>
					"isFocused":false,
					"hasException":false
				</c:otherwise>                
			</c:choose>
		}
		<c:if test="${!status.last}">,</c:if>
      	</c:forEach>
	],
	"mapData" : {
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