<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="hippo" uri="http://hippo.nhncorp.com/hippo" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
[
	<c:forEach items="${applications}" var="application" varStatus="status">
		"${application}"<c:if test="${!status.last}">,</c:if>
	</c:forEach>
]