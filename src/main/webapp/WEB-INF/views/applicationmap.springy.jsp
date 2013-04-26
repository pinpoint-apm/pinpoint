<%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
{
"graphdata" : {
	"nodes" : [
	<c:forEach items="${nodes}" var="node" varStatus="status">
	    {
		    "name" : "${node.applicationName}",
		    "hosts" : [
		    <c:forEach items="${node.hosts}" var="host" varStatus="status2">
		        "${host}"
		        <c:if test="${!status2.last}">,</c:if>
		    </c:forEach>
		    ],
		    "serviceType" : "${node.serviceType.desc}",
		    "terminal" : "${node.serviceType.terminal}",
		    "agents" : [
		    <c:forEach items="${node.agents}" var="agent" varStatus="status3">
		        ${agent.json}
		        <c:if test="${!status3.last}">,</c:if>
		    </c:forEach>
		    ]
	    } <c:if test="${!status.last}">,</c:if>
	</c:forEach>
	],
	"links" : [
	<c:forEach items="${links}" var="link" varStatus="status">
	    {
			"source" : ${link.from.sequence},
			"target" : ${link.to.sequence},
			"sourceinfo" : ${link.from.json},
			"targetinfo" : ${link.to.json},
			"value" : ${link.histogram.totalCount},
			"error" : ${link.histogram.errorCount},
			"slow" : ${link.histogram.slowCount},
			"histogram" : ${link.histogram}
		} <c:if test="${!status.last}">,</c:if>
	</c:forEach>
	]
},
"gojs" : {
	"nodeDataArray": [
	 	{	"key" : "UNKNOWN_GROUP",
	 		"isGroup" : true,
			"text" : "UNKNOWN"
		},
		<c:forEach items="${nodes}" var="node" varStatus="status">
		    {
		    	"key" : ${status.count},
			    "text" : "${node.applicationName}",
			    "hosts" : [
			    <c:forEach items="${node.hosts}" var="host" varStatus="status2">
			        "${host}"
			        <c:if test="${!status2.last}">,</c:if>
			    </c:forEach>
			    ],
			    "category" : 
			    <c:choose>
			    	<c:when test="${node.serviceType.desc == 'UNKNOWN_CLOUD'}">
						"${node.serviceType.desc}", "group" : "UNKNOWN_GROUP",
					</c:when>
			    	<c:otherwise>"${node.serviceType.desc}",</c:otherwise>
			    </c:choose>
			    "terminal" : "${node.serviceType.terminal}",
			    "agents" : [
			    <c:forEach items="${node.agents}" var="agent" varStatus="status3">
			        ${agent.json}
			        <c:if test="${!status3.last}">,</c:if>
			    </c:forEach>
			    ]
		    } <c:if test="${!status.last}">,</c:if>
		</c:forEach>
  	],
  	"linkDataArray": [
	 	<c:forEach items="${links}" var="link" varStatus="status">
		    {
				"from" : ${link.from.sequence + 1},
				"to" : ${link.to.sequence + 1},
				"sourceinfo" : ${link.from.json},
				"targetinfo" : ${link.to.json},
				"text" : ${link.histogram.totalCount},
				"error" : ${link.histogram.errorCount},
				"slow" : ${link.histogram.slowCount},
				"histogram" : ${link.histogram}
			} <c:if test="${!status.last}">,</c:if>
		</c:forEach>   	
  	]
}
	
}