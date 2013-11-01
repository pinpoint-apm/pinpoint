<%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
{
	"srcApplicationName" : "${srcApplicationName}",
	"destApplicationName" : "${destApplicationName}",
	"srcApplicationType" : "${srcApplicationType.desc}",
	"destApplicationType" : "${destApplicationType.desc}",
	"srcApplicationTypeCode" : "${srcApplicationType.code}",
	"destApplicationTypeCode" : "${destApplicationType.code}",
	"from" : ${from},
	"to" : ${to},
	"lastFetchedTimestamp" : ${lastFetchedTimestamp},
	"failedCount" : ${linkStatistics.failedCount},
	"successCount" : ${linkStatistics.successCount},
	"histogramSummary" : [
		<c:if test="${linkStatistics.failedCount > 0 or linkStatistics.successCount > 0}">
		{
			"key" : "Responsetime Histogram",
			"values" : [
		        <c:set var="lastSlot" scope="page" value="0"/>
				<c:forEach items="${histogramSummary}" var="item" varStatus="status">
				{
					<c:choose>
						<c:when test="${item.key == linkStatistics.error}">
						"label" : "error",
						</c:when>
						<c:when test="${item.key == linkStatistics.slow}">
						"label" : "${lastSlot}+",
						</c:when>
						<c:otherwise>
						"label" : "${item.key}",
						</c:otherwise>
					</c:choose>
					"value" : ${item.value}
				} <c:if test="${!status.last}">,</c:if>
		        <c:set var="lastSlot" scope="page" value="${item.key}"/>
				</c:forEach>
			]
		}
		</c:if>
	],
	"timeseriesHistogram" : [ <c:forEach items="${timeseriesSlotIndex}" var="slot" varStatus="status">
		{
			<c:choose>
				<c:when test="${slot.key == 2147483646}">"key" : "Slow",</c:when>
				<c:when test="${slot.key == 2147483647}">"key" : "Failed",</c:when>
				<c:otherwise>
					<c:if test="${slot.key >= 1000}">"key" : "${slot.key / 1000}s",</c:if>
					<c:if test="${slot.key < 1000}">"key" : "${slot.key}ms",</c:if>
				</c:otherwise>
			</c:choose>
			"values" : [
				<c:forEach var="entry" items="${timeseriesValue[slot.value]}" varStatus="mapStatus">
				[ ${entry.key}, ${entry.value} ]<c:if test="${!mapStatus.last}">,</c:if></c:forEach>
			]
		}<c:if test="${!status.last}">,</c:if></c:forEach>
	]
}