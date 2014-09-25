<%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
{
	"resultFrom" : ${resultFrom}, 
	"resultTo" : ${resultTo},
	"sourceApplicationName" : "${sourceApplication.name}",
	"targetApplicationName" : "${targetApplication.name}",
	"sourceApplicationType" : "${sourceApplication.serviceType.desc}",
	"targetApplicationType" : "${targetApplication.serviceType.desc}",
	"sourceApplicationTypeCode" : "${sourceApplication.serviceTypeCode}",
	"targetApplicationTypeCode" : "${targetApplication.serviceTypeCode}",
	"from" : ${range.from},
	"to" : ${range.to},
	"failedCount" : ${linkStatistics.errorCount},
	"successCount" : ${linkStatistics.successCount},
	"timeSeriesHistogram" : ${timeSeriesHistogram}
}