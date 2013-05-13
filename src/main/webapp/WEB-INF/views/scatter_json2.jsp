<%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
{
	"queryStart" : ${queryStart},
	"queryEnd" : ${queryEnd},
	/* format = x, y, traceId, type, x, y, traceId, type, ... */
	"scatter" : [ <c:forEach items="${scatter}" var="dot" varStatus="status">${dot.timestamp},${dot.executionTime},"${dot.traceId}", <c:choose><c:when test="${dot.exceptionCode == 1}">"Failed"</c:when><c:otherwise>"Success"</c:otherwise></c:choose><c:if test="${!status.last}">,</c:if></c:forEach> ]
}