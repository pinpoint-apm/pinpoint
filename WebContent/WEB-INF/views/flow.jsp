<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
{
	"nodes":
		[
		<c:forEach items="${nodes}" var="node" varStatus="status">
		{"name":"${node}"}<c:if test="${!status.last}">,</c:if>
		</c:forEach>
		],
	"links":
		[
		<c:forEach items="${links}" var="link" varStatus="status">
		{"source":${link.from.sequence},"target":${link.to.sequence},"value":${link.callCount}}
		<c:if test="${!status.last}">,</c:if>
		</c:forEach>
		]
}