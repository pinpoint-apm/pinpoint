<%@ page language="java" contentType="application/javascript; charset=UTF-8" pageEncoding="UTF-8" %><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>${callback}(
{
	"resultFrom" : ${resultFrom},
	"resultTo" : ${resultTo},
	"scatter" : [ <c:forEach items="${scatter}" var="dot" varStatus="status">${dot.timestamp},${dot.executionTime},"${dot.transactionId}", <c:choose><c:when test="${dot.exceptionCode == 1}">0</c:when><c:otherwise>1</c:otherwise></c:choose><c:if test="${!status.last}">,</c:if></c:forEach> ]
});