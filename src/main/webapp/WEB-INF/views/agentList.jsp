<%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
[
<c:forEach items="${applicationAgentList}" var="agentId" varStatus="status">
	"${agentId}"
	<c:if test="${!status.last}">,</c:if>
</c:forEach>
]