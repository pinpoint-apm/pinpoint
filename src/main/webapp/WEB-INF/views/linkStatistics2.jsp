<%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
{
	"resultFrom" : ${resultFrom}, 
	"resultTo" : ${resultTo},
	"srcApplicationName" : "${srcApplication.name}",
	"destApplicationName" : "${destApplication.name}",
	"srcApplicationType" : "${srcApplication.serviceType.desc}",
	"destApplicationType" : "${destApplication.serviceType.desc}",
	"srcApplicationTypeCode" : "${srcApplication.serviceTypeCode}",
	"destApplicationTypeCode" : "${destApplication.serviceTypeCode}",
	"from" : ${from},
	"to" : ${to},
	"failedCount" : ${linkStatistics.failedCount},
	"successCount" : ${linkStatistics.successCount},
	"timeseriesHistogram" : {
		"time" : [
			<c:forEach var="entry" items="${timeseriesValue[0]}" varStatus="status">
				${entry.key}
				<c:if test="${!status.last}">,</c:if>
			</c:forEach>
		],
		"value" : [
			<c:forEach items="${timeseriesSlotIndex}" var="slot" varStatus="status">
				{
				<c:choose>
					<c:when test="${slot.key == 2147483646}">"key" : "Slow",</c:when>
					<c:when test="${slot.key == 2147483647}">"key" : "Failed",</c:when>
					<c:otherwise>
						<c:if test="${slot.key >= 1000}">"key" : "${slot.key / 1000}s",</c:if>
						<c:if test="${slot.key < 1000}">"key" : "${slot.key}ms",</c:if>
					</c:otherwise>
				</c:choose>
				"data" : [
					<c:forEach var="entry" items="${timeseriesValue[slot.value]}" varStatus="mapStatus">
						${entry.value}
						<c:if test="${!mapStatus.last}">,</c:if>
					</c:forEach>
				]
				}
				<c:if test="${!status.last}">,</c:if>
			</c:forEach>
		]
	}
}