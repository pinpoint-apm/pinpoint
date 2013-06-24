<%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
{
	"from" : ${from},
	"to" : ${to},
	"histogramSummary" : [
		<c:if test="${applicationStatistics.successCount > 0 or applicationStatistics.failedCount > 0}">
		{
			"key" : "Response time Histogram",
			"values" : [
				<c:forEach items="${applicationStatistics.values}" var="item" varStatus="status">
				{
					<c:choose>
						<c:when test="${item.key == 2147483647}">
						"label" : "Failed",
						</c:when>
						<c:when test="${item.key == 2147483646}">
						"label" : "Slow",
						</c:when>
						<c:otherwise>
						"label" : "${item.key}",
						</c:otherwise>
					</c:choose>
					"value" : ${item.value}
				} <c:if test="${!status.last}">,</c:if>
				</c:forEach>
			]
		}
		</c:if>
	]
}