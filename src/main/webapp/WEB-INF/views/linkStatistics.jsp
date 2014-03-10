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
	"from" : ${range.from},
	"to" : ${range.to},
	"failedCount" : ${linkStatistics.errorCount},
	"successCount" : ${linkStatistics.successCount},
	"timeSeriesHistogram" : ${timeSeriesHistogram}
}