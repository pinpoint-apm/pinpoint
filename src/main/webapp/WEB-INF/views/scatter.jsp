<%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
{
	"scatter" : [
	<jsp:useBean id="scatter" scope="request" type="java.util.List<com.nhn.pinpoint.web.vo.scatter.Dot>"/>
	<c:forEach items="${scatter}" var="dot" varStatus="status">
		{
			"traceId" : "${dot.transactionId}",
			"timestamp" : ${dot.acceptedTime},
			"executionTime" : ${dot.elapsedTime},
			"resultCode" : ${dot.exceptionCode}
		}
	    <c:if test="${!status.last}">,</c:if>
	</c:forEach>
	]
}