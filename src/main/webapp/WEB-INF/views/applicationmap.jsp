<%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
{
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