<%@ page contentType="application/json; charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="pinpoint" uri="http://pinpoint.nhncorp.com/pinpoint" %>
[
	<c:forEach items="${applications}" var="application" varStatus="status">
		{ "applicationName" : "${application.name}", "serviceType" : "${application.serviceType}", "code" : "${application.serviceType.code}" }
		<c:if test="${!status.last}">,</c:if>
	</c:forEach>
]