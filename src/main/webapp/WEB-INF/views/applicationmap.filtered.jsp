<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="hippo" uri="http://hippo.nhncorp.com/hippo" %>
{
	"applicationMapData" : {
		"nodeDataArray": [
			<c:forEach items="${nodes}" var="node" varStatus="status">
			{
 			   	"id" : ${status.count},
 			   	"key" : ${status.count},
 			    "text" : "${node}",
 			    "hosts" : [
 			    <c:forEach items="${node.hosts}" var="host" varStatus="status2">
 			        "${host}"
 					<c:if test="${!status2.last}">,</c:if>
				</c:forEach>
				],
				"category" : "${node.serviceType.desc}",
				"terminal" : "${node.serviceType.terminal}"
			} <c:if test="${!status.last}">,</c:if>
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
			} <c:if test="${!status.last}">,</c:if>
			</c:forEach>   	
		]
	}
}